package com.github.retrooper.packetevents.protocol.item.armormaterial;

import com.github.retrooper.packetevents.protocol.mapper.AbstractMappedEntity;
import com.github.retrooper.packetevents.util.mappings.TypesBuilderData;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

public class StaticArmorMaterial extends AbstractMappedEntity implements ArmorMaterial {
   @Internal
   public StaticArmorMaterial(@Nullable TypesBuilderData data) {
      super(data);
   }
}
