package com.magmaguy.elitemobs.playerdata.statusscreen;

import com.magmaguy.elitemobs.config.menus.premade.PlayerStatusMenuConfig;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.skills.CombatLevelCalculator;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

final class PlayerStatusOverview {
   private PlayerStatusOverview() {
   }

   static List<String> lines(Player player) {
      List<String> configuredLines = PlayerStatusMenuConfig.getLandingOverviewLines();
      return configuredLines != null && !configuredLines.isEmpty() ? lines(configuredLines, capture(player)) : List.of();
   }

   private static List<String> lines(List<String> configuredLines, PlayerStatusOverview.Values values) {
      if (configuredLines != null && !configuredLines.isEmpty()) {
         List<String> parsedLines = new ArrayList<>(configuredLines.size());

         for (String configuredLine : configuredLines) {
            if (configuredLine != null) {
               parsedLines.add(values.resolve(configuredLine));
            }
         }

         return parsedLines;
      } else {
         return List.of();
      }
   }

   static String text(Player player) {
      return String.join("\n", lines(player));
   }

   static ItemStack decorateHeader(ItemStack configuredHeader, Player player) {
      if (configuredHeader == null) {
         return null;
      } else {
         ItemStack header = configuredHeader.clone();
         ItemMeta meta = header.getItemMeta();
         if (meta == null) {
            return header;
         } else {
            PlayerStatusOverview.Values values = capture(player);
            List<String> overviewLines = lines(PlayerStatusMenuConfig.getLandingOverviewLines(), values);
            if (meta.hasDisplayName()) {
               meta.setDisplayName(values.resolve(meta.getDisplayName()));
            }

            List<String> lore = meta.hasLore() && meta.getLore() != null ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
            lore.replaceAll(values::resolve);
            if (!lore.isEmpty() && !overviewLines.isEmpty()) {
               lore.add("");
            }

            lore.addAll(overviewLines);
            meta.setLore(lore);
            header.setItemMeta(meta);
            return header;
         }
      }
   }

   private static PlayerStatusOverview.Values capture(Player player) {
      String money = EconomyHandler.formatCurrency(EconomyHandler.checkCurrency(player.getUniqueId()));
      String combatLevel = String.valueOf(CombatLevelCalculator.calculateCombatLevel(player.getUniqueId()));
      List<?> quests = PlayerData.getQuests(player.getUniqueId());
      String activeQuests = String.valueOf(quests == null ? 0 : quests.size());
      String score = String.valueOf(PlayerData.getScore(player.getUniqueId()));
      return new PlayerStatusOverview.Values(money, combatLevel, activeQuests, score);
   }

   private static String resolve(String line, String money, String combatLevel, String activeQuests, String score) {
      return line.replace("$money", money)
         .replace("$combatLevel", combatLevel)
         .replace("$threat", combatLevel)
         .replace("$activeQuests", activeQuests)
         .replace("$score", score);
   }

   private record Values(String money, String combatLevel, String activeQuests, String score) {
      String resolve(String line) {
         return PlayerStatusOverview.resolve(line, this.money, this.combatLevel, this.activeQuests, this.score);
      }
   }
}
