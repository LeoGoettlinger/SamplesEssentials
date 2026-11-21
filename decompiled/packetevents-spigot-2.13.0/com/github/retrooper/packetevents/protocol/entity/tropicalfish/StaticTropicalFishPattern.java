package com.github.retrooper.packetevents.protocol.entity.tropicalfish;

import com.github.retrooper.packetevents.protocol.mapper.AbstractMappedEntity;
import com.github.retrooper.packetevents.util.mappings.TypesBuilderData;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

public class StaticTropicalFishPattern extends AbstractMappedEntity implements TropicalFishPattern {
   private final TropicalFishPattern.Base base;

   @Internal
   public StaticTropicalFishPattern(@Nullable TypesBuilderData data, TropicalFishPattern.Base base) {
      super(data);
      this.base = base;
   }

   @Override
   public TropicalFishPattern.Base getBase() {
      return this.base;
   }
}
