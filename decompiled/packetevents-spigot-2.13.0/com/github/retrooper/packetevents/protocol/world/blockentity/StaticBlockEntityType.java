package com.github.retrooper.packetevents.protocol.world.blockentity;

import com.github.retrooper.packetevents.protocol.mapper.AbstractMappedEntity;
import com.github.retrooper.packetevents.util.mappings.TypesBuilderData;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

public class StaticBlockEntityType extends AbstractMappedEntity implements BlockEntityType {
   @Internal
   public StaticBlockEntityType(@Nullable TypesBuilderData data) {
      super(data);
   }
}
