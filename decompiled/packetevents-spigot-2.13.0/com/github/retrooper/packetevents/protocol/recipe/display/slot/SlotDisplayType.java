package com.github.retrooper.packetevents.protocol.recipe.display.slot;

import com.github.retrooper.packetevents.protocol.mapper.MappedEntity;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

public interface SlotDisplayType<T extends SlotDisplay<?>> extends MappedEntity {
   T read(PacketWrapper<?> wrapper);

   void write(PacketWrapper<?> wrapper, T display);
}
