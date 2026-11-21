package de.theredend2000.advancedhunt.menu.collection;

import de.theredend2000.advancedhunt.Main;
import de.theredend2000.advancedhunt.lib.xseries.XMaterial;
import de.theredend2000.advancedhunt.menu.PagedMenu;
import de.theredend2000.advancedhunt.menu.common.SkullInfo;
import de.theredend2000.advancedhunt.menu.common.TreasureMenuItemSupport;
import de.theredend2000.advancedhunt.menu.reward.RewardsMenu;
import de.theredend2000.advancedhunt.menu.treasure.TreasureActionMenu;
import de.theredend2000.advancedhunt.menu.treasure.WhoFoundMenu;
import de.theredend2000.advancedhunt.model.Collection;
import de.theredend2000.advancedhunt.model.CollectionRewardHolder;
import de.theredend2000.advancedhunt.model.TreasureCore;
import de.theredend2000.advancedhunt.model.TreasureRewardHolder;
import de.theredend2000.advancedhunt.util.HeadHelper;
import de.theredend2000.advancedhunt.util.ItemBuilder;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

public class CollectionListMenu extends PagedMenu {
   private final UUID collectionId;

   public CollectionListMenu(Player playerMenuUtility, UUID collectionId, Main plugin) {
      super(playerMenuUtility, plugin);
      this.collectionId = collectionId;
   }

   @Override
   public String getMenuName() {
      return this.plugin.getMessageManager().getMessage("gui.collection_content.title", false);
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
      this.addMenuBorder();
      this.addLeaderboardButton();
      List<TreasureCore> treasures = this.plugin.getTreasureManager().getTreasureCoresInCollection(this.collectionId);
      if (treasures != null && !treasures.isEmpty()) {
         this.addPagedButtons(treasures.size());
         int startIndex = this.page * this.maxItemsPerPage;
         int endIndex = Math.min(startIndex + this.maxItemsPerPage, treasures.size());
         this.hasNextPage = endIndex < treasures.size();

         for (int i = startIndex; i < endIndex; i++) {
            TreasureCore treasureCore = treasures.get(i);
            int displayIndex = i - startIndex;
            int slot = this.getSlotForPagedIndex(displayIndex);
            ItemStack item = this.createTreasureItem(treasureCore, i, null, null);
            this.addButton(slot, item, e -> this.handleTreasureClick(e, treasureCore));
            UUID treasureId = treasureCore.getId();
            CompletableFuture<Integer> foundCountFuture = this.plugin.getDataRepository().getFoundPlayerCount(treasureId);
            boolean isHead = HeadHelper.isHeadMaterialName(treasureCore.getMaterial()) || HeadHelper.isPlayerHead(item);
            CompletableFuture<SkullInfo> skullInfoFuture = isHead
               ? TreasureMenuItemSupport.loadSkullInfo(this.plugin, treasureId)
               : CompletableFuture.completedFuture(null);
            int finalI = i;
            foundCountFuture.<SkullInfo, CollectionListMenu.TreasureItemData>thenCombine(
                  skullInfoFuture, (x$0, x$1) -> new CollectionListMenu.TreasureItemData(x$0, x$1)
               )
               .thenAccept(data -> Bukkit.getScheduler().runTask(this.plugin, () -> {
                  if (this.isViewingThisMenu()) {
                     ItemStack updated = this.createTreasureItem(treasureCore, finalI, data.foundCount(), data.skullInfo());
                     this.updateSlot(slot, updated);
                  }
               }));
         }

         this.index = endIndex - 1;
      } else {
         this.addStaticItem(
            22,
            this.getWarningIcon(
               this.plugin.getMessageManager().getMessage("gui.collection_content.no_treasures.name", false),
               this.plugin.getMessageManager().getMessageList("gui.collection_content.no_treasures.lore", false)
            )
         );
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

   private void addLeaderboardButton() {
      this.plugin
         .getDataRepository()
         .loadCollections()
         .thenAccept(
            collections -> {
               Collection collection = collections.stream().filter(c -> c.getId().equals(this.collectionId)).findFirst().orElse(null);
               if (collection != null) {
                  Bukkit.getScheduler()
                     .runTask(
                        this.plugin,
                        () -> {
                           ItemStack leaderboardButton = new ItemBuilder(XMaterial.GOLDEN_HELMET)
                              .setDisplayName(this.plugin.getMessageManager().getMessage("gui.leaderboard.view_button.name", false))
                              .setLore(this.plugin.getMessageManager().getMessageList("gui.leaderboard.view_button.lore", false))
                              .addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
                              .build();
                           this.addButton(8, leaderboardButton, e -> new LeaderboardMenu((Player)e.getWhoClicked(), this.plugin, collection, this).open());
                        }
                     );
               }
            }
         );
   }

   private ItemStack createTreasureItem(TreasureCore treasureCore, int index, Integer playerFoundSize, SkullInfo skullInfo) {
      ItemStack item = TreasureMenuItemSupport.resolveDisplayItem(treasureCore, skullInfo);
      ItemBuilder builder = new ItemBuilder(item);
      TreasureMenuItemSupport.applySkullInfo(builder, skullInfo);
      builder.setDisplayName(
         this.plugin.getMessageManager().getMessage("gui.collection_content.treasure_item.name", false, "%number%", String.valueOf(index + 1))
      );
      String foundCount = playerFoundSize == null ? "..." : String.valueOf(playerFoundSize);
      List<String> lore = this.plugin
         .getMessageManager()
         .getMessageList(
            "gui.collection_content.treasure_item.lore",
            false,
            "%count%",
            foundCount,
            "%x%",
            String.valueOf(treasureCore.getLocation().getBlockX()),
            "%y%",
            String.valueOf(treasureCore.getLocation().getBlockY()),
            "%z%",
            String.valueOf(treasureCore.getLocation().getBlockZ()),
            "%world%",
            treasureCore.getWorldNameOr("?")
         );
      if (this.playerMenuUtility.hasPermission("advancedhunt.admin.teleport")) {
         lore.add(this.plugin.getMessageManager().getMessage("gui.collection_content.treasure_item.lore_teleport", false));
      }

      if (this.playerMenuUtility.hasPermission("advancedhunt.admin.view_finders")) {
         lore.add(this.plugin.getMessageManager().getMessage("gui.collection_content.treasure_item.lore_who_found", false));
      }

      if (this.playerMenuUtility.hasPermission("advancedhunt.admin.rewards")) {
         lore.add(this.plugin.getMessageManager().getMessage("gui.collection_content.treasure_item.lore_reward", false));
      }

      if (this.playerMenuUtility.hasPermission("advancedhunt.admin.teleport")
         || this.playerMenuUtility.hasPermission("advancedhunt.admin.view_finders")
         || this.playerMenuUtility.hasPermission("advancedhunt.admin.rewards")) {
         lore.add(this.plugin.getMessageManager().getMessage("gui.collection_content.treasure_item.lore_action_menu_bedrock", false));
      }

      builder.setLore(lore);
      return builder.build();
   }

   private void handleTreasureClick(InventoryClickEvent e, TreasureCore treasureCore) {
      Player p = (Player)e.getWhoClicked();
      ClickType clickType = e.getClick();
      if (clickType != ClickType.RIGHT && clickType != ClickType.SHIFT_RIGHT) {
         if (clickType == ClickType.LEFT) {
            if (!p.hasPermission("advancedhunt.admin.teleport")) {
               p.sendMessage(this.plugin.getMessageManager().getMessage("error.no_permission"));
            } else {
               p.closeInventory();
               this.teleportToTreasure(p, treasureCore);
            }
         } else if (clickType != ClickType.SHIFT_LEFT) {
            this.plugin.getTreasureManager().getFullTreasureAsync(treasureCore.getId()).thenAccept(fullTreasure -> {
               if (fullTreasure != null) {
                  Bukkit.getScheduler().runTask(this.plugin, () -> new TreasureActionMenu(p, this.plugin, fullTreasure, this).open());
               }
            });
         } else if (!p.hasPermission("advancedhunt.admin.rewards")) {
            p.sendMessage(this.plugin.getMessageManager().getMessage("error.no_permission"));
         } else {
            this.plugin.getTreasureManager().getFullTreasureAsync(treasureCore.getId()).thenAccept(treasure -> {
               if (treasure != null) {
                  Collection collectionContext = this.plugin.getCollectionManager().getCollectionById(this.collectionId).orElse(null);
                  Bukkit.getScheduler().runTask(this.plugin, () -> {
                     RewardsMenu menu = (RewardsMenu)new RewardsMenu(p, this.plugin, new TreasureRewardHolder(this.plugin, treasure)).setPreviousMenu(this);
                     if (collectionContext != null) {
                        menu.setAlternateContext(new CollectionRewardHolder(this.plugin, collectionContext));
                     }

                     menu.open();
                  });
               }
            });
         }
      } else if (!p.hasPermission("advancedhunt.admin.view_finders")) {
         p.sendMessage(this.plugin.getMessageManager().getMessage("error.no_permission"));
      } else {
         new WhoFoundMenu(p, this.plugin, treasureCore.getId(), this).open();
      }
   }

   private void teleportToTreasure(Player player, TreasureCore treasureCore) {
      Location location = treasureCore.getLocation();
      if (location != null && location.getWorld() != null) {
         if (!location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4)) {
            location.getChunk().load();
            Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.performSafeTeleport(player, location), 1L);
         } else {
            this.performSafeTeleport(player, location);
         }
      } else {
         player.sendMessage(this.plugin.getMessageManager().getMessage("feedback.teleport.invalid_location"));
      }
   }

   private void performSafeTeleport(Player player, Location treasureLocation) {
      Location safeLoc = treasureLocation.clone().add(0.5, 1.5, 0.5);
      safeLoc.setYaw(player.getLocation().getYaw());
      safeLoc.setPitch(player.getLocation().getPitch());
      Location checkLoc = safeLoc.clone();
      Material blockAbove = checkLoc.getBlock().getType();
      Material blockAbove2 = checkLoc.clone().add(0.0, 1.0, 0.0).getBlock().getType();
      if (blockAbove.isSolid() || blockAbove2.isSolid()) {
         Location safeLocation = this.findSafeLocation(treasureLocation);
         if (safeLocation == null) {
            player.sendMessage(this.plugin.getMessageManager().getMessage("feedback.teleport.unsafe"));
            return;
         }

         safeLoc = safeLocation;
         safeLocation.setYaw(player.getLocation().getYaw());
         safeLocation.setPitch(player.getLocation().getPitch());
         player.sendMessage(this.plugin.getMessageManager().getMessage("feedback.teleport.adjusted"));
      }

      player.teleport(safeLoc);
      player.sendMessage(this.plugin.getMessageManager().getMessage("feedback.teleport.success"));
   }

   private Location findSafeLocation(Location center) {
      for (int y = 0; y <= 5; y++) {
         for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
               Location testLoc = center.clone().add(x + 0.5, y + 1.5, z + 0.5);
               Material below = testLoc.clone().subtract(0.0, 1.0, 0.0).getBlock().getType();
               Material at = testLoc.getBlock().getType();
               Material above = testLoc.clone().add(0.0, 1.0, 0.0).getBlock().getType();
               if (below.isSolid() && !at.isSolid() && !above.isSolid()) {
                  return testLoc;
               }
            }
         }
      }

      return null;
   }

   private static final class TreasureItemData {
      private final int foundCount;
      private final SkullInfo skullInfo;

      private TreasureItemData(int foundCount, SkullInfo skullInfo) {
         this.foundCount = foundCount;
         this.skullInfo = skullInfo;
      }

      private int foundCount() {
         return this.foundCount;
      }

      private SkullInfo skullInfo() {
         return this.skullInfo;
      }
   }
}
