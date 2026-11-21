package de.theredend2000.advancedhunt.util.itemsadder;

import dev.lone.itemsadder.api.CustomBlock;
import dev.lone.itemsadder.api.CustomFurniture;
import dev.lone.itemsadder.api.CustomStack;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

public class ApiItemsAdderBridge implements ItemsAdderBridge {
   @Override
   public boolean isItemsAdderBlock(Block block) {
      return block == null ? false : CustomBlock.byAlreadyPlaced(block) != null;
   }

   @Override
   public String getCustomBlockId(Block block) {
      if (block == null) {
         return null;
      } else {
         CustomBlock customBlock = CustomBlock.byAlreadyPlaced(block);
         return customBlock != null ? customBlock.getNamespacedID() : null;
      }
   }

   @Override
   public boolean isCustomBlockItem(ItemStack item) {
      return item == null ? false : CustomBlock.byItemStack(item) != null;
   }

   @Override
   public String getCustomBlockId(ItemStack item) {
      if (item == null) {
         return null;
      } else {
         CustomBlock customBlock = CustomBlock.byItemStack(item);
         return customBlock != null ? customBlock.getNamespacedID() : null;
      }
   }

   @Override
   public boolean isCustomFurniture(ItemStack item) {
      return this.getCustomFurnitureId(item) != null;
   }

   @Override
   public String getCustomFurnitureId(ItemStack item) {
      if (item == null) {
         return null;
      } else {
         CustomStack stack = CustomStack.byItemStack(item);
         if (stack == null) {
            return null;
         } else {
            String id = stack.getNamespacedID();
            if (id == null) {
               return null;
            } else {
               Set<String> furnitureIds = CustomFurniture.getNamespacedIdsInRegistry();
               return furnitureIds != null && furnitureIds.contains(id) ? id : null;
            }
         }
      }
   }

   @Override
   public String getCustomFurnitureId(Entity entity) {
      if (entity == null) {
         return null;
      } else {
         CustomFurniture furniture = CustomFurniture.byAlreadySpawned(entity);
         return furniture != null ? furniture.getNamespacedID() : null;
      }
   }

   @Override
   public ItemStack getCustomItem(String namespacedId) {
      if (namespacedId == null) {
         return null;
      } else {
         CustomStack stack = CustomStack.getInstance(namespacedId);
         if (stack != null) {
            return stack.getItemStack();
         } else {
            CustomBlock block = CustomBlock.getInstance(namespacedId);
            return block != null ? block.getItemStack() : null;
         }
      }
   }

   @Override
   public BlockData getCustomBlockData(String namespacedId) {
      if (namespacedId != null && !namespacedId.isEmpty()) {
         try {
            CustomBlock block = CustomBlock.getInstance(namespacedId);
            if (block != null) {
               try {
                  return block.getBaseBlockData();
               } catch (Throwable var5) {
               }
            }

            try {
               return CustomBlock.getBaseBlockData(namespacedId);
            } catch (Throwable var4) {
               return null;
            }
         } catch (Throwable var6) {
            return null;
         }
      } else {
         return null;
      }
   }

   @Override
   public boolean isCustomFurnitureId(String namespacedId) {
      if (namespacedId != null && !namespacedId.isEmpty()) {
         try {
            Set<String> ids = CustomFurniture.getNamespacedIdsInRegistry();
            return ids != null && ids.contains(namespacedId);
         } catch (Throwable var3) {
            return false;
         }
      } else {
         return false;
      }
   }

   @Override
   public boolean placeCustomBlock(String namespacedId, Location location) {
      if (namespacedId != null && location != null) {
         try {
            return CustomBlock.place(namespacedId, location) != null;
         } catch (Throwable var4) {
            return false;
         }
      } else {
         return false;
      }
   }

   @Override
   public boolean removeCustomBlock(Location location) {
      if (location == null) {
         return false;
      } else {
         try {
            return CustomBlock.remove(location);
         } catch (Throwable var3) {
            return false;
         }
      }
   }

   @Override
   public boolean spawnCustomFurniture(String namespacedId, Location location) {
      if (namespacedId != null && location != null) {
         try {
            CustomFurniture furniture = CustomFurniture.spawnPreciseNonSolid(namespacedId, location);
            if (furniture != null) {
               return true;
            }
         } catch (Throwable var5) {
         }

         try {
            return location.getBlock() == null ? false : CustomFurniture.spawn(namespacedId, location.getBlock()) != null;
         } catch (Throwable var4) {
            return false;
         }
      } else {
         return false;
      }
   }

   @Override
   public boolean removeCustomFurniture(Location location) {
      if (location == null) {
         return false;
      } else {
         try {
            if (location.getBlock() == null) {
               return false;
            }

            CustomFurniture furniture = CustomFurniture.byAlreadySpawned(location.getBlock());
            if (furniture != null) {
               furniture.remove(false);
               return true;
            }
         } catch (Throwable var3) {
         }

         return false;
      }
   }
}
