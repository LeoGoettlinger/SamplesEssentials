package com.github.retrooper.packetevents.protocol.component.predicates;

import com.github.retrooper.packetevents.protocol.mapper.MappedEntity;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

public interface ComponentPredicateType<T extends IComponentPredicate> extends MappedEntity {
   T read(PacketWrapper<?> wrapper);

   void write(PacketWrapper<?> wrapper, T predicate);
}
