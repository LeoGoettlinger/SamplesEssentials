package com.viaversion.viaversion.libs.mcstructs.converter.codec;

import com.viaversion.viaversion.libs.mcstructs.converter.DataConverter;
import com.viaversion.viaversion.libs.mcstructs.converter.model.Result;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

public class DynamicMapCodec<K, V> implements Codec<Map<K, V>> {
   private final Codec<K> keyCodec;
   private final Function<K, Codec<? extends V>> keyToValueCodec;

   public DynamicMapCodec(Codec<K> keyCodec, Function<K, Codec<? extends V>> keyToValueCodec) {
      this.keyCodec = keyCodec;
      this.keyToValueCodec = keyToValueCodec;
   }

   public <S> Result<S> serialize(DataConverter<S> converter, Map<K, V> element) {
      Map<S, S> out = new HashMap();

      for (Entry<K, V> entry : element.entrySet()) {
         Result<S> key = this.keyCodec.serialize(converter, entry.getKey());
         if (key.isError()) {
            return key.mapError();
         }

         Codec<V> valueCodec = (Codec<V>)this.keyToValueCodec.apply(entry.getKey());
         Result<S> value = valueCodec.serialize(converter, entry.getValue());
         if (value.isError()) {
            return value.mapError();
         }

         out.put(key.get(), value.get());
      }

      return converter.createMergedMap(out);
   }

   @Override
   public <S> Result<Map<K, V>> deserialize(DataConverter<S> converter, S data) {
      Result<Map<S, S>> mapResult = converter.asMap(data);
      return mapResult.mapResult(map -> {
         Map<K, V> out = new HashMap<>();

         for (Entry<S, S> entry : map.entrySet()) {
            Result<K> key = this.keyCodec.deserialize(converter, entry.getKey());
            if (key.isError()) {
               return key.mapError();
            }

            Codec<? extends V> valueCodec = this.keyToValueCodec.apply(key.get());
            if (valueCodec == null) {
               return Result.error("No codec found for key: " + key.get());
            }

            Result<V> value = (Result<V>)valueCodec.deserialize(converter, entry.getValue());
            if (value.isError()) {
               return value.mapError();
            }

            out.put(key.get(), value.get());
         }

         return Result.success(out);
      });
   }
}
