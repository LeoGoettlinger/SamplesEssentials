package de.oliver.fancynpcs.api.utils;

import java.time.Instant;
import java.util.Date;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public final class Interval {
   private final long value;

   public Interval(long value) {
      this.value = value;
   }

   @NotNull
   public static Interval now() {
      return new Interval(System.currentTimeMillis());
   }

   @NotNull
   public static Interval of(long interval, @NotNull Interval.Unit unit) {
      return new Interval(interval * unit.factor);
   }

   @NotNull
   public static Interval of(double interval, @NotNull Interval.Unit unit) {
      return new Interval(Math.round(interval * unit.factor));
   }

   @NotNull
   public static Interval between(long n, long m, @NotNull Interval.Unit unit) {
      return new Interval((n - m) * unit.factor);
   }

   @NotNull
   public static Interval between(double n, double m, @NotNull Interval.Unit unit) {
      return new Interval(Math.round((n - m) * unit.factor));
   }

   public double as(@NotNull Interval.Unit unit) {
      return this.value / unit.factor;
   }

   @NotNull
   public Interval add(@NotNull Interval other) {
      return new Interval(this.value + other.value);
   }

   @NotNull
   public Interval add(long n, @NotNull Interval.Unit unit) {
      return new Interval(this.value + n * unit.factor);
   }

   @NotNull
   public Interval remove(@NotNull Interval other) {
      return new Interval(this.value - other.value);
   }

   @NotNull
   public Interval remove(long n, @NotNull Interval.Unit unit) {
      return new Interval(this.value - n * unit.factor);
   }

   @NotNull
   public Date toDate() {
      return new Date(this.value);
   }

   @NotNull
   public Instant toInstant() {
      return Instant.ofEpochMilli(this.value);
   }

   @NotNull
   @Override
   public String toString() {
      if (this.value < 1000L) {
         return this.value
               % Interval.Unit.YEARS.getFactor()
               % Interval.Unit.MONTHS.getFactor()
               % Interval.Unit.DAYS.getFactor()
               % Interval.Unit.HOURS.getFactor()
               % Interval.Unit.MINUTES.getFactor()
               % Interval.Unit.SECONDS.getFactor()
               / Interval.Unit.MILLISECONDS.getFactor()
            + "ms";
      } else {
         long years = this.value / Interval.Unit.YEARS.getFactor();
         long months = this.value % Interval.Unit.YEARS.getFactor() / Interval.Unit.MONTHS.getFactor();
         long days = this.value % Interval.Unit.YEARS.getFactor() % Interval.Unit.MONTHS.getFactor() / Interval.Unit.DAYS.getFactor();
         long hours = this.value
            % Interval.Unit.YEARS.getFactor()
            % Interval.Unit.MONTHS.getFactor()
            % Interval.Unit.DAYS.getFactor()
            / Interval.Unit.HOURS.getFactor();
         long minutes = this.value
            % Interval.Unit.YEARS.getFactor()
            % Interval.Unit.MONTHS.getFactor()
            % Interval.Unit.DAYS.getFactor()
            % Interval.Unit.HOURS.getFactor()
            / Interval.Unit.MINUTES.getFactor();
         long seconds = this.value
            % Interval.Unit.YEARS.getFactor()
            % Interval.Unit.MONTHS.getFactor()
            % Interval.Unit.DAYS.getFactor()
            % Interval.Unit.HOURS.getFactor()
            % Interval.Unit.MINUTES.getFactor()
            / Interval.Unit.SECONDS.getFactor();
         StringBuilder builder = new StringBuilder();
         if (years > 0L) {
            builder.append(years).append("y ");
         }

         if (months > 0L) {
            builder.append(months).append("mo ");
         }

         if (days > 0L) {
            builder.append(days).append("d ");
         }

         if (hours > 0L) {
            builder.append(hours).append("h ");
         }

         if (minutes > 0L) {
            builder.append(minutes).append("min ");
         }

         if (seconds > 0L) {
            builder.append(seconds).append("s");
         }

         if (builder.charAt(builder.length() - 1) == ' ') {
            builder.deleteCharAt(builder.length() - 1);
         }

         return builder.toString();
      }
   }

   public static enum Unit {
      MILLISECONDS(1L, "ms"),
      TICKS(50L, "t"),
      SECONDS(1000L, "s"),
      MINUTES(60000L, "min"),
      HOURS(3600000L, "h"),
      DAYS(86400000L, "d"),
      MONTHS(2629800000L, "mo"),
      YEARS(31557600000L, "y");

      private final long factor;
      private final String shortCode;

      private Unit(final long factor, @NotNull final String shortCode) {
         this.factor = factor;
         this.shortCode = shortCode;
      }

      @Nullable
      public static Interval.Unit fromShortCode(@NotNull String shortCode) {
         for (Interval.Unit unit : values()) {
            if (unit.shortCode.equalsIgnoreCase(shortCode)) {
               return unit;
            }
         }

         return null;
      }

      public long getFactor() {
         return this.factor;
      }

      @NotNull
      public String getShortCode() {
         return this.shortCode;
      }
   }
}
