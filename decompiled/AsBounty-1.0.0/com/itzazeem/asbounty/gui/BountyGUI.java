package com.itzazeem.asbounty.gui;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.itzazeem.asbounty.AsBounty;
import com.itzazeem.asbounty.manager.SignManager;
import com.itzazeem.asbounty.utils.MessageUtils;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class BountyGUI implements Listener {
   private final AsBounty plugin;
   private final Player player;
   private final SignManager signManager;
   private BountyGUI.BountyInventoryHolder holder;
   private Inventory inventory;
   private String currentSort = "highest_price";
   private int currentPage = 0;
   private String searchFilter = null;
   private boolean isSearchMode = false;
   private boolean isClosed = false;
   private boolean isReloading = false;
   private long lastClickTime = 0L;
   private boolean isProcessing = false;
   private static final int ITEMS_PER_PAGE = 45;
   private static final Map<UUID, BountyGUI> openGUIs = new ConcurrentHashMap<>();

   public BountyGUI(AsBounty plugin, Player player, SignManager signManager) {
      this.plugin = plugin;
      this.player = player;
      this.signManager = signManager;
      plugin.getServer().getPluginManager().registerEvents(this, plugin);
   }

   public void open() {
      this.openPage(0);
   }

   public void openWithSearch(String search) {
      this.searchFilter = search;
      this.isSearchMode = true;
      this.currentPage = 0;
      this.openPage(0);
   }

   public void clearSearch() {
      this.searchFilter = null;
      this.isSearchMode = false;
      this.currentPage = 0;
      this.openPage(0);
   }

   private String formatBountyAmount(double amount) {
      boolean abbreviationsEnabled = this.plugin.getConfig().getBoolean("economy.abbreviations.enabled", true);
      if (!abbreviationsEnabled) {
         return MessageUtils.formatAmount(amount);
      } else {
         List<String> formats = this.plugin.getConfig().getStringList("economy.abbreviations.formats");
         if (amount >= 1.0E12 && formats.contains("T")) {
            return String.format("%.1fT", amount / 1.0E12);
         } else if (amount >= 1.0E9 && formats.contains("B")) {
            return String.format("%.1fB", amount / 1.0E9);
         } else if (amount >= 1000000.0 && formats.contains("M")) {
            return String.format("%.1fM", amount / 1000000.0);
         } else {
            return amount >= 1000.0 && formats.contains("K") ? String.format("%.1fK", amount / 1000.0) : MessageUtils.formatAmount(amount);
         }
      }
   }

   private int getOpenBountiesCount() {
      return this.plugin.getBountyManager().getAllBountiesSorted("newest").size();
   }

   private int getAllTimeBountiesCount() {
      try {
         int var3;
         try (
            PreparedStatement stmt = this.plugin.getDatabaseConnection().prepareStatement("SELECT COUNT(*) AS total FROM bounties");
            ResultSet rs = stmt.executeQuery();
         ) {
            if (!rs.next()) {
               return 0;
            }

            var3 = rs.getInt("total");
         }

         return var3;
      } catch (SQLException var9) {
         var9.printStackTrace();
         return 0;
      }
   }

   private void openPage(int page) {
      if (!this.isClosed) {
         this.currentPage = page;
         List<Entry<UUID, Double>> allBounties = this.plugin.getBountyManager().getAllBountiesSorted(this.currentSort);
         List<Entry<UUID, Double>> bounties;
         if (this.isSearchMode && this.searchFilter != null && !this.searchFilter.isEmpty()) {
            bounties = allBounties.stream().filter(entryx -> {
               String name = this.plugin.getHeadCacheManager().getPlayerNameSync((UUID)entryx.getKey());
               return name != null && name.toLowerCase().contains(this.searchFilter.toLowerCase());
            }).toList();
         } else {
            bounties = allBounties;
         }

         int totalPages = (int)Math.ceil(bounties.size() / 45.0);
         if (totalPages == 0) {
            totalPages = 1;
         }

         String title = this.plugin
            .getConfigManager()
            .getMainGuiConfig()
            .getString("gui-main.title", "&8AsBounty (Page %page%/%max%)")
            .replace("%page%", String.valueOf(page + 1))
            .replace("%max%", String.valueOf(totalPages));
         title = MessageUtils.colorize(title);
         int rows = this.plugin.getConfigManager().getMainGuiConfig().getInt("gui-main.rows", 6);
         this.holder = new BountyGUI.BountyInventoryHolder(this);
         Inventory newInventory = Bukkit.createInventory(this.holder, rows * 9, title);
         int start = page * 45;
         int end = Math.min(start + 45, bounties.size());
         int slot = 0;

         for (int i = start; i < end; i++) {
            Entry<UUID, Double> entry = bounties.get(i);
            UUID uuid = entry.getKey();
            double bounty = entry.getValue();
            this.loadPlayerHeadAsync(newInventory, uuid, slot, bounty);
            slot++;
         }

         this.addNavigationItems(newInventory);
         this.inventory = newInventory;
         openGUIs.put(this.player.getUniqueId(), this);
         this.isProcessing = false;
         this.isReloading = true;
         this.openInventoryLater();
      }
   }

   private void loadPlayerHeadAsync(Inventory inv, UUID uuid, int slot, double bounty) {
      this.plugin.getHeadCacheManager().getPlayerName(uuid).thenAccept(playerName -> {
         if (playerName != null && !this.isClosed) {
            this.plugin.getHeadCacheManager().getOrFetchHeadBase64(uuid, playerName).thenAccept(base64 -> {
               if (!this.isClosed) {
                  ItemStack head = this.createPlayerHead(uuid, playerName, bounty, base64);
                  if (head != null) {
                     inv.setItem(slot, head);
                  }
               }
            });
         }
      });
   }

   private ItemStack createPlayerHead(UUID uuid, String name, double bounty, String base64) {
      ItemStack head = new ItemStack(Material.PLAYER_HEAD);
      SkullMeta meta = (SkullMeta)head.getItemMeta();
      if (base64 != null && !base64.isEmpty()) {
         try {
            PlayerProfile profile = Bukkit.createProfile(uuid, name);
            profile.setProperty(new ProfileProperty("textures", base64));
            meta.setPlayerProfile(profile);
         } catch (Exception var12) {
            Player target = Bukkit.getPlayer(uuid);
            if (target != null) {
               meta.setOwningPlayer(target);
            } else {
               meta.setOwningPlayer(Bukkit.getOfflinePlayer(uuid));
            }
         }
      } else {
         Player target = Bukkit.getPlayer(uuid);
         if (target != null && target.isOnline()) {
            meta.setOwningPlayer(target);
         } else {
            meta.setOwningPlayer(Bukkit.getOfflinePlayer(uuid));
         }
      }

      meta.setDisplayName(
         MessageUtils.colorize(
            this.plugin.getConfigManager().getMainGuiConfig().getString("gui-main.items.bounty-head.name", "&#00fc88%name%").replace("%name%", name)
         )
      );
      List<String> lore = new ArrayList<>();

      for (String line : this.plugin.getConfigManager().getMainGuiConfig().getStringList("gui-main.items.bounty-head.lore")) {
         String formattedBounty = this.formatBountyAmount(bounty);
         lore.add(MessageUtils.colorize(line.replace("%bounty%", formattedBounty)));
      }

      meta.setLore(lore);
      head.setItemMeta(meta);
      return head;
   }

   private void addNavigationItems(Inventory inv) {
      int searchSlot = this.plugin.getConfigManager().getMainGuiConfig().getInt("gui-main.items.search.slot", 50);
      inv.setItem(searchSlot, this.createGuiItem("search"));
      int refreshSlot = this.plugin.getConfigManager().getMainGuiConfig().getInt("gui-main.items.refresh.slot", 49);
      inv.setItem(refreshSlot, this.createGuiItem("refresh"));
      int sortSlot = this.plugin.getConfigManager().getMainGuiConfig().getInt("gui-main.items.sort.slot", 48);
      inv.setItem(sortSlot, this.createSortItem());
      int prevSlot = this.plugin.getConfigManager().getMainGuiConfig().getInt("gui-main.items.previous-page.slot", 45);
      inv.setItem(prevSlot, this.createGuiItem("previous-page"));
      int nextSlot = this.plugin.getConfigManager().getMainGuiConfig().getInt("gui-main.items.next-page.slot", 53);
      inv.setItem(nextSlot, this.createGuiItem("next-page"));
   }

   private ItemStack createGuiItem(String key) {
      String path = "gui-main.items." + key;
      Material material = Material.getMaterial(this.plugin.getConfigManager().getMainGuiConfig().getString(path + ".material", "STONE"));
      if (material == null) {
         material = Material.STONE;
      }

      ItemStack item = new ItemStack(material);
      ItemMeta meta = item.getItemMeta();
      meta.setDisplayName(MessageUtils.colorize(this.plugin.getConfigManager().getMainGuiConfig().getString(path + ".name", "&fItem")));
      int openBounties = this.getOpenBountiesCount();
      int allTimeBounties = this.getAllTimeBountiesCount();
      List<String> lore = new ArrayList<>();

      for (String line : this.plugin.getConfigManager().getMainGuiConfig().getStringList(path + ".lore")) {
         String replaced = line.replace("%bountys-now%", String.valueOf(openBounties)).replace("%bountys-alltime%", String.valueOf(allTimeBounties));
         lore.add(MessageUtils.colorize(replaced));
      }

      meta.setLore(lore);
      item.setItemMeta(meta);
      return item;
   }

   private ItemStack createSortItem() {
      String path = "gui-main.items.sort";
      Material material = Material.getMaterial(this.plugin.getConfigManager().getMainGuiConfig().getString(path + ".material", "HOPPER"));
      if (material == null) {
         material = Material.HOPPER;
      }

      ItemStack item = new ItemStack(material);
      ItemMeta meta = item.getItemMeta();
      List<String> keys = this.plugin.getConfigManager().getMainGuiConfig().getStringList(path + ".keys");
      List<String> options = this.plugin.getConfigManager().getMainGuiConfig().getStringList(path + ".options");
      String activePrefix = this.plugin.getConfigManager().getMainGuiConfig().getString(path + ".active_prefix", "&#00fc88● ");
      String inactivePrefix = this.plugin.getConfigManager().getMainGuiConfig().getString(path + ".inactive_prefix", "&f● ");
      meta.setDisplayName(MessageUtils.colorize(this.plugin.getConfigManager().getMainGuiConfig().getString(path + ".name", "&fSort")));
      List<String> lore = new ArrayList<>();

      for (String line : this.plugin.getConfigManager().getMainGuiConfig().getStringList(path + ".lore")) {
         lore.add(MessageUtils.colorize(line));
      }

      for (int i = 0; i < keys.size() && i < options.size(); i++) {
         String prefix = keys.get(i).equals(this.currentSort) ? activePrefix : inactivePrefix;
         lore.add(MessageUtils.colorize(prefix + options.get(i)));
      }

      meta.setLore(lore);
      item.setItemMeta(meta);
      return item;
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = false
   )
   public void onInventoryClick(InventoryClickEvent event) {
      if (event.getWhoClicked() instanceof Player clickedPlayer) {
         Inventory topInventory = event.getView().getTopInventory();
         Inventory clickedInventory = event.getClickedInventory();
         BountyGUI registeredGui = openGUIs.get(clickedPlayer.getUniqueId());
         boolean isOurTopInventory = topInventory.getHolder() instanceof BountyGUI.BountyInventoryHolder;
         boolean isOurClickedInventory = clickedInventory != null && clickedInventory.getHolder() instanceof BountyGUI.BountyInventoryHolder;
         boolean isOurGuiByMap = registeredGui != null && registeredGui.inventory != null && topInventory.equals(registeredGui.inventory);
         if (isOurTopInventory || isOurClickedInventory || isOurGuiByMap) {
            event.setCancelled(true);
            ItemStack cursor = clickedPlayer.getItemOnCursor();
            if (cursor != null && !cursor.getType().isAir()) {
               clickedPlayer.setItemOnCursor(null);
            }

            if (isOurClickedInventory || isOurGuiByMap) {
               BountyGUI gui;
               if (isOurClickedInventory) {
                  BountyGUI.BountyInventoryHolder bHolder = (BountyGUI.BountyInventoryHolder)clickedInventory.getHolder();
                  gui = bHolder.gui;
               } else {
                  gui = registeredGui;
               }

               if (gui != null && gui.player == clickedPlayer && !gui.isClosed && !event.isShiftClick() && !event.isRightClick()) {
                  ClickType clickType = event.getClick();
                  if (clickType != ClickType.NUMBER_KEY
                     && clickType != ClickType.DOUBLE_CLICK
                     && clickType != ClickType.CREATIVE
                     && clickType != ClickType.SWAP_OFFHAND
                     && clickType != ClickType.CONTROL_DROP
                     && clickType != ClickType.DROP
                     && !gui.isProcessing) {
                     ItemStack currentItem = event.getCurrentItem();
                     if (currentItem != null && currentItem.getType() != Material.AIR) {
                        long currentTime = System.currentTimeMillis();
                        int slot = event.getSlot();
                        int sortSlot = this.plugin.getConfigManager().getMainGuiConfig().getInt("gui-main.items.sort.slot", 48);
                        if (slot == sortSlot) {
                           if (currentTime - this.lastClickTime >= 300L) {
                              this.lastClickTime = currentTime;
                              gui.isProcessing = true;
                              List<String> keys = this.plugin.getConfigManager().getMainGuiConfig().getStringList("gui-main.items.sort.keys");
                              int currentIndex = keys.indexOf(gui.currentSort);
                              if (currentIndex + 1 < keys.size()) {
                                 gui.currentSort = keys.get(currentIndex + 1);
                              } else {
                                 gui.currentSort = keys.get(0);
                              }

                              gui.reloadGUI();
                              this.plugin.getSoundManager().playSound(clickedPlayer, "gui-click");
                              gui.isProcessing = false;
                           }
                        } else {
                           int refreshSlot = this.plugin.getConfigManager().getMainGuiConfig().getInt("gui-main.items.refresh.slot", 49);
                           if (slot == refreshSlot) {
                              if (currentTime - this.lastClickTime >= 300L) {
                                 this.lastClickTime = currentTime;
                                 gui.isProcessing = true;
                                 gui.reloadGUI();
                                 this.plugin.getSoundManager().playSound(clickedPlayer, "gui-click");
                                 gui.isProcessing = false;
                              }
                           } else {
                              int prevSlot = this.plugin.getConfigManager().getMainGuiConfig().getInt("gui-main.items.previous-page.slot", 45);
                              if (slot == prevSlot) {
                                 if (currentTime - this.lastClickTime >= 300L) {
                                    this.lastClickTime = currentTime;
                                    gui.isProcessing = true;
                                    this.plugin.getSoundManager().playSound(clickedPlayer, "gui-click");
                                    if (gui.currentPage > 0) {
                                       gui.openPage(gui.currentPage - 1);
                                    }

                                    gui.isProcessing = false;
                                 }
                              } else {
                                 int nextSlot = this.plugin.getConfigManager().getMainGuiConfig().getInt("gui-main.items.next-page.slot", 53);
                                 if (slot == nextSlot) {
                                    if (currentTime - this.lastClickTime >= 300L) {
                                       this.lastClickTime = currentTime;
                                       gui.isProcessing = true;
                                       this.plugin.getSoundManager().playSound(clickedPlayer, "gui-click");
                                       List<Entry<UUID, Double>> allBounties = this.plugin.getBountyManager().getAllBountiesSorted(gui.currentSort);
                                       List<Entry<UUID, Double>> bounties;
                                       if (gui.isSearchMode && gui.searchFilter != null && !gui.searchFilter.isEmpty()) {
                                          bounties = allBounties.stream().filter(bEntry -> {
                                             String name = this.plugin.getHeadCacheManager().getPlayerNameSync(bEntry.getKey());
                                             return name != null && name.toLowerCase().contains(gui.searchFilter.toLowerCase());
                                          }).toList();
                                       } else {
                                          bounties = allBounties;
                                       }

                                       int totalPages = (int)Math.ceil(bounties.size() / 45.0);
                                       if (totalPages == 0) {
                                          totalPages = 1;
                                       }

                                       if (gui.currentPage < totalPages - 1) {
                                          gui.openPage(gui.currentPage + 1);
                                       }

                                       gui.isProcessing = false;
                                    }
                                 } else {
                                    int searchSlot = this.plugin.getConfigManager().getMainGuiConfig().getInt("gui-main.items.search.slot", 50);
                                    if (slot == searchSlot) {
                                       if (currentTime - this.lastClickTime >= 300L) {
                                          this.lastClickTime = currentTime;
                                          this.plugin.getSoundManager().playSound(clickedPlayer, "gui-click");
                                          clickedPlayer.closeInventory();
                                          if (this.signManager != null && this.plugin.getConfig().getBoolean("chat-input.SignAPI", true)) {
                                             this.signManager.openSignEditor(clickedPlayer, (player1, input) -> {
                                                if (input != null && !input.isEmpty()) {
                                                   new BountyGUI(this.plugin, player1, this.signManager).openWithSearch(input);
                                                } else {
                                                   new BountyGUI(this.plugin, player1, this.signManager).open();
                                                }
                                             });
                                          } else {
                                             MessageUtils.sendMessageWithPrefix(
                                                clickedPlayer,
                                                this.plugin.getConfigManager().getMessage("search-prompt"),
                                                this.plugin.getConfigManager().isPrefixEnabled(),
                                                this.plugin.getConfigManager().getPrefix()
                                             );
                                          }
                                       }
                                    } else if (slot < 45) {
                                       ItemStack item = event.getCurrentItem();
                                       if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                                          this.plugin.getSoundManager().playSound(clickedPlayer, "gui-click");
                                       }
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private void reloadGUI() {
      if (!this.isClosed) {
         int currentPageCopy = this.currentPage;
         String currentSortCopy = this.currentSort;
         List<Entry<UUID, Double>> allBounties = this.plugin.getBountyManager().getAllBountiesSorted(currentSortCopy);
         List<Entry<UUID, Double>> bounties;
         if (this.isSearchMode && this.searchFilter != null && !this.searchFilter.isEmpty()) {
            bounties = allBounties.stream().filter(entryx -> {
               String name = this.plugin.getHeadCacheManager().getPlayerNameSync((UUID)entryx.getKey());
               return name != null && name.toLowerCase().contains(this.searchFilter.toLowerCase());
            }).toList();
         } else {
            bounties = allBounties;
         }

         int totalPages = (int)Math.ceil(bounties.size() / 45.0);
         if (totalPages == 0) {
            totalPages = 1;
         }

         String title = this.plugin
            .getConfigManager()
            .getMainGuiConfig()
            .getString("gui-main.title", "&8AsBounty (Page %page%/%max%)")
            .replace("%page%", String.valueOf(currentPageCopy + 1))
            .replace("%max%", String.valueOf(totalPages));
         title = MessageUtils.colorize(title);
         int rows = this.plugin.getConfigManager().getMainGuiConfig().getInt("gui-main.rows", 6);
         this.holder = new BountyGUI.BountyInventoryHolder(this);
         Inventory newInventory = Bukkit.createInventory(this.holder, rows * 9, title);
         int start = currentPageCopy * 45;
         int end = Math.min(start + 45, bounties.size());
         int slot = 0;

         for (int i = start; i < end; i++) {
            Entry<UUID, Double> entry = bounties.get(i);
            UUID uuid = entry.getKey();
            double bounty = entry.getValue();
            this.loadPlayerHeadAsync(newInventory, uuid, slot, bounty);
            slot++;
         }

         this.addNavigationItems(newInventory);
         this.inventory = newInventory;
         openGUIs.put(this.player.getUniqueId(), this);
         this.isReloading = true;
         this.openInventoryLater();
      }
   }

   private void openInventoryLater() {
      try {
         if (this.player.getScheduler() != null) {
            this.player.getScheduler().run(this.plugin, scheduledTask -> {
               if (!this.isClosed && this.player.isOnline()) {
                  this.player.openInventory(this.inventory);
               }

               this.isReloading = false;
            }, null);
            return;
         }
      } catch (Throwable var2) {
      }

      try {
         Bukkit.getScheduler().runTask(this.plugin, () -> {
            if (!this.isClosed && this.player.isOnline()) {
               this.player.openInventory(this.inventory);
            }

            this.isReloading = false;
         });
      } catch (UnsupportedOperationException var3) {
         if (!this.isClosed && this.player.isOnline()) {
            this.player.openInventory(this.inventory);
         }

         this.isReloading = false;
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = false
   )
   public void onInventoryDrag(InventoryDragEvent event) {
      if (event.getWhoClicked() instanceof Player clickedPlayer) {
         Inventory topInventory = event.getView().getTopInventory();
         boolean isOurTopInventory = topInventory.getHolder() instanceof BountyGUI.BountyInventoryHolder;
         int topSize = topInventory.getSize();
         boolean dragsIntoTargetInventory = false;

         for (int slot : event.getRawSlots()) {
            if (slot < topSize) {
               dragsIntoTargetInventory = true;
               break;
            }
         }

         if (isOurTopInventory || dragsIntoTargetInventory) {
            event.setCancelled(true);
            ItemStack cursor = clickedPlayer.getItemOnCursor();
            if (cursor != null && !cursor.getType().isAir()) {
               clickedPlayer.setItemOnCursor(null);
            }
         }
      }
   }

   @EventHandler
   public void onInventoryClose(InventoryCloseEvent event) {
      Inventory inv = event.getInventory();
      if (inv.getHolder() instanceof BountyGUI.BountyInventoryHolder holder) {
         BountyGUI gui = holder.gui;
         if (gui != null && !gui.isReloading) {
            gui.isClosed = true;
            openGUIs.remove(event.getPlayer().getUniqueId());
            HandlerList.unregisterAll(gui);
         }
      }
   }

   private static class BountyInventoryHolder implements InventoryHolder {
      private final BountyGUI gui;

      BountyInventoryHolder(BountyGUI gui) {
         this.gui = gui;
      }

      public Inventory getInventory() {
         return this.gui.inventory;
      }
   }
}
