package com.viaversion.viaversion.libs.mcstructs.converter.codec;

import com.viaversion.viaversion.libs.mcstructs.converter.DataConverter;
import com.viaversion.viaversion.libs.mcstructs.converter.SerializedData;
import com.viaversion.viaversion.libs.mcstructs.converter.mapcodec.MapCodec;
import com.viaversion.viaversion.libs.mcstructs.converter.mapcodec.impl.FieldMapCodec;
import com.viaversion.viaversion.libs.mcstructs.converter.mapcodec.impl.TypedMapCodec;
import com.viaversion.viaversion.libs.mcstructs.converter.model.Either;
import com.viaversion.viaversion.libs.mcstructs.converter.model.Result;
import com.viaversion.viaversion.libs.mcstructs.converter.types.IdentifiedType;
import com.viaversion.viaversion.libs.mcstructs.converter.types.NamedType;
import com.viaversion.viaversion.libs.mcstructs.core.Identifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Codec<T> extends DataSerializer<T>, DataDeserializer<T> {
   Codec<Boolean> UNIT = unit(() -> true);
   Codec<Boolean> BOOLEAN = new Codec<Boolean>() {
      public <S> Result<S> serialize(DataConverter<S> converter, Boolean element) {
         return Result.success(converter.createBoolean(element));
      }

      @Override
      public <S> Result<Boolean> deserialize(DataConverter<S> converter, S data) {
         return converter.asBoolean(data);
      }
   };
   Codec<Byte> BYTE = new Codec<Byte>() {
      public <S> Result<S> serialize(DataConverter<S> converter, Byte element) {
         return Result.success(converter.createByte(element));
      }

      @Override
      public <S> Result<Byte> deserialize(DataConverter<S> converter, S data) {
         return converter.asNumber(data).map(Number::byteValue);
      }
   };
   Codec<Integer> UNSIGNED_BYTE = BYTE.flatMap(i -> {
      if (i < 0) {
         return Result.error("Value is smaller than minimum: " + i + " < 0");
      } else {
         return i > 255 ? Result.error("Value is bigger than maximum: " + i + " > 255") : Result.success((byte)(i & 0xFF));
      }
   }, b -> Result.success(b & 255));
   Codec<Short> SHORT = new Codec<Short>() {
      public <S> Result<S> serialize(DataConverter<S> converter, Short element) {
         return Result.success(converter.createShort(element));
      }

      @Override
      public <S> Result<Short> deserialize(DataConverter<S> converter, S data) {
         return converter.asNumber(data).map(Number::shortValue);
      }
   };
   Codec<Integer> INTEGER = new Codec<Integer>() {
      public <S> Result<S> serialize(DataConverter<S> converter, Integer element) {
         return Result.success(converter.createInt(element));
      }

      @Override
      public <S> Result<Integer> deserialize(DataConverter<S> converter, S data) {
         return converter.asNumber(data).map(Number::intValue);
      }
   };
   Codec<Long> LONG = new Codec<Long>() {
      public <S> Result<S> serialize(DataConverter<S> converter, Long element) {
         return Result.success(converter.createLong(element));
      }

      @Override
      public <S> Result<Long> deserialize(DataConverter<S> converter, S data) {
         return converter.asNumber(data).map(Number::longValue);
      }
   };
   Codec<Float> FLOAT = new Codec<Float>() {
      public <S> Result<S> serialize(DataConverter<S> converter, Float element) {
         return Result.success(converter.createFloat(element));
      }

      @Override
      public <S> Result<Float> deserialize(DataConverter<S> converter, S data) {
         return converter.asNumber(data).map(Number::floatValue);
      }
   };
   Codec<Double> DOUBLE = new Codec<Double>() {
      public <S> Result<S> serialize(DataConverter<S> converter, Double element) {
         return Result.success(converter.createDouble(element));
      }

      @Override
      public <S> Result<Double> deserialize(DataConverter<S> converter, S data) {
         return converter.asNumber(data).map(Number::doubleValue);
      }
   };
   Codec<String> STRING = new Codec<String>() {
      public <S> Result<S> serialize(DataConverter<S> converter, String element) {
         return Result.success(converter.createString(element));
      }

      @Override
      public <S> Result<String> deserialize(DataConverter<S> converter, S data) {
         return converter.asString(data);
      }
   };
   Codec<byte[]> BYTE_ARRAY = new Codec<byte[]>() {
      public <S> Result<S> serialize(DataConverter<S> converter, byte[] element) {
         return Result.success(converter.createByteArray(element));
      }

      @Override
      public <S> Result<byte[]> deserialize(DataConverter<S> converter, S data) {
         return converter.asByteArray(data);
      }
   };
   Codec<int[]> INT_ARRAY = new Codec<int[]>() {
      public <S> Result<S> serialize(DataConverter<S> converter, int[] element) {
         return Result.success(converter.createIntArray(element));
      }

      @Override
      public <S> Result<int[]> deserialize(DataConverter<S> converter, S data) {
         return converter.asIntArray(data);
      }
   };
   Codec<long[]> LONG_ARRAY = new Codec<long[]>() {
      public <S> Result<S> serialize(DataConverter<S> converter, long[] element) {
         return Result.success(converter.createLongArray(element));
      }

      @Override
      public <S> Result<long[]> deserialize(DataConverter<S> converter, S data) {
         return converter.asLongArray(data);
      }
   };
   Codec<Identifier> STRING_IDENTIFIER = STRING.mapThrowing(Identifier::get, Identifier::of);
   Codec<String> STRING_IDENTIFIER_PATH = STRING.verified(s -> s.matches("[_\\-a-z0-9/.]*") ? Result.error("Invalid identifier path: " + s) : null);
   Codec<UUID> INT_ARRAY_UUID = INT_ARRAY.verified(array -> array.length != 4 ? Result.error("UUID array must have a length of 4") : null)
      .map(
         uuid -> new int[]{
            (int)(uuid.getMostSignificantBits() >> 32),
            (int)uuid.getMostSignificantBits(),
            (int)(uuid.getLeastSignificantBits() >> 32),
            (int)uuid.getLeastSignificantBits()
         },
         ints -> {
            long mostSigBits = (long)ints[0] << 32 | ints[1] & 4294967295L;
            long leastSigBits = (long)ints[2] << 32 | ints[3] & 4294967295L;
            return new UUID(mostSigBits, leastSigBits);
         }
      );
   Codec<UUID> STRICT_STRING_UUID = STRING.mapThrowing(UUID::toString, UUID::fromString);
   Codec<SerializedData<?>> RAW = new Codec<SerializedData<?>>() {
      public <S> Result<S> serialize(DataConverter<S> converter, SerializedData<?> element) {
         return element.convert(converter);
      }

      @Override
      public <S> Result<SerializedData<?>> deserialize(DataConverter<S> converter, S data) {
         return Result.success(new SerializedData<>(data, converter));
      }
   };

   static <T> Codec<T> unit(final Supplier<T> supplier) {
      return new Codec<T>() {
         @Override
         public <S> Result<S> serialize(DataConverter<S> converter, T element) {
            return Result.success(converter.emptyMap());
         }

         @Override
         public <S> Result<T> deserialize(DataConverter<S> converter, S data) {
            return converter.asMap(data).map(map -> supplier.get());
         }
      };
   }

   static <T> Codec<T> failing(final String error) {
      return new Codec<T>() {
         @Override
         public <S> Result<S> serialize(DataConverter<S> converter, T element) {
            return Result.error(error);
         }

         @Override
         public <S> Result<T> deserialize(DataConverter<S> converter, S data) {
            return Result.error(error);
         }
      };
   }

   static <T> Codec<T> ofThrowing(final DataSerializer<T> serializer, final DataDeserializer<T> deserializer) {
      return new Codec<T>() {
         @Override
         public <S> Result<S> serialize(DataConverter<S> converter, T element) {
            try {
               return serializer.serialize(converter, element);
            } catch (Throwable var4) {
               return Result.error(var4);
            }
         }

         @Override
         public <S> Result<T> deserialize(DataConverter<S> converter, S data) {
            try {
               return deserializer.deserialize(converter, data);
            } catch (Throwable var4) {
               return Result.error(var4);
            }
         }
      };
   }

   static <T> Codec<T> recursive(final Function<Codec<T>, Codec<T>> creator) {
      return new Codec<T>() {
         private final Codec<T> codec = creator.apply(this);

         @Override
         public <S> Result<S> serialize(DataConverter<S> converter, T element) {
            return this.codec.serialize(converter, element);
         }

         @Override
         public <S> Result<T> deserialize(DataConverter<S> converter, S data) {
            return this.codec.deserialize(converter, data);
         }
      };
   }

   static Codec<Integer> minInt(int minInclusive) {
      return rangedInt(minInclusive, Integer.MAX_VALUE);
   }

   static Codec<Integer> rangedInt(int minInclusive, int maxInclusive) {
      return INTEGER.verified(i -> {
         if (i < minInclusive) {
            return Result.error("Value is smaller than minimum: " + i + " < " + minInclusive);
         } else {
            return i > maxInclusive ? Result.error("Value is bigger than maximum: " + i + " > " + maxInclusive) : null;
         }
      });
   }

   static Codec<Float> minFloat(float minInclusive) {
      return rangedFloat(minInclusive, Float.MAX_VALUE);
   }

   static Codec<Float> minExclusiveFloat(float minExclusive) {
      return FLOAT.verified(f -> f <= minExclusive ? Result.error("Value is smaller or equal to minimum: " + f + " <= " + minExclusive) : null);
   }

   static Codec<Float> rangedFloat(float minInclusive, float maxInclusive) {
      return FLOAT.verified(f -> {
         if (f < minInclusive) {
            return Result.error("Value is smaller than minimum: " + f + " < " + minInclusive);
         } else {
            return f > maxInclusive ? Result.error("Value is bigger than maximum: " + f + " > " + maxInclusive) : null;
         }
      });
   }

   static Codec<String> sizedString(int minInclusive, int maxInclusive) {
      return STRING.verified(s -> {
         if (s.length() < minInclusive) {
            return Result.error("String is shorter than minimum: " + s.length() + " < " + minInclusive);
         } else {
            return s.length() > maxInclusive ? Result.error("String is longer than maximum: " + s.length() + " > " + maxInclusive) : null;
         }
      });
   }

   @SafeVarargs
   static <T extends NamedType> Codec<T> named(T... values) {
      return STRING.flatMap(named -> Result.success(named.getName()), name -> {
         for (T value : values) {
            if (value.getName().equals(name)) {
               return Result.success(value);
            }
         }

         String available = "";

         for (T valuex : values) {
            available = available + valuex.getName() + ", ";
         }

         if (!available.isEmpty()) {
            available = available.substring(0, available.length() - 2);
         }

         return Result.error("Unknown named value: " + name + " (" + available + ")");
      });
   }

   static <T extends IdentifiedType> Codec<T> identified(T[] values) {
      return STRING_IDENTIFIER.flatMap(identified -> Result.success(identified.getIdentifier()), identifier -> {
         for (T value : values) {
            if (value.getIdentifier().equals(identifier)) {
               return Result.success(value);
            }
         }

         String available = "";

         for (T valuex : values) {
            available = available + valuex.getIdentifier() + ", ";
         }

         if (!available.isEmpty()) {
            available = available.substring(0, available.length() - 2);
         }

         return Result.error("Unknown identified value: " + identifier + " (" + available + ")");
      });
   }

   static <L, R> Codec<Either<L, R>> either(final Codec<L> leftCodec, final Codec<R> rightCodec) {
      return new Codec<Either<L, R>>() {
         public <S> Result<S> serialize(DataConverter<S> converter, Either<L, R> element) {
            if (element.isLeft()) {
               return leftCodec.serialize(converter, element.getLeft());
            } else {
               return element.isRight() ? rightCodec.serialize(converter, element.getRight()) : Result.error("Either is neither left nor right");
            }
         }

         @Override
         public <S> Result<Either<L, R>> deserialize(DataConverter<S> converter, S data) {
            Result<L> left = leftCodec.deserialize(converter, data);
            if (!left.isError()) {
               return Result.success(Either.left(left.get()));
            } else {
               Result<R> right = rightCodec.deserialize(converter, data);
               return !right.isError()
                  ? Result.success(Either.right(right.get()))
                  : Result.mergeErrors("Failed to deserialize as either left or right", Arrays.asList(left, right));
            }
         }
      };
   }

   static <T> Codec<T> withAlternative(Codec<T> codec, Codec<T> alternative) {
      return either(codec, alternative).map(Either::left, Either::unwrap);
   }

   static <T, A> Codec<T> withAlternative(Codec<T> codec, Codec<A> alternative, Function<A, T> mapper) {
      return either(codec, alternative).map(Either::left, either -> either.xmap(Function.identity(), mapper));
   }

   @SafeVarargs
   static <T> Codec<T> oneOf(final Codec<T>... codecs) {
      if (codecs.length == 0) {
         throw new IllegalArgumentException("At least one codec is required");
      } else {
         return new Codec<T>() {
            @Override
            public <S> Result<S> serialize(DataConverter<S> converter, T element) {
               List<Result<?>> failed = null;

               for (Codec<T> codec : codecs) {
                  Result<S> result = codec.serialize(converter, element);
                  if (!result.isError()) {
                     return result;
                  }

                  if (failed == null) {
                     failed = new ArrayList<>();
                  }

                  failed.add(result.mapError());
               }

               return Result.mergeErrors("Failed to serialize with " + codecs.length + " codecs", failed);
            }

            @Override
            public <S> Result<T> deserialize(DataConverter<S> converter, S data) {
               List<Result<?>> failed = null;

               for (Codec<T> codec : codecs) {
                  Result<T> result = codec.deserialize(converter, data);
                  if (!result.isError()) {
                     return result;
                  }

                  if (failed == null) {
                     failed = new ArrayList<>();
                  }

                  failed.add(result.mapError());
               }

               return Result.mergeErrors("Failed to deserialize with " + codecs.length + " codecs", failed);
            }
         };
      }
   }

   static <K, V> Codec<Map<K, V>> mapOf(Codec<K> keyCodec, Codec<V> valueCodec) {
      return mapOf(keyCodec, key -> valueCodec);
   }

   static <K, V> Codec<Map<K, V>> mapOf(final Codec<K> keyCodec, final Function<K, Codec<V>> valueFunction) {
      return new Codec<Map<K, V>>() {
         public <S> Result<S> serialize(DataConverter<S> converter, Map<K, V> element) {
            Map<S, S> map = new HashMap();

            for (Entry<K, V> entry : element.entrySet()) {
               Result<S> key = keyCodec.serialize(converter, entry.getKey());
               if (key.isError()) {
                  return key.mapError();
               }

               Result<S> value = valueFunction.apply(entry.getKey()).serialize(converter, entry.getValue());
               if (value.isError()) {
                  return value.mapError();
               }

               map.put(key.get(), value.get());
            }

            return converter.createMergedMap(map);
         }

         @Override
         public <S> Result<Map<K, V>> deserialize(DataConverter<S> converter, S data) {
            Result<Map<S, S>> map = converter.asMap(data);
            if (map.isError()) {
               return map.mapError();
            } else {
               Map<K, V> converted = new HashMap<>();

               for (Entry<S, S> entry : map.get().entrySet()) {
                  Result<K> key = keyCodec.deserialize(converter, entry.getKey());
                  if (key.isError()) {
                     return key.mapError();
                  }

                  Result<V> value = valueFunction.apply(key.get()).deserialize(converter, entry.getValue());
                  if (value.isError()) {
                     return value.mapError();
                  }

                  converted.put(key.get(), value.get());
               }

               return Result.success(converted);
            }
         }
      };
   }

   static <T> Codec<List<T>> compactList(Codec<T> elementCodec) {
      return compactList(elementCodec, elementCodec.listOf());
   }

   static <T> Codec<List<T>> compactList(Codec<T> elementCodec, Codec<List<T>> listCodec) {
      return either(listCodec, elementCodec)
         .map(list -> list.size() == 1 ? Either.right(list.get(0)) : Either.left(list), either -> either.xmap(list -> list, item -> {
            List<T> list = new ArrayList<>();
            list.add(item);
            return list;
         }));
   }

   static <T> Codec<T> lazyInit(final Supplier<Codec<T>> initializer) {
      return new Codec<T>() {
         private Codec<T> codec;

         @Override
         public <S> Result<S> serialize(DataConverter<S> converter, T element) {
            if (this.codec == null) {
               this.codec = initializer.get();
            }

            return this.codec.serialize(converter, element);
         }

         @Override
         public <S> Result<T> deserialize(DataConverter<S> converter, S data) {
            if (this.codec == null) {
               this.codec = initializer.get();
            }

            return this.codec.deserialize(converter, data);
         }
      };
   }

   default <N> Codec<N> map(final Function<N, T> serializer, final Function<T, N> deserializer) {
      return new Codec<N>() {
         @Override
         public <S> Result<S> serialize(DataConverter<S> converter, N element) {
            return Codec.this.serialize(converter, serializer.apply(element));
         }

         @Override
         public <S> Result<N> deserialize(DataConverter<S> converter, S data) {
            return Codec.this.deserialize(converter, data).map(deserializer);
         }
      };
   }

   default <N> Codec<N> mapThrowing(ThrowingFunction<N, T> serializer, ThrowingFunction<T, N> deserializer) {
      return this.flatMap(n -> {
         try {
            return Result.success(serializer.apply(n));
         } catch (Throwable var3) {
            return Result.error(var3);
         }
      }, t -> {
         try {
            return Result.success(deserializer.apply(t));
         } catch (Throwable var3) {
            return Result.error(var3);
         }
      });
   }

   default <N> Codec<N> flatMap(final Function<N, Result<T>> serializer, final Function<T, Result<N>> deserializer) {
      return new Codec<N>() {
         @Override
         public <S> Result<S> serialize(DataConverter<S> converter, N element) {
            Result<T> result = serializer.apply(element);
            return result.isError() ? result.mapError() : Codec.this.serialize(converter, result.get());
         }

         @Override
         public <S> Result<N> deserialize(DataConverter<S> converter, S data) {
            Result<T> result = Codec.this.deserialize(converter, data);
            return result.isError() ? result.mapError() : deserializer.apply(result.get());
         }
      };
   }

   default <N> Codec<N> converterFlatMap(
      final BiFunction<DataConverter<?>, N, Result<T>> serializer, final BiFunction<DataConverter<?>, T, Result<N>> deserializer
   ) {
      return new Codec<N>() {
         @Override
         public <S> Result<S> serialize(DataConverter<S> converter, N element) {
            Result<T> result = serializer.apply(converter, element);
            return result.isError() ? result.mapError() : Codec.this.serialize(converter, result.get());
         }

         @Override
         public <S> Result<N> deserialize(DataConverter<S> converter, S data) {
            Result<T> result = Codec.this.deserialize(converter, data);
            return result.isError() ? result.mapError() : deserializer.apply(converter, result.get());
         }
      };
   }

   default <N> Codec<N> typed(Function<N, T> typeMapper, Function<T, MapCodec<? extends N>> continuation) {
      return this.typedMap(typeMapper, continuation).asCodec();
   }

   default <N> Codec<N> typed(String typeKey, Function<N, T> typeMapper, Function<T, MapCodec<? extends N>> continuation) {
      return this.typedMap(typeKey, typeMapper, continuation).asCodec();
   }

   default <N> MapCodec<N> typedMap(Function<N, T> typeMapper, Function<T, MapCodec<? extends N>> continuation) {
      return this.typedMap("type", typeMapper, continuation);
   }

   default <N> MapCodec<N> typedMap(String typeKey, Function<N, T> typeMapper, Function<T, MapCodec<? extends N>> continuation) {
      return new TypedMapCodec<>(typeKey, this, typeMapper, continuation);
   }

   default Codec<List<T>> listOf() {
      return this.listOf(Integer.MAX_VALUE);
   }

   default Codec<List<T>> listOf(int maxElements) {
      return this.listOf(0, maxElements);
   }

   default Codec<List<T>> listOf(final int minElements, final int maxElements) {
      return new Codec<List<T>>() {
         public <S> Result<S> serialize(DataConverter<S> converter, List<T> element) {
            if (element.size() < minElements) {
               return Result.error("List size is smaller than minimum: " + element.size() + " < " + minElements);
            } else if (element.size() > maxElements) {
               return Result.error("List size is bigger than maximum: " + element.size() + " > " + maxElements);
            } else {
               List<S> converted = new ArrayList<>();

               for (T value : element) {
                  Result<S> result = Codec.this.serialize(converter, value);
                  if (result.isError()) {
                     return result.mapError();
                  }

                  converted.add(result.get());
               }

               return Result.success(converter.createList(converted));
            }
         }

         @Override
         public <S> Result<List<T>> deserialize(DataConverter<S> converter, S data) {
            Result<List<S>> result = converter.asList(data);
            if (result.isError()) {
               return result.mapError();
            } else {
               List<S> list = result.get();
               if (list.size() < minElements) {
                  return Result.error("List size is smaller than minimum: " + list.size() + " < " + minElements);
               } else if (list.size() > maxElements) {
                  return Result.error("List size is bigger than maximum: " + list.size() + " > " + maxElements);
               } else {
                  List<T> converted = new ArrayList<>();

                  for (S value : list) {
                     Result<T> element = Codec.this.deserialize(converter, value);
                     if (element.isError()) {
                        return element.mapError();
                     }

                     converted.add(element.get());
                  }

                  return Result.success(converted);
               }
            }
         }
      };
   }

   default Codec<List<T>> optionalListOf() {
      return this.optionalListOf(Integer.MAX_VALUE);
   }

   default Codec<List<T>> optionalListOf(int maxElements) {
      return this.optionalListOf(0, maxElements);
   }

   default Codec<List<T>> optionalListOf(int minElements, int maxElements) {
      final Codec<List<T>> listCodec = this.listOf(minElements, maxElements);
      return new Codec<List<T>>() {
         @Override
         public <S> Result<List<T>> deserialize(DataConverter<S> converter, S data) {
            Result<T> single = Codec.this.deserialize(converter, data);
            if (single.isError()) {
               return listCodec.deserialize(converter, data);
            } else {
               List<T> list = new ArrayList<>();
               list.add(single.get());
               return Result.success(list);
            }
         }

         public <S> Result<S> serialize(DataConverter<S> converter, List<T> element) {
            return element.size() == 1 ? Codec.this.serialize(converter, element.get(0)) : listCodec.serialize(converter, element);
         }
      };
   }

   default Codec<List<T>> nonEmptyList() {
      return this.listOf().verified(list -> list.isEmpty() ? Result.error("List is empty") : null);
   }

   default Codec<List<T>> compactListOf() {
      return compactList(this, this.listOf());
   }

   default Codec<T> verified(final Function<T, Result<Void>> verifier) {
      return new Codec<T>() {
         @Override
         public <S> Result<S> serialize(DataConverter<S> converter, T element) {
            Result<Void> verify = verifier.apply(element);
            return verify != null && verify.isError() ? verify.mapError() : Codec.this.serialize(converter, element);
         }

         @Override
         public <S> Result<T> deserialize(DataConverter<S> converter, S data) {
            Result<T> result = Codec.this.deserialize(converter, data);
            if (result.isError()) {
               return result;
            } else {
               Result<Void> verify = verifier.apply(result.get());
               return verify != null && verify.isError() ? verify.mapError() : result;
            }
         }
      };
   }

   default Codec<T> converterVerified(final BiFunction<DataConverter<?>, T, Result<Void>> verifier) {
      return new Codec<T>() {
         @Override
         public <S> Result<S> serialize(DataConverter<S> converter, T element) {
            Result<Void> verify = verifier.apply(converter, element);
            return verify != null && verify.isError() ? verify.mapError() : Codec.this.serialize(converter, element);
         }

         @Override
         public <S> Result<T> deserialize(DataConverter<S> converter, S data) {
            Result<T> result = Codec.this.deserialize(converter, data);
            if (result.isError()) {
               return result;
            } else {
               Result<Void> verify = verifier.apply(converter, result.get());
               return verify != null && verify.isError() ? verify.mapError() : result;
            }
         }
      };
   }

   default FieldMapCodec.Builder.Stage1<T> mapCodec(String key) {
      return FieldMapCodec.builder(this, key);
   }
}
