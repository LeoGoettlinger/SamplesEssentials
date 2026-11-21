package com.github.retrooper.packetevents.protocol.world.positionsource;

import com.github.retrooper.packetevents.protocol.mapper.AbstractMappedEntity;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.util.mappings.TypesBuilderData;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

public class StaticPositionSourceType<T extends PositionSource> extends AbstractMappedEntity implements PositionSourceType<T> {
   private final PacketWrapper.Reader<T> reader;
   private final PacketWrapper.Writer<T> writer;
   private final PositionSourceTypes.Decoder<T> decoder;
   private final PositionSourceTypes.Encoder<T> encoder;

   @Internal
   public StaticPositionSourceType(
      @Nullable TypesBuilderData data,
      PacketWrapper.Reader<T> reader,
      PacketWrapper.Writer<T> writer,
      PositionSourceTypes.Decoder<T> decoder,
      PositionSourceTypes.Encoder<T> encoder
   ) {
      super(data);
      this.reader = reader;
      this.writer = writer;
      this.decoder = decoder;
      this.encoder = encoder;
   }

   @Override
   public T read(PacketWrapper<?> wrapper) {
      return this.reader.apply(wrapper);
   }

   @Override
   public void write(PacketWrapper<?> wrapper, T source) {
      this.writer.accept(wrapper, source);
   }

   @Override
   public T decode(NBTCompound compound, ClientVersion version) {
      return this.decoder.decode(compound, version);
   }

   @Override
   public void encode(T source, ClientVersion version, NBTCompound compound) {
      this.encoder.encode(source, version, compound);
   }
}
