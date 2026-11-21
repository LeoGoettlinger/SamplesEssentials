package com.github.retrooper.packetevents.protocol.entity.type;

import com.github.retrooper.packetevents.protocol.mapper.LegacyMappedEntity;
import com.github.retrooper.packetevents.protocol.mapper.MappedEntity;
import java.util.Optional;

public interface EntityType extends MappedEntity, LegacyMappedEntity {
   boolean isInstanceOf(EntityType parent);

   Optional<EntityType> getParent();
}
