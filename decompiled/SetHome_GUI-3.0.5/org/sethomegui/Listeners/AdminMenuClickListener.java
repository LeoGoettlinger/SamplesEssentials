package org.sethomegui.Listeners;

import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.sethomegui.SetHomeGUI;
import org.sethomegui.Managers.AdminConfirmHolder;
import org.sethomegui.Managers.AdminGUIManager;
import org.sethomegui.Managers.AdminHomesHolder;
import org.sethomegui.Managers.AdminMainHolder;
import org.sethomegui.SetHomeGUI.libs.YamlDocument;
import org.sethomegui.SetHomeGUI.libs.block.implementation.Section;
import org.sethomegui.Utils.MenuActions;
import org.sethomegui.Utils.Utils;

public class AdminMenuClickListener implements Listener {
   private final SetHomeGUI plugin;

   public AdminMenuClickListener(SetHomeGUI plugin) {
      this.plugin = plugin;
   }

   @EventHandler
   public void onAdminInventoryClick(InventoryClickEvent event) {
      if (event.getWhoClicked() instanceof Player) {
         Player admin = (Player)event.getWhoClicked();
         UUID uuid = admin.getUniqueId();
         InventoryHolder holder = event.getInventory().getHolder();
         if (holder instanceof AdminMainHolder || holder instanceof AdminHomesHolder || holder instanceof AdminConfirmHolder) {
            event.setCancelled(true);
            if (event.getRawSlot() >= 0 && event.getRawSlot() < event.getView().getTopInventory().getSize()) {
               ItemStack clicked = event.getCurrentItem();
               int slot = event.getSlot();
               if (clicked != null && clicked.getType() != Material.AIR && clicked.hasItemMeta()) {
                  String menuPath = holder instanceof AdminMainHolder
                     ? "gui.admin-gui"
                     : (holder instanceof AdminHomesHolder ? "gui.admin-gui.admin-player-homes-gui" : "gui.admin-gui.admin-confirmation-gui");
                  if (MenuActions.execute(this.plugin, admin, this.plugin.getGuisConfig().getSection(menuPath), slot)) {
                     admin.playSound(admin.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
                  } else {
                     AdminGUIManager manager = this.plugin.getAdminGUIManager();
                     String action = (String)clicked.getItemMeta().getPersistentDataContainer().get(manager.actionKey, PersistentDataType.STRING);
                     if (action == null) {
                        action = "";
                     }

                     int currentPage = manager.getPage(uuid);
                     FileConfiguration mainConfig = this.plugin.getConfig();
                     if (holder instanceof AdminMainHolder) {
                        switch (action) {
                           case "search":
                              admin.playSound(admin.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
                              manager.openSearchChat(admin);
                              return;
                           case "prev_page":
                              if (currentPage > 1) {
                                 admin.playSound(admin.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
                                 manager.setPage(uuid, currentPage - 1);
                                 manager.openAdminMenu(admin);
                              }

                              return;
                           case "next_page":
                              admin.playSound(admin.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
                              manager.setPage(uuid, currentPage + 1);
                              manager.openAdminMenu(admin);
                              return;
                           default:
                              Section mainSection = this.plugin.getGuisConfig().getSection("gui.admin-gui");
                              boolean isPlayerHead = action.equals("open_player") || mainSection != null && mainSection.getIntList("slots").contains(slot);
                              if (isPlayerHead) {
                                 String uuidStr = null;
                                 if (clicked.getItemMeta().hasLore()) {
                                    for (String line : clicked.getItemMeta().getLore()) {
                                       String stripped = ChatColor.stripColor(line);
                                       if (stripped.contains("h_target:")) {
                                          uuidStr = stripped.split("h_target:")[1].trim();
                                          break;
                                       }
                                    }
                                 }

                                 if (uuidStr == null) {
                                    uuidStr = (String)clicked.getItemMeta().getPersistentDataContainer().get(manager.targetUuidKey, PersistentDataType.STRING);
                                 }

                                 if (uuidStr != null) {
                                    admin.playSound(admin.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
                                    manager.setPage(uuid, 1);
                                    manager.openAdminPlayerHomesMenu(admin, UUID.fromString(uuidStr), 1);
                                 }
                              }
                        }
                     } else if (holder instanceof AdminHomesHolder) {
                        UUID targetUuid = ((AdminHomesHolder)holder).getAuditedPlayerUuid();
                        switch (action) {
                           case "go_back":
                              admin.playSound(admin.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
                              manager.setPage(uuid, 1);
                              manager.openAdminMenu(admin);
                              break;
                           case "homes_prev":
                              if (currentPage > 1) {
                                 admin.playSound(admin.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
                                 manager.openAdminPlayerHomesMenu(admin, targetUuid, currentPage - 1);
                              }
                              break;
                           case "homes_next":
                              admin.playSound(admin.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
                              manager.openAdminPlayerHomesMenu(admin, targetUuid, currentPage + 1);
                              break;
                           case "interact_home":
                              String homeName = (String)clicked.getItemMeta()
                                 .getPersistentDataContainer()
                                 .get(manager.targetHomeKey, PersistentDataType.STRING);
                              if (homeName == null) {
                                 return;
                              }

                              if (event.getClick().isLeftClick()) {
                                 admin.playSound(admin.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
                                 admin.closeInventory();

                                 try {
                                    YamlDocument targetData = this.plugin.getHomeManager().getPlayerFile(targetUuid);
                                    if (targetData == null) {
                                       return;
                                    }

                                    Section hData = targetData.getSection(homeName);
                                    if (hData != null) {
                                       World world = Bukkit.getWorld(hData.getString("world", "world"));
                                       double x = hData.getDouble("x");
                                       double y = hData.getDouble("y");
                                       double z = hData.getDouble("z");
                                       float yaw = hData.getFloat("yaw", 0.0F);
                                       float pitch = hData.getFloat("pitch", 0.0F);
                                       if (world != null) {
                                          Location targetLoc = new Location(world, x, y, z, yaw, pitch);
                                          admin.getScheduler().run(this.plugin, task -> admin.teleport(targetLoc), null);
                                          String msg = mainConfig.getString("messages.admin.teleport-success", "&a[Admin] Teleported to %home%!");
                                          admin.sendMessage(Utils.color(msg.replace("%home%", homeName)));
                                       }
                                    }
                                 } catch (Exception var29) {
                                 }
                              } else if (event.getClick().isRightClick()) {
                                 admin.playSound(admin.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
                                 manager.openAdminConfirmationMenu(admin, targetUuid, homeName);
                              }
                        }
                     } else {
                        if (holder instanceof AdminConfirmHolder confirmHolder) {
                           UUID targetUuid = confirmHolder.getTargetUuid();
                           String homeNamex = confirmHolder.getHomeName();
                           if (action.equals("confirm_wipe")) {
                              admin.playSound(admin.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0F, 1.0F);

                              try {
                                 YamlDocument targetDatax = this.plugin.getHomeManager().getPlayerFile(targetUuid);
                                 if (targetDatax == null) {
                                    return;
                                 }

                                 targetDatax.set(homeNamex, null);
                                 List<String> homeNames = targetDatax.getStringList("homes");
                                 if (homeNames != null) {
                                    homeNames.remove(homeNamex);
                                    targetDatax.set("homes", homeNames);
                                 }

                                 targetDatax.save();
                                 String msg = mainConfig.getString("messages.admin.delete-success", "&a[Admin] Home %home% deleted.");
                                 admin.sendMessage(Utils.color(msg.replace("%home%", homeNamex)));
                              } catch (Exception var30) {
                              }

                              manager.openAdminPlayerHomesMenu(admin, targetUuid, 1);
                           } else if (action.equals("cancel_wipe")) {
                              admin.playSound(admin.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
                              manager.openAdminPlayerHomesMenu(admin, targetUuid, 1);
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @EventHandler
   public void onAdminInventoryClose(InventoryCloseEvent event) {
      if (event.getPlayer() instanceof Player) {
         Player admin = (Player)event.getPlayer();
         if (event.getInventory().getHolder() instanceof AdminMainHolder) {
            if (admin.hasMetadata("admin_search_mode")) {
               return;
            }

            AdminGUIManager manager = this.plugin.getAdminGUIManager();
            manager.setSearchFilter(admin.getUniqueId(), "");
         }
      }
   }
}
