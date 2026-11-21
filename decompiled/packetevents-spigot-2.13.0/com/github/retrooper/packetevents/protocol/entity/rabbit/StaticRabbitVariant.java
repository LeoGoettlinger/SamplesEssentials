package com.github.retrooper.packetevents.protocol.entity.rabbit;

import com.github.retrooper.packetevents.protocol.mapper.AbstractMappedEntity;
import com.github.retrooper.packetevents.util.mappings.TypesBuilderData;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

public class StaticRabbitVariant extends AbstractMappedEntity implements RabbitVariant {
   @Internal
   public StaticRabbitVariant(@Nullable TypesBuilderData data) {
      super(data);
   }
}
