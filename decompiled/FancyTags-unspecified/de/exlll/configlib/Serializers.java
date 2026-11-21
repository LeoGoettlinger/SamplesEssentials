package de.exlll.configlib;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

final class Serializers {
   private Serializers() {
   }

   static <S extends Serializer<?, ?>> S newCustomSerializer(Class<S> serializerType, SerializerContext context) {
      return Reflect.hasConstructor(serializerType, SerializerContext.class)
         ? Reflect.callConstructor(serializerType, new Class[]{SerializerContext.class}, context)
         : Reflect.callNoParamConstructor(serializerType);
   }

   static final class ArraySerializer<T1, T2> implements Serializer<T1[], List<T2>> {
      private final Class<?> componentType;
      private final Serializer<T1, T2> serializer;
      private final boolean outputNulls;
      private final boolean inputNulls;

      public ArraySerializer(Class<?> componentType, Serializer<T1, T2> serializer, boolean outputNulls, boolean inputNulls) {
         this.componentType = Validator.requireNonNull(componentType, "component type");
         this.serializer = Validator.requireNonNull(serializer, "element serializer");
         this.outputNulls = outputNulls;
         this.inputNulls = inputNulls;
      }

      public List<T2> serialize(T1[] element) {
         Stream<T2> stream = this.outputNulls
            ? Arrays.stream(element).map(s -> s == null ? null : this.serializer.serialize((T1)s))
            : Arrays.stream(element).filter(Objects::nonNull).map(this.serializer::serialize);
         return stream.toList();
      }

      public T1[] deserialize(List<T2> element) {
         Stream<T1> stream = this.inputNulls
            ? element.stream().map(t -> t == null ? null : this.serializer.deserialize((T2)t))
            : element.stream().filter(Objects::nonNull).map(this.serializer::deserialize);
         IntFunction<T1[]> f = (IntFunction<T1[]>)(value -> Reflect.newArray((Class<Object>)this.componentType, value));
         return stream.toArray(f);
      }

      public Class<?> getComponentType() {
         return this.componentType;
      }

      public Serializer<T1, T2> getElementSerializer() {
         return this.serializer;
      }
   }

   static final class BigDecimalSerializer implements Serializer<BigDecimal, String> {
      public String serialize(BigDecimal element) {
         return element.toString();
      }

      public BigDecimal deserialize(String element) {
         return new BigDecimal(element);
      }
   }

   static final class BigIntegerSerializer implements Serializer<BigInteger, String> {
      public String serialize(BigInteger element) {
         return element.toString();
      }

      public BigInteger deserialize(String element) {
         return new BigInteger(element);
      }
   }

   static final class BooleanSerializer implements Serializer<Boolean, Boolean> {
      public Boolean serialize(Boolean element) {
         return element;
      }

      public Boolean deserialize(Boolean element) {
         return element;
      }
   }

   static final class CharacterSerializer implements Serializer<Character, String> {
      public String serialize(Character element) {
         return element.toString();
      }

      public Character deserialize(String element) {
         int length = element.length();
         if (length == 0) {
            String msg = "An empty string cannot be converted to a character.";
            throw new ConfigurationException(msg);
         } else if (length > 1) {
            String msg = "String '" + element + "' is too long to be converted to a character.";
            throw new ConfigurationException(msg);
         } else {
            return element.charAt(0);
         }
      }
   }

   static class CollectionSerializer<S, T, L extends Collection<S>, R extends Collection<T>> implements Serializer<L, R> {
      private final Serializer<S, T> serializer;
      private final boolean outputNulls;
      private final boolean inputNulls;
      private final Supplier<L> lSupplier;
      private final Supplier<R> rSupplier;

      public CollectionSerializer(Serializer<S, T> serializer, boolean outputNulls, boolean inputNulls, Supplier<L> lSupplier, Supplier<R> rSupplier) {
         this.serializer = Validator.requireNonNull(serializer, "element serializer");
         this.outputNulls = outputNulls;
         this.inputNulls = inputNulls;
         this.lSupplier = lSupplier;
         this.rSupplier = rSupplier;
      }

      public final R serialize(L element) {
         Stream<T> stream = this.outputNulls
            ? element.stream().map(s -> s == null ? null : this.serializer.serialize((S)s))
            : element.stream().filter(Objects::nonNull).map(this.serializer::serialize);
         return stream.collect(Collectors.toCollection(this.rSupplier));
      }

      public final L deserialize(R element) {
         Stream<S> stream = this.inputNulls
            ? element.stream().map(t -> (R)(t == null ? null : this.serializer.deserialize((T)t)))
            : element.stream().filter(Objects::nonNull).map(this.serializer::deserialize);
         return stream.collect(Collectors.toCollection(this.lSupplier));
      }

      public final Serializer<S, T> getElementSerializer() {
         return this.serializer;
      }
   }

   static final class EnumSerializer implements Serializer<Enum<?>, String> {
      private final Class<? extends Enum<?>> cls;

      public EnumSerializer(Class<? extends Enum<?>> cls) {
         this.cls = Validator.requireNonNull(cls, "enum class");
      }

      public String serialize(Enum<?> element) {
         return element.name();
      }

      public Enum<?> deserialize(String element) {
         for (Enum<?> constant : this.cls.getEnumConstants()) {
            if (constant.name().equals(element)) {
               return constant;
            }
         }

         String msg = this.createExceptionMessage(element);
         throw new ConfigurationException(msg);
      }

      private String createExceptionMessage(String element) {
         String enums = Arrays.stream(this.cls.getEnumConstants()).map(Enum::name).collect(Collectors.joining(", ", "[", "]"));
         return "Enum class " + this.cls.getSimpleName() + " does not contain enum '" + element + "'. Valid values are: " + enums;
      }

      public Class<? extends Enum<?>> getEnumCls() {
         return this.cls;
      }
   }

   static final class FileSerializer implements Serializer<File, String> {
      public String serialize(File element) {
         return element.toString();
      }

      public File deserialize(String element) {
         return new File(element);
      }
   }

   static final class InstantSerializer implements Serializer<Instant, String> {
      public String serialize(Instant element) {
         return element.toString();
      }

      public Instant deserialize(String element) {
         return Instant.parse(element);
      }
   }

   static final class ListSerializer<S, T> extends Serializers.CollectionSerializer<S, T, List<S>, List<T>> {
      public ListSerializer(Serializer<S, T> serializer, boolean outputNulls, boolean inputNulls) {
         super(serializer, outputNulls, inputNulls, ArrayList::new, ArrayList::new);
      }
   }

   static final class LocalDateSerializer implements Serializer<LocalDate, String> {
      public String serialize(LocalDate element) {
         return element.toString();
      }

      public LocalDate deserialize(String element) {
         return LocalDate.parse(element);
      }
   }

   static final class LocalDateTimeSerializer implements Serializer<LocalDateTime, String> {
      public String serialize(LocalDateTime element) {
         return element.truncatedTo(ChronoUnit.SECONDS).toString();
      }

      public LocalDateTime deserialize(String element) {
         return LocalDateTime.parse(element);
      }
   }

   static final class LocalTimeSerializer implements Serializer<LocalTime, String> {
      public String serialize(LocalTime element) {
         return element.truncatedTo(ChronoUnit.SECONDS).toString();
      }

      public LocalTime deserialize(String element) {
         return LocalTime.parse(element);
      }
   }

   static final class MapSerializer<S1, T1, S2, T2> implements Serializer<Map<S1, S2>, Map<T1, T2>> {
      private final Serializer<S1, T1> keySerializer;
      private final Serializer<S2, T2> valSerializer;
      private final boolean outputNulls;
      private final boolean inputNulls;

      public MapSerializer(Serializer<S1, T1> keySerializer, Serializer<S2, T2> valSerializer, boolean outputNulls, boolean inputNulls) {
         this.keySerializer = Validator.requireNonNull(keySerializer, "key serializer");
         this.valSerializer = Validator.requireNonNull(valSerializer, "value serializer");
         this.outputNulls = outputNulls;
         this.inputNulls = inputNulls;
      }

      public Map<T1, T2> serialize(Map<S1, S2> element) {
         Map<T1, T2> result = new LinkedHashMap<>();

         for (Entry<S1, S2> entry : element.entrySet()) {
            if (this.outputNulls || !isEntryNull(entry)) {
               S1 s1key = entry.getKey();
               S2 s2val = entry.getValue();
               T1 t1key = s1key == null ? null : this.keySerializer.serialize(s1key);
               T2 t2val = s2val == null ? null : this.valSerializer.serialize(s2val);
               result.put(t1key, t2val);
            }
         }

         return result;
      }

      public Map<S1, S2> deserialize(Map<T1, T2> element) {
         Map<S1, S2> result = new LinkedHashMap<>();

         for (Entry<T1, T2> entry : element.entrySet()) {
            if (this.inputNulls || !isEntryNull(entry)) {
               T1 t1key = entry.getKey();
               T2 t2val = entry.getValue();
               S1 s1key = t1key == null ? null : this.keySerializer.deserialize(t1key);
               S2 s2val = t2val == null ? null : this.valSerializer.deserialize(t2val);
               result.put(s1key, s2val);
            }
         }

         return result;
      }

      private static boolean isEntryNull(Entry<?, ?> entry) {
         return entry == null || entry.getKey() == null || entry.getValue() == null;
      }

      public Serializer<S1, T1> getKeySerializer() {
         return this.keySerializer;
      }

      public Serializer<S2, T2> getValueSerializer() {
         return this.valSerializer;
      }
   }

   static final class NumberSerializer implements Serializer<Number, Number> {
      private final Class<? extends Number> cls;

      public NumberSerializer(Class<? extends Number> cls) {
         this.cls = Validator.requireNonNull(cls, "number class");
         Validator.requirePrimitiveOrWrapperNumberType(cls);
      }

      public Number serialize(Number element) {
         if (Reflect.isIntegerType(this.cls)) {
            return element.longValue();
         } else if (Reflect.isFloatingPointType(this.cls)) {
            return element.doubleValue();
         } else {
            String msg = "Invalid element '" + element + "' with type " + element.getClass();
            throw new ConfigurationException(msg);
         }
      }

      public Number deserialize(Number element) {
         if (Reflect.isIntegerType(element.getClass())) {
            return this.deserializeFromIntegerType(element);
         } else if (Reflect.isFloatingPointType(element.getClass())) {
            return this.deserializeFromFloatingPointType(element);
         } else {
            String clsName = element.getClass().getSimpleName();
            String msg = "Cannot deserialize element '"
               + element
               + "' of type "
               + clsName
               + ".\nThis serializer only supports primitive number types and their wrapper types.";
            throw new ConfigurationException(msg);
         }
      }

      private Number deserializeFromFloatingPointType(Number element) {
         double value = element.doubleValue();
         if (this.cls != float.class && this.cls != Float.class) {
            return value;
         } else if (Double.isNaN(value)) {
            return Float.NaN;
         } else if (value == Double.POSITIVE_INFINITY) {
            return Float.POSITIVE_INFINITY;
         } else if (value == Double.NEGATIVE_INFINITY) {
            return Float.NEGATIVE_INFINITY;
         } else {
            if (value != 0.0) {
               this.requireFloatingPointInRange(value);
            }

            return element.floatValue();
         }
      }

      private Number deserializeFromIntegerType(Number element) {
         long value = element.longValue();
         if (this.cls == byte.class || this.cls == Byte.class) {
            this.requireIntegerInRange(value, -128L, 127L);
            return element.byteValue();
         } else if (this.cls == short.class || this.cls == Short.class) {
            this.requireIntegerInRange(value, -32768L, 32767L);
            return element.shortValue();
         } else if (this.cls != int.class && this.cls != Integer.class) {
            return value;
         } else {
            this.requireIntegerInRange(value, -2147483648L, 2147483647L);
            return element.intValue();
         }
      }

      private void requireIntegerInRange(long value, long low, long high) {
         if (value < low || value > high) {
            String msg = this.baseExceptionMessage(value) + "It does not fit into the range of valid values [" + low + ", " + high + "].";
            throw new ConfigurationException(msg);
         }
      }

      private void requireFloatingPointInRange(double value) {
         String clsName = this.cls.getSimpleName();
         if (value > -Float.MIN_VALUE && value < Float.MIN_VALUE) {
            String msg = this.baseExceptionMessage(value) + "It is smaller than the smallest possible " + clsName + " value.";
            throw new ConfigurationException(msg);
         } else if (value < -Float.MAX_VALUE || value > Float.MAX_VALUE) {
            String msg = this.baseExceptionMessage(value) + "It is larger than the largest possible " + clsName + " value.";
            throw new ConfigurationException(msg);
         }
      }

      private <T extends Number> String baseExceptionMessage(T value) {
         String clsName = this.cls.getSimpleName();
         return "Number " + value + " cannot be converted to type " + clsName + ". ";
      }

      public Class<? extends Number> getNumberClass() {
         return this.cls;
      }
   }

   static final class PathSerializer implements Serializer<Path, String> {
      public String serialize(Path element) {
         return element.toString();
      }

      public Path deserialize(String element) {
         return Path.of(element);
      }
   }

   static final class PrimitiveBooleanArraySerializer implements Serializer<Object, List<Boolean>> {
      public List<Boolean> serialize(Object element) {
         boolean[] array = (boolean[])element;
         return IntStream.range(0, array.length).mapToObj(i -> array[i]).toList();
      }

      public Object deserialize(List<Boolean> element) {
         boolean[] array = new boolean[element.size()];

         for (int i = 0; i < element.size(); i++) {
            array[i] = Validator.requireNonNullArrayElement(element.get(i), "boolean", i);
         }

         return array;
      }
   }

   static final class PrimitiveByteArraySerializer implements Serializer<Object, List<Number>> {
      private static final Serializers.NumberSerializer serializer = new Serializers.NumberSerializer(byte.class);

      public List<Number> serialize(Object element) {
         byte[] array = (byte[])element;
         return IntStream.range(0, array.length).mapToObj(i -> serializer.serialize((byte)array[i])).toList();
      }

      public Object deserialize(List<Number> element) {
         byte[] array = new byte[element.size()];

         for (int i = 0; i < element.size(); i++) {
            Number number = Validator.requireNonNullArrayElement(element.get(i), "byte", i);
            array[i] = (Byte)serializer.deserialize(number);
         }

         return array;
      }
   }

   static final class PrimitiveCharacterArraySerializer implements Serializer<Object, List<String>> {
      private static final Serializers.CharacterSerializer serializer = new Serializers.CharacterSerializer();

      public List<String> serialize(Object element) {
         char[] array = (char[])element;
         return IntStream.range(0, array.length).mapToObj(i -> serializer.serialize(array[i])).toList();
      }

      public Object deserialize(List<String> element) {
         char[] array = new char[element.size()];

         for (int i = 0; i < element.size(); i++) {
            String character = Validator.requireNonNullArrayElement(element.get(i), "char", i);
            array[i] = serializer.deserialize(character);
         }

         return array;
      }
   }

   static final class PrimitiveDoubleArraySerializer implements Serializer<Object, List<Number>> {
      private static final Serializers.NumberSerializer serializer = new Serializers.NumberSerializer(double.class);

      public List<Number> serialize(Object element) {
         double[] array = (double[])element;
         return Arrays.stream(array).mapToObj(serializer::serialize).toList();
      }

      public Object deserialize(List<Number> element) {
         double[] array = new double[element.size()];

         for (int i = 0; i < element.size(); i++) {
            Number number = Validator.requireNonNullArrayElement(element.get(i), "double", i);
            array[i] = (Double)serializer.deserialize(number);
         }

         return array;
      }
   }

   static final class PrimitiveFloatArraySerializer implements Serializer<Object, List<Number>> {
      private static final Serializers.NumberSerializer serializer = new Serializers.NumberSerializer(float.class);

      public List<Number> serialize(Object element) {
         float[] array = (float[])element;
         return IntStream.range(0, array.length).mapToObj(i -> serializer.serialize((float)array[i])).toList();
      }

      public Object deserialize(List<Number> element) {
         float[] array = new float[element.size()];

         for (int i = 0; i < element.size(); i++) {
            Number number = Validator.requireNonNullArrayElement(element.get(i), "float", i);
            array[i] = (Float)serializer.deserialize(number);
         }

         return array;
      }
   }

   static final class PrimitiveIntegerArraySerializer implements Serializer<Object, List<Number>> {
      private static final Serializers.NumberSerializer serializer = new Serializers.NumberSerializer(int.class);

      public List<Number> serialize(Object element) {
         int[] array = (int[])element;
         return Arrays.stream(array).mapToObj(serializer::serialize).toList();
      }

      public Object deserialize(List<Number> element) {
         int[] array = new int[element.size()];

         for (int i = 0; i < element.size(); i++) {
            Number number = Validator.requireNonNullArrayElement(element.get(i), "int", i);
            array[i] = (Integer)serializer.deserialize(number);
         }

         return array;
      }
   }

   static final class PrimitiveLongArraySerializer implements Serializer<Object, List<Number>> {
      private static final Serializers.NumberSerializer serializer = new Serializers.NumberSerializer(long.class);

      public List<Number> serialize(Object element) {
         long[] array = (long[])element;
         return Arrays.stream(array).mapToObj(serializer::serialize).toList();
      }

      public Object deserialize(List<Number> element) {
         long[] array = new long[element.size()];

         for (int i = 0; i < element.size(); i++) {
            Number number = Validator.requireNonNullArrayElement(element.get(i), "long", i);
            array[i] = (Long)serializer.deserialize(number);
         }

         return array;
      }
   }

   static final class PrimitiveShortArraySerializer implements Serializer<Object, List<Number>> {
      private static final Serializers.NumberSerializer serializer = new Serializers.NumberSerializer(short.class);

      public List<Number> serialize(Object element) {
         short[] array = (short[])element;
         return IntStream.range(0, array.length).mapToObj(i -> serializer.serialize((short)array[i])).toList();
      }

      public Object deserialize(List<Number> element) {
         short[] array = new short[element.size()];

         for (int i = 0; i < element.size(); i++) {
            Number number = Validator.requireNonNullArrayElement(element.get(i), "short", i);
            array[i] = (Short)serializer.deserialize(number);
         }

         return array;
      }
   }

   static final class SetAsListSerializer<S, T> extends Serializers.CollectionSerializer<S, T, Set<S>, List<T>> {
      public SetAsListSerializer(Serializer<S, T> serializer, boolean outputNulls, boolean inputNulls) {
         super(serializer, outputNulls, inputNulls, HashSet::new, ArrayList::new);
      }
   }

   static final class SetSerializer<S, T> extends Serializers.CollectionSerializer<S, T, Set<S>, Set<T>> {
      public SetSerializer(Serializer<S, T> serializer, boolean outputNulls, boolean inputNulls) {
         super(serializer, outputNulls, inputNulls, HashSet::new, LinkedHashSet::new);
      }
   }

   static final class StringSerializer implements Serializer<String, String> {
      public String serialize(String element) {
         return element;
      }

      public String deserialize(String element) {
         return element;
      }
   }

   static final class UriSerializer implements Serializer<URI, String> {
      public String serialize(URI element) {
         return element.toString();
      }

      public URI deserialize(String element) {
         return URI.create(element);
      }
   }

   static final class UrlSerializer implements Serializer<URL, String> {
      public String serialize(URL element) {
         return element.toString();
      }

      public URL deserialize(String element) {
         try {
            return new URL(element);
         } catch (MalformedURLException var3) {
            throw new RuntimeException(var3);
         }
      }
   }

   static final class UuidSerializer implements Serializer<UUID, String> {
      public String serialize(UUID element) {
         return element.toString();
      }

      public UUID deserialize(String element) {
         return UUID.fromString(element);
      }
   }
}
