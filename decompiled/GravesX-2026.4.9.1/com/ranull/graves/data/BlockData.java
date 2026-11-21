package com.ranull.graves.data;

import java.io.Serializable;
import java.util.UUID;
import org.bukkit.Location;

public class BlockData implements Serializable {
   private static final long serialVersionUID = 1L;
   private final Location location;
   private final UUID graveUUID;
   private final String replaceMaterial;
   private final String replaceData;

   public BlockData(Location location, UUID graveUUID, String replaceMaterial, String replaceData) {
      this.location = location;
      this.graveUUID = graveUUID;
      this.replaceMaterial = replaceMaterial;
      this.replaceData = replaceData;
   }

   public Location getLocation() {
      return this.location.clone();
   }

   public UUID getGraveUUID() {
      return this.graveUUID;
   }

   public String getReplaceMaterial() {
      return this.replaceMaterial;
   }

   public String getReplaceData() {
      return this.replaceData;
   }

   public static enum BlockType {
      DEATH,
      NORMAL;

      public boolean isDeath() {
         return this == DEATH;
      }

      public boolean isNormal() {
         return this == NORMAL;
      }

      public static BlockData.BlockType fromString(String value) {
         if (value == null) {
            return NORMAL;
         } else {
            String cleaned = value.trim();
            if (cleaned.isEmpty()) {
               return NORMAL;
            } else {
               try {
                  return valueOf(cleaned.toUpperCase());
               } catch (IllegalArgumentException var3) {
                  return NORMAL;
               }
            }
         }
      }
   }
}
