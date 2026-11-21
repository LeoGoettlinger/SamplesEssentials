package com.github.retrooper.packetevents.protocol.chat.clickevent;

import com.github.retrooper.packetevents.protocol.mapper.MappedEntity;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface ClickEventAction<T extends ClickEvent> extends MappedEntity {
   boolean isAllowFromServer();

   T decode(NBTCompound compound, PacketWrapper<?> wrapper);

   void encode(NBTCompound compound, PacketWrapper<?> wrapper, T clickEvent);
}
