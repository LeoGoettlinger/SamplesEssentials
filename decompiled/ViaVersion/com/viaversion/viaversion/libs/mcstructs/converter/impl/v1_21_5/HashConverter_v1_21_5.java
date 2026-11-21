package com.viaversion.viaversion.libs.mcstructs.converter.impl.v1_21_5;

import com.viaversion.viaversion.libs.mcstructs.converter.DataConverter;
import com.viaversion.viaversion.libs.mcstructs.converter.hash.HashBuilder;
import com.viaversion.viaversion.libs.mcstructs.converter.hash.HashCode;
import com.viaversion.viaversion.libs.mcstructs.converter.hash.HashFunction;
import com.viaversion.viaversion.libs.mcstructs.converter.model.Result;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;

public class HashConverter_v1_21_5 implements DataConverter<HashCode> {
   private static final byte TAG_EMPTY = 1;
   private static final byte TAG_MAP_START = 2;
   private static final byte TAG_MAP_END = 3;
   private static final byte TAG_LIST_START = 4;
   private static final byte TAG_LIST_END = 5;
   private static final byte TAG_BYTE = 6;
   private static final byte TAG_SHORT = 7;
   private static final byte TAG_INT = 8;
   private static final byte TAG_LONG = 9;
   private static final byte TAG_FLOAT = 10;
   private static final byte TAG_DOUBLE = 11;
   private static final byte TAG_STRING = 12;
   private static final byte TAG_BOOLEAN = 13;
   private static final byte TAG_BYTE_ARRAY_START = 14;
   private static final byte TAG_BYTE_ARRAY_END = 15;
   private static final byte TAG_INT_ARRAY_START = 16;
   private static final byte TAG_INT_ARRAY_END = 17;
   private static final byte TAG_LONG_ARRAY_START = 18;
   private static final byte TAG_LONG_ARRAY_END = 19;
   private static final byte[] EMPTY_HASH = new byte[]{1};
   private static final byte[] EMPTY_MAP = new byte[]{2, 3};
   private static final byte[] EMPTY_LIST = new byte[]{4, 5};
   private static final byte[] FALSE = new byte[]{13, 0};
   private static final byte[] TRUE = new byte[]{13, 1};
   private static final Comparator<HashCode> HASH_CODE_COMPARATOR = Comparator.comparingLong(HashCode::asLong);
   private static final Comparator<Entry<HashCode, HashCode>> MAP_COMPARATOR = Entry.<HashCode, HashCode>comparingByKey(HASH_CODE_COMPARATOR)
      .thenComparing(Entry.comparingByValue(HASH_CODE_COMPARATOR));
   private static final String TAG_ERROR = "Unable to convert HashCode to %s";
   public static final HashConverter_v1_21_5 CRC32C = new HashConverter_v1_21_5(HashFunction.CRC32C);
   private final HashFunction hashFunction;
   private final HashCode emptyHash;
   private final HashCode emptyMapHash;
   private final HashCode emptyListHash;
   private final HashCode trueHash;
   private final HashCode falseHash;

   public HashConverter_v1_21_5(HashFunction hashFunction) {
      this.hashFunction = hashFunction;
      this.emptyHash = hashFunction.hash(EMPTY_HASH);
      this.emptyMapHash = hashFunction.hash(EMPTY_MAP);
      this.emptyListHash = hashFunction.hash(EMPTY_LIST);
      this.trueHash = hashFunction.hash(TRUE);
      this.falseHash = hashFunction.hash(FALSE);
   }

   public <N> N convertTo(DataConverter<N> converter, @Nullable HashCode element) {
      throw new UnsupportedOperationException("Can't convert HashCode to other types");
   }

   public HashCode empty() {
      return this.emptyHash;
   }

   public HashCode emptyList() {
      return this.emptyListHash;
   }

   public HashCode emptyMap() {
      return this.emptyMapHash;
   }

   public HashCode createBoolean(boolean value) {
      return value ? this.trueHash : this.falseHash;
   }

   public HashCode createByte(byte value) {
      return this.hashFunction.builder(2).addByte((byte)6).addByte(value).hash();
   }

   public HashCode createShort(short value) {
      return this.hashFunction.builder(3).addByte((byte)7).addShort(value).hash();
   }

   public HashCode createInt(int value) {
      return this.hashFunction.builder(5).addByte((byte)8).addInt(value).hash();
   }

   public HashCode createLong(long value) {
      return this.hashFunction.builder(9).addByte((byte)9).addLong(value).hash();
   }

   public HashCode createFloat(float value) {
      return this.hashFunction.builder(5).addByte((byte)10).addFloat(value).hash();
   }

   public HashCode createDouble(double value) {
      return this.hashFunction.builder(9).addByte((byte)11).addDouble(value).hash();
   }

   public HashCode createString(String value) {
      return this.hashFunction.builder().addByte((byte)12).addInt(value.length()).addCharSequence(value).hash();
   }

   public HashCode createNumber(Number number) {
      if (number instanceof Byte) {
         return this.createByte((Byte)number);
      } else if (number instanceof Short) {
         return this.createShort((Short)number);
      } else if (number instanceof Integer) {
         return this.createInt((Integer)number);
      } else if (number instanceof Long) {
         return this.createLong((Long)number);
      } else if (number instanceof Float) {
         return this.createFloat((Float)number);
      } else {
         return number instanceof Double ? this.createDouble((Double)number) : this.createDouble(number.doubleValue());
      }
   }

   public HashCode createList(List<HashCode> values) {
      HashBuilder builder = this.hashFunction.builder().addByte((byte)4);

      for (HashCode value : values) {
         builder.addBytes(value.asBytes());
      }

      return builder.addByte((byte)5).hash();
   }

   public Result<HashCode> mergeList(@Nullable HashCode list, List<HashCode> values) {
      return Result.error("Can't merge already hashed lists");
   }

   public HashCode createByteArray(byte[] value) {
      return this.hashFunction.builder(value.length + 2).addByte((byte)14).addBytes(value).addByte((byte)15).hash();
   }

   public HashCode createIntArray(int[] value) {
      HashBuilder builder = this.hashFunction.builder(value.length * 4 + 2).addByte((byte)16);

      for (int i : value) {
         builder.addInt(i);
      }

      return builder.addByte((byte)17).hash();
   }

   public HashCode createLongArray(long[] value) {
      HashBuilder builder = this.hashFunction.builder(value.length * 8 + 2).addByte((byte)18);

      for (long l : value) {
         builder.addLong(l);
      }

      return builder.addByte((byte)19).hash();
   }

   public HashCode createUnsafeMap(Map<HashCode, HashCode> values) {
      return this.createMergedMap(values).get();
   }

   @Override
   public Result<HashCode> createMergedMap(Map<HashCode, HashCode> values) {
      HashBuilder builder = this.hashFunction.builder();
      builder.addByte((byte)2);
      values.entrySet().stream().sorted(MAP_COMPARATOR).forEach(entry -> builder.addBytes(entry.getKey().asBytes()).addBytes(entry.getValue().asBytes()));
      return Result.success(builder.addByte((byte)3).hash());
   }

   public Result<HashCode> mergeMap(@Nullable HashCode map, Map<HashCode, HashCode> values) {
      return Result.error("Can't merge already hashed maps");
   }

   public Result<Boolean> asBoolean(HashCode element) {
      return Result.error(String.format("Unable to convert HashCode to %s", "boolean"));
   }

   public Result<Number> asNumber(HashCode element) {
      return Result.error(String.format("Unable to convert HashCode to %s", "number"));
   }

   public Result<String> asString(HashCode element) {
      return Result.error(String.format("Unable to convert HashCode to %s", "string"));
   }

   public Result<List<HashCode>> asList(HashCode element) {
      return Result.error(String.format("Unable to convert HashCode to %s", "list"));
   }

   public Result<Map<HashCode, HashCode>> asMap(HashCode element) {
      return Result.error(String.format("Unable to convert HashCode to %s", "map"));
   }

   public Result<Map<String, HashCode>> asStringTypeMap(HashCode element) {
      return Result.error(String.format("Unable to convert HashCode to %s", "string type map"));
   }

   public Result<byte[]> asByteArray(HashCode element) {
      return Result.error(String.format("Unable to convert HashCode to %s", "byte array"));
   }

   public Result<int[]> asIntArray(HashCode element) {
      return Result.error(String.format("Unable to convert HashCode to %s", "int array"));
   }

   public Result<long[]> asLongArray(HashCode element) {
      return Result.error(String.format("Unable to convert HashCode to %s", "long array"));
   }
}
