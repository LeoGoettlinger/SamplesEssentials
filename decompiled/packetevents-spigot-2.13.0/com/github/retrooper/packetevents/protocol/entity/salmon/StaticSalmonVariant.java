package com.github.retrooper.packetevents.protocol.entity.salmon;

import com.github.retrooper.packetevents.protocol.mapper.AbstractMappedEntity;
import com.github.retrooper.packetevents.util.mappings.TypesBuilderData;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

public class StaticSalmonVariant extends AbstractMappedEntity implements SalmonVariant {
   @Internal
   public StaticSalmonVariant(@Nullable TypesBuilderData data) {
      super(data);
   }
}
