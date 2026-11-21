package com.github.retrooper.packetevents.protocol.entity.parrot;

import com.github.retrooper.packetevents.protocol.mapper.AbstractMappedEntity;
import com.github.retrooper.packetevents.util.mappings.TypesBuilderData;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

public class StaticParrotVariant extends AbstractMappedEntity implements ParrotVariant {
   @Internal
   public StaticParrotVariant(@Nullable TypesBuilderData data) {
      super(data);
   }
}
