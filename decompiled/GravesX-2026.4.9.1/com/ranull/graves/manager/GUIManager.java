package com.ranull.graves.manager;

import com.ranull.graves.Graves;
import com.ranull.graves.compatibility.CompatibilityInventoryView;
import com.ranull.graves.integration.MiniMessage;
import com.ranull.graves.inventory.GraveList;
import com.ranull.graves.inventory.GraveMenu;
import com.ranull.graves.type.Grave;
import com.ranull.graves.util.InventoryUtil;
import com.ranull.graves.util.StringUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

public class GUIManager {
   private final Graves plugin;

   public GUIManager(Graves plugin) {
      this.plugin = plugin;
   }

   public void openGraveList(Entity entity) {
      this.openGraveList(entity, entity.getUniqueId(), true);
   }

   public void openGraveList(Entity entity, boolean sound) {
      this.openGraveList(entity, entity.getUniqueId(), sound);
   }

   public void openGraveList(Entity entity, Entity entity2) {
      this.openGraveList(entity, entity2.getUniqueId(), true);
   }

   public void openGraveList(Entity entity, UUID uuid) {
      this.openGraveList(entity, uuid, true);
   }

   public void refreshMenus() {
      if (this.plugin.isEnabled()) {
         for (Player player : this.plugin.getServer().getOnlinePlayers()) {
            InventoryView openInventory = player.getOpenInventory();
            if (openInventory != null) {
               Inventory topInventory = CompatibilityInventoryView.getTopInventory(openInventory);
               if (topInventory.getHolder() instanceof GraveList graveList) {
                  this.setGraveListItems(topInventory, graveList.getUUID());
               } else if (topInventory.getHolder() instanceof GraveMenu graveMenu) {
                  this.setGraveMenuItems(topInventory, graveMenu.getGrave());
               }
            }
         }
      }
   }

   public void refreshMenusSafely() {
      List<Player> players = new ArrayList<>(this.plugin.getServer().getOnlinePlayers());
      if (!players.isEmpty()) {
         for (Player player : players) {
            if (player != null && player.isOnline()) {
               Location loc;
               try {
                  loc = player.getLocation();
               } catch (Throwable var6) {
                  continue;
               }

               if (loc.getWorld() != null) {
                  Location anchor = new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ());
                  this.plugin.getSchedulerManager().execute(anchor, () -> {
                     if (player.isOnline()) {
                        InventoryView openInventory;
                        try {
                           openInventory = player.getOpenInventory();
                        } catch (Throwable var9) {
                           return;
                        }

                        Inventory topInventory = CompatibilityInventoryView.getTopInventory(openInventory);
                        if (topInventory != null) {
                           try {
                              InventoryHolder holder = topInventory.getHolder();
                              if (holder instanceof GraveList graveList) {
                                 this.setGraveListItems(topInventory, graveList.getUUID());
                              } else if (holder instanceof GraveMenu graveMenu) {
                                 this.setGraveMenuItems(topInventory, graveMenu.getGrave());
                              }
                           } catch (IllegalStateException var7) {
                              this.plugin.debugMessage("Skipping menu refresh for " + player.getName() + ": " + var7.getMessage(), 2);
                           } catch (Throwable var8) {
                           }
                        }
                     }
                  });
               }
            }
         }
      }
   }

   public void openGraveList(Entity entity, UUID uuid, boolean sound) {
      if (entity instanceof Player player) {
         List<String> permissionList = this.plugin.getPermissionList(player);
         List<Grave> playerGraveList = this.plugin.getGraveManager().getGraveList(uuid);
         if (!playerGraveList.isEmpty()) {
            GraveList graveList = new GraveList(uuid, playerGraveList);
            Inventory inventory;
            if (this.plugin.getIntegrationManager().hasMiniMessage()) {
               String guiTitle = StringUtil.parseString(
                  this.plugin
                     .getConfigManager()
                     .getConfigSection("gui.menu.list.title", player, permissionList)
                     .getString("gui.menu.list.title", "Graves Main Menu"),
                  player,
                  this.plugin
               );
               String guiNew = MiniMessage.convertLegacyToMiniMessage(guiTitle);
               inventory = this.plugin
                  .getServer()
                  .createInventory(graveList, InventoryUtil.getInventorySize(playerGraveList.size()), MiniMessage.parseString(guiNew));
            } else {
               inventory = this.plugin
                  .getServer()
                  .createInventory(
                     graveList,
                     InventoryUtil.getInventorySize(playerGraveList.size()),
                     StringUtil.parseString(
                        this.plugin
                           .getConfigManager()
                           .getConfigSection("gui.menu.list.title", player, permissionList)
                           .getString("gui.menu.list.title", "Graves Main Menu"),
                        player,
                        this.plugin
                     )
                  );
            }

            this.setGraveListItems(inventory, playerGraveList);
            graveList.setInventory(inventory);
            player.openInventory(graveList.getInventory());
            if (sound) {
               this.plugin.getEntityManager().playPlayerSound("sound.menu-open", player, permissionList);
            }
         } else {
            this.plugin.getEntityManager().sendMessage("message.empty", player, permissionList);
         }
      }
   }

   public void setGraveListItems(Inventory inventory, UUID uuid) {
      this.setGraveListItems(inventory, this.plugin.getGraveManager().getGraveList(uuid));
   }

   public void setGraveListItems(Inventory inventory, List<Grave> graveList) {
      inventory.clear();
      int count = 1;

      for (Grave grave : graveList) {
         inventory.addItem(new ItemStack[]{this.plugin.getItemStackManager().createGraveListItemStack(count, grave)});
         count++;
      }
   }

   public void openGraveMenu(Entity entity, Grave grave) {
      this.openGraveMenu(entity, grave, true);
   }

   public void openGraveMenu(Entity entity, Grave grave, boolean sound) {
      if (entity instanceof Player player) {
         GraveMenu graveMenu = new GraveMenu(grave);
         String title;
         if (this.plugin.getIntegrationManager().hasMiniMessage()) {
            String newTitle = StringUtil.parseString(
               this.plugin
                  .getConfigManager()
                  .getConfigSection("gui.menu.grave.title", player, grave.getPermissionList())
                  .getString("gui.menu.grave.title", "Grave"),
               player,
               this.plugin
            );
            title = MiniMessage.parseString(newTitle);
         } else {
            title = StringUtil.parseString(
               this.plugin
                  .getConfigManager()
                  .getConfigSection("gui.menu.grave.title", player, grave.getPermissionList())
                  .getString("gui.menu.grave.title", "Grave"),
               player,
               this.plugin
            );
         }

         int size = this.computeGraveMenuSize(grave);
         Inventory inventory = this.plugin.getServer().createInventory(graveMenu, size, title);
         this.setGraveMenuItems(inventory, grave);
         graveMenu.setInventory(inventory);
         player.openInventory(graveMenu.getInventory());
         if (sound) {
            this.plugin.getEntityManager().playPlayerSound("sound.menu-open", player, grave);
         }
      }
   }

   public void setGraveMenuItems(Inventory inventory, Grave grave) {
      inventory.clear();
      ConfigurationSection configurationSection = this.plugin
         .getConfigManager()
         .getConfigSection("gui.menu.grave.slot", grave)
         .getConfigurationSection("gui.menu.grave.slot");
      if (configurationSection != null) {
         for (String string : configurationSection.getKeys(false)) {
            try {
               int slot = Integer.parseInt(string);
               if (slot >= 0 && slot < inventory.getSize()) {
                  if (this.plugin
                     .getConfigManager()
                     .getConfigSection("gui.menu.grave.slot." + slot + ".enabled", grave)
                     .getBoolean("gui.menu.grave.slot." + slot + ".enabled")) {
                     inventory.setItem(slot, this.plugin.getItemStackManager().createGraveMenuItemStack(slot, grave));
                  }
               } else {
                  this.plugin
                     .debugMessage("[GUI] Grave menu slot " + slot + " out of bounds (size=" + inventory.getSize() + "). Check gui.menu.grave.slot." + slot, 2);
               }
            } catch (NumberFormatException var7) {
               this.plugin.debugMessage(string + " is not an int", 1);
            }
         }
      }
   }

   private int computeGraveMenuSize(Grave grave) {
      ConfigurationSection slots = this.plugin.getConfigManager().getConfigSection("gui.menu.grave.slot", grave).getConfigurationSection("gui.menu.grave.slot");
      int maxSlot = -1;
      if (slots != null) {
         for (String key : slots.getKeys(false)) {
            try {
               int slot = Integer.parseInt(key);
               if (slot >= 0) {
                  boolean enabled = this.plugin
                     .getConfigManager()
                     .getConfigSection("gui.menu.grave.slot." + slot + ".enabled", grave)
                     .getBoolean("gui.menu.grave.slot." + slot + ".enabled");
                  if (enabled) {
                     maxSlot = Math.max(maxSlot, slot);
                  }
               }
            } catch (NumberFormatException var8) {
            }
         }
      }

      int required = Math.max(9, maxSlot >= 0 ? maxSlot + 1 : 9);
      int size = (required + 8) / 9 * 9;
      if (size > 54) {
         this.plugin.getLogger().warning("[GUI] Grave menu requires " + size + " slots (maxSlot=" + maxSlot + "). Clamping to 54.");
         size = 54;
      }

      return size;
   }
}
