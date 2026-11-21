package com.viaversion.viaversion.bukkit.providers;

import com.viaversion.viaversion.ViaVersionPlugin;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.bukkit.platform.PaperViaInjector;
import com.viaversion.viaversion.bukkit.util.LegacyBlockToItem;
import com.viaversion.viaversion.protocols.v1_21_2to1_21_4.provider.PickItemProvider;
import java.lang.reflect.Method;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BlockStateMeta;

public class BukkitPickItemProvider extends PickItemProvider {
   private static final BukkitPickItemProvider.BlockToItem BLOCK_TO_ITEM = BukkitPickItemProvider.BlockToItem.build();
   private static final BukkitPickItemProvider.GetStorageContents GET_STORAGE_CONTENT = BukkitPickItemProvider.GetStorageContents.build();
   private static final BukkitPickItemProvider.SetInHand SET_IN_HAND = BukkitPickItemProvider.SetInHand.build();
   private static final boolean HAS_SPAWN_EGG_METHOD = PaperViaInjector.hasMethod(ItemFactory.class, Material.class, "getSpawnEgg", EntityType.class);
   private static final Method GET_SPAWN_EGG_ITEMSTACK_METHOD;
   private static final double BLOCK_RANGE = 5.5;
   private static final double BLOCK_RANGE_SQUARED = 30.25;
   private static final double ENTITY_RANGE = 6.0;
   private final ViaVersionPlugin plugin;

   public BukkitPickItemProvider(ViaVersionPlugin plugin) {
      this.plugin = plugin;
   }

   @Override
   public void pickItemFromBlock(UserConnection connection, BlockPosition blockPosition, boolean includeData) {
      UUID uuid = connection.getProtocolInfo().getUuid();
      Player player = this.plugin.getServer().getPlayer(uuid);
      if (player != null) {
         this.plugin.runSyncFor(() -> {
            Location playerLocation = player.getLocation();
            if (!(blockPosition.distanceFromCenterSquared(playerLocation.getX(), playerLocation.getY(), playerLocation.getZ()) > 30.25)) {
               Block block = player.getWorld().getBlockAt(blockPosition.x(), blockPosition.y(), blockPosition.z());
               if (block.getType() != Material.AIR) {
                  ItemStack item = BLOCK_TO_ITEM.apply(block, includeData && player.getGameMode() == GameMode.CREATIVE);
                  if (item != null && item.getType() != Material.AIR) {
                     this.pickItem(player, item);
                  }
               }
            }
         }, player);
      }
   }

   @Override
   public void pickItemFromEntity(UserConnection connection, int entityId, boolean includeData) {
      if (HAS_SPAWN_EGG_METHOD || GET_SPAWN_EGG_ITEMSTACK_METHOD != null) {
         UUID uuid = connection.getProtocolInfo().getUuid();
         Player player = this.plugin.getServer().getPlayer(uuid);
         if (player != null) {
            this.plugin
               .runSyncFor(
                  () -> {
                     Entity entity = player.getWorld()
                        .getNearbyEntities(player.getLocation(), 6.0, 6.0, 6.0)
                        .stream()
                        .filter(ex -> ex.getEntityId() == entityId)
                        .findAny()
                        .orElse(null);
                     if (entity != null) {
                        if (GET_SPAWN_EGG_ITEMSTACK_METHOD == null) {
                           Material spawnEggType = Bukkit.getItemFactory().getSpawnEgg(entity.getType());
                           if (spawnEggType != null) {
                              this.pickItem(player, new ItemStack(spawnEggType, 1));
                           }
                        } else {
                           try {
                              ItemStack spawnEggItem = (ItemStack)GET_SPAWN_EGG_ITEMSTACK_METHOD.invoke(Bukkit.getItemFactory(), entity.getType());
                              if (spawnEggItem != null) {
                                 this.pickItem(player, spawnEggItem);
                              }
                           } catch (ReflectiveOperationException var5x) {
                              throw new RuntimeException(var5x);
                           }
                        }
                     }
                  },
                  player
               );
         }
      }
   }

   private void pickItem(Player player, ItemStack item) {
      PlayerInventory inventory = player.getInventory();
      ItemStack[] contents = GET_STORAGE_CONTENT.apply(inventory);
      int sourceSlot = -1;

      for (int i = 0; i < contents.length; i++) {
         ItemStack content = contents[i];
         if (content != null && content.isSimilar(item)) {
            sourceSlot = i;
            break;
         }
      }

      if (sourceSlot != -1) {
         this.moveToHotbar(inventory, sourceSlot, contents);
      } else if (player.getGameMode() == GameMode.CREATIVE) {
         this.spawnItem(item, inventory, contents);
      }
   }

   private void spawnItem(ItemStack item, PlayerInventory inventory, ItemStack[] contents) {
      int targetSlot = this.findEmptyHotbarSlot(inventory, inventory.getHeldItemSlot());
      inventory.setHeldItemSlot(targetSlot);
      ItemStack heldItem = inventory.getItem(targetSlot);
      int emptySlot = targetSlot;
      if (heldItem != null && heldItem.getType() != Material.AIR) {
         for (int i = 0; i < contents.length; i++) {
            if (contents[i] == null || contents[i].getType() == Material.AIR) {
               emptySlot = i;
               break;
            }
         }
      }

      inventory.setItem(emptySlot, heldItem);
      SET_IN_HAND.apply(inventory, item);
   }

   private void moveToHotbar(PlayerInventory inventory, int sourceSlot, ItemStack[] contents) {
      if (sourceSlot < 9) {
         inventory.setHeldItemSlot(sourceSlot);
      } else {
         int heldSlot = inventory.getHeldItemSlot();
         int targetSlot = this.findEmptyHotbarSlot(inventory, heldSlot);
         inventory.setHeldItemSlot(targetSlot);
         ItemStack heldItem = inventory.getItem(targetSlot);
         SET_IN_HAND.apply(inventory, contents[sourceSlot]);
         inventory.setItem(sourceSlot, heldItem);
      }
   }

   private int findEmptyHotbarSlot(PlayerInventory inventory, int heldSlot) {
      for (int i = 0; i < 9; i++) {
         ItemStack item = inventory.getItem(i);
         if (item == null || item.getType() == Material.AIR) {
            return i;
         }
      }

      return heldSlot;
   }

   static {
      if (PaperViaInjector.hasMethod(ItemFactory.class, ItemStack.class, "getSpawnEgg", EntityType.class)) {
         try {
            GET_SPAWN_EGG_ITEMSTACK_METHOD = ItemFactory.class.getDeclaredMethod("getSpawnEgg", EntityType.class);
         } catch (ReflectiveOperationException var1) {
            throw new RuntimeException(var1);
         }
      } else {
         GET_SPAWN_EGG_ITEMSTACK_METHOD = null;
      }
   }

   @FunctionalInterface
   private interface BlockToItem {
      ItemStack apply(Block var1, boolean var2);

      static BukkitPickItemProvider.BlockToItem build() {
         if (PaperViaInjector.hasMethod("org.bukkit.block.data.BlockData", "getPlacementMaterial")) {
            return (block, includeData) -> {
               ItemStack item = new ItemStack(block.getBlockData().getPlacementMaterial(), 1);
               if (includeData && item.getItemMeta() instanceof BlockStateMeta blockStateMeta) {
                  blockStateMeta.setBlockState(block.getState());
                  item.setItemMeta(blockStateMeta);
               }

               return item;
            };
         } else {
            ProtocolVersion version = Via.getAPI().getServerVersion().lowestSupportedProtocolVersion();
            if (version.equalTo(ProtocolVersion.v1_8)) {
               LegacyBlockToItem legacy = LegacyBlockToItem.getInstance();
               return legacy != null ? (block, includeData) -> legacy.blockToItem(block) : (b, i) -> null;
            } else {
               return PaperViaInjector.hasMethod(Material.class, "isItem")
                  ? (block, includeData) -> {
                     if (!block.getType().isItem()) {
                        return null;
                     } else {
                        return version.newerThanOrEqualTo(ProtocolVersion.v1_13)
                           ? new ItemStack(block.getType())
                           : new ItemStack(block.getType(), 1, (short)0, block.getData());
                     }
                  }
                  : (block, includeData) -> null;
            }
         }
      }
   }

   @FunctionalInterface
   private interface GetStorageContents {
      ItemStack[] apply(PlayerInventory var1);

      static BukkitPickItemProvider.GetStorageContents build() {
         return PaperViaInjector.hasMethod(Inventory.class, "getStorageContents") ? Inventory::getStorageContents : Inventory::getContents;
      }
   }

   @FunctionalInterface
   private interface SetInHand {
      void apply(PlayerInventory var1, ItemStack var2);

      static BukkitPickItemProvider.SetInHand build() {
         return PaperViaInjector.hasMethod(PlayerInventory.class, "setItemInMainHand") ? PlayerInventory::setItemInMainHand : PlayerInventory::setItemInHand;
      }
   }
}
