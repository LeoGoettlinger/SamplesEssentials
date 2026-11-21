package com.github.retrooper.packetevents.protocol.potion;

import com.github.retrooper.packetevents.protocol.mapper.AbstractMappedEntity;
import com.github.retrooper.packetevents.util.mappings.TypesBuilderData;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

public class StaticPotionType extends AbstractMappedEntity implements PotionType {
   @Internal
   public StaticPotionType(@Nullable TypesBuilderData data) {
      super(data);
   }
}
