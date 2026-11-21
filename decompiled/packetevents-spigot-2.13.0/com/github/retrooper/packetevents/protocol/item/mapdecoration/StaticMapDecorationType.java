package com.github.retrooper.packetevents.protocol.item.mapdecoration;

import com.github.retrooper.packetevents.protocol.mapper.AbstractMappedEntity;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.util.mappings.TypesBuilderData;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

public class StaticMapDecorationType extends AbstractMappedEntity implements MapDecorationType {
   private final ResourceLocation assetId;
   private final boolean showOnItemFrame;
   private final int mapColor;
   private final boolean explorationMapElement;
   private final boolean trackCount;

   @Internal
   public StaticMapDecorationType(
      @Nullable TypesBuilderData data, ResourceLocation assetId, boolean showOnItemFrame, int mapColor, boolean explorationMapElement, boolean trackCount
   ) {
      super(data);
      this.assetId = assetId;
      this.showOnItemFrame = showOnItemFrame;
      this.mapColor = mapColor;
      this.explorationMapElement = explorationMapElement;
      this.trackCount = trackCount;
   }

   @Override
   public ResourceLocation getAssetId() {
      return this.assetId;
   }

   @Override
   public boolean isShowOnItemFrame() {
      return this.showOnItemFrame;
   }

   @Override
   public int getMapColor() {
      return this.mapColor;
   }

   @Override
   public boolean isExplorationMapElement() {
      return this.explorationMapElement;
   }

   @Override
   public boolean isTrackCount() {
      return this.trackCount;
   }
}
