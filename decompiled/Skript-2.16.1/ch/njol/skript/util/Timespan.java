package ch.njol.skript.util;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.localization.GeneralWords;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Noun;
import ch.njol.util.Math2;
import ch.njol.util.NonNullPair;
import ch.njol.util.coll.CollectionUtils;
import ch.njol.yggdrasil.YggdrasilSerializable;
import com.google.common.base.Preconditions;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Timespan implements YggdrasilSerializable, Comparable<Timespan>, TemporalAmount {
   private static final Pattern TIMESPAN_PATTERN = Pattern.compile("^(\\d+):(\\d\\d)(:\\d\\d){0,2}(?<ms>\\.\\d{1,4})?$");
   private static final Pattern TIMESPAN_NUMBER_PATTERN = Pattern.compile("^\\d+(\\.\\d+)?$");
   private static final Pattern TIMESPAN_SPLIT_PATTERN = Pattern.compile("[:.]");
   private static final Pattern SHORT_FORM_PATTERN = Pattern.compile("^(\\d+(?:\\.\\d+)?)([a-zA-Z]+)$");
   private static final Noun FOREVER_NAME = new Noun("time.forever");
   private static final List<NonNullPair<Noun, Long>> SIMPLE_VALUES = Arrays.asList(
      new NonNullPair<>(Timespan.TimePeriod.YEAR.name, Timespan.TimePeriod.YEAR.time),
      new NonNullPair<>(Timespan.TimePeriod.MONTH.name, Timespan.TimePeriod.MONTH.time),
      new NonNullPair<>(Timespan.TimePeriod.WEEK.name, Timespan.TimePeriod.WEEK.time),
      new NonNullPair<>(Timespan.TimePeriod.DAY.name, Timespan.TimePeriod.DAY.time),
      new NonNullPair<>(Timespan.TimePeriod.HOUR.name, Timespan.TimePeriod.HOUR.time),
      new NonNullPair<>(Timespan.TimePeriod.MINUTE.name, Timespan.TimePeriod.MINUTE.time),
      new NonNullPair<>(Timespan.TimePeriod.SECOND.name, Timespan.TimePeriod.SECOND.time)
   );
   private static final Map<String, Long> PARSE_VALUES = new HashMap<>();
   private final long millis;

   @Nullable
   public static Timespan parse(String value) {
      return parse(value, ParseContext.DEFAULT);
   }

   @Nullable
   public static Timespan parse(String value, ParseContext context) {
      if (value.isEmpty()) {
         return null;
      } else {
         long totalMillis = 0L;
         boolean minecraftTime = false;
         boolean isMinecraftTimeSet = false;
         Matcher matcher = TIMESPAN_PATTERN.matcher(value);
         if (matcher.matches()) {
            String[] substring = TIMESPAN_SPLIT_PATTERN.split(value);
            long[] times = new long[]{
               1L, Timespan.TimePeriod.SECOND.time, Timespan.TimePeriod.MINUTE.time, Timespan.TimePeriod.HOUR.time, Timespan.TimePeriod.DAY.time
            };
            boolean hasMs = value.contains(".");
            int length = substring.length;
            int offset = 2;
            if ((length != 4 || hasMs) && length != 5) {
               if (length == 3 && !hasMs || length == 4) {
                  offset = 1;
               }
            } else {
               offset = 0;
            }

            for (int i = 0; i < substring.length; i++) {
               totalMillis += times[offset + i] * Utils.parseLong(substring[i]);
            }
         } else {
            String[] substring = value.toLowerCase(Locale.ENGLISH).split("\\s+");

            for (int i = 0; i < substring.length; i++) {
               String sub = substring[i];
               if (sub.equals(GeneralWords.and.toString())) {
                  if (i == 0 || i == substring.length - 1) {
                     return null;
                  }
               } else {
                  double amount = 1.0;
                  if (Noun.isIndefiniteArticle(sub)) {
                     if (i == substring.length - 1) {
                        return null;
                     }

                     sub = substring[++i];
                  } else if (TIMESPAN_NUMBER_PATTERN.matcher(sub).matches()) {
                     if (i == substring.length - 1) {
                        return null;
                     }

                     try {
                        amount = Double.parseDouble(sub);
                     } catch (NumberFormatException var13) {
                        throw new IllegalArgumentException("Invalid timespan: " + value);
                     }

                     sub = substring[++i];
                  }

                  if (CollectionUtils.contains(Language.getList("time.real"), sub)) {
                     if (i == substring.length - 1 || isMinecraftTimeSet && minecraftTime) {
                        return null;
                     }

                     sub = substring[++i];
                  } else if (CollectionUtils.contains(Language.getList("time.minecraft"), sub)) {
                     if (i == substring.length - 1 || isMinecraftTimeSet && !minecraftTime) {
                        return null;
                     }

                     minecraftTime = true;
                     sub = substring[++i];
                  }

                  if (sub.endsWith(",")) {
                     sub = sub.substring(0, sub.length() - 1);
                  }

                  if (context == ParseContext.COMMAND) {
                     Matcher shortFormMatcher = SHORT_FORM_PATTERN.matcher(sub);
                     if (shortFormMatcher.matches()) {
                        amount = Double.parseDouble(shortFormMatcher.group(1));
                        sub = shortFormMatcher.group(2).toLowerCase(Locale.ENGLISH);
                     }
                  }

                  Long millis = PARSE_VALUES.get(sub.toLowerCase(Locale.ENGLISH));
                  if (millis == null) {
                     return null;
                  }

                  if (minecraftTime && millis != Timespan.TimePeriod.TICK.time) {
                     amount /= 72.0;
                  }

                  totalMillis += Math.round(amount * millis.longValue());
                  isMinecraftTimeSet = true;
               }
            }
         }

         return new Timespan(totalMillis);
      }
   }

   @org.jetbrains.annotations.Contract("_ -> new")
   @NotNull
   public static Timespan fromDuration(@NotNull Duration duration) {
      return new Timespan(duration.toMillis());
   }

   @org.jetbrains.annotations.Contract(
      value = " -> new",
      pure = true
   )
   @NotNull
   public static Timespan infinite() {
      return new Timespan(Long.MAX_VALUE);
   }

   public static String toString(long millis) {
      return toString(millis, 0);
   }

   public static String toString(long millis, int flags) {
      if (millis == Long.MAX_VALUE) {
         return FOREVER_NAME.toString(false);
      } else {
         for (int i = 0; i < SIMPLE_VALUES.size() - 1; i++) {
            NonNullPair<Noun, Long> pair = SIMPLE_VALUES.get(i);
            long second1 = pair.getSecond();
            if (millis >= second1) {
               long remainder = millis % second1;
               double second = 1.0 * remainder / SIMPLE_VALUES.get(i + 1).getSecond().longValue();
               if (!"0".equals(Skript.toString(second))) {
                  return toString(Math.floor(1.0 * millis / second1), pair, flags) + " " + GeneralWords.and + " " + toString(remainder, flags);
               }

               return toString(1.0 * millis / second1, pair, flags);
            }
         }

         return toString(1.0 * millis / SIMPLE_VALUES.get(SIMPLE_VALUES.size() - 1).getSecond().longValue(), SIMPLE_VALUES.get(SIMPLE_VALUES.size() - 1), flags);
      }
   }

   private static String toString(double amount, NonNullPair<Noun, Long> pair, int flags) {
      return pair.getFirst().withAmount(amount, flags);
   }

   public Timespan() {
      this.millis = 0L;
   }

   public Timespan(long millis) {
      Preconditions.checkArgument(millis >= 0L, "millis must be >= 0");
      this.millis = millis;
   }

   public Timespan(Timespan.TimePeriod timePeriod, long time) {
      Preconditions.checkArgument(time >= 0L, "time must be >= 0");
      this.millis = time * timePeriod.getTime();
   }

   @Deprecated(
      since = "2.10.0",
      forRemoval = true
   )
   public static Timespan fromTicks(long ticks) {
      return new Timespan(ticks * 50L);
   }

   @Deprecated(
      since = "2.10.0",
      forRemoval = true
   )
   public static Timespan fromTicks_i(long ticks) {
      return new Timespan(ticks * 50L);
   }

   @Deprecated(
      since = "2.10.0",
      forRemoval = true
   )
   public long getMilliSeconds() {
      return this.getAs(Timespan.TimePeriod.MILLISECOND);
   }

   @Deprecated(
      since = "2.10.0",
      forRemoval = true
   )
   public long getTicks() {
      return this.getAs(Timespan.TimePeriod.TICK);
   }

   @Deprecated(
      since = "2.10.0",
      forRemoval = true
   )
   public long getTicks_i() {
      return this.getAs(Timespan.TimePeriod.TICK);
   }

   public boolean isInfinite() {
      return this.millis == Long.MAX_VALUE;
   }

   public long getAs(Timespan.TimePeriod timePeriod) {
      return this.millis / timePeriod.getTime();
   }

   public Duration getDuration() {
      return Duration.ofMillis(this.millis);
   }

   @org.jetbrains.annotations.Contract(
      value = "_ -> new",
      pure = true
   )
   public Timespan add(Timespan timespan) {
      if (!this.isInfinite() && !timespan.isInfinite()) {
         long millis = Math2.addClamped(this.millis, timespan.getAs(Timespan.TimePeriod.MILLISECOND));
         return new Timespan(millis);
      } else {
         return infinite();
      }
   }

   @org.jetbrains.annotations.Contract(
      value = "_ -> new",
      pure = true
   )
   public Timespan subtract(Timespan timespan) {
      if (!this.isInfinite() && !timespan.isInfinite()) {
         long millis = Math.max(0L, this.millis - timespan.getAs(Timespan.TimePeriod.MILLISECOND));
         return new Timespan(millis);
      } else {
         return infinite();
      }
   }

   @org.jetbrains.annotations.Contract(
      value = "_ -> new",
      pure = true
   )
   public Timespan multiply(double scalar) {
      Preconditions.checkArgument(scalar >= 0.0);
      if (Double.isInfinite(scalar)) {
         return infinite();
      } else {
         double value = this.getAs(Timespan.TimePeriod.MILLISECOND) * scalar;
         return new Timespan((long)Math.min(value, 9.223372E18F));
      }
   }

   @org.jetbrains.annotations.Contract(
      value = "_ -> new",
      pure = true
   )
   public Timespan divide(double scalar) {
      Preconditions.checkArgument(scalar >= 0.0, "Cannot divide a timespan by non-positive value");
      if (this.isInfinite()) {
         return infinite();
      } else {
         double value = this.getAs(Timespan.TimePeriod.MILLISECOND) / scalar;
         if (Double.isNaN(value)) {
            return new Timespan(0L);
         } else {
            return Double.isInfinite(value) ? infinite() : new Timespan((long)Math.min(value, 9.223372E18F));
         }
      }
   }

   @org.jetbrains.annotations.Contract(
      pure = true
   )
   public double divide(Timespan other) {
      if (this.isInfinite()) {
         return other.isInfinite() ? Double.NaN : Double.POSITIVE_INFINITY;
      } else {
         return other.isInfinite() ? 0.0 : (double)this.getAs(Timespan.TimePeriod.MILLISECOND) / other.getAs(Timespan.TimePeriod.MILLISECOND);
      }
   }

   public Timespan difference(Timespan timespan) {
      if (!this.isInfinite() && !timespan.isInfinite()) {
         long millis = Math.abs(this.millis - timespan.getAs(Timespan.TimePeriod.MILLISECOND));
         return new Timespan(millis);
      } else {
         return infinite();
      }
   }

   @Override
   public long get(TemporalUnit unit) {
      if (unit instanceof Timespan.TimePeriod period) {
         return this.getComponent(period);
      } else if (unit instanceof ChronoUnit chrono) {
         return switch (chrono) {
            case YEARS -> this.getComponent(Timespan.TimePeriod.YEAR);
            case MONTHS -> this.getComponent(Timespan.TimePeriod.MONTH);
            case WEEKS -> this.getComponent(Timespan.TimePeriod.WEEK);
            case DAYS -> this.getComponent(Timespan.TimePeriod.DAY);
            case HOURS -> this.getComponent(Timespan.TimePeriod.HOUR);
            case MINUTES -> this.getComponent(Timespan.TimePeriod.MINUTE);
            case SECONDS -> this.getComponent(Timespan.TimePeriod.SECOND);
            case MILLIS -> this.getComponent(Timespan.TimePeriod.MILLISECOND);
            default -> throw new UnsupportedTemporalTypeException("Not a supported time unit: " + chrono);
         };
      } else {
         throw new UnsupportedTemporalTypeException("Not a supported temporal unit: " + unit);
      }
   }

   private long getComponent(Timespan.TimePeriod period) {
      if (this.isInfinite()) {
         return Long.MAX_VALUE;
      } else {
         long remainder = this.millis;

         for (Timespan.TimePeriod p : Timespan.TimePeriod.values()) {
            if (p.ordinal() > period.ordinal()) {
               remainder %= p.getTime();
            }
         }

         return remainder / period.getTime();
      }
   }

   @Override
   public List<TemporalUnit> getUnits() {
      return List.of(Timespan.TimePeriod.values()).reversed();
   }

   @Override
   public Temporal addTo(Temporal temporal) {
      return temporal.plus(this.millis, ChronoUnit.MILLIS);
   }

   @Override
   public Temporal subtractFrom(Temporal temporal) {
      return temporal.minus(this.millis, ChronoUnit.MILLIS);
   }

   public int compareTo(@Nullable Timespan time) {
      return Long.compare(this.millis, time == null ? this.millis : time.millis);
   }

   @Override
   public int hashCode() {
      return Long.hashCode(this.millis);
   }

   @Override
   public boolean equals(@Nullable Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else {
         return obj instanceof Timespan other ? this.millis == other.millis : false;
      }
   }

   @Override
   public String toString() {
      return toString(this.millis);
   }

   public String toString(int flags) {
      return toString(this.millis, flags);
   }

   static {
      Language.addListener(() -> {
         for (Timespan.TimePeriod time : Timespan.TimePeriod.values()) {
            PARSE_VALUES.put(time.name.getSingular().toLowerCase(Locale.ENGLISH), time.getTime());
            PARSE_VALUES.put(time.name.getPlural().toLowerCase(Locale.ENGLISH), time.getTime());
            PARSE_VALUES.put(time.shortName.getSingular().toLowerCase(Locale.ENGLISH), time.getTime());
            PARSE_VALUES.put(time.shortName.getPlural().toLowerCase(Locale.ENGLISH), time.getTime());
         }
      });
   }

   public static enum TimePeriod implements TemporalUnit {
      MILLISECOND(1L),
      TICK(50L),
      SECOND(1000L),
      MINUTE,
      HOUR,
      DAY,
      WEEK,
      MONTH,
      YEAR;

      private final Noun name = new Noun("time." + this.name().toLowerCase(Locale.ENGLISH) + ".full");
      private final Noun shortName = new Noun("time." + this.name().toLowerCase(Locale.ENGLISH) + ".short");
      private final long time;

      private TimePeriod(long time) {
         this.time = time;
      }

      public long getTime() {
         return this.time;
      }

      public String getFullForm() {
         return this.name.toString();
      }

      public String getShortForm() {
         return this.shortName.toString();
      }

      @Override
      public Duration getDuration() {
         return Duration.ofMillis(this.time);
      }

      @Override
      public boolean isDurationEstimated() {
         return false;
      }

      @Override
      public boolean isDateBased() {
         return false;
      }

      @Override
      public boolean isTimeBased() {
         return true;
      }

      @Override
      public <R extends Temporal> R addTo(R temporal, long amount) {
         return (R)temporal.plus(amount, this);
      }

      @Override
      public long between(Temporal temporal1Inclusive, Temporal temporal2Exclusive) {
         return temporal1Inclusive.until(temporal2Exclusive, this);
      }

      // $VF: Failed to inline enum fields
      // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
      static {
         MINUTE = new Timespan.TimePeriod(SECOND.time * 60L);
         HOUR = new Timespan.TimePeriod(MINUTE.time * 60L);
         DAY = new Timespan.TimePeriod(HOUR.time * 24L);
         WEEK = new Timespan.TimePeriod(DAY.time * 7L);
         MONTH = new Timespan.TimePeriod(DAY.time * 30L);
         YEAR = new Timespan.TimePeriod(DAY.time * 365L);
      }
   }
}
