package com.ranull.graves.manager;

import com.ranull.graves.Graves;
import com.ranull.graves.data.EntityData;
import com.ranull.graves.type.Grave;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class HologramManager extends EntityDataManager {
   private final Graves plugin;

   public HologramManager(Graves plugin) {
      super(plugin);
      this.plugin = plugin;
   }

   public void createHologram(Location location, Grave grave) {
      if (grave == null) {
         this.plugin.debugMessage("[Holograms] createHologram skipped: grave is null", 1);
      } else {
         this.plugin
            .debugMessage("[Holograms] Creating hologram for grave=" + grave.getUUID() + " at " + (location != null ? this.toLocKey(location) : "null"), 1);
         if (this.plugin.getVersionManager().isHasTextDisplays()) {
            this.plugin.debugMessage("[Holograms] Using TextDisplay backend for grave=" + grave.getUUID(), 2);
            this.plugin.getTextDisplayManager().createHologram(location, grave);
         } else {
            this.plugin.debugMessage("[Holograms] Using ArmorStand backend for grave=" + grave.getUUID(), 2);
            this.plugin.getArmorStandManager().createHologram(location, grave);
         }
      }
   }

   public void removeHologram(Grave grave) {
      if (grave == null) {
         this.plugin.debugMessage("[Holograms] removeHologram(grave) skipped: grave is null", 1);
      } else if (grave.getUUID() == null) {
         this.plugin.debugMessage("[Holograms] removeHologram(grave) skipped: grave UUID is null", 1);
      } else {
         this.plugin.debugMessage("[Holograms] removeHologram(grave=" + grave.getUUID() + ") starting", 1);
         if (this.plugin.getVersionManager().isHasTextDisplays()) {
            this.plugin.debugMessage("[Holograms] Routing grave=" + grave.getUUID() + " removal to TextDisplayManager and ArmorStandManager", 2);
            this.plugin.getTextDisplayManager().removeHologram(grave);
            this.plugin.getArmorStandManager().removeHologram(grave);
         } else {
            this.plugin.debugMessage("[Holograms] Routing grave=" + grave.getUUID() + " removal to ArmorStandManager only", 2);
            this.plugin.getArmorStandManager().removeHologram(grave);
         }

         this.plugin.debugMessage("[Holograms] removeHologram(grave=" + grave.getUUID() + ") finished dispatch", 1);
      }
   }

   @Deprecated(
      forRemoval = true
   )
   public void removeHologram(List<EntityData> entityDataList) {
      String message = "HologramManager#removeHologram(List<EntityData>) is no longer supported. Use removeHologram(Grave) instead.";
      this.plugin.getLogger().severe(message + " size=" + (entityDataList != null ? entityDataList.size() : 0));
      this.plugin.debugMessage("[Holograms] " + message, 1);
      throw new UnsupportedOperationException(message);
   }

   @Deprecated(
      forRemoval = true
   )
   public void removeHologram(EntityData entityData) {
      String message = "HologramManager#removeHologram(EntityData) is no longer supported. Use removeHologram(Grave) instead.";
      this.plugin.getLogger().severe(message + " entity=" + (entityData != null ? entityData.getUUIDEntity() : "null"));
      this.plugin.debugMessage("[Holograms] " + message, 1);
      throw new UnsupportedOperationException(message);
   }

   @Deprecated(
      forRemoval = true
   )
   public void removeHologram(Map<EntityData, Entity> entityDataMap) {
      String message = "HologramManager#removeHologram(Map<EntityData, Entity>) is no longer supported. Use removeHologram(Grave) instead.";
      this.plugin.getLogger().severe(message + " size=" + (entityDataMap != null ? entityDataMap.size() : 0));
      this.plugin.debugMessage("[Holograms] " + message, 1);
      throw new UnsupportedOperationException(message);
   }

   public void purgeLingeringHolograms() {
      this.plugin.debugMessage("[Cleanup] Starting hologram purge dispatch", 1);
      if (this.plugin.getVersionManager().isHasTextDisplays()) {
         this.plugin.debugMessage("[Cleanup] Purging TextDisplay and ArmorStand holograms", 2);
         this.plugin.getTextDisplayManager().purgeLingeringHolograms();
         this.plugin.getArmorStandManager().purgeLingeringHolograms();
      } else {
         this.plugin.debugMessage("[Cleanup] Purging ArmorStand holograms only", 2);
         this.plugin.getArmorStandManager().purgeLingeringHolograms();
      }

      this.plugin.debugMessage("[Cleanup] Finished hologram purge dispatch", 1);
   }

   public Grave hasGrave(UUID graveUUID) {
      if (graveUUID == null) {
         this.plugin.debugMessage("[Holograms] hasGrave lookup skipped: graveUUID is null", 2);
         return null;
      } else {
         Grave grave = this.plugin.getCacheManager().getGraveMap().get(graveUUID);
         this.plugin.debugMessage("[Holograms] hasGrave lookup for " + graveUUID + " -> " + (grave != null ? "hit" : "miss"), 3);
         return grave;
      }
   }

   private String toLocKey(Location loc) {
      return loc != null && loc.getWorld() != null ? loc.getWorld().getName() + ":" + loc.getBlockX() + ":" + loc.getBlockY() + ":" + loc.getBlockZ() : null;
   }
}
