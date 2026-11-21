package com.github.retrooper.packetevents.protocol.entity.mooshroom;

import com.github.retrooper.packetevents.protocol.mapper.AbstractMappedEntity;
import com.github.retrooper.packetevents.util.mappings.TypesBuilderData;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

public class StaticMooshroomVariant extends AbstractMappedEntity implements MooshroomVariant {
   @Internal
   public StaticMooshroomVariant(@Nullable TypesBuilderData data) {
      super(data);
   }
}
