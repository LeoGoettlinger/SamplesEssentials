package com.github.retrooper.packetevents.protocol.util;

import com.github.retrooper.packetevents.protocol.nbt.NBT;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.nbt.NBTString;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Predicate;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public interface NbtMapCodec<T> extends NbtMapEncoder<T>, NbtMapDecoder<T> {
   static <K, V> NbtMapCodec<Map<K, V>> codecOfMap(NbtCodec<K> keyCodec, NbtCodec<? extends V> valueCodec) {
      return codecOfMap(keyCodec, __ -> valueCodec);
   }

   static <K, V> NbtMapCodec<Map<K, V>> codecOfMap(NbtCodec<K> keyCodec, Function<K, NbtCodec<? extends V>> valueCodec) {
      return codecOfMap(keyCodec, null, valueCodec);
   }

   static <K, V> NbtMapCodec<Map<K, V>> codecOfMap(NbtCodec<K> keyCodec, @Nullable Predicate<K> keyPredicate, Function<K, NbtCodec<? extends V>> valueCodec) {
      return new NbtMapCodec<Map<K, V>>() {
         public Map<K, V> decode(NBTCompound compound, PacketWrapper<?> wrapper) throws NbtCodecException {
            Map<K, V> map = new HashMap<>(compound.size());

            for (Entry<String, NBT> entry : compound.getTags().entrySet()) {
               K key = keyCodec.decode(new NBTString(entry.getKey()), wrapper);
               if (keyPredicate == null || keyPredicate.test(key)) {
                  V value = (V)valueCodec.apply(key).decode(entry.getValue(), wrapper);
                  map.put(key, value);
               }
            }

            return Collections.unmodifiableMap(map);
         }

         public void encode(NBTCompound compound, PacketWrapper<?> wrapper, Map<K, V> value) throws NbtCodecException {
            for (Entry<K, V> entry : value.entrySet()) {
               if (keyPredicate == null || keyPredicate.test(entry.getKey())) {
                  String name = keyCodec.encode(wrapper, entry.getKey()).castOrThrow(NBTString.class).getValue();
                  NbtCodec<V> codec = (NbtCodec<V>)valueCodec.apply(entry.getKey());
                  NBT tag = codec.encode(wrapper, entry.getValue());
                  compound.setTag(name, tag);
               }
            }
         }
      };
   }

   default NbtCodec<T> codec() {
      return new NbtCodec<T>() {
         @Override
         public T decode(NBT nbt, PacketWrapper<?> wrapper) throws NbtCodecException {
            NBTCompound compound = nbt.castOrThrow(NBTCompound.class);
            return (T)NbtMapCodec.this.decode(compound, wrapper);
         }

         @Override
         public NBT encode(PacketWrapper<?> wrapper, T value) throws NbtCodecException {
            NBTCompound compound = new NBTCompound();
            NbtMapCodec.this.encode(compound, wrapper, value);
            return compound;
         }
      };
   }

   default <Z> NbtMapCodec<Z> apply(Function<T, Z> forward, Function<Z, T> back) {
      return new NbtMapCodec<Z>() {
         @Override
         public Z decode(NBTCompound compound, PacketWrapper<?> wrapper) throws NbtCodecException {
            return forward.apply((T)NbtMapCodec.this.decode(compound, wrapper));
         }

         @Override
         public void encode(NBTCompound compound, PacketWrapper<?> wrapper, Z value) throws NbtCodecException {
            NbtMapCodec.this.encode(compound, wrapper, back.apply(value));
         }
      };
   }
}
