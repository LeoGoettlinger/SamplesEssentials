package com.github.retrooper.packetevents.protocol.world.positionsource;

import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.world.positionsource.builtin.BlockPositionSource;
import com.github.retrooper.packetevents.protocol.world.positionsource.builtin.EntityPositionSource;
import com.github.retrooper.packetevents.util.mappings.VersionedRegistry;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

public final class PositionSourceTypes {
   private static final VersionedRegistry<PositionSourceType<?>> REGISTRY = new VersionedRegistry<>("position_source_type");
   public static final PositionSourceType<BlockPositionSource> BLOCK = define(
      "block", BlockPositionSource::read, BlockPositionSource::write, BlockPositionSource::decodeSource, BlockPositionSource::encodeSource
   );
   public static final PositionSourceType<EntityPositionSource> ENTITY = define(
      "entity", EntityPositionSource::read, EntityPositionSource::write, EntityPositionSource::decodeSource, EntityPositionSource::encodeSource
   );

   private PositionSourceTypes() {
   }

   public static VersionedRegistry<PositionSourceType<?>> getRegistry() {
      return REGISTRY;
   }

   @Internal
   public static <T extends PositionSource> PositionSourceType<T> define(
      String name,
      PacketWrapper.Reader<T> reader,
      PacketWrapper.Writer<T> writer,
      PositionSourceTypes.Decoder<T> decoder,
      PositionSourceTypes.Encoder<T> encoder
   ) {
      return REGISTRY.define(name, data -> new StaticPositionSourceType<>(data, reader, writer, decoder, encoder));
   }

   @Nullable
   public static PositionSourceType<?> getByName(String name) {
      return REGISTRY.getByName(name);
   }

   public static PositionSourceType<?> getById(ClientVersion version, int id) {
      return REGISTRY.getById(version, id);
   }

   static {
      REGISTRY.unloadMappings();
   }

   @FunctionalInterface
   public interface Decoder<T> {
      T decode(NBTCompound compound, ClientVersion version);
   }

   @FunctionalInterface
   public interface Encoder<T> {
      void encode(T value, ClientVersion version, NBTCompound compound);
   }
}
