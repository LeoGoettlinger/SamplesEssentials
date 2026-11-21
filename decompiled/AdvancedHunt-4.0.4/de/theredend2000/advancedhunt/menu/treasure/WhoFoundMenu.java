package de.theredend2000.advancedhunt.menu.treasure;

import de.theredend2000.advancedhunt.Main;
import de.theredend2000.advancedhunt.lib.xseries.XMaterial;
import de.theredend2000.advancedhunt.menu.Menu;
import de.theredend2000.advancedhunt.menu.PagedMenu;
import de.theredend2000.advancedhunt.util.ItemBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class WhoFoundMenu extends PagedMenu {
   private final UUID treasureId;
   private List<UUID> finderUuids;
   private boolean isLoading = true;

   public WhoFoundMenu(Player player, Main plugin, UUID treasureId, Menu previousMenu) {
      super(player, plugin);
      this.treasureId = treasureId;
      this.previousMenu = previousMenu;
      this.finderUuids = new ArrayList<>();
   }

   @Override
   public String getMenuName() {
      return this.plugin.getMessageManager().getMessage("gui.finders.title", false);
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
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.finders.loading", false))
            .build();
         this.addStaticItem(22, loadingItem);
         this.fetchFinderData();
      } else {
         this.addMenuBorder();
         if (this.finderUuids.isEmpty()) {
            this.addStaticItem(
               22,
               this.getWarningIcon(
                  this.plugin.getMessageManager().getMessage("gui.finders.empty.name", false),
                  this.plugin.getMessageManager().getMessageList("gui.finders.empty.lore", false)
               )
            );
         } else {
            this.addPagedButtons(this.finderUuids.size());
            int startIndex = this.page * this.maxItemsPerPage;
            int endIndex = Math.min(startIndex + this.maxItemsPerPage, this.finderUuids.size());
            this.hasNextPage = endIndex < this.finderUuids.size();

            for (int i = startIndex; i < endIndex; i++) {
               UUID playerUuid = this.finderUuids.get(i);
               int displayIndex = i - startIndex;
               ItemStack playerHead = this.createPlayerHeadItem(playerUuid);
               this.addPagedItem(displayIndex, playerHead, e -> {});
            }

            this.index = endIndex - 1;
         }
      }
   }

   private void fetchFinderData() {
      this.plugin.getDataRepository().getPlayersWhoFound(this.treasureId).thenAccept(uuids -> {
         this.finderUuids = (List<UUID>)(uuids != null ? uuids : new ArrayList<>());
         this.isLoading = false;
         Bukkit.getScheduler().runTask(this.plugin, () -> {
            if (this.inventory != null && this.playerMenuUtility.getOpenInventory().getTopInventory().equals(this.inventory)) {
               this.refresh();
            }
         });
      }).exceptionally(ex -> {
         this.plugin.getLogger().warning("Failed to fetch finder data for treasure " + this.treasureId + ": " + ex.getMessage());
         this.finderUuids = new ArrayList<>();
         this.isLoading = false;
         Bukkit.getScheduler().runTask(this.plugin, () -> {
            if (this.inventory != null && this.playerMenuUtility.getOpenInventory().getTopInventory().equals(this.inventory)) {
               this.refresh();
            }
         });
         return null;
      });
   }

   private ItemStack createPlayerHeadItem(UUID playerUuid) {
      OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUuid);
      String playerName = offlinePlayer.getName() != null ? offlinePlayer.getName() : playerUuid.toString();
      ItemStack head = XMaterial.PLAYER_HEAD.parseItem();
      ItemBuilder builder = new ItemBuilder(head)
         .setSkullOwner(playerUuid)
         .setDisplayName(this.plugin.getMessageManager().getMessage("gui.finders.entry.name", false, "%player%", playerName));

      for (String line : this.plugin.getMessageManager().getMessageList("gui.finders.entry.lore", false, "%player%", playerName)) {
         builder.addLoreLine(line);
      }

      return builder.build();
   }
}
