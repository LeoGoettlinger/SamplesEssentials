package com.magmaguy.elitemobs.config.menus.premade;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.config.menus.MenusConfigFields;
import com.magmaguy.elitemobs.items.customloottable.CurrencyCustomLootEntry;
import com.magmaguy.elitemobs.items.customloottable.CustomLootEntry;
import com.magmaguy.elitemobs.items.customloottable.EliteCustomLootEntry;
import com.magmaguy.elitemobs.items.customloottable.ItemStackCustomLootEntry;
import com.magmaguy.elitemobs.items.customloottable.VanillaCustomLootEntry;
import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import com.magmaguy.elitemobs.quests.objectives.Objective;
import com.magmaguy.elitemobs.quests.rewards.QuestReward;
import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

public class DynamicQuestMenuConfig extends MenusConfigFields {
   private static String questName;
   private static String headerTextLines;
   private static String defaultLoreTextLines;
   private static String acceptTextLines;
   private static String acceptHoverLines;
   private static String acceptCommandLines;
   private static String acceptedTextLines;
   private static String acceptedHoverLines;
   private static String acceptedCommandLines;
   private static String completedTextLines;
   private static String completedHoverLines;
   private static String completedCommandLines;
   private static String ongoingColorCode;
   private static String completedColorCode;
   private static String objectivesLine;
   private static String rewardsLine;
   private static String killQuestDefaultSummaryLine;
   private static String rewardsDefaultSummaryLine;

   public DynamicQuestMenuConfig() {
      super("dynamic_quest_screen", true);
   }

   public static String getKillQuestDefaultSummaryLine(Objective objective) {
      String newString = killQuestDefaultSummaryLine;
      newString = newString.replace("$name", ChatColor.BLACK + ChatColor.stripColor(objective.getObjectiveName()));
      newString = newString.replace("$current", objective.getCurrentAmount() + "");
      newString = newString.replace("$target", objective.getTargetAmount() + "");
      return !objective.isObjectiveCompleted() ? newString.replace("$color", ongoingColorCode) : newString.replace("$color", completedColorCode);
   }

   public static List<TextComponent> getRewardsDefaultSummaryLine(QuestReward questReward) {
      List<TextComponent> textComponent = new ArrayList<>();
      if (questReward.getCustomLootTable() == null) {
         return textComponent;
      } else {
         for (CustomLootEntry customLootEntry : questReward.getCustomLootTable().getEntries()) {
            ItemStack itemStack = null;
            if (customLootEntry instanceof EliteCustomLootEntry) {
               itemStack = ((EliteCustomLootEntry)customLootEntry)
                  .generateItemStack(questReward.getRewardLevel(), Bukkit.getPlayer(questReward.getPlayerUUID()), null);
            } else if (customLootEntry instanceof ItemStackCustomLootEntry) {
               itemStack = ((ItemStackCustomLootEntry)customLootEntry).generateItemStack();
            }

            if (itemStack != null) {
               textComponent.add(
                  new TextComponent(
                     rewardsDefaultSummaryLine.replace("$amount", customLootEntry.getAmount() + "")
                        .replace("$rewardName", itemStack.getItemMeta().getDisplayName())
                        .replace("$chance", (int)(customLootEntry.getChance() * 100.0) + "")
                  )
               );
            } else {
               if (customLootEntry instanceof VanillaCustomLootEntry) {
                  itemStack = ((VanillaCustomLootEntry)customLootEntry).generateItemStack();
               }

               if (itemStack != null) {
                  textComponent.add(
                     new TextComponent(
                        rewardsDefaultSummaryLine.replace("$amount", customLootEntry.getAmount() + "")
                           .replace("$rewardName", itemStack.getType().toString().replace("_", " "))
                           .replace("$chance", (int)(customLootEntry.getChance() * 100.0) + "")
                     )
                  );
               } else if (customLootEntry instanceof CurrencyCustomLootEntry) {
                  textComponent.add(
                     new TextComponent(
                        rewardsDefaultSummaryLine.replace("$amount", customLootEntry.getAmount() + "")
                           .replace(
                              "$rewardName", ((CurrencyCustomLootEntry)customLootEntry).getCurrencyAmount() + " " + EconomySettingsConfig.getCurrencyName()
                           )
                           .replace("$chance", (int)(customLootEntry.getChance() * 100.0) + "")
                     )
                  );
               }
            }
         }

         return textComponent;
      }
   }

   @Override
   public void processAdditionalFields() {
      questName = ConfigurationEngine.setString(this.file, this.fileConfiguration, "questName", "Slay $amount $name", true);
      headerTextLines = ConfigurationEngine.setString(
         this.file, this.fileConfiguration, "headerTextLines2", ChatColorConverter.convert("&c&lGuild request!\n"), true
      );
      defaultLoreTextLines = ConfigurationEngine.setString(this.file, this.fileConfiguration, "defaultLoreTextLines", "&8Slay $amount $name!", true);
      acceptTextLines = ConfigurationEngine.setString(this.file, this.fileConfiguration, "acceptTextLines", "&a&lAccept!", true);
      acceptHoverLines = ConfigurationEngine.setString(this.file, this.fileConfiguration, "acceptHoverLines", "&aClick to \n&aaccept quest!", true);
      acceptCommandLines = ConfigurationEngine.setString(this.file, this.fileConfiguration, "acceptCommandLines", "/em quest accept $questID", false);
      acceptedTextLines = ConfigurationEngine.setString(this.file, this.fileConfiguration, "acceptedTextLines", "&2&lAccepted! &4[Abandon]", true);
      acceptedHoverLines = ConfigurationEngine.setString(this.file, this.fileConfiguration, "acceptedHoverLines", "&aClick to abandon quest!", true);
      acceptedCommandLines = ConfigurationEngine.setString(this.file, this.fileConfiguration, "acceptedCommandLines3", "/em quest leave $questID", false);
      completedTextLines = ConfigurationEngine.setString(this.file, this.fileConfiguration, "completedTextLines", "&2&l[Turn in!]", true);
      completedHoverLines = ConfigurationEngine.setString(this.file, this.fileConfiguration, "completedHoverLines", "&aClick to turn quest in!", true);
      completedCommandLines = ConfigurationEngine.setString(this.file, this.fileConfiguration, "completedCommandLines", "/em quest complete $questID", false);
      objectivesLine = ConfigurationEngine.setString(this.file, this.fileConfiguration, "objectivesLine", "&c&lObjectives:", true);
      killQuestDefaultSummaryLine = ConfigurationEngine.setString(
         this.file, this.fileConfiguration, "killQuestDefaultSummaryLine", "&c➤Kill $name:$color$current&0/$color$target", true
      );
      rewardsLine = ConfigurationEngine.setString(this.file, this.fileConfiguration, "rewardsLine", "&2&lRewards:", true);
      rewardsDefaultSummaryLine = ConfigurationEngine.setString(
         this.file, this.fileConfiguration, "rewardsDefaultSummaryLine", "&2➤$amountx $rewardName &8($chance%)", true
      );
      ongoingColorCode = ConfigurationEngine.setString(this.file, this.fileConfiguration, "ongoingQuestColorCode", "&c", false);
      completedColorCode = ConfigurationEngine.setString(this.file, this.fileConfiguration, "ongoingQuestColorCode", "&2", false);
   }

   @Generated
   public static String getQuestName() {
      return questName;
   }

   @Generated
   public static String getHeaderTextLines() {
      return headerTextLines;
   }

   @Generated
   public static String getDefaultLoreTextLines() {
      return defaultLoreTextLines;
   }

   @Generated
   public static String getAcceptTextLines() {
      return acceptTextLines;
   }

   @Generated
   public static String getAcceptHoverLines() {
      return acceptHoverLines;
   }

   @Generated
   public static String getAcceptCommandLines() {
      return acceptCommandLines;
   }

   @Generated
   public static String getAcceptedTextLines() {
      return acceptedTextLines;
   }

   @Generated
   public static String getAcceptedHoverLines() {
      return acceptedHoverLines;
   }

   @Generated
   public static String getAcceptedCommandLines() {
      return acceptedCommandLines;
   }

   @Generated
   public static String getCompletedTextLines() {
      return completedTextLines;
   }

   @Generated
   public static String getCompletedHoverLines() {
      return completedHoverLines;
   }

   @Generated
   public static String getCompletedCommandLines() {
      return completedCommandLines;
   }

   @Generated
   public static String getOngoingColorCode() {
      return ongoingColorCode;
   }

   @Generated
   public static String getCompletedColorCode() {
      return completedColorCode;
   }

   @Generated
   public static String getObjectivesLine() {
      return objectivesLine;
   }

   @Generated
   public static String getRewardsLine() {
      return rewardsLine;
   }

   @Generated
   public static String getRewardsDefaultSummaryLine() {
      return rewardsDefaultSummaryLine;
   }
}
