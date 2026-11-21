package com.github.retrooper.packetevents.protocol.dialog.body;

import com.github.retrooper.packetevents.protocol.mapper.AbstractMappedEntity;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.util.NbtMapDecoder;
import com.github.retrooper.packetevents.protocol.util.NbtMapEncoder;
import com.github.retrooper.packetevents.util.mappings.TypesBuilderData;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class StaticDialogBodyType<T extends DialogBody> extends AbstractMappedEntity implements DialogBodyType<T> {
   private final NbtMapDecoder<T> decoder;
   private final NbtMapEncoder<T> encoder;

   @Internal
   public StaticDialogBodyType(@Nullable TypesBuilderData data, NbtMapDecoder<T> decoder, NbtMapEncoder<T> encoder) {
      super(data);
      this.decoder = decoder;
      this.encoder = encoder;
   }

   @Override
   public T decode(NBTCompound compound, PacketWrapper<?> wrapper) {
      return this.decoder.decode(compound, wrapper);
   }

   @Override
   public void encode(NBTCompound compound, PacketWrapper<?> wrapper, T dialogBody) {
      this.encoder.encode(compound, wrapper, dialogBody);
   }
}
