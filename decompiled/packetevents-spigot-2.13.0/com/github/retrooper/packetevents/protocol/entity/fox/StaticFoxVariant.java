package com.github.retrooper.packetevents.protocol.entity.fox;

import com.github.retrooper.packetevents.protocol.mapper.AbstractMappedEntity;
import com.github.retrooper.packetevents.util.mappings.TypesBuilderData;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

public class StaticFoxVariant extends AbstractMappedEntity implements FoxVariant {
   @Internal
   public StaticFoxVariant(@Nullable TypesBuilderData data) {
      super(data);
   }
}
