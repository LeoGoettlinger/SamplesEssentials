package com.github.retrooper.packetevents.protocol.item.mapdecoration;

import com.github.retrooper.packetevents.protocol.mapper.MappedEntity;
import com.github.retrooper.packetevents.resources.ResourceLocation;

public interface MapDecorationType extends MappedEntity {
   ResourceLocation getAssetId();

   boolean isShowOnItemFrame();

   int getMapColor();

   boolean isExplorationMapElement();

   boolean isTrackCount();
}
