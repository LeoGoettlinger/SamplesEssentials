package com.viaversion.viabackwards.api.entities.storage;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viabackwards.api.BackwardsProtocol;
import com.viaversion.viabackwards.api.data.BackwardsMappingData;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.util.ComponentUtil;
import java.util.Locale;
import org.checkerframework.checker.nullness.qual.Nullable;

public class EntityReplacement {
   private final BackwardsProtocol<?, ?, ?, ?> protocol;
   private final int id;
   private final int replacementId;
   private final String key;
   private final String translateKey;
   private final String fallbackName;
   private EntityReplacement.ComponentType componentType = EntityReplacement.ComponentType.NONE;
   private EntityReplacement.EntityDataCreator defaultData;

   public EntityReplacement(BackwardsProtocol<?, ?, ?, ?> protocol, EntityType type, int replacementId) {
      this(protocol, type.name().toLowerCase(Locale.ROOT), type.getId(), replacementId);
   }

   public EntityReplacement(BackwardsProtocol<?, ?, ?, ?> protocol, String key, int id, int replacementId) {
      this.protocol = protocol;
      this.id = id;
      this.replacementId = replacementId;
      this.key = key;
      this.translateKey = "vb.entity." + key;
      this.fallbackName = BackwardsMappingData.nameFromIdentifier(this.key);
   }

   public EntityReplacement jsonName() {
      this.componentType = EntityReplacement.ComponentType.JSON;
      return this;
   }

   public EntityReplacement tagName() {
      this.componentType = EntityReplacement.ComponentType.TAG;
      return this;
   }

   public EntityReplacement plainName() {
      this.componentType = EntityReplacement.ComponentType.PLAIN;
      return this;
   }

   public EntityReplacement spawnEntityData(EntityReplacement.EntityDataCreator handler) {
      this.defaultData = handler;
      return this;
   }

   public boolean hasBaseData() {
      return this.defaultData != null;
   }

   public int typeId() {
      return this.id;
   }

   @Nullable
   public Object entityName() {
      if (this.componentType == EntityReplacement.ComponentType.NONE) {
         return null;
      } else if (this.componentType == EntityReplacement.ComponentType.JSON) {
         return ComponentUtil.plainToJson(this.fallbackName);
      } else if (this.componentType == EntityReplacement.ComponentType.TAG) {
         CompoundTag tag = new CompoundTag();
         tag.putString("color", "white");
         tag.putString("translate", this.translateKey);
         tag.putString("fallback", this.fallbackName);
         tag.putBoolean("italic", false);
         return tag;
      } else {
         return this.fallbackName;
      }
   }

   public int replacementId() {
      return this.replacementId;
   }

   @Nullable
   public EntityReplacement.EntityDataCreator defaultData() {
      return this.defaultData;
   }

   public boolean isObjectType() {
      return false;
   }

   public int objectData() {
      return -1;
   }

   @Override
   public String toString() {
      return "EntityReplacement{protocol="
         + this.protocol
         + ", id="
         + this.id
         + ", replacementId="
         + this.replacementId
         + ", key='"
         + this.key
         + "', componentType="
         + this.componentType
         + ", defaultData="
         + this.defaultData
         + "}";
   }

   private static enum ComponentType {
      PLAIN,
      JSON,
      TAG,
      NONE;
   }

   @FunctionalInterface
   public interface EntityDataCreator {
      void createData(WrappedEntityData var1);
   }
}
