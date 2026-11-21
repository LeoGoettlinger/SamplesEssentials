package com.github.retrooper.packetevents.protocol.entity.horse;

import com.github.retrooper.packetevents.protocol.mapper.AbstractMappedEntity;
import com.github.retrooper.packetevents.util.mappings.TypesBuilderData;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

public class StaticHorseVariant extends AbstractMappedEntity implements HorseVariant {
   @Internal
   public StaticHorseVariant(@Nullable TypesBuilderData data) {
      super(data);
   }
}
