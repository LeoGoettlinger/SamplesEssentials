package com.github.retrooper.packetevents.protocol.component;

import com.github.retrooper.packetevents.protocol.mapper.MappedEntity;
import com.github.retrooper.packetevents.protocol.nbt.NBT;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.function.Function;
import org.jetbrains.annotations.ApiStatus.Internal;

public interface ComponentType<T> extends MappedEntity {
   T read(PacketWrapper<?> wrapper);

   void write(PacketWrapper<?> wrapper, T content);

   T decode(NBT nbt, ClientVersion version);

   NBT encode(T value, ClientVersion version);

   @Internal
   <Z> ComponentType<Z> legacyMap(Function<T, Z> mapper, Function<Z, T> unmapper);

   public interface Decoder<T> {
      T decode(NBT nbt, ClientVersion version);
   }

   public interface Encoder<T> {
      NBT encode(T value, ClientVersion version);
   }
}
