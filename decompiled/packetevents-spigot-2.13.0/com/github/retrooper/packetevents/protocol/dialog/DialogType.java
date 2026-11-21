package com.github.retrooper.packetevents.protocol.dialog;

import com.github.retrooper.packetevents.protocol.mapper.MappedEntity;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface DialogType<T extends Dialog> extends MappedEntity {
   T decode(NBTCompound compound, PacketWrapper<?> wrapper);

   void encode(NBTCompound compound, PacketWrapper<?> wrapper, T dialog);
}
