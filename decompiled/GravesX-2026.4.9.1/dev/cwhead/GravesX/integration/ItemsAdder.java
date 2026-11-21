package dev.cwhead.GravesX.integration;

import com.ranull.graves.Graves;
import com.ranull.graves.data.EntityData;
import com.ranull.graves.manager.EntityDataManager;
import com.ranull.graves.type.Grave;
import com.ranull.graves.util.BlockFaceUtil;
import com.ranull.graves.util.LocationUtil;
import dev.lone.itemsadder.api.CustomBlock;
import dev.lone.itemsadder.api.CustomFurniture;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;

public final class ItemsAdder extends EntityDataManager {
   private final Graves plugin;
   private final AtomicBoolean ready = new AtomicBoolean(false);

   public ItemsAdder(Graves plugin) {
      super(plugin);
      this.plugin = plugin;
   }

   public boolean isReady() {
      return this.ready.get();
   }

   public void setReady(boolean value) {
      this.ready.set(value);
   }

   private boolean requireReady(Grave grave, String action) {
      if (this.ready.get()) {
         return true;
      } else {
         if (grave != null) {
            this.plugin.debugMessage("ItemsAdder not ready; skipping " + action + " for grave " + grave.getUUID(), 2);
         } else {
            this.plugin.debugMessage("ItemsAdder not ready; skipping " + action, 2);
         }

         return false;
      }
   }

   public void createFurniture(Location location, Grave grave) {
      if (grave != null && location != null && location.getWorld() != null) {
         if (this.requireReady(grave, "furniture spawn")) {
            location = LocationUtil.roundLocation(location).add(0.5, 0.0, 0.5);
            location.setYaw(BlockFaceUtil.getBlockFaceYaw(BlockFaceUtil.getYawBlockFace(location.getYaw()).getOppositeFace()));
            location.setPitch(grave.getPitch());
            if (this.plugin.getConfigManager().getConfigSection("itemsadder.furniture.enabled", grave).getBoolean("itemsadder.furniture.enabled")) {
               String name = this.plugin.getConfigManager().getConfigSection("itemsadder.furniture.name", grave).getString("itemsadder.furniture.name", "");
               if (!name.isEmpty()) {
                  location.getBlock().setType(Material.AIR);
                  CustomFurniture furniture = CustomFurniture.spawn(name, location.getBlock());
                  Entity entity = furniture != null ? furniture.getEntity() : null;
                  if (furniture != null && entity != null) {
                     furniture.teleport(location);
                     this.createEntityData(entity, grave, EntityData.Type.ITEMSADDER);
                     this.plugin
                        .debugMessage(
                           "Placing ItemsAdder furniture for "
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
                  } else {
                     this.plugin.debugMessage("Can't find ItemsAdder furniture " + name, 1);
                  }
               }
            }
         }
      }
   }

   public void removeFurniture(Grave grave) {
      if (grave != null) {
         this.removeFurniture(this.getEntityDataMap(this.getLoadedEntityDataList(grave)));
      }
   }

   public void removeFurniture(EntityData entityData) {
      if (entityData != null) {
         this.removeFurniture(this.getEntityDataMap(List.of(entityData)));
      }
   }

   public void removeFurniture(Map<EntityData, Entity> entityDataMap) {
      if (entityDataMap != null && !entityDataMap.isEmpty()) {
         List<EntityData> toRemove = new ArrayList<>();

         for (Entry<EntityData, Entity> entry : entityDataMap.entrySet()) {
            Entity e = entry.getValue();
            if (e != null) {
               try {
                  CustomFurniture.remove(e, false);
               } catch (Throwable var9) {
                  try {
                     e.remove();
                  } catch (Throwable var8) {
                  }
               }

               toRemove.add(entry.getKey());
            }
         }

         this.plugin.getDataManager().removeEntityData(toRemove);
      }
   }

   public void createBlock(Location location, Grave grave) {
      if (grave != null && location != null && location.getWorld() != null) {
         if (this.requireReady(grave, "block place")) {
            if (this.plugin.getConfigManager().getConfigSection("itemsadder.block.enabled", grave).getBoolean("itemsadder.block.enabled")) {
               String name = this.plugin.getConfigManager().getConfigSection("itemsadder.block.name", grave).getString("itemsadder.block.name", "");
               if (!name.isEmpty()) {
                  CustomBlock block = CustomBlock.place(name, location);
                  if (block != null) {
                     this.plugin
                        .debugMessage(
                           "Placing ItemsAdder block for "
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
                  } else {
                     this.plugin.debugMessage("Can't find ItemsAdder block " + name, 1);
                  }
               }
            }
         }
      }
   }

   public boolean isCustomBlock(Location location) {
      if (location != null && location.getWorld() != null) {
         CustomBlock.byAlreadyPlaced(location.getBlock());
      }

      return false;
   }

   public void removeBlock(Location location) {
      if (location != null && location.getWorld() != null) {
         boolean removed = CustomBlock.remove(location);
         if (!removed) {
            this.plugin
               .debugMessage(
                  "Failed to remove ItemsAdder block at "
                     + location.getWorld().getName()
                     + ", "
                     + location.getBlockX()
                     + "x, "
                     + location.getBlockY()
                     + "y, "
                     + location.getBlockZ()
                     + "z",
                  2
               );
         }
      }
   }

   public boolean hasFurniture(Grave grave) {
      if (grave == null) {
         return false;
      } else {
         Map<EntityData, Entity> map = this.getEntityDataMap(this.getLoadedEntityDataList(grave));
         if (map.isEmpty()) {
            return false;
         } else {
            for (Entity e : map.values()) {
               if (e != null && e.isValid() && !e.isDead()) {
                  try {
                     if (CustomFurniture.byAlreadySpawned(e) != null) {
                        return true;
                     }
                  } catch (Throwable var6) {
                     return true;
                  }
               }
            }

            return false;
         }
      }
   }

   public boolean hasBlock(Grave grave) {
      if (grave == null) {
         return false;
      } else {
         Location loc = grave.getLocationDeath();
         return loc != null && loc.getWorld() != null ? this.isCustomBlock(loc) : false;
      }
   }
}
