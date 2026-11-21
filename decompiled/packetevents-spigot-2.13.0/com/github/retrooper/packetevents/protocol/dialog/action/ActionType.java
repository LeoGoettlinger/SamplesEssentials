package com.github.retrooper.packetevents.protocol.dialog.action;

import com.github.retrooper.packetevents.protocol.mapper.MappedEntity;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface ActionType<T extends Action> extends MappedEntity {
   T decode(NBTCompound compound, PacketWrapper<?> wrapper);

   void encode(NBTCompound compound, PacketWrapper<?> wrapper, T action);
}
