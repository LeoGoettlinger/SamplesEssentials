package dev.cwhead.GravesX.integration;

import com.ranull.graves.Graves;
import com.ranull.graves.data.EntityData;
import com.ranull.graves.manager.EntityDataManager;
import com.ranull.graves.type.Grave;
import com.ranull.graves.util.BlockFaceUtil;
import com.ranull.graves.util.LocationUtil;
import dev.cwhead.GravesX.listener.integration.craftengine.CraftEngineBlockListener;
import dev.cwhead.GravesX.listener.integration.craftengine.CraftEngineFurnitureListener;
import dev.cwhead.GravesX.provider.CustomItemStorageProvider;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public final class CraftEngine extends EntityDataManager implements CustomItemStorageProvider {
   private static final String PROVIDER_ID = "CRAFTENGINE";
   private final Graves plugin;
   private final Plugin craftEnginePlugin;
   private final Method keyOfStringMethod;
   private final Method isCustomItemMethod;
   private final Method getCustomItemIdMethod;
   private final Method itemByIdMethod;
   private final Method blockPlaceMethod;
   private final Method blockRemoveMethod;
   private final Method blockIsCustomBlockMethod;
   private final Method blockGetCustomStateMethod;
   private final Method furniturePlaceMethod;
   private final Method furnitureRemoveMethod;
   private final Method furnitureIsFurnitureMethod;
   private final Method furnitureIsCollisionEntityMethod;
   private final Method furnitureIsSeatMethod;
   private final Method furnitureByMetaEntityMethod;
   private final Method furnitureByColliderMethod;
   private final Method furnitureBySeatMethod;
   private final CraftEngineBlockListener blockListener;
   private final CraftEngineFurnitureListener furnitureListener;

   public CraftEngine(Graves plugin, Plugin craftEnginePlugin) throws ReflectiveOperationException {
      super(plugin);
      this.plugin = Objects.requireNonNull(plugin, "plugin");
      this.craftEnginePlugin = Objects.requireNonNull(craftEnginePlugin, "craftEnginePlugin");
      ClassLoader classLoader = craftEnginePlugin.getClass().getClassLoader();
      Class<?> keyClass = Class.forName("net.momirealms.craftengine.core.util.Key", true, classLoader);
      Class<?> craftEngineItems = Class.forName("net.momirealms.craftengine.bukkit.api.CraftEngineItems", true, classLoader);
      Class<?> craftEngineBlocks = Class.forName("net.momirealms.craftengine.bukkit.api.CraftEngineBlocks", true, classLoader);
      Class<?> craftEngineFurniture = Class.forName("net.momirealms.craftengine.bukkit.api.CraftEngineFurniture", true, classLoader);
      this.keyOfStringMethod = keyClass.getMethod("of", String.class);
      this.isCustomItemMethod = craftEngineItems.getMethod("isCustomItem", ItemStack.class);
      this.getCustomItemIdMethod = craftEngineItems.getMethod("getCustomItemId", ItemStack.class);
      this.itemByIdMethod = craftEngineItems.getMethod("byId", String.class);
      this.blockPlaceMethod = craftEngineBlocks.getMethod("place", Location.class, keyClass, boolean.class);
      this.blockRemoveMethod = craftEngineBlocks.getMethod("remove", Block.class);
      this.blockIsCustomBlockMethod = craftEngineBlocks.getMethod("isCustomBlock", Block.class);
      this.blockGetCustomStateMethod = craftEngineBlocks.getMethod("getCustomBlockState", Block.class);
      this.furniturePlaceMethod = craftEngineFurniture.getMethod("place", Location.class, keyClass);
      this.furnitureRemoveMethod = craftEngineFurniture.getMethod("remove", Entity.class);
      this.furnitureIsFurnitureMethod = craftEngineFurniture.getMethod("isFurniture", Entity.class);
      this.furnitureIsCollisionEntityMethod = craftEngineFurniture.getMethod("isCollisionEntity", Entity.class);
      this.furnitureIsSeatMethod = craftEngineFurniture.getMethod("isSeat", Entity.class);
      this.furnitureByMetaEntityMethod = craftEngineFurniture.getMethod("getLoadedFurnitureByMetaEntity", Entity.class);
      this.furnitureByColliderMethod = craftEngineFurniture.getMethod("getLoadedFurnitureByCollider", Entity.class);
      this.furnitureBySeatMethod = craftEngineFurniture.getMethod("getLoadedFurnitureBySeat", Entity.class);
      this.blockListener = new CraftEngineBlockListener(plugin, this);
      this.furnitureListener = new CraftEngineFurnitureListener(plugin, this);
      this.registerListeners();
   }

   @Override
   public String getProviderId() {
      return "CRAFTENGINE";
   }

   public Plugin getCraftEnginePlugin() {
      return this.craftEnginePlugin;
   }

   public void registerListeners() {
      this.plugin.getServer().getPluginManager().registerEvents(this.blockListener, this.plugin);
      this.plugin.getServer().getPluginManager().registerEvents(this.furnitureListener, this.plugin);
   }

   public void unregisterListeners() {
      HandlerList.unregisterAll(this.blockListener);
      HandlerList.unregisterAll(this.furnitureListener);
   }

   public boolean isCustomItem(ItemStack itemStack) {
      if (itemStack != null && itemStack.getType() != Material.AIR) {
         try {
            return Boolean.TRUE.equals(this.isCustomItemMethod.invoke(null, itemStack));
         } catch (Throwable var3) {
            this.debugFailure("custom item check", var3);
            return false;
         }
      } else {
         return false;
      }
   }

   @Override
   public String getCustomItemId(ItemStack itemStack) {
      if (itemStack != null && itemStack.getType() != Material.AIR && this.isCustomItem(itemStack)) {
         try {
            Object key = this.getCustomItemIdMethod.invoke(null, itemStack);
            return key != null ? key.toString() : null;
         } catch (Throwable var3) {
            this.debugFailure("custom item id lookup", var3);
            return null;
         }
      } else {
         return null;
      }
   }

   @Override
   public ItemStack rebuildCustomItem(String id, int amount) {
      if (id != null && !id.isBlank()) {
         try {
            Object definition = this.itemByIdMethod.invoke(null, id);
            if (definition == null) {
               return null;
            }

            Method buildBukkitItem = this.getBuildBukkitItemMethod(definition.getClass());
            if (buildBukkitItem.invoke(definition) instanceof ItemStack itemStack) {
               itemStack.setAmount(Math.max(1, amount));
               return itemStack;
            }
         } catch (Throwable var7) {
            this.debugFailure("custom item rebuild for " + id, var7);
         }

         return null;
      } else {
         return null;
      }
   }

   public void createBlock(Location location, Grave grave) {
      if (location != null && location.getWorld() != null && grave != null) {
         if (this.plugin.getConfigManager().getConfigSection("craftengine.block.enabled", grave).getBoolean("craftengine.block.enabled", false)) {
            String name = this.plugin.getConfigManager().getConfigSection("craftengine.block.name", grave).getString("craftengine.block.name", "");
            if (name != null && !name.isBlank()) {
               try {
                  boolean placed = Boolean.TRUE.equals(this.blockPlaceMethod.invoke(null, location, this.toKey(name), true));
                  if (placed) {
                     this.plugin
                        .debugMessage(
                           "Placing CraftEngine block for "
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
                     this.plugin.debugMessage("Can't find CraftEngine block " + name, 1);
                  }
               } catch (Throwable var5) {
                  this.debugFailure("block place for " + name, var5);
               }
            }
         }
      }
   }

   public boolean isCustomBlock(Location location) {
      return location != null && location.getWorld() != null && this.isCustomBlock(location.getBlock());
   }

   public boolean isCustomBlock(Block block) {
      if (block != null && block.getWorld() != null) {
         try {
            return Boolean.TRUE.equals(this.blockIsCustomBlockMethod.invoke(null, block));
         } catch (Throwable var3) {
            this.debugFailure("custom block check", var3);
            return false;
         }
      } else {
         return false;
      }
   }

   public String getCustomBlockId(Block block) {
      if (block != null && block.getWorld() != null) {
         try {
            Object state = this.blockGetCustomStateMethod.invoke(null, block);
            return state != null ? this.getBlockStateId(state) : null;
         } catch (Throwable var3) {
            this.debugFailure("custom block id lookup", var3);
            return null;
         }
      } else {
         return null;
      }
   }

   public void removeBlock(Location location) {
      if (location != null && location.getWorld() != null) {
         try {
            this.blockRemoveMethod.invoke(null, location.getBlock());
         } catch (Throwable var3) {
            this.debugFailure("block remove", var3);
         }
      }
   }

   public void createFurniture(Location location, Grave grave) {
      if (location != null && location.getWorld() != null && grave != null) {
         if (this.plugin.getConfigManager().getConfigSection("craftengine.furniture.enabled", grave).getBoolean("craftengine.furniture.enabled", false)) {
            String name = this.plugin.getConfigManager().getConfigSection("craftengine.furniture.name", grave).getString("craftengine.furniture.name", "");
            if (name != null && !name.isBlank()) {
               Location spawnLocation = LocationUtil.roundLocation(location).add(0.5, 0.0, 0.5);
               spawnLocation.setYaw(BlockFaceUtil.getBlockFaceYaw(BlockFaceUtil.getYawBlockFace(spawnLocation.getYaw()).getOppositeFace()));
               spawnLocation.setPitch(grave.getPitch());

               try {
                  spawnLocation.getBlock().setType(Material.AIR);
                  Object furniture = this.furniturePlaceMethod.invoke(null, spawnLocation, this.toKey(name));
                  Entity entity = this.getFurnitureBaseEntity(furniture);
                  if (entity != null) {
                     this.createEntityData(spawnLocation, entity.getUniqueId(), grave.getUUID(), EntityData.Type.CRAFTENGINE);
                     this.plugin
                        .debugMessage(
                           "Placing CraftEngine furniture for "
                              + grave.getUUID()
                              + " at "
                              + spawnLocation.getWorld().getName()
                              + ", "
                              + (spawnLocation.getBlockX() + 0.5)
                              + "x, "
                              + (spawnLocation.getBlockY() + 0.5)
                              + "y, "
                              + (spawnLocation.getBlockZ() + 0.5)
                              + "z",
                           1
                        );
                  } else {
                     this.plugin.debugMessage("Can't find CraftEngine furniture " + name, 1);
                  }
               } catch (Throwable var7) {
                  this.debugFailure("furniture place for " + name, var7);
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
            Entity entity = entry.getValue();
            if (entity != null) {
               try {
                  this.furnitureRemoveMethod.invoke(null, entity);
               } catch (Throwable var9) {
                  try {
                     entity.remove();
                  } catch (Throwable var8) {
                  }

                  this.debugFailure("furniture remove", var9);
               }

               toRemove.add(entry.getKey());
            }
         }

         this.plugin.getDataManager().removeEntityData(toRemove);
      }
   }

   @Override
   public Grave getGrave(Entity entity) {
      if (entity == null) {
         return null;
      } else {
         Entity baseEntity = this.getFurnitureBaseEntity(entity);
         if (baseEntity != null) {
            Grave grave = super.getGrave(baseEntity);
            if (grave != null) {
               return grave;
            }
         }

         return super.getGrave(entity);
      }
   }

   public Entity getFurnitureBaseEntity(Entity entity) {
      Object furniture = this.getFurniture(entity);
      return this.getFurnitureBaseEntity(furniture);
   }

   public boolean isFurnitureEntity(Entity entity) {
      return this.getFurniture(entity) != null;
   }

   public String getFurnitureId(Entity entity) {
      Object furniture = this.getFurniture(entity);
      if (furniture == null) {
         return null;
      } else {
         try {
            Object key = furniture.getClass().getMethod("id").invoke(furniture);
            return key != null ? key.toString() : null;
         } catch (Throwable var4) {
            this.debugFailure("furniture id lookup", var4);
            return null;
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
            for (Entry<EntityData, Entity> entry : map.entrySet()) {
               EntityData data = entry.getKey();
               Entity entity = entry.getValue();
               if (data != null
                  && entity != null
                  && data.getType() == EntityData.Type.CRAFTENGINE
                  && entity.isValid()
                  && !entity.isDead()
                  && this.isFurnitureEntity(entity)) {
                  return true;
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
         Location location = grave.getLocationDeath();
         return location != null && location.getWorld() != null ? this.isCustomBlock(location) : false;
      }
   }

   private Object getFurniture(Entity entity) {
      if (entity == null) {
         return null;
      } else {
         try {
            if (Boolean.TRUE.equals(this.furnitureIsFurnitureMethod.invoke(null, entity))) {
               Object furniture = this.furnitureByMetaEntityMethod.invoke(null, entity);
               if (furniture != null) {
                  return furniture;
               }
            }

            if (Boolean.TRUE.equals(this.furnitureIsCollisionEntityMethod.invoke(null, entity))) {
               Object furniture = this.furnitureByColliderMethod.invoke(null, entity);
               if (furniture != null) {
                  return furniture;
               }
            }

            if (Boolean.TRUE.equals(this.furnitureIsSeatMethod.invoke(null, entity))) {
               return this.furnitureBySeatMethod.invoke(null, entity);
            }
         } catch (Throwable var3) {
            this.debugFailure("furniture lookup", var3);
         }

         return null;
      }
   }

   private Entity getFurnitureBaseEntity(Object furniture) {
      if (furniture == null) {
         return null;
      } else {
         try {
            if (furniture.getClass().getMethod("bukkitEntity").invoke(furniture) instanceof Entity entity) {
               return entity;
            }
         } catch (NoSuchMethodException var6) {
            try {
               if (furniture.getClass().getMethod("getBukkitEntity").invoke(furniture) instanceof Entity entity) {
                  return entity;
               }
            } catch (Throwable var5) {
               this.debugFailure("furniture base entity lookup", var5);
            }
         } catch (Throwable var7) {
            this.debugFailure("furniture base entity lookup", var7);
         }

         return null;
      }
   }

   private Object toKey(String id) throws ReflectiveOperationException {
      return this.keyOfStringMethod.invoke(null, id);
   }

   private String getBlockStateId(Object state) {
      try {
         Object owner = state.getClass().getMethod("owner").invoke(state);
         Object key = owner.getClass().getMethod("key").invoke(owner);
         Object location = key.getClass().getMethod("location").invoke(key);
         Object string = location.getClass().getMethod("asString").invoke(location);
         return string != null ? string.toString() : null;
      } catch (Throwable var6) {
         String value = state.toString();
         int propertiesIndex = value.indexOf(91);
         return propertiesIndex >= 0 ? value.substring(0, propertiesIndex) : value;
      }
   }

   private Method getBuildBukkitItemMethod(Class<?> definitionClass) throws NoSuchMethodException {
      try {
         return definitionClass.getMethod("buildBukkitItem");
      } catch (NoSuchMethodException var7) {
         for (Method method : definitionClass.getMethods()) {
            if (method.getName().equals("buildBukkitItem") && method.getParameterCount() == 0 && ItemStack.class.isAssignableFrom(method.getReturnType())) {
               return method;
            }
         }

         throw var7;
      }
   }

   private void debugFailure(String action, Throwable throwable) {
      Throwable cause = throwable.getCause() != null ? throwable.getCause() : throwable;
      this.plugin.debugMessage("CraftEngine " + action + " failed: " + cause.getMessage(), 2);
   }
}
