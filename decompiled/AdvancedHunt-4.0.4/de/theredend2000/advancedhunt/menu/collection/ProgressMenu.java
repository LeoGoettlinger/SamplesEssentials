package de.theredend2000.advancedhunt.menu.collection;

import de.theredend2000.advancedhunt.Main;
import de.theredend2000.advancedhunt.lib.xseries.XMaterial;
import de.theredend2000.advancedhunt.menu.PagedMenu;
import de.theredend2000.advancedhunt.menu.common.SkullInfo;
import de.theredend2000.advancedhunt.menu.common.TreasureMenuItemSupport;
import de.theredend2000.advancedhunt.model.PlayerData;
import de.theredend2000.advancedhunt.model.TreasureCore;
import de.theredend2000.advancedhunt.util.HeadHelper;
import de.theredend2000.advancedhunt.util.ItemBuilder;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class ProgressMenu extends PagedMenu {
   private final UUID collectionId;
   private final String collectionName;
   private boolean isLoading = true;
   private List<TreasureCore> treasures;
   private PlayerData playerData;

   public ProgressMenu(Player playerMenuUtility, UUID collectionId, String collectionName, Main plugin) {
      super(playerMenuUtility, plugin);
      this.collectionId = collectionId;
      this.collectionName = collectionName;
   }

   @Override
   public String getMenuName() {
      return ChatColor.translateAlternateColorCodes(
         '&', this.plugin.getMessageManager().getMessage("gui.progress.title", false, "%collection%", this.collectionName)
      );
   }

   @Override
   public int getSlots() {
      return 54;
   }

   @Override
   public void handleMenu(InventoryClickEvent e) {
   }

   @Override
   public void setMenuItems() {
      if (this.isLoading) {
         this.addMenuBorder();
         ItemStack loadingItem = new ItemBuilder(XMaterial.HOPPER)
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.progress.loading", false))
            .build();
         this.addStaticItem(22, loadingItem);
         this.fetchData();
      } else {
         this.addMenuBorder();
         this.addStatsButton();
         if (this.treasures != null && !this.treasures.isEmpty()) {
            int startIndex = this.page * this.maxItemsPerPage;
            int endIndex = Math.min(startIndex + this.maxItemsPerPage, this.treasures.size());
            this.hasNextPage = endIndex < this.treasures.size();
            this.addPagedButtons(this.treasures.size());
            int renderedPage = this.page;

            for (int i = startIndex; i < endIndex; i++) {
               TreasureCore treasureCore = this.treasures.get(i);
               boolean isFound = this.playerData.getFoundTreasures().contains(treasureCore.getId());
               int pageIndex = i - startIndex;
               int slot = this.getSlotForPagedIndex(pageIndex);
               ItemStack item = this.createTreasureItem(treasureCore, i, isFound, null);
               this.addStaticItem(slot, item);
               boolean isHeadTreasure = HeadHelper.isHeadMaterialName(treasureCore.getMaterial()) || HeadHelper.isPlayerHead(item);
               if (isFound && isHeadTreasure) {
                  UUID treasureId = treasureCore.getId();
                  int finalI = i;
                  TreasureMenuItemSupport.loadSkullInfo(this.plugin, treasureId).thenAccept(skullInfo -> {
                     if (skullInfo != null) {
                        Bukkit.getScheduler().runTask(this.plugin, () -> {
                           if (this.isViewingThisMenu(renderedPage)) {
                              ItemStack updated = this.createTreasureItem(treasureCore, finalI, true, skullInfo);
                              this.updateSlot(slot, updated);
                           }
                        });
                     }
                  });
               }
            }

            this.index = endIndex - 1;
         } else {
            this.addStaticItem(
               22,
               this.getWarningIcon(
                  this.plugin.getMessageManager().getMessage("gui.progress.no_treasures.name", false),
                  this.plugin.getMessageManager().getMessageList("gui.progress.no_treasures.lore", false)
               )
            );
         }
      }
   }

   private boolean isViewingThisMenu() {
      if (this.playerMenuUtility == null) {
         return false;
      } else if (!this.playerMenuUtility.isOnline()) {
         return false;
      } else if (this.playerMenuUtility.getOpenInventory() == null) {
         return false;
      } else {
         return this.playerMenuUtility.getOpenInventory().getTopInventory() == null
            ? false
            : this.playerMenuUtility.getOpenInventory().getTopInventory().getHolder() == this;
      }
   }

   private boolean isViewingThisMenu(int expectedPage) {
      return this.page == expectedPage && this.isViewingThisMenu();
   }

   private void fetchData() {
      Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
         PlayerData loadedPlayerData = this.plugin.getPlayerManager().getPlayerDataWithTimeout(this.playerMenuUtility.getUniqueId());
         if (loadedPlayerData == null) {
            loadedPlayerData = new PlayerData(this.playerMenuUtility.getUniqueId());
         }

         this.playerData = loadedPlayerData;
         this.treasures = this.plugin.getTreasureManager().getTreasureCoresInCollection(this.collectionId);
         this.isLoading = false;
         Bukkit.getScheduler().runTask(this.plugin, () -> {
            if (this.isViewingThisMenu()) {
               this.refresh();
            }
         });
      });
   }

   private void addStatsButton() {
      if (this.treasures != null && !this.treasures.isEmpty()) {
         int foundCount = (int)this.treasures.stream().filter(treasure -> this.playerData.getFoundTreasures().contains(treasure.getId())).count();
         int total = this.treasures.size();
         double percentage = total > 0 ? foundCount * 100.0 / total : 0.0;
         ItemStack statsItem = new ItemBuilder(XMaterial.BOOK)
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.progress.stats.name", false))
            .setLore(
               this.plugin
                  .getMessageManager()
                  .getMessageList(
                     "gui.progress.stats.lore",
                     false,
                     "%found%",
                     String.valueOf(foundCount),
                     "%total%",
                     String.valueOf(total),
                     "%percentage%",
                     String.format(Locale.US, "%.1f", percentage),
                     "%collection%",
                     this.collectionName
                  )
            )
            .build();
         this.addStaticItem(8, statsItem);
      }
   }

   private ItemStack createTreasureItem(TreasureCore treasureCore, int index, boolean isFound, SkullInfo skullInfo) {
      ItemBuilder builder;
      String statusColor;
      if (isFound) {
         statusColor = ChatColor.GREEN.toString();
         ItemStack item = TreasureMenuItemSupport.resolveDisplayItem(treasureCore, skullInfo);
         builder = new ItemBuilder(item);
         TreasureMenuItemSupport.applySkullInfo(builder, skullInfo);
      } else {
         statusColor = ChatColor.RED.toString();
         builder = new ItemBuilder(XMaterial.RED_STAINED_GLASS_PANE);
      }

      String status = isFound
         ? this.plugin.getMessageManager().getMessage("gui.progress.treasure.status_found", false)
         : this.plugin.getMessageManager().getMessage("gui.progress.treasure.status_not_found", false);
      builder.setDisplayName(
         statusColor
            + ChatColor.translateAlternateColorCodes(
               '&', this.plugin.getMessageManager().getMessage("gui.progress.treasure.name", false, "%number%", String.valueOf(index + 1), "%status%", status)
            )
      );
      List<String> lore = this.plugin
         .getMessageManager()
         .getMessageList(
            isFound ? "gui.progress.treasure.lore_found" : "gui.progress.treasure.lore_not_found",
            false,
            "%x%",
            String.valueOf(treasureCore.getLocation().getBlockX()),
            "%y%",
            String.valueOf(treasureCore.getLocation().getBlockY()),
            "%z%",
            String.valueOf(treasureCore.getLocation().getBlockZ()),
            "%world%",
            treasureCore.getWorldNameOr("?")
         );
      builder.setLore(lore);
      return builder.build();
   }
}
