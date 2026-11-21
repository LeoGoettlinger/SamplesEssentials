package com.github.retrooper.packetevents.protocol.entity.cow;

import com.github.retrooper.packetevents.protocol.mapper.AbstractMappedEntity;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.util.mappings.TypesBuilderData;
import java.util.Objects;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.ApiStatus.Obsolete;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class StaticCowVariant extends AbstractMappedEntity implements CowVariant {
   private final CowVariant.ModelType modelType;
   private final ResourceLocation assetId;
   private final ResourceLocation babyAssetId;

   @Obsolete
   public StaticCowVariant(CowVariant.ModelType modelType, ResourceLocation assetId) {
      this(null, modelType, assetId, assetId);
   }

   public StaticCowVariant(CowVariant.ModelType modelType, ResourceLocation assetId, ResourceLocation babyAssetId) {
      this(null, modelType, assetId, babyAssetId);
   }

   @Internal
   public StaticCowVariant(@Nullable TypesBuilderData data, CowVariant.ModelType modelType, ResourceLocation assetId, ResourceLocation babyAssetId) {
      super(data);
      this.modelType = modelType;
      this.assetId = assetId;
      this.babyAssetId = babyAssetId;
   }

   public CowVariant copy(@Nullable TypesBuilderData newData) {
      return new StaticCowVariant(newData, this.modelType, this.assetId, this.babyAssetId);
   }

   @Override
   public CowVariant.ModelType getModelType() {
      return this.modelType;
   }

   @Override
   public ResourceLocation getAssetId() {
      return this.assetId;
   }

   @Override
   public ResourceLocation getBabyAssetId() {
      return this.babyAssetId;
   }

   @Override
   public boolean deepEquals(@Nullable Object obj) {
      if (!(obj instanceof StaticCowVariant)) {
         return false;
      } else {
         StaticCowVariant that = (StaticCowVariant)obj;
         if (!this.modelType.equals(that.modelType)) {
            return false;
         } else {
            return !this.assetId.equals(that.assetId) ? false : this.babyAssetId.equals(that.babyAssetId);
         }
      }
   }

   @Override
   public int deepHashCode() {
      return Objects.hash(this.modelType, this.assetId, this.babyAssetId);
   }
}
