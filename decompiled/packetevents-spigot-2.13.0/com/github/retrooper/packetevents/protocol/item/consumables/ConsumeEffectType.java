package com.github.retrooper.packetevents.protocol.item.consumables;

import com.github.retrooper.packetevents.protocol.mapper.MappedEntity;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

public interface ConsumeEffectType<T extends ConsumeEffect<?>> extends MappedEntity {
   T read(PacketWrapper<?> wrapper);

   void write(PacketWrapper<?> wrapper, T effect);
}
