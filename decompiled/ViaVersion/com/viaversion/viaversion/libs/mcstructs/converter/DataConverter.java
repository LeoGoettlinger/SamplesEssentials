package com.viaversion.viaversion.libs.mcstructs.converter;

import com.viaversion.viaversion.libs.mcstructs.converter.codec.Codec;
import com.viaversion.viaversion.libs.mcstructs.converter.model.Result;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;

public interface DataConverter<T> extends ConsumerTracking {
   default DataConverter<T> forkIfDefault() {
      return this;
   }

   <N> N convertTo(DataConverter<N> var1, @Nullable T var2);

   default <N> T convertFrom(DataConverter<N> converter, @Nullable N element) {
      return converter.convertTo(this, element);
   }

   default <N> N convertList(DataConverter<N> to, T list) {
      List<T> in = this.asList(list).orElse(new ArrayList<>());
      List<N> out = new ArrayList<>();

      for (T element : in) {
         out.add(this.convertTo(to, element));
      }

      return to.createList(out);
   }

   default <N> N convertMap(DataConverter<N> to, T map) {
      Map<T, T> in = this.asMap(map).orElse(new HashMap<>());
      Map<N, N> out = new HashMap<>();

      for (Entry<T, T> entry : in.entrySet()) {
         out.put(this.convertTo(to, entry.getKey()), this.convertTo(to, entry.getValue()));
      }

      return to.createUnsafeMap(out);
   }

   default T empty() {
      return null;
   }

   T createBoolean(boolean var1);

   Result<Boolean> asBoolean(T var1);

   T createNumber(Number var1);

   Result<Number> asNumber(T var1);

   default T createByte(byte value) {
      return this.createNumber(value);
   }

   default T createShort(short value) {
      return this.createNumber(value);
   }

   default T createInt(int value) {
      return this.createNumber(value);
   }

   default T createLong(long value) {
      return this.createNumber(value);
   }

   default T createFloat(float value) {
      return this.createNumber(value);
   }

   default T createDouble(double value) {
      return this.createNumber(value);
   }

   T createString(String var1);

   Result<String> asString(T var1);

   default T createList(List<T> values) {
      return this.mergeList(null, values).get();
   }

   default T emptyList() {
      return this.createList(Collections.emptyList());
   }

   Result<T> mergeList(@Nullable T var1, List<T> var2);

   default Result<T> mergeList(@Nullable T list, T... values) {
      return this.mergeList(list, Arrays.asList(values));
   }

   Result<List<T>> asList(T var1);

   T createUnsafeMap(Map<T, T> var1);

   default T emptyMap() {
      return this.createUnsafeMap(Collections.emptyMap());
   }

   default Result<T> createMergedMap(Map<T, T> values) {
      T map = this.emptyMap();

      for (Entry<T, T> entry : values.entrySet()) {
         Result<T> mergedMap = this.mergeMap(map, entry.getKey(), entry.getValue());
         if (mergedMap.isError()) {
            return mergedMap;
         }

         map = mergedMap.get();
      }

      return Result.success(map);
   }

   Result<T> mergeMap(@Nullable T var1, Map<T, T> var2);

   default Result<T> mergeMap(@Nullable T map, T key, T value) {
      return this.mergeMap(map, Collections.singletonMap(key, value));
   }

   default Result<T> mergeMap(@Nullable T map, T... keyValue) {
      if (keyValue.length % 2 != 0) {
         return Result.error("Key-Value pairs must be even");
      } else {
         Map<T, T> mapValues = new HashMap<>();

         for (int i = 0; i < keyValue.length; i += 2) {
            mapValues.put(keyValue[i], keyValue[i + 1]);
         }

         return this.mergeMap(map, mapValues);
      }
   }

   Result<Map<T, T>> asMap(T var1);

   Result<Map<String, T>> asStringTypeMap(T var1);

   T createByteArray(byte[] var1);

   Result<byte[]> asByteArray(T var1);

   T createIntArray(int[] var1);

   Result<int[]> asIntArray(T var1);

   T createLongArray(long[] var1);

   Result<long[]> asLongArray(T var1);

   default Codec<T> toCodec() {
      return new Codec<T>() {
         @Override
         public <S> Result<T> deserialize(DataConverter<S> converter, S data) {
            try {
               return Result.success(DataConverter.this.fork(converter).convertTo(DataConverter.this, data));
            } catch (Throwable var4) {
               return Result.error(var4);
            }
         }

         @Override
         public <S> Result<S> serialize(DataConverter<S> converter, T element) {
            try {
               return Result.success(converter.fork(converter).convertFrom(DataConverter.this, element));
            } catch (Throwable var4) {
               return Result.error(var4);
            }
         }
      };
   }

   default <O> DataConverter<O> fork(DataConverter<O> other) {
      if (this.currentConsumer() != null) {
         DataConverter<O> fork = other.forkIfDefault();
         fork.setCurrentConsumer(this.currentConsumer());
         return fork;
      } else {
         return other;
      }
   }
}
