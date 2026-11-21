package com.github.retrooper.packetevents.protocol.debug.poi;

import com.github.retrooper.packetevents.protocol.mapper.AbstractMappedEntity;
import com.github.retrooper.packetevents.util.mappings.TypesBuilderData;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class StaticPoiType extends AbstractMappedEntity implements PoiType {
   @Internal
   public StaticPoiType(@Nullable TypesBuilderData data) {
      super(data);
   }
}
