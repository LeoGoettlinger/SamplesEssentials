package com.magmaguy.elitemobs.playerdata.statusscreen;

import com.magmaguy.elitemobs.config.menus.premade.PlayerStatusMenuConfig;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class StatsPage {
   private StatsPage() {
   }

   protected static TextComponent statsPage(Player targetPlayer) {
      TextComponent textComponent = new TextComponent();
      StatsPage.StatsSnapshot snapshot = StatsPage.StatsSnapshot.capture(targetPlayer);

      for (int i = 0; i < 13; i++) {
         if (PlayerStatusMenuConfig.getStatsTextLines()[i] != null && !PlayerStatusMenuConfig.getStatsTextLines()[i].contains("$guildtier")) {
            TextComponent line = new TextComponent(snapshot.replacePlaceholders(PlayerStatusMenuConfig.getStatsTextLines()[i]) + "\n");
            if (PlayerStatusMenuConfig.getStatsHoverLines() != null
               && PlayerStatusMenuConfig.getStatsHoverLines()[i] != null
               && !PlayerStatusMenuConfig.getStatsHoverLines()[i].isEmpty()) {
               PlayerStatusScreen.setHoverText(line, PlayerStatusMenuConfig.getStatsHoverLines()[i]);
            }

            if (PlayerStatusMenuConfig.getStatsCommandLines() != null
               && PlayerStatusMenuConfig.getStatsCommandLines()[i] != null
               && !PlayerStatusMenuConfig.getStatsCommandLines()[i].isEmpty()) {
               line.setClickEvent(new ClickEvent(Action.RUN_COMMAND, PlayerStatusMenuConfig.getStatsCommandLines()[i]));
            }

            textComponent.addExtra(line);
         }
      }

      return textComponent;
   }

   protected static void statsPage(Player targetPlayer, Player requestingPlayer) {
      Inventory inventory = Bukkit.createInventory(requestingPlayer, 27, PlayerStatusMenuConfig.getStatsChestMenuName());
      StatsPage.StatsSnapshot snapshot = StatsPage.StatsSnapshot.capture(targetPlayer);
      inventory.setItem(PlayerStatusMenuConfig.getStatsRankSlot(), snapshot.replacePlaceholders(PlayerStatusMenuConfig.getStatsRankItem()));
      inventory.setItem(PlayerStatusMenuConfig.getStatsScoreSlot(), snapshot.replacePlaceholders(PlayerStatusMenuConfig.getStatsScoreItem()));
      inventory.setItem(
         PlayerStatusMenuConfig.getStatsDungeonsCompletedSlot(), snapshot.replacePlaceholders(PlayerStatusMenuConfig.getStatsDungeonsCompletedItem())
      );
      inventory.setItem(PlayerStatusMenuConfig.getStatsCombatLevelSlot(), snapshot.replacePlaceholders(PlayerStatusMenuConfig.getStatsCombatLevelItem()));
      inventory.setItem(PlayerStatusMenuConfig.getStatsEliteKillsSlot(), snapshot.replacePlaceholders(PlayerStatusMenuConfig.getStatsEliteKillsItem()));
      inventory.setItem(
         PlayerStatusMenuConfig.getStatsMaxEliteLevelKilledSlot(), snapshot.replacePlaceholders(PlayerStatusMenuConfig.getStatsMaxEliteLevelKilledItem())
      );
      inventory.setItem(
         PlayerStatusMenuConfig.getStatsQuestsCompletedSlot(), snapshot.replacePlaceholders(PlayerStatusMenuConfig.getStatsQuestsCompletedItem())
      );
      inventory.setItem(26, PlayerStatusMenuConfig.getBackItem());
      if (requestingPlayer.openInventory(inventory) != null) {
         StatusInventorySafety.protect(inventory);
         StatsPage.StatsPageEvents.pageInventories.add(inventory);
      }
   }

   public static class StatsPageEvents implements Listener {
      private static final Set<Inventory> pageInventories = new HashSet<>();

      public static void shutdown() {
         pageInventories.clear();
      }

      @EventHandler
      public void onInventoryInteract(InventoryClickEvent event) {
         Player player = ((Player)event.getWhoClicked()).getPlayer();
         if (pageInventories.contains(event.getInventory())) {
            event.setCancelled(true);
            if (event.getClickedInventory() == event.getView().getTopInventory()) {
               if (event.getSlot() == 26) {
                  player.closeInventory();
                  CoverPage.coverPage(player);
               }
            }
         }
      }

      @EventHandler
      public void onInventoryClose(InventoryCloseEvent event) {
         pageInventories.remove(event.getInventory());
      }
   }

   public static final class StatsSnapshot {
      private final String money;
      private final String kills;
      private final String highestKill;
      private final String deaths;
      private final String questsCompleted;
      private final String dungeonsCompleted;
      private final String combatLevel;
      private final String score;
      private final String rank;
      private final String rankTotal;

      private StatsSnapshot(Player player) {
         UUID playerId = player.getUniqueId();
         PlayerData.ScoreRank scoreRank = PlayerData.getScoreRank(playerId);
         this.money = EconomyHandler.formatCurrency(EconomyHandler.checkCurrency(playerId));
         this.kills = String.valueOf(PlayerData.getKills(playerId));
         this.highestKill = String.valueOf(PlayerData.getHighestLevelKilled(playerId));
         this.deaths = String.valueOf(PlayerData.getDeaths(playerId));
         this.questsCompleted = String.valueOf(PlayerData.getQuestsCompleted(playerId));
         this.dungeonsCompleted = String.valueOf(PlayerData.getDungeonsCompleted(playerId));
         this.combatLevel = String.valueOf(PlayerData.getPlayerLevel(playerId));
         this.score = String.valueOf(PlayerData.getScore(playerId));
         this.rank = scoreRank.isAvailable() ? "#" + scoreRank.position() : PlayerStatusMenuConfig.getStatsRankUnavailableText();
         this.rankTotal = scoreRank.isAvailable() ? String.valueOf(scoreRank.playerCount()) : PlayerStatusMenuConfig.getStatsRankTotalUnavailableText();
      }

      public static StatsPage.StatsSnapshot capture(Player player) {
         return new StatsPage.StatsSnapshot(player);
      }

      public String replacePlaceholders(String value) {
         return value == null
            ? ""
            : value.replace("$ranktotal", this.rankTotal)
               .replace("$rank", this.rank)
               .replace("$dungeonsCompleted", this.dungeonsCompleted)
               .replace("$dungeons", this.dungeonsCompleted)
               .replace("$combatLevel", this.combatLevel)
               .replace("$combat", this.combatLevel)
               .replace("$questsCompleted", this.questsCompleted)
               .replace("$highestkill", this.highestKill)
               .replace("$maxKill", this.highestKill)
               .replace("$money", this.money)
               .replace("$kills", this.kills)
               .replace("$deaths", this.deaths)
               .replace("$quests", this.questsCompleted)
               .replace("$score", this.score);
      }

      public ItemStack replacePlaceholders(ItemStack template) {
         ItemStack itemStack = template.clone();
         ItemMeta itemMeta = itemStack.getItemMeta();
         if (itemMeta == null) {
            return itemStack;
         } else {
            if (itemMeta.hasDisplayName()) {
               itemMeta.setDisplayName(this.replacePlaceholders(itemMeta.getDisplayName()));
            }

            if (itemMeta.hasLore()) {
               List<String> lore = itemMeta.getLore();
               if (lore != null) {
                  List<String> replacedLore = new ArrayList<>(lore.size());

                  for (String line : lore) {
                     replacedLore.add(this.replacePlaceholders(line));
                  }

                  itemMeta.setLore(replacedLore);
               }
            }

            itemStack.setItemMeta(itemMeta);
            return itemStack;
         }
      }
   }
}
