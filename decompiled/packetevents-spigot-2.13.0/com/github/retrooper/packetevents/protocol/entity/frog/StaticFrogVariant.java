package com.github.retrooper.packetevents.protocol.entity.frog;

import com.github.retrooper.packetevents.protocol.mapper.AbstractMappedEntity;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.util.mappings.TypesBuilderData;
import java.util.Objects;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class StaticFrogVariant extends AbstractMappedEntity implements FrogVariant {
   private final ResourceLocation assetId;

   public StaticFrogVariant(ResourceLocation assetId) {
      this(null, assetId);
   }

   @Internal
   public StaticFrogVariant(@Nullable TypesBuilderData data, ResourceLocation assetId) {
      super(data);
      this.assetId = assetId;
   }

   public FrogVariant copy(@Nullable TypesBuilderData newData) {
      return new StaticFrogVariant(newData, this.assetId);
   }

   @Override
   public ResourceLocation getAssetId() {
      return this.assetId;
   }

   @Override
   public boolean deepEquals(@Nullable Object obj) {
      if (!(obj instanceof StaticFrogVariant)) {
         return false;
      } else {
         StaticFrogVariant that = (StaticFrogVariant)obj;
         return this.assetId.equals(that.assetId);
      }
   }

   @Override
   public int deepHashCode() {
      return Objects.hash(this.assetId);
   }
}
