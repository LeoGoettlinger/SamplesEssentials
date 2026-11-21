package com.ranull.graves.integration;

import com.mira.furnitureengine.furniture.FurnitureManager;
import com.mira.furnitureengine.furniture.core.Furniture;
import com.ranull.graves.Graves;
import com.ranull.graves.data.EntityData;
import com.ranull.graves.listener.integration.furnitureengine.FurnitureBreakListener;
import com.ranull.graves.listener.integration.furnitureengine.FurnitureInteractListener;
import com.ranull.graves.manager.EntityDataManager;
import com.ranull.graves.type.Grave;
import com.ranull.graves.util.BlockFaceUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Rotation;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.HandlerList;

/** @deprecated */
public class FurnitureEngine extends EntityDataManager {
   private final Graves plugin;
   private final FurnitureInteractListener furnitureInteractListener;
   private final FurnitureBreakListener furnitureBreakListener;

   @Deprecated
   public FurnitureEngine(Graves plugin) {
      super(plugin);
      this.plugin = plugin;
      this.furnitureInteractListener = new FurnitureInteractListener(plugin, this);
      this.furnitureBreakListener = new FurnitureBreakListener(this);
      this.registerListeners();
   }

   @Deprecated
   public void registerListeners() {
      this.plugin.getServer().getPluginManager().registerEvents(this.furnitureInteractListener, this.plugin);
      this.plugin.getServer().getPluginManager().registerEvents(this.furnitureBreakListener, this.plugin);
   }

   @Deprecated
   public void unregisterListeners() {
      if (this.furnitureInteractListener != null) {
         HandlerList.unregisterAll(this.furnitureInteractListener);
      }

      if (this.furnitureBreakListener != null) {
         HandlerList.unregisterAll(this.furnitureBreakListener);
      }
   }

   @Deprecated
   public void createFurniture(Location location, Grave grave) {
      if (this.plugin.getConfigManager().getConfigSection("furnitureengine.enabled", grave).getBoolean("furnitureengine.enabled")) {
         String name = this.plugin.getConfigManager().getConfigSection("furnitureengine.name", grave).getString("furnitureengine.name", "");
         location.getBlock().setType(Material.AIR);
         if (this.placeFurniture(name, location, BlockFaceUtil.getBlockFaceRotation(BlockFaceUtil.getYawBlockFace(location.getYaw())))) {
            ItemFrame itemFrame = this.getItemFrame(location);
            if (itemFrame != null && location.getWorld() != null) {
               this.createEntityData(location, itemFrame.getUniqueId(), grave.getUUID(), EntityData.Type.FURNITUREENGINE);
               this.plugin
                  .debugMessage(
                     "Placing FurnitureEngine furniture for "
                        + grave.getUUID()
                        + " at "
                        + location.getWorld().getName()
                        + ", "
                        + (location.getBlockX() + 0.5)
                        + "x, "
                        + (location.getBlockY() + 0.5)
                        + "y, "
                        + (location.getBlockZ() + 0.5)
                        + "z",
                     1
                  );
            }
         }
      }
   }

   @Deprecated
   public void removeFurniture(Grave grave) {
      this.cleanupItemFrame(grave);

      try {
         this.breakFurniture(grave.getLocationDeath());
      } catch (NullPointerException var3) {
      }

      this.removeFurniture(this.getEntityDataMap(this.getLoadedEntityDataList(grave)));
   }

   @Deprecated
   public void removeFurniture(EntityData entityData) {
      this.removeFurniture(this.getEntityDataMap(List.of(entityData)));
   }

   @Deprecated
   public void removeFurniture(Map<EntityData, Entity> entityDataMap) {
      List<EntityData> entityDataList = new ArrayList<>();

      for (Entry<EntityData, Entity> entry : entityDataMap.entrySet()) {
         this.breakFurniture(entry.getValue().getLocation());
         entry.getValue().remove();
         entityDataList.add(entry.getKey());
      }

      this.plugin.getDataManager().removeEntityData(entityDataList);
   }

   @Deprecated
   public ItemFrame getItemFrame(Location location) {
      location = location.clone().add(0.0, 1.0, 0.0);
      if (location.getWorld() != null) {
         for (Entity entity : location.getWorld().getNearbyEntities(location, 0.13, 0.2, 0.13)) {
            if (entity instanceof ItemFrame itemFrame) {
               return itemFrame;
            }
         }
      }

      return null;
   }

   @Deprecated
   public void cleanupItemFrame(Grave grave) {
      Location location = grave.getLocationDeath();
      if (location.getWorld() != null) {
         for (Entity entity : location.getWorld().getNearbyEntities(location, 0.7, 1.0, 0.7)) {
            if (entity instanceof ItemFrame itemFrame) {
               itemFrame.remove();
            }
         }
      }
   }

   @Deprecated
   private boolean placeFurniture(String name, Location location, Rotation rotation) {
      try {
         Furniture furniture = FurnitureManager.getInstance().getFurniture(name);
         furniture.spawn(location, rotation, null);
         return true;
      } catch (NoSuchMethodError var5) {
         this.plugin
            .warningMessage(
               "Furniture placing failed at:"
                  + location.getWorld().getName()
                  + ", x"
                  + location.getBlockX()
                  + ", y"
                  + location.getBlockY()
                  + ", z"
                  + location.getBlockZ()
            );
         return false;
      }
   }

   @Deprecated
   private void breakFurniture(Location location) {
      try {
         Furniture furniture = FurnitureManager.getInstance().isFurniture(location);
         if (furniture != null) {
            furniture.breakFurniture(null, location);
         }
      } catch (NoSuchMethodError var3) {
         this.plugin
            .warningMessage(
               "Furniture breaking failed at:"
                  + location.getWorld().getName()
                  + ", x"
                  + location.getBlockX()
                  + ", y"
                  + location.getBlockY()
                  + ", z"
                  + location.getBlockZ()
            );
      }
   }

   @Deprecated
   public boolean hasFurniture(Grave grave) {
      if (grave == null) {
         return false;
      } else {
         Location loc = grave.getLocationDeath();
         if (loc == null || loc.getWorld() == null) {
            loc = grave.getLocationDeath();
         }

         if (loc != null && loc.getWorld() != null) {
            try {
               if (FurnitureManager.getInstance().isFurniture(loc) != null) {
                  return true;
               }

               Location up = loc.clone().add(0.0, 1.0, 0.0);
               if (FurnitureManager.getInstance().isFurniture(up) != null) {
                  return true;
               }

               Map<EntityData, Entity> map = this.getEntityDataMap(this.getLoadedEntityDataList(grave));

               for (Entry<EntityData, Entity> e : map.entrySet()) {
                  EntityData data = e.getKey();
                  Entity ent = e.getValue();
                  if (data != null && ent != null && data.getType() == EntityData.Type.FURNITUREENGINE && ent.isValid() && !ent.isDead()) {
                     Location el = ent.getLocation();
                     if (el.getWorld() != null && el.getWorld().equals(loc.getWorld()) && el.distanceSquared(up) <= 0.25) {
                        return true;
                     }
                  }
               }
            } catch (Throwable var10) {
            }

            return false;
         } else {
            return false;
         }
      }
   }
}
