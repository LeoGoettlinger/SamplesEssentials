package com.github.retrooper.packetevents.protocol.entity.axolotl;

import com.github.retrooper.packetevents.protocol.mapper.AbstractMappedEntity;
import com.github.retrooper.packetevents.util.mappings.TypesBuilderData;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

public class StaticAxolotlVariant extends AbstractMappedEntity implements AxolotlVariant {
   @Internal
   public StaticAxolotlVariant(@Nullable TypesBuilderData data) {
      super(data);
   }
}
