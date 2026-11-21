package com.github.retrooper.packetevents.protocol.entity.nautilus;

import com.github.retrooper.packetevents.protocol.mapper.AbstractMappedEntity;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.util.mappings.TypesBuilderData;
import java.util.Objects;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class StaticZombieNautilusVariant extends AbstractMappedEntity implements ZombieNautilusVariant {
   private final ZombieNautilusVariant.ModelType modelType;
   private final ResourceLocation assetId;

   public StaticZombieNautilusVariant(ZombieNautilusVariant.ModelType modelType, ResourceLocation assetId) {
      this(null, modelType, assetId);
   }

   @Internal
   public StaticZombieNautilusVariant(@Nullable TypesBuilderData data, ZombieNautilusVariant.ModelType modelType, ResourceLocation assetId) {
      super(data);
      this.modelType = modelType;
      this.assetId = assetId;
   }

   public ZombieNautilusVariant copy(@Nullable TypesBuilderData newData) {
      return new StaticZombieNautilusVariant(newData, this.modelType, this.assetId);
   }

   @Override
   public ZombieNautilusVariant.ModelType getModelType() {
      return this.modelType;
   }

   @Override
   public ResourceLocation getAssetId() {
      return this.assetId;
   }

   @Override
   public boolean deepEquals(@Nullable Object obj) {
      if (obj != null && this.getClass() == obj.getClass()) {
         StaticZombieNautilusVariant that = (StaticZombieNautilusVariant)obj;
         return this.modelType != that.modelType ? false : this.assetId.equals(that.assetId);
      } else {
         return false;
      }
   }

   @Override
   public int deepHashCode() {
      return Objects.hash(this.modelType, this.assetId);
   }
}
