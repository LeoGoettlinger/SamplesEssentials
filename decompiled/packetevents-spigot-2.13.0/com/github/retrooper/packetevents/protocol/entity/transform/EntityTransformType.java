package com.github.retrooper.packetevents.protocol.entity.transform;

import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;

public class EntityTransformType {
   public static final EntityTransformType IDENTITY = new EntityTransformType(null, 1.0F, null);
   @Nullable
   private EntityTransformType.TypeModifier typeModifier;
   private float scale;
   @Nullable
   private UserProfile playerSkin;

   public EntityTransformType(@Nullable EntityTransformType.TypeModifier typeModifier, float scale, @Nullable UserProfile playerSkin) {
      this.typeModifier = typeModifier;
      this.scale = scale;
      this.playerSkin = playerSkin;
   }

   public Optional<EntityTransformType.TypeModifier> getTypeModifier() {
      return Optional.ofNullable(this.typeModifier);
   }

   public void setTypeModifier(@Nullable EntityTransformType.TypeModifier typeModifier) {
      this.typeModifier = typeModifier;
   }

   public float getScale() {
      return this.scale;
   }

   public void setScale(float scale) {
      this.scale = scale;
   }

   public Optional<UserProfile> getPlayerSkin() {
      return Optional.ofNullable(this.playerSkin);
   }

   public void setPlayerSkin(@Nullable UserProfile playerSkin) {
      this.playerSkin = playerSkin;
   }

   public static class TypeModifier {
      private EntityType entityType;
      @Nullable
      private NBTCompound tag;

      public TypeModifier(EntityType entityType, @Nullable NBTCompound tag) {
         this.entityType = entityType;
         this.tag = tag;
      }

      public EntityType getEntityType() {
         return this.entityType;
      }

      public void setEntityType(EntityType entityType) {
         this.entityType = entityType;
      }

      public Optional<NBTCompound> getTag() {
         return Optional.ofNullable(this.tag);
      }

      public void setTag(@Nullable NBTCompound tag) {
         this.tag = tag;
      }
   }
}
