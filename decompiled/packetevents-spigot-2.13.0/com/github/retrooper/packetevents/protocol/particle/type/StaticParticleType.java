package com.github.retrooper.packetevents.protocol.particle.type;

import com.github.retrooper.packetevents.protocol.mapper.AbstractMappedEntity;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.particle.data.ParticleData;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.util.mappings.TypesBuilderData;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

public class StaticParticleType<T extends ParticleData> extends AbstractMappedEntity implements ParticleType<T> {
   private final PacketWrapper.Reader<T> reader;
   private final PacketWrapper.Writer<T> writer;
   private final ParticleTypes.Decoder<T> decoder;
   private final ParticleTypes.Encoder<T> encoder;

   @Internal
   public StaticParticleType(
      @Nullable TypesBuilderData data,
      PacketWrapper.Reader<T> reader,
      PacketWrapper.Writer<T> writer,
      ParticleTypes.Decoder<T> decoder,
      ParticleTypes.Encoder<T> encoder
   ) {
      super(data);
      this.reader = reader;
      this.writer = writer;
      this.decoder = decoder;
      this.encoder = encoder;
   }

   @Override
   public T readData(PacketWrapper<?> wrapper) {
      return this.reader.apply(wrapper);
   }

   @Override
   public void writeData(PacketWrapper<?> wrapper, T data) {
      if (this.writer != null) {
         this.writer.accept(wrapper, data);
      } else if (!data.isEmpty()) {
         throw new UnsupportedOperationException("Trying to write non-empty data for " + this.getName());
      }
   }

   @Override
   public T decodeData(NBTCompound compound, ClientVersion version) {
      return this.decoder.decode(compound, version);
   }

   @Override
   public void encodeData(T value, ClientVersion version, NBTCompound compound) {
      if (this.encoder != null) {
         this.encoder.encode(value, version, compound);
      } else if (!value.isEmpty()) {
         throw new UnsupportedOperationException("Trying to encode non-empty data for " + this.getName());
      }
   }
}
