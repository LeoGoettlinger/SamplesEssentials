package com.github.retrooper.packetevents.protocol.potion;

import com.github.retrooper.packetevents.protocol.mapper.AbstractMappedEntity;
import com.github.retrooper.packetevents.util.mappings.TypesBuilderData;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

public class StaticPotion extends AbstractMappedEntity implements Potion {
   @Internal
   public StaticPotion(@Nullable TypesBuilderData data) {
      super(data);
   }
}
