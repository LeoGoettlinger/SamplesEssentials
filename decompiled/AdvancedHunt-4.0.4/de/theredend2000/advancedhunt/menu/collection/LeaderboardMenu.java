package de.theredend2000.advancedhunt.menu.collection;

import de.theredend2000.advancedhunt.Main;
import de.theredend2000.advancedhunt.lib.xseries.XMaterial;
import de.theredend2000.advancedhunt.managers.LeaderboardManager;
import de.theredend2000.advancedhunt.menu.Menu;
import de.theredend2000.advancedhunt.menu.PagedMenu;
import de.theredend2000.advancedhunt.model.Collection;
import de.theredend2000.advancedhunt.util.ItemBuilder;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

public class LeaderboardMenu extends PagedMenu {
   private final Collection collection;
   private List<LeaderboardManager.LeaderboardEntry> entries;
   private boolean isLoading = true;
   private int playerRank = -1;
   private int playerScore = 0;
   private int totalTreasures = 0;

   public LeaderboardMenu(Player player, Main plugin, Collection collection, Menu previousMenu) {
      super(player, plugin);
      this.collection = collection;
      this.previousMenu = previousMenu;
      this.entries = new ArrayList<>();
   }

   @Override
   public String getMenuName() {
      return this.plugin.getMessageManager().getMessage("gui.leaderboard.title", false, "%collection%", this.collection.getName());
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
      int displaylimit = this.plugin.getConfig().getInt("leaderboard.display-limit");
      int refresh = this.plugin.getConfig().getInt("leaderboard.cache-refresh-interval");
      double minutes = Math.round(refresh / 60.0 * 10.0) / 10.0;
      this.addStaticItem(
         8,
         new ItemBuilder(XMaterial.PLAYER_HEAD)
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.leaderboard.info.name", false))
            .setLore(
               this.plugin
                  .getMessageManager()
                  .getMessageList(
                     "gui.leaderboard.info.lore",
                     false,
                     "%display_limit%",
                     String.valueOf(displaylimit),
                     "%refresh_interval%",
                     String.valueOf(refresh),
                     "%minutes%",
                     String.valueOf(minutes)
                  )
            )
            .setSkullTexture(
               "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTY0MzlkMmUzMDZiMjI1NTE2YWE5YTZkMDA3YTdlNzVlZGQyZDUwMTVkMTEzYjQyZjQ0YmU2MmE1MTdlNTc0ZiJ9fX0="
            )
            .build()
      );
      this.addMenuBorder();
      if (this.isLoading) {
         ItemStack loadingItem = new ItemBuilder(XMaterial.HOPPER)
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.leaderboard.loading.name", false))
            .setLore(this.plugin.getMessageManager().getMessageList("gui.leaderboard.loading.lore", false))
            .build();
         this.addStaticItem(22, loadingItem);
         this.fetchLeaderboardData();
      } else {
         this.addYourRankIndicator();
         if (this.entries.isEmpty()) {
            this.addStaticItem(
               22,
               this.getWarningIcon(
                  this.plugin.getMessageManager().getMessage("gui.leaderboard.empty.name", false),
                  this.plugin.getMessageManager().getMessageList("gui.leaderboard.empty.lore", false)
               )
            );
         } else {
            this.addPagedButtons(this.entries.size());
            int startIndex = this.page * this.maxItemsPerPage;
            int endIndex = Math.min(startIndex + this.maxItemsPerPage, this.entries.size());
            this.hasNextPage = endIndex < this.entries.size();

            for (int i = startIndex; i < endIndex; i++) {
               LeaderboardManager.LeaderboardEntry entry = this.entries.get(i);
               int displayIndex = i - startIndex;
               boolean isCurrentPlayer = entry.getPlayerId().equals(this.playerMenuUtility.getUniqueId());
               ItemStack playerItem = this.createLeaderboardItem(entry, isCurrentPlayer);
               this.addPagedItem(displayIndex, playerItem, e -> {});
            }

            this.index = endIndex - 1;
         }
      }
   }

   private void fetchLeaderboardData() {
      Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
         List<LeaderboardManager.LeaderboardEntry> cachedEntries = this.plugin.getLeaderboardManager().getEntries(this.collection.getName());
         this.entries = (List<LeaderboardManager.LeaderboardEntry>)(cachedEntries != null ? cachedEntries : new ArrayList<>());
         this.plugin.getDataRepository().loadPlayerData(this.playerMenuUtility.getUniqueId()).thenAccept(playerData -> {
            if (playerData != null) {
               this.totalTreasures = this.plugin.getTreasureManager().getTreasureCoresInCollection(this.collection.getId()).size();
               this.playerScore = this.plugin.getTreasureManager().filterFoundByCollection(playerData.getFoundTreasures(), this.collection.getId()).size();
               this.calculatePlayerRank();
            } else {
               this.totalTreasures = 0;
               this.playerScore = 0;
               this.playerRank = -1;
            }

            this.isLoading = false;
            Bukkit.getScheduler().runTask(this.plugin, () -> {
               if (this.inventory != null && this.playerMenuUtility.getOpenInventory().getTopInventory().equals(this.inventory)) {
                  this.refresh();
               }
            });
         });
      });
   }

   private void calculatePlayerRank() {
      if (this.playerScore == 0) {
         this.playerRank = -1;
      } else {
         for (LeaderboardManager.LeaderboardEntry entry : this.entries) {
            if (entry.getPlayerId().equals(this.playerMenuUtility.getUniqueId())) {
               this.playerRank = entry.getRank();
               return;
            }
         }

         int rank = 1;

         for (LeaderboardManager.LeaderboardEntry entryx : this.entries) {
            if (entryx.getScore() > this.playerScore) {
               rank++;
            }
         }

         this.playerRank = rank;
      }
   }

   private void addYourRankIndicator() {
      ItemStack indicatorItem;
      if (this.playerScore != 0 && this.playerRank != -1) {
         double percentage = this.totalTreasures > 0 ? this.playerScore * 100.0 / this.totalTreasures : 0.0;
         indicatorItem = new ItemBuilder(XMaterial.LIME_STAINED_GLASS_PANE)
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.leaderboard.your_rank.name", false))
            .setLore(
               this.plugin
                  .getMessageManager()
                  .getMessageList(
                     "gui.leaderboard.your_rank.lore",
                     false,
                     "%rank%",
                     String.valueOf(this.playerRank),
                     "%score%",
                     String.valueOf(this.playerScore),
                     "%total%",
                     String.valueOf(this.totalTreasures),
                     "%percentage%",
                     String.format("%.1f", percentage)
                  )
            )
            .build();
      } else {
         indicatorItem = new ItemBuilder(XMaterial.RED_STAINED_GLASS_PANE)
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.leaderboard.not_ranked.name", false))
            .setLore(this.plugin.getMessageManager().getMessageList("gui.leaderboard.not_ranked.lore", false))
            .build();
      }

      this.addStaticItem(4, indicatorItem);
   }

   private ItemStack createLeaderboardItem(LeaderboardManager.LeaderboardEntry entry, boolean isCurrentPlayer) {
      String playerName = entry.getPlayerName();
      ItemStack head = XMaterial.PLAYER_HEAD.parseItem();
      String nameKey = isCurrentPlayer ? "gui.leaderboard.player_entry_highlighted.name" : "gui.leaderboard.player_entry.name";
      String loreKey = isCurrentPlayer ? "gui.leaderboard.player_entry_highlighted.lore" : "gui.leaderboard.player_entry.lore";
      ItemBuilder builder = new ItemBuilder(head)
         .setSkullOwner(entry.getPlayerId())
         .setDisplayName(this.plugin.getMessageManager().getMessage(nameKey, false, "%rank%", String.valueOf(entry.getRank()), "%player%", playerName));

      for (String line : this.plugin
         .getMessageManager()
         .getMessageList(loreKey, false, "%rank%", String.valueOf(entry.getRank()), "%player%", playerName, "%score%", String.valueOf(entry.getScore()))) {
         builder.addLoreLine(line);
      }

      if (isCurrentPlayer) {
         builder.addEnchant(Enchantment.UNBREAKING, 1);
         builder.addItemFlags(ItemFlag.HIDE_ENCHANTS);
      }

      return builder.build();
   }
}
