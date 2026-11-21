package ac.grim.grimac.shaded.maps.weak;

import ac.grim.grimac.shaded.maps.Fluent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Converter {
   private static final String DEFAULT_MAP_KEY = "value";
   private static final Map<Class<?>, Function<Object, ? extends Converter>> typeConverters = Collections.unmodifiableMap(
      new Fluent.LinkedHashMap()
         .append(Integer.class, Converter.IntConverter::new)
         .append(Long.class, Converter.LongConverter::new)
         .append(Double.class, Converter.DoubleConverter::new)
         .append(BigDecimal.class, Converter.DecimalConverter::new)
         .append(Weak.class, o -> convert(((Weak)o).asObject()))
         .append(OptionalWeak.class, o -> new Converter.OptionalConverter(((OptionalWeak)o).asObject()))
         .append(Map.class, Converter.MapConverter::new)
         .append(Iterable.class, Converter.IterableConverter::new)
         .append(Optional.class, Converter.OptionalConverter::new)
         .append(Date.class, Converter.UtilDateInstantConverter::new)
         .append(Object.class, Converter::new)
   );
   protected final Object o;

   public static Converter convert(Object value) {
      return value instanceof Object[]
         ? convert(Arrays.asList((Object[])value))
         : typeConverters.getOrDefault(
               value.getClass(),
               typeConverters.entrySet()
                  .stream()
                  .filter(entry -> !entry.getKey().equals(Date.class) && entry.getKey().isInstance(value))
                  .findFirst()
                  .map(Entry::getValue)
                  .get()
            )
            .apply(value);
   }

   private static boolean doesNotThrow(Supplier<?> method) {
      try {
         method.get();
         return true;
      } catch (RuntimeException var2) {
         return false;
      }
   }

   Converter(Object o) {
      this.o = o;
   }

   public String intoString() {
      return this.o instanceof String ? (String)this.o : this.o.toString();
   }

   public boolean intoStringWorks() {
      return doesNotThrow(this::intoString);
   }

   public int intoInteger() {
      return this.intoDecimal().setScale(0, RoundingMode.HALF_UP).intValueExact();
   }

   public boolean intoIntegerWorks() {
      return doesNotThrow(this::intoInteger);
   }

   public long intoLong() {
      return this.intoDecimal().setScale(0, RoundingMode.HALF_UP).longValueExact();
   }

   public boolean intoLongWorks() {
      return doesNotThrow(this::intoLong);
   }

   public double intoDouble() {
      return this.intoDecimal().doubleValue();
   }

   public boolean intoDoubleWorks() {
      return doesNotThrow(this::intoDouble);
   }

   public BigDecimal intoDecimal() {
      return new BigDecimal(this.intoString());
   }

   public boolean intoDecimalWorks() {
      return doesNotThrow(this::intoDecimal);
   }

   public Map intoMap() {
      return new Fluent.HashMap<String, Object>().append("value", this.o);
   }

   public boolean intoMapWorks() {
      return doesNotThrow(this::intoMap);
   }

   public List intoList() {
      List list = new ArrayList();
      list.add(this.o);
      return list;
   }

   public boolean intoListWorks() {
      return doesNotThrow(this::intoList);
   }

   public LocalDateTime intoLocalDateTime() {
      return LocalDateTime.from(ConverterTimeFormats.parseWithDefaults(this.intoString()));
   }

   public boolean intoLocalDateTimeWorks() {
      return doesNotThrow(this::intoLocalDateTime);
   }

   public ZonedDateTime intoZonedDateTime() {
      return ZonedDateTime.from(ConverterTimeFormats.parseWithDefaults(this.intoString()));
   }

   public boolean intoZonedDateTimeWorks() {
      return doesNotThrow(this::intoZonedDateTime);
   }

   public ZonedDateTime intoZonedDateTimeOrUse(ZoneId fallback) {
      try {
         return this.intoZonedDateTime();
      } catch (RuntimeException var3) {
         return this.intoLocalDateTime().atZone(fallback);
      }
   }

   public ConverterMaybe maybe() {
      return new ConverterMaybe(this.o);
   }

   static class DecimalConverter extends Converter.TypeConverter<BigDecimal> {
      DecimalConverter(Object o) {
         super(o);
      }

      @Override
      public BigDecimal intoDecimal() {
         return this.literal();
      }
   }

   static class DoubleConverter extends Converter.TypeConverter<Double> {
      DoubleConverter(Object o) {
         super(o);
      }

      @Override
      public double intoDouble() {
         return this.literal();
      }

      @Override
      public BigDecimal intoDecimal() {
         return new BigDecimal(this.literal());
      }
   }

   static class IntConverter extends Converter.TypeConverter<Integer> {
      IntConverter(Object o) {
         super(o);
      }

      @Override
      public int intoInteger() {
         return this.literal();
      }

      @Override
      public long intoLong() {
         return this.literal().intValue();
      }

      @Override
      public double intoDouble() {
         return this.literal().intValue();
      }

      @Override
      public BigDecimal intoDecimal() {
         return new BigDecimal(this.literal());
      }
   }

   static class IterableConverter extends Converter.TypeConverter<Iterable<?>> {
      IterableConverter(Object o) {
         super(o);
      }

      private Optional<Object> onlyElement() {
         Iterator<?> iterator = this.literal().iterator();
         return Optional.ofNullable(iterator.hasNext() ? iterator.next() : null).filter(o -> !iterator.hasNext());
      }

      @Override
      public String intoString() {
         return this.onlyElement().map(o -> convert(o).intoString()).orElseGet(() -> super.intoString());
      }

      @Override
      public int intoInteger() {
         return this.onlyElement().map(o -> convert(o).intoInteger()).orElseGet(() -> super.intoInteger());
      }

      @Override
      public long intoLong() {
         return this.onlyElement().map(o -> convert(o).intoLong()).orElseGet(() -> super.intoLong());
      }

      @Override
      public double intoDouble() {
         return this.onlyElement().map(o -> convert(o).intoDouble()).orElseGet(() -> super.intoDouble());
      }

      @Override
      public BigDecimal intoDecimal() {
         return this.onlyElement().map(o -> convert(o).intoDecimal()).orElseGet(() -> super.intoDecimal());
      }

      @Override
      public Map intoMap() {
         Map<Integer, Object> map = new LinkedHashMap<>();
         Iterator<?> iterator = this.literal().iterator();

         for (int i = 0; iterator.hasNext(); i++) {
            map.put(i, iterator.next());
         }

         return map;
      }

      @Override
      public List intoList() {
         return StreamSupport.stream(this.literal().spliterator(), false).collect(Collectors.toCollection(ArrayList::new));
      }
   }

   static class LongConverter extends Converter.TypeConverter<Long> {
      LongConverter(Object o) {
         super(o);
      }

      @Override
      public int intoInteger() {
         if (this.literal() >= -2147483648L && this.literal() <= 2147483647L) {
            return this.literal().intValue();
         } else {
            throw new IllegalArgumentException(this.literal() + " too large/small to be cast to int");
         }
      }

      @Override
      public long intoLong() {
         return this.literal();
      }

      @Override
      public double intoDouble() {
         return this.literal().longValue();
      }

      @Override
      public BigDecimal intoDecimal() {
         return new BigDecimal(this.literal());
      }
   }

   static class MapConverter extends Converter.TypeConverter<Map<?, ?>> {
      MapConverter(Object o) {
         super(o);
      }

      private Optional<Object> value() {
         return Optional.ofNullable(this.literal().get("value"));
      }

      @Override
      public String intoString() {
         return this.value().map(o -> convert(o).intoString()).orElseGet(() -> super.intoString());
      }

      @Override
      public int intoInteger() {
         return this.value().map(o -> convert(o).intoInteger()).orElseGet(() -> super.intoInteger());
      }

      @Override
      public long intoLong() {
         return this.value().map(o -> convert(o).intoLong()).orElseGet(() -> super.intoLong());
      }

      @Override
      public double intoDouble() {
         return this.value().map(o -> convert(o).intoDouble()).orElseGet(() -> super.intoDouble());
      }

      @Override
      public BigDecimal intoDecimal() {
         return this.value().map(o -> convert(o).intoDecimal()).orElseGet(() -> super.intoDecimal());
      }

      @Override
      public Map intoMap() {
         return new LinkedHashMap<>(this.literal());
      }

      @Override
      public List intoList() {
         return new ArrayList<>(this.literal().values());
      }
   }

   static class OptionalConverter extends Converter.TypeConverter<Optional<?>> {
      OptionalConverter(Object o) {
         super(o);
      }

      @Override
      public String intoString() {
         return this.literal().<String>map(o -> convert(o).intoString()).orElseGet(() -> super.intoString());
      }

      @Override
      public int intoInteger() {
         return this.literal().<Integer>map(o -> convert(o).intoInteger()).orElseGet(() -> super.intoInteger());
      }

      @Override
      public long intoLong() {
         return this.literal().<Long>map(o -> convert(o).intoLong()).orElseGet(() -> super.intoLong());
      }

      @Override
      public double intoDouble() {
         return this.literal().<Double>map(o -> convert(o).intoDouble()).orElseGet(() -> super.intoDouble());
      }

      @Override
      public BigDecimal intoDecimal() {
         return this.literal().<BigDecimal>map(o -> convert(o).intoDecimal()).orElseGet(() -> super.intoDecimal());
      }

      @Override
      public Map intoMap() {
         return this.literal().<Map>map(o -> convert(o).intoMap()).orElseGet(LinkedHashMap::new);
      }

      @Override
      public List intoList() {
         return this.literal().<List>map(o -> convert(o).intoList()).orElseGet(ArrayList::new);
      }
   }

   abstract static class TypeConverter<T> extends Converter {
      TypeConverter(Object o) {
         super(o);
      }

      protected T literal() {
         return (T)this.o;
      }
   }

   static class UtilDateInstantConverter extends Converter.TypeConverter<Date> {
      UtilDateInstantConverter(Object o) {
         super(o);
      }

      @Override
      public BigDecimal intoDecimal() {
         return new BigDecimal(this.intoLong());
      }

      @Override
      public double intoDouble() {
         return this.intoLong();
      }

      @Override
      public long intoLong() {
         return this.literal().getTime();
      }

      @Override
      public String intoString() {
         return String.valueOf(this.literal().getTime());
      }
   }
}
