package org.sethomegui.Managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.sethomegui.SetHomeGUI;
import org.sethomegui.SetHomeGUI.libs.YamlDocument;
import org.sethomegui.SetHomeGUI.libs.block.implementation.Section;
import org.sethomegui.Utils.Utils;

public class GUIManager {
   private final SetHomeGUI plugin;
   private final Map<UUID, Integer> playerPage = new HashMap<>();
   private final Map<UUID, String> pendingDeletionHome = new HashMap<>();

   public GUIManager(SetHomeGUI plugin) {
      this.plugin = plugin;
   }

   public void openMainGUI(Player player) {
      YamlDocument config = this.plugin.getGuisConfig();
      Section mainSection = config.getSection("gui.main-gui");
      if (mainSection != null) {
         String title = Utils.setPlaceholders(player, mainSection.getString("title", "Menu"), this.plugin);
         int size = mainSection.getInt("size", 27);
         Inventory gui = Bukkit.createInventory(null, size, title);
         Section itemsSection = mainSection.getSection("items");
         if (itemsSection != null) {
            for (Object keyObj : itemsSection.getKeys()) {
               String key = String.valueOf(keyObj);
               if (!key.equals("admin-menu") || player.hasPermission("sethome.admin")) {
                  Section itemData = itemsSection.getSection(key);
                  if (itemData != null) {
                     ItemStack item = this.createBaseItem(player, itemData);
                     ItemMeta meta = item.getItemMeta();
                     if (meta != null) {
                        meta.getPersistentDataContainer().set(this.plugin.getActionKey(), PersistentDataType.STRING, itemData.getString("action", key));
                        item.setItemMeta(meta);
                     }

                     if (itemData.contains("slots")) {
                        for (int slot : itemData.getIntList("slots")) {
                           if (slot >= 0 && slot < size) {
                              gui.setItem(slot, item);
                           }
                        }
                     } else {
                        int slotx = itemData.getInt("slot", 0);
                        if (slotx >= 0 && slotx < size) {
                           gui.setItem(slotx, item);
                        }
                     }
                  }
               }
            }
         }

         player.openInventory(gui);
      }
   }

   public void openHomesGUI(Player player) {
      YamlDocument config = this.plugin.getGuisConfig();
      Section section = config.getSection("gui.homes-gui");
      if (section != null) {
         UUID uuid = player.getUniqueId();
         YamlDocument playerFile = this.plugin.getHomeManager().getPlayerFile(uuid);
         List<String> rawHomesList = playerFile != null ? playerFile.getStringList("homes") : null;
         if (rawHomesList == null) {
            rawHomesList = new ArrayList<>();
         }

         int currentPage = this.playerPage.getOrDefault(uuid, 1);
         int totalHomes = rawHomesList.size();
         List<Integer> homeSlots = section.getIntList("home-slots");
         int homesPerPage = homeSlots.size();
         int maxPages = (int)Math.ceil((double)totalHomes / homesPerPage);
         if (maxPages == 0) {
            maxPages = 1;
         }

         if (currentPage > maxPages) {
            currentPage = maxPages;
            this.playerPage.put(uuid, maxPages);
         }

         String title = section.getString("title", "Homes").replace("%page%", String.valueOf(currentPage)).replace("%max_page%", String.valueOf(maxPages));
         title = Utils.setPlaceholders(player, title, this.plugin);
         int size = section.getInt("size", 54);
         Inventory gui = Bukkit.createInventory(null, size, title);
         this.renderStaticItems(player, gui, section, currentPage, maxPages);
         Section itemTemplate = section.getSection("items.home-item");
         if (itemTemplate != null && totalHomes > 0) {
            int startIndex = (currentPage - 1) * homesPerPage;

            for (int i = 0; i < homesPerPage; i++) {
               int homeIndex = startIndex + i;
               if (homeIndex >= totalHomes) {
                  break;
               }

               String homeName = rawHomesList.get(homeIndex);
               int slot = homeSlots.get(i);
               ItemStack homeItem = this.createBaseItem(player, itemTemplate);
               ItemMeta meta = homeItem.getItemMeta();
               if (meta != null) {
                  String name = meta.getDisplayName().replace("%home_name%", homeName);
                  meta.setDisplayName(name);
                  if (meta.hasLore()) {
                     List<String> lore = new ArrayList<>();
                     String world = playerFile.getString(homeName + ".world", "world");
                     String x = String.format(Locale.US, "%.2f", playerFile.getDouble(homeName + ".x"));
                     String y = String.format(Locale.US, "%.2f", playerFile.getDouble(homeName + ".y"));
                     String z = String.format(Locale.US, "%.2f", playerFile.getDouble(homeName + ".z"));

                     for (String line : meta.getLore()) {
                        lore.add(line.replace("%home_world%", world).replace("%home_x%", x).replace("%home_y%", y).replace("%home_z%", z));
                     }

                     meta.setLore(lore);
                  }

                  homeItem.setItemMeta(meta);
               }

               gui.setItem(slot, homeItem);
            }
         }

         player.openInventory(gui);
      }
   }

   public void openConfirmationGUI(Player player, String homeName) {
      UUID uuid = player.getUniqueId();
      this.pendingDeletionHome.put(uuid, homeName);
      YamlDocument config = this.plugin.getGuisConfig();
      Section section = config.getSection("gui.confirmation-gui");
      if (section != null) {
         String title = section.getString("title", "Confirm Deletion").replace("%home_name%", homeName);
         title = Utils.setPlaceholders(player, title, this.plugin);
         int size = section.getInt("size", 27);
         Inventory gui = Bukkit.createInventory(null, size, title);
         Section itemsSection = section.getSection("items");
         if (itemsSection != null) {
            for (Object keyObj : itemsSection.getKeys()) {
               String key = String.valueOf(keyObj);
               Section itemData = itemsSection.getSection(key);
               if (itemData != null) {
                  ItemStack item = this.createBaseItem(player, itemData);
                  ItemMeta meta = item.getItemMeta();
                  if (meta != null) {
                     if (meta.hasDisplayName()) {
                        meta.setDisplayName(meta.getDisplayName().replace("%home_name%", homeName));
                     }

                     if (meta.hasLore()) {
                        List<String> lore = new ArrayList<>();

                        for (String line : meta.getLore()) {
                           lore.add(line.replace("%home_name%", homeName));
                        }

                        meta.setLore(lore);
                     }

                     meta.getPersistentDataContainer().set(this.plugin.getActionKey(), PersistentDataType.STRING, itemData.getString("action", key));
                     item.setItemMeta(meta);
                  }

                  if (itemData.contains("slots")) {
                     for (int slot : itemData.getIntList("slots")) {
                        if (slot >= 0 && slot < size) {
                           gui.setItem(slot, item);
                        }
                     }
                  } else if (itemData.contains("slot")) {
                     int slotx = itemData.getInt("slot");
                     if (slotx >= 0 && slotx < size) {
                        gui.setItem(slotx, item);
                     }
                  }
               }
            }
         }

         player.openInventory(gui);
      }
   }

   private void renderStaticItems(Player player, Inventory gui, Section menuSection, int currentPage, int maxPages) {
      Section itemsSection = menuSection.getSection("items");
      if (itemsSection != null) {
         for (Object keyObj : itemsSection.getKeys()) {
            String key = String.valueOf(keyObj);
            if (!key.equalsIgnoreCase("home-item")) {
               Section itemData = itemsSection.getSection(key);
               if (itemData != null) {
                  ItemStack item = this.createBaseItem(player, itemData);
                  ItemMeta meta = item.getItemMeta();
                  if (meta != null) {
                     if (meta.hasDisplayName()) {
                        meta.setDisplayName(
                           meta.getDisplayName().replace("%page%", String.valueOf(currentPage)).replace("%max_page%", String.valueOf(maxPages))
                        );
                     }

                     if (meta.hasLore()) {
                        List<String> lore = new ArrayList<>();

                        for (String line : meta.getLore()) {
                           lore.add(line.replace("%page%", String.valueOf(currentPage)).replace("%max_page%", String.valueOf(maxPages)));
                        }

                        meta.setLore(lore);
                     }

                     meta.getPersistentDataContainer().set(this.plugin.getActionKey(), PersistentDataType.STRING, itemData.getString("action", key));
                     item.setItemMeta(meta);
                  }

                  if (itemData.contains("slots")) {
                     for (int slot : itemData.getIntList("slots")) {
                        if (slot >= 0 && slot < gui.getSize()) {
                           gui.setItem(slot, item);
                        }
                     }
                  } else if (itemData.contains("slot")) {
                     int slotx = itemData.getInt("slot");
                     if (slotx >= 0 && slotx < gui.getSize()) {
                        gui.setItem(slotx, item);
                     }
                  }
               }
            }
         }
      }
   }

   private ItemStack createBaseItem(Player player, Section section) {
      String type = section.getString("type", "STONE").toUpperCase();
      ItemStack item;
      if (type.equals("HEAD")) {
         item = Utils.getHeadFromBase64(section.getString("value", ""));
      } else {
         Material mat = Material.matchMaterial(type);
         item = new ItemStack(mat != null ? mat : Material.BARRIER);
      }

      ItemMeta meta = item.getItemMeta();
      if (meta != null) {
         String rawName = section.getString("display-name", section.getString("name", null));
         if (rawName != null) {
            if (rawName.isEmpty()) {
               meta.setDisplayName(Utils.color("&f "));
            } else {
               meta.setDisplayName(Utils.setPlaceholders(player, rawName, this.plugin));
            }
         }

         List<String> lore = section.getStringList("lore");
         if (lore != null && !lore.isEmpty()) {
            List<String> coloredLore = new ArrayList<>();

            for (String line : lore) {
               coloredLore.add(Utils.setPlaceholders(player, line, this.plugin));
            }

            meta.setLore(coloredLore);
         }

         item.setItemMeta(meta);
      }

      return item;
   }

   public String getMainMenuTitle() {
      return Utils.color(this.plugin.getGuisConfig().getString("gui.main-gui.title", "Menu"));
   }

   public String getHomesMenuTitle() {
      return Utils.color(this.plugin.getGuisConfig().getString("gui.homes-gui.title", "Homes"));
   }

   public String getPendingDeletionHome(UUID uuid) {
      return this.pendingDeletionHome.get(uuid);
   }

   public void clearPendingDeletion(UUID uuid) {
      this.pendingDeletionHome.remove(uuid);
   }

   public void playConfiguredClickSound(Player player, String guiPath) {
      String rawSound = this.plugin.getGuisConfig().getString("gui." + guiPath + ".click-sound");
      if (rawSound != null && !rawSound.isEmpty()) {
         String formattedSound = Utils.formatSoundName(rawSound);
         player.playSound(player.getLocation(), formattedSound, 1.0F, 1.0F);
      }
   }

   public int getPlayerPage(UUID uuid) {
      return this.playerPage.getOrDefault(uuid, 1);
   }

   public void setPlayerPage(UUID uuid, int page) {
      this.playerPage.put(uuid, page);
   }
}
