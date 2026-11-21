package com.ranull.graves.data;

import java.io.Serializable;
import java.util.UUID;
import org.bukkit.Location;

public class EntityData implements Serializable {
   private static final long serialVersionUID = 1L;
   private final Location location;
   private final UUID uuidEntity;
   private final UUID uuidGrave;
   private final EntityData.Type type;

   public EntityData(Location location, UUID uuidEntity, UUID uuidGrave, EntityData.Type type) {
      this.location = location;
      this.uuidEntity = uuidEntity;
      this.uuidGrave = uuidGrave;
      this.type = type;
   }

   public Location getLocation() {
      return this.location.clone();
   }

   public UUID getUUIDEntity() {
      return this.uuidEntity;
   }

   public UUID getUUIDGrave() {
      return this.uuidGrave;
   }

   public EntityData.Type getType() {
      return this.type;
   }

   public boolean isForGrave(UUID graveUUID) {
      return this.uuidGrave.equals(graveUUID);
   }

   public boolean isForEntity(UUID entityUUID) {
      return this.uuidEntity.equals(entityUUID);
   }

   public boolean isSameWorld(Location other) {
      return other != null && this.location.getWorld() != null && other.getWorld() != null && this.location.getWorld().equals(other.getWorld());
   }

   public boolean isWithinDistanceSquared(Location other, double maxDistanceSq) {
      return !this.isSameWorld(other) ? false : this.location.distanceSquared(other) <= maxDistanceSq;
   }

   public static enum Type {
      HOLOGRAM,
      ARMOR_STAND,
      ITEM_FRAME,
      MANNEQUIN,
      FURNITURELIB,
      FURNITUREENGINE,
      ITEMSADDER,
      ORAXEN,
      NEXO,
      PLAYERNPC,
      CRAFTENGINE,
      CUSTOM;

      public boolean isVanilla() {
         return this == HOLOGRAM || this == ARMOR_STAND || this == ITEM_FRAME || this == MANNEQUIN;
      }

      public boolean isIntegration() {
         return this == FURNITURELIB || this == FURNITUREENGINE || this == ITEMSADDER || this == ORAXEN || this == NEXO || this == PLAYERNPC;
      }

      public boolean isCustom() {
         return this == CUSTOM;
      }

      public static EntityData.Type fromString(String value) {
         if (value == null) {
            return CUSTOM;
         } else {
            String cleaned = value.trim();
            if (cleaned.isEmpty()) {
               return CUSTOM;
            } else {
               try {
                  return valueOf(cleaned.toUpperCase());
               } catch (IllegalArgumentException var3) {
                  return CUSTOM;
               }
            }
         }
      }
   }
}
