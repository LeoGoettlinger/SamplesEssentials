package org.sethomegui.Managers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;
import org.sethomegui.SetHomeGUI;
import org.sethomegui.SetHomeGUI.libs.YamlDocument;
import org.sethomegui.SetHomeGUI.libs.block.implementation.Section;
import org.sethomegui.Utils.Utils;

public class AdminGUIManager {
   private final SetHomeGUI plugin;
   private final Map<UUID, Integer> adminPage = new HashMap<>();
   private final Map<UUID, String> searchFilters = new HashMap<>();
   private final List<OfflinePlayer> cachedPlayers = new CopyOnWriteArrayList<>();
   private final Map<UUID, String> cachedNames = new ConcurrentHashMap<>();
   private final Map<UUID, Integer> cachedHomeCounts = new ConcurrentHashMap<>();
   public final NamespacedKey actionKey;
   public final NamespacedKey targetUuidKey;
   public final NamespacedKey targetHomeKey;

   public AdminGUIManager(SetHomeGUI plugin) {
      this.plugin = plugin;
      this.actionKey = new NamespacedKey(plugin, "adm_action");
      this.targetUuidKey = new NamespacedKey(plugin, "adm_target_uuid");
      this.targetHomeKey = new NamespacedKey(plugin, "adm_target_home");
   }

   public void loadPlayersCacheAsync() {
      Bukkit.getAsyncScheduler()
         .runNow(
            this.plugin,
            task -> {
               long startTime = System.currentTimeMillis();
               List<OfflinePlayer> tempPlayers = new ArrayList<>();

               for (UUID pUuid : this.plugin.getStorageManager().listPlayers()) {
                  OfflinePlayer op = Bukkit.getOfflinePlayer(pUuid);
                  tempPlayers.add(op);
                  String name = op.getName();
                  if (name != null) {
                     this.cachedNames.put(pUuid, name.toLowerCase());
                  }
               }

               tempPlayers.sort(Comparator.comparing(opx -> opx.getName() != null ? opx.getName() : ""));
               this.cachedPlayers.clear();
               this.cachedPlayers.addAll(tempPlayers);
               this.cachedHomeCounts.clear();
               this.cachedHomeCounts.putAll(this.plugin.getStorageManager().countHomesByPlayer());
               this.plugin
                  .getLogger()
                  .info("Loaded " + this.cachedPlayers.size() + " admin records into memory cache (" + (System.currentTimeMillis() - startTime) + "ms).");
            }
         );
   }

   public void registerPlayerInCache(OfflinePlayer player) {
      this.cachedHomeCounts.put(player.getUniqueId(), this.plugin.getHomeManager().getHomeCount(player.getUniqueId()));
      if (!this.cachedPlayers.contains(player)) {
         this.cachedPlayers.add(player);
         if (player.getName() != null) {
            this.cachedNames.put(player.getUniqueId(), player.getName().toLowerCase());
         }

         Bukkit.getAsyncScheduler().runNow(this.plugin, task -> this.cachedPlayers.sort(Comparator.comparing(op -> op.getName() != null ? op.getName() : "")));
      }
   }

   public void removePlayerFromCacheIfEmpty(OfflinePlayer player) {
      int remaining = this.plugin.getHomeManager().getHomeCount(player.getUniqueId());
      if (remaining <= 0) {
         this.cachedPlayers.remove(player);
         this.cachedNames.remove(player.getUniqueId());
         this.cachedHomeCounts.remove(player.getUniqueId());
      } else {
         this.cachedHomeCounts.put(player.getUniqueId(), remaining);
      }
   }

   public void openAdminMenu(Player player) {
      UUID uuid = player.getUniqueId();
      YamlDocument config = this.plugin.getGuisConfig();
      Section section = config.getSection("gui.admin-gui");
      if (section != null) {
         String unknownNameConfig = section.getString("unknown-player-name", "&7Unknown");
         int currentPage = this.adminPage.getOrDefault(uuid, 1);
         String filter = this.searchFilters.getOrDefault(uuid, "").toLowerCase();
         List<OfflinePlayer> filteredPlayers = new ArrayList<>();

         for (OfflinePlayer op : this.cachedPlayers) {
            if (!filter.isEmpty()) {
               String name = this.cachedNames.get(op.getUniqueId());
               if (name == null || !name.contains(filter)) {
                  continue;
               }
            }

            filteredPlayers.add(op);
         }

         List<Integer> slots = section.getIntList("slots");
         int itemsPerPage = slots.size();
         if (itemsPerPage != 0) {
            int maxPages = (int)Math.ceil((double)filteredPlayers.size() / itemsPerPage);
            if (maxPages == 0) {
               maxPages = 1;
            }

            if (currentPage > maxPages) {
               currentPage = maxPages;
               this.adminPage.put(uuid, maxPages);
            }

            String titleStr = section.getString("title", "Admin Menu")
               .replace("%page%", String.valueOf(currentPage))
               .replace("%max_page%", String.valueOf(maxPages));
            int size = section.getInt("size", 54);
            Inventory gui;
            if (player.getOpenInventory().getTopInventory().getHolder() instanceof AdminMainHolder
               && player.getOpenInventory().getTopInventory().getSize() == size) {
               gui = player.getOpenInventory().getTopInventory();
               gui.clear();
            } else {
               gui = Bukkit.createInventory(new AdminMainHolder(), size, Utils.color(titleStr));
            }

            Section itemsSec = section.getSection("items");
            if (itemsSec != null) {
               for (Object keyObj : itemsSec.getKeys()) {
                  String key = String.valueOf(keyObj);
                  if (!key.equalsIgnoreCase("player-head")) {
                     Section itemData = itemsSec.getSection(key);
                     if (itemData != null) {
                        ItemStack item;
                        if (itemData.contains("value")) {
                           item = Utils.getHeadFromBase64(itemData.getString("value"));
                        } else {
                           Material mat = Material.matchMaterial(itemData.getString("type", "STONE"));
                           item = new ItemStack(mat != null ? mat : Material.STONE);
                        }

                        ItemMeta meta = item.getItemMeta();
                        if (meta != null) {
                           meta.setDisplayName(Utils.color(itemData.getString("display-name", "")));
                           if (itemData.contains("lore")) {
                              List<String> lore = new ArrayList<>();

                              for (String line : itemData.getStringList("lore")) {
                                 lore.add(Utils.color(line));
                              }

                              meta.setLore(lore);
                           }

                           if (key.equalsIgnoreCase("search-button")) {
                              meta.getPersistentDataContainer().set(this.actionKey, PersistentDataType.STRING, "search");
                           } else if (key.equalsIgnoreCase("previous-page")) {
                              meta.getPersistentDataContainer().set(this.actionKey, PersistentDataType.STRING, "prev_page");
                           } else if (key.equalsIgnoreCase("next-page")) {
                              meta.getPersistentDataContainer().set(this.actionKey, PersistentDataType.STRING, "next_page");
                           }

                           item.setItemMeta(meta);
                        }

                        if (itemData.contains("slots")) {
                           for (int s : itemData.getIntList("slots")) {
                              gui.setItem(s, item);
                           }
                        } else if (itemData.contains("slot")) {
                           gui.setItem(itemData.getInt("slot"), item);
                        }
                     }
                  }
               }
            }

            Section headTemplate = section.getSection("items.player-head");
            if (headTemplate != null && !filteredPlayers.isEmpty()) {
               int startIndex = (currentPage - 1) * itemsPerPage;

               for (int i = 0; i < itemsPerPage; i++) {
                  int globalIndex = startIndex + i;
                  if (globalIndex >= filteredPlayers.size()) {
                     break;
                  }

                  OfflinePlayer target = filteredPlayers.get(globalIndex);
                  int headSlot = slots.get(i);
                  ItemStack head = Utils.getPlayerHead(target.getUniqueId());
                  ItemMeta metax = head.getItemMeta();
                  if (metax != null) {
                     String pName = target.getName() != null ? target.getName() : Utils.color(unknownNameConfig);
                     metax.setDisplayName(Utils.color(headTemplate.getString("display-name", "").replace("%player_name%", pName)));
                     Integer totalHomes = this.cachedHomeCounts.get(target.getUniqueId());
                     if (totalHomes == null) {
                        totalHomes = this.plugin.getHomeManager().getHomeCount(target.getUniqueId());
                        this.cachedHomeCounts.put(target.getUniqueId(), totalHomes);
                     }

                     List<String> lore = new ArrayList<>();

                     for (String line : headTemplate.getStringList("lore")) {
                        lore.add(
                           Utils.color(line.replace("%player_uuid%", target.getUniqueId().toString()).replace("%current_homes%", String.valueOf(totalHomes)))
                        );
                     }

                     metax.setLore(lore);
                     metax.getPersistentDataContainer().set(this.actionKey, PersistentDataType.STRING, "open_player");
                     metax.getPersistentDataContainer().set(this.targetUuidKey, PersistentDataType.STRING, target.getUniqueId().toString());
                     head.setItemMeta(metax);
                  }

                  gui.setItem(headSlot, head);
               }
            }

            if (player.getOpenInventory().getTopInventory() != gui) {
               player.openInventory(gui);
            }
         }
      }
   }

   public void openAdminPlayerHomesMenu(Player admin, UUID targetUuid, int page) {
      Section section = this.plugin.getGuisConfig().getSection("gui.admin-gui.admin-player-homes-gui");
      if (section != null) {
         Bukkit.getAsyncScheduler()
            .runNow(
               this.plugin,
               loadTask -> {
                  YamlDocument loaded = this.plugin.getHomeManager().getPlayerFile(targetUuid);
                  if (loaded != null && !loaded.getStringList("homes").isEmpty()) {
                     this.renderAdminPlayerHomesMenu(admin, targetUuid, page, section, loaded);
                  } else {
                     String noHomesMsg = this.plugin
                        .getMainConfig()
                        .getString("messages.admin.no-homes-found", "&cThis player does not have any homes configurations.");
                     admin.sendMessage(Utils.color(noHomesMsg));
                  }
               }
            );
      }
   }

   private void renderAdminPlayerHomesMenu(Player admin, UUID targetUuid, int page, Section section, YamlDocument targetData) {
      admin.getScheduler()
         .run(
            this.plugin,
            task -> {
               try {
                  List<String> homeNames = targetData.getStringList("homes");
                  if (homeNames == null) {
                     homeNames = new ArrayList<>();
                  }

                  List<Integer> slots = section.getIntList("home-slots");
                  int itemsPerPage = slots.size();
                  if (itemsPerPage == 0) {
                     return;
                  }

                  int maxPage = (int)Math.ceil((double)homeNames.size() / itemsPerPage);
                  if (maxPage == 0) {
                     maxPage = 1;
                  }

                  int currentPage = Math.min(page, maxPage);
                  this.setPage(admin.getUniqueId(), currentPage);
                  String targetName = Bukkit.getOfflinePlayer(targetUuid).getName();
                  if (targetName == null) {
                     targetName = "Unknown";
                  }

                  String titleStr = section.getString("title", "&8Homes of %player%")
                     .replace("%player%", targetName)
                     .replace("%page%", String.valueOf(currentPage))
                     .replace("%max_page%", String.valueOf(maxPage));
                  int size = section.getInt("size", 54);
                  Inventory gui;
                  if (admin.getOpenInventory().getTopInventory().getHolder() instanceof AdminHomesHolder
                     && admin.getOpenInventory().getTopInventory().getSize() == size) {
                     gui = admin.getOpenInventory().getTopInventory();
                     gui.clear();
                  } else {
                     gui = Bukkit.createInventory(new AdminHomesHolder(targetUuid), size, Utils.color(titleStr));
                  }

                  Section itemsSec = section.getSection("items");
                  if (itemsSec != null) {
                     for (Object itemKey : itemsSec.getKeys()) {
                        String id = String.valueOf(itemKey);
                        if (!id.equals("home-item")) {
                           Section itemSec = section.getSection("items." + id);
                           if (itemSec != null) {
                              ItemStack item;
                              if (itemSec.contains("value")) {
                                 item = Utils.getHeadFromBase64(itemSec.getString("value"));
                              } else {
                                 Material mat = Material.matchMaterial(itemSec.getString("type", "BARRIER"));
                                 item = new ItemStack(mat != null ? mat : Material.BARRIER);
                              }

                              ItemMeta meta = item.getItemMeta();
                              if (meta != null) {
                                 meta.setDisplayName(Utils.color(itemSec.getString("display-name", "")));
                                 List<String> lore = new ArrayList<>();

                                 for (String line : itemSec.getStringList("lore")) {
                                    lore.add(Utils.color(line));
                                 }

                                 meta.setLore(lore);
                                 if (id.equalsIgnoreCase("back")) {
                                    meta.getPersistentDataContainer().set(this.actionKey, PersistentDataType.STRING, "go_back");
                                 } else if (id.equalsIgnoreCase("previous-page")) {
                                    meta.getPersistentDataContainer().set(this.actionKey, PersistentDataType.STRING, "homes_prev");
                                 } else if (id.equalsIgnoreCase("next-page")) {
                                    meta.getPersistentDataContainer().set(this.actionKey, PersistentDataType.STRING, "homes_next");
                                 }

                                 item.setItemMeta(meta);
                              }

                              if (itemSec.contains("slot")) {
                                 gui.setItem(itemSec.getInt("slot"), item);
                              } else if (itemSec.contains("slots")) {
                                 for (int s : itemSec.getIntList("slots")) {
                                    gui.setItem(s, item);
                                 }
                              }
                           }
                        }
                     }
                  }

                  Section homeTemplate = section.getSection("items.home-item");
                  if (homeTemplate != null && !homeNames.isEmpty()) {
                     int startIndex = (currentPage - 1) * itemsPerPage;

                     for (int i = 0; i < itemsPerPage; i++) {
                        int globalIndex = startIndex + i;
                        if (globalIndex >= homeNames.size()) {
                           break;
                        }

                        String homeName = homeNames.get(globalIndex);
                        Section homeData = targetData.getSection(homeName);
                        if (homeData != null) {
                           Material mat = Material.matchMaterial(homeTemplate.getString("type", "CYAN_BED"));
                           ItemStack homeItem = new ItemStack(mat != null ? mat : Material.CYAN_BED);
                           ItemMeta metax = homeItem.getItemMeta();
                           if (metax != null) {
                              metax.setDisplayName(Utils.color(homeTemplate.getString("display-name", "").replace("%home_name%", homeName)));
                              List<String> lore = new ArrayList<>();

                              for (String line : homeTemplate.getStringList("lore")) {
                                 String formatted = line.replace("%home_world%", homeData.getString("world", "world"))
                                    .replace("%home_x%", String.format(Locale.US, "%.1f", homeData.getDouble("x", 0.0)))
                                    .replace("%home_y%", String.format(Locale.US, "%.1f", homeData.getDouble("y", 0.0)))
                                    .replace("%home_z%", String.format(Locale.US, "%.1f", homeData.getDouble("z", 0.0)));
                                 lore.add(Utils.color(formatted));
                              }

                              metax.setLore(lore);
                              metax.getPersistentDataContainer().set(this.actionKey, PersistentDataType.STRING, "interact_home");
                              metax.getPersistentDataContainer().set(this.targetUuidKey, PersistentDataType.STRING, targetUuid.toString());
                              metax.getPersistentDataContainer().set(this.targetHomeKey, PersistentDataType.STRING, homeName);
                              homeItem.setItemMeta(metax);
                           }

                           gui.setItem(slots.get(i), homeItem);
                        }
                     }
                  }

                  if (admin.getOpenInventory().getTopInventory() != gui) {
                     admin.openInventory(gui);
                  }
               } catch (Exception var30) {
                  String recordErrorMsg = this.plugin.getMainConfig().getString("messages.admin.player-record-error", "&cError processing player record.");
                  admin.sendMessage(Utils.color(recordErrorMsg));
                  var30.printStackTrace();
               }
            },
            null
         );
   }

   public void openAdminConfirmationMenu(Player admin, UUID targetUuid, String homeName) {
      Section section = this.plugin.getGuisConfig().getSection("gui.admin-gui.admin-confirmation-gui");
      if (section != null) {
         admin.getScheduler()
            .run(
               this.plugin,
               task -> {
                  String titleStr = section.getString("title", "Confirm").replace("%home_name%", homeName);
                  int size = section.getInt("size", 27);
                  Inventory gui;
                  if (admin.getOpenInventory().getTopInventory().getHolder() instanceof AdminConfirmHolder
                     && admin.getOpenInventory().getTopInventory().getSize() == size) {
                     gui = admin.getOpenInventory().getTopInventory();
                     gui.clear();
                  } else {
                     gui = Bukkit.createInventory(new AdminConfirmHolder(targetUuid, homeName), size, Utils.color(titleStr));
                  }

                  Section itemsSec = section.getSection("items");
                  if (itemsSec != null) {
                     for (Object itemKey : itemsSec.getKeys()) {
                        String id = String.valueOf(itemKey);
                        Section itemSec = section.getSection("items." + id);
                        if (itemSec != null) {
                           Material mat = Material.matchMaterial(itemSec.getString("type", "BARRIER"));
                           ItemStack item = new ItemStack(mat != null ? mat : Material.BARRIER);
                           ItemMeta meta = item.getItemMeta();
                           if (meta != null) {
                              meta.setDisplayName(Utils.color(itemSec.getString("display-name", "").replace("%home_name%", homeName)));
                              List<String> lore = new ArrayList<>();

                              for (String line : itemSec.getStringList("lore")) {
                                 lore.add(Utils.color(line.replace("%home_name%", homeName)));
                              }

                              meta.setLore(lore);
                              if (id.equalsIgnoreCase("confirm-button")) {
                                 meta.getPersistentDataContainer().set(this.actionKey, PersistentDataType.STRING, "confirm_wipe");
                              } else if (id.equalsIgnoreCase("cancel-button")) {
                                 meta.getPersistentDataContainer().set(this.actionKey, PersistentDataType.STRING, "cancel_wipe");
                              }

                              item.setItemMeta(meta);
                           }

                           if (itemSec.contains("slot")) {
                              gui.setItem(itemSec.getInt("slot"), item);
                           } else if (itemSec.contains("slots")) {
                              for (int s : itemSec.getIntList("slots")) {
                                 gui.setItem(s, item);
                              }
                           }
                        }
                     }
                  }

                  if (admin.getOpenInventory().getTopInventory() != gui) {
                     admin.openInventory(gui);
                  }
               },
               null
            );
      }
   }

   public void openSearchChat(Player player) {
      player.closeInventory();

      for (String line : this.plugin.getConfig().getStringList("messages.admin.search-prompt")) {
         player.sendMessage(Utils.color(line));
      }

      player.setMetadata("admin_search_mode", new FixedMetadataValue(this.plugin, true));
   }

   public int getPage(UUID uuid) {
      return this.adminPage.getOrDefault(uuid, 1);
   }

   public void setPage(UUID uuid, int page) {
      this.adminPage.put(uuid, page);
   }

   public void setSearchFilter(UUID uuid, String filter) {
      this.searchFilters.put(uuid, filter);
   }
}
