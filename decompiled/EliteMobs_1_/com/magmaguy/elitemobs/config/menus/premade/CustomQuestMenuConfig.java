package com.magmaguy.elitemobs.config.menus.premade;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.config.customarenas.CustomArenasConfig;
import com.magmaguy.elitemobs.config.customarenas.CustomArenasConfigFields;
import com.magmaguy.elitemobs.config.menus.MenusConfigFields;
import com.magmaguy.elitemobs.items.customloottable.CurrencyCustomLootEntry;
import com.magmaguy.elitemobs.items.customloottable.CustomLootEntry;
import com.magmaguy.elitemobs.items.customloottable.EliteCustomLootEntry;
import com.magmaguy.elitemobs.items.customloottable.ItemStackCustomLootEntry;
import com.magmaguy.elitemobs.items.customloottable.VanillaCustomLootEntry;
import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import com.magmaguy.elitemobs.quests.objectives.ArenaObjective;
import com.magmaguy.elitemobs.quests.objectives.ClassUnlockObjective;
import com.magmaguy.elitemobs.quests.objectives.CustomFetchObjective;
import com.magmaguy.elitemobs.quests.objectives.CustomKillObjective;
import com.magmaguy.elitemobs.quests.objectives.DialogObjective;
import com.magmaguy.elitemobs.quests.objectives.Objective;
import com.magmaguy.elitemobs.quests.rewards.QuestReward;
import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CustomQuestMenuConfig extends MenusConfigFields {
   private static String headerTextLines;
   private static String acceptTextLines;
   private static String acceptHoverLines;
   private static String acceptCommandLines;
   private static String acceptedTextLines;
   private static String acceptedHoverLines;
   private static String acceptedCommandLines;
   private static String trackTextLines;
   private static String trackHoverLines;
   private static String trackCommandLines;
   private static String untrackTextLines;
   private static String untrackHoverLines;
   private static String untrackCommandLines;
   private static String completedTextLines;
   private static String completedHoverLines;
   private static String completedCommandLines;
   private static String ongoingColorCode;
   private static String completedColorCode;
   private static String objectivesLine;
   private static String rewardsLine;
   private static String turnedInHoverLines;
   private static String killQuestDefaultSummaryLine;
   private static String fetchQuestDefaultSummaryLine;
   private static String dialogQuestDefaultSummaryLine;
   private static String arenaQuestDefaultSummaryLine;
   private static String classUnlockQuestDefaultSummaryLine;
   private static String rewardsDefaultSummaryLine;
   private static boolean useQuestTracking;

   public CustomQuestMenuConfig() {
      super("custom_quest_screen", true);
   }

   public static String getObjectiveLine(Objective objective) {
      if (objective == null) {
         return "";
      } else {
         String newString = "";
         if (objective instanceof CustomKillObjective) {
            newString = killQuestDefaultSummaryLine;
         } else if (objective instanceof DialogObjective) {
            newString = dialogQuestDefaultSummaryLine.replace("$location", safeString(((DialogObjective)objective).getTargetLocation()));
         } else if (objective instanceof CustomFetchObjective) {
            newString = fetchQuestDefaultSummaryLine;
         } else if (objective instanceof ClassUnlockObjective) {
            newString = classUnlockQuestDefaultSummaryLine;
         } else if (objective instanceof ArenaObjective) {
            String arenaFilename = ((ArenaObjective)objective).getArenaFilename();
            CustomArenasConfigFields arenaFields = CustomArenasConfig.getCustomArena(arenaFilename);
            String arenaDisplayName = arenaFields != null ? arenaFields.getArenaName() : arenaFilename;
            newString = arenaQuestDefaultSummaryLine.replace("$arenaName", safeString(arenaDisplayName));
         }

         newString = newString.replace("$name", safeObjectiveName(objective));
         newString = newString.replace("$current", objective.getCurrentAmount() + "");
         newString = newString.replace("$target", objective.getTargetAmount() + "");
         return !objective.isObjectiveCompleted() ? newString.replace("$color", ongoingColorCode) : newString.replace("$color", completedColorCode);
      }
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
               ItemMeta itemMeta = itemStack.getItemMeta();
               String rewardName = itemMeta != null && itemMeta.getDisplayName() != null && !itemMeta.getDisplayName().isEmpty()
                  ? itemMeta.getDisplayName()
                  : itemStack.getType().toString().replace("_", " ");
               textComponent.add(
                  new TextComponent(
                     rewardsDefaultSummaryLine.replace("$amount", customLootEntry.getAmount() + "")
                        .replace("$rewardName", rewardName)
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

   private static String safeObjectiveName(Objective objective) {
      if (objective != null && objective.getObjectiveName() != null) {
         String strippedName = ChatColor.stripColor(objective.getObjectiveName());
         return ChatColor.BLACK + safeString(strippedName);
      } else {
         return "";
      }
   }

   private static String safeString(String value) {
      return value == null ? "" : value;
   }

   @Override
   public void processAdditionalFields() {
      headerTextLines = ConfigurationEngine.setString(
         this.file, this.fileConfiguration, "headerTextLines2", ChatColorConverter.convert("&a&l『&c&l$questName&a&l』\n"), true
      );
      acceptTextLines = ConfigurationEngine.setString(this.file, this.fileConfiguration, "acceptTextLines", "&a&l[Accept!]", true);
      acceptHoverLines = ConfigurationEngine.setString(this.file, this.fileConfiguration, "acceptHoverLines", "&aClick to \n&aaccept quest!", true);
      acceptCommandLines = ConfigurationEngine.setString(this.file, this.fileConfiguration, "acceptCommandLines", "/em quest accept $questID", false);
      acceptedTextLines = ConfigurationEngine.setString(
         this.file, this.fileConfiguration, "acceptedTextLines", "&2&lAccepted! &aTurn in with $npcName &4[Abandon]", true
      );
      acceptedHoverLines = ConfigurationEngine.setString(this.file, this.fileConfiguration, "acceptedHoverLines", "&aClick to abandon quest!", true);
      acceptedCommandLines = ConfigurationEngine.setString(this.file, this.fileConfiguration, "acceptedCommandLines3", "/em quest leave $questID", false);
      useQuestTracking = ConfigurationEngine.setBoolean(this.fileConfiguration, "useQuestTracking", true);
      trackTextLines = ConfigurationEngine.setString(this.file, this.fileConfiguration, "trackTextLines", "&2&l[Track]", true);
      trackHoverLines = ConfigurationEngine.setString(this.file, this.fileConfiguration, "trackHoverLines", "&aClick to track quest!", true);
      trackCommandLines = ConfigurationEngine.setString(this.file, this.fileConfiguration, "trackCommandLines3", "/em quest track $questID", false);
      untrackTextLines = ConfigurationEngine.setString(this.file, this.fileConfiguration, "untrackTextLines", "&4&l[Untrack]", true);
      untrackHoverLines = ConfigurationEngine.setString(this.file, this.fileConfiguration, "untrackHoverLines", "&cClick to untrack quest!", true);
      untrackCommandLines = ConfigurationEngine.setString(this.file, this.fileConfiguration, "untrackCommandLines", "/em quest track $questID", false);
      completedTextLines = ConfigurationEngine.setString(this.file, this.fileConfiguration, "completedTextLines", "&2&l[Turn in!]", true);
      completedHoverLines = ConfigurationEngine.setString(this.file, this.fileConfiguration, "completedHoverLines", "&aClick to turn quest in!", true);
      completedCommandLines = ConfigurationEngine.setString(this.file, this.fileConfiguration, "completedCommandLines", "/em quest complete $questID", false);
      turnedInHoverLines = ConfigurationEngine.setString(this.file, this.fileConfiguration, "turnedInHoverLines", "&8Already turned in!", true);
      objectivesLine = ConfigurationEngine.setString(this.file, this.fileConfiguration, "objectivesLine", "&c&lObjectives:", true);
      killQuestDefaultSummaryLine = ConfigurationEngine.setString(
         this.file, this.fileConfiguration, "killQuestDefaultSummaryLine", "&c➤Kill $name:$color$current&0/$color$target", true
      );
      fetchQuestDefaultSummaryLine = ConfigurationEngine.setString(
         this.file, this.fileConfiguration, "fetchQuestDefaultSummaryLine", "&c➤Get $name:$color&$current&0/$color$target", true
      );
      dialogQuestDefaultSummaryLine = ConfigurationEngine.setString(
         this.file, this.fileConfiguration, "dialogQuestDefaultSummaryLine", "&c➤Go talk to $name $location", true
      );
      arenaQuestDefaultSummaryLine = ConfigurationEngine.setString(
         this.file, this.fileConfiguration, "arenaQuestDefaultSummaryLine", "&c➤Complete $arenaName", true
      );
      classUnlockQuestDefaultSummaryLine = ConfigurationEngine.setString(
         this.file, this.fileConfiguration, "classUnlockQuestDefaultSummaryLine", "&c➤Unlock $name", true
      );
      rewardsLine = ConfigurationEngine.setString(this.file, this.fileConfiguration, "rewardsLine", "&2&lRewards:", true);
      rewardsDefaultSummaryLine = ConfigurationEngine.setString(
         this.file, this.fileConfiguration, "rewardsDefaultSummaryLine", "&2➤$amountx $rewardName &8($chance%)", true
      );
      ongoingColorCode = ConfigurationEngine.setString(this.file, this.fileConfiguration, "ongoingQuestColorCode", "&c", false);
      completedColorCode = ConfigurationEngine.setString(this.file, this.fileConfiguration, "ongoingQuestColorCode", "&2", false);
   }

   @Generated
   public static String getHeaderTextLines() {
      return headerTextLines;
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
   public static String getTrackTextLines() {
      return trackTextLines;
   }

   @Generated
   public static String getTrackHoverLines() {
      return trackHoverLines;
   }

   @Generated
   public static String getTrackCommandLines() {
      return trackCommandLines;
   }

   @Generated
   public static String getUntrackTextLines() {
      return untrackTextLines;
   }

   @Generated
   public static String getUntrackHoverLines() {
      return untrackHoverLines;
   }

   @Generated
   public static String getUntrackCommandLines() {
      return untrackCommandLines;
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

   @Generated
   public static boolean isUseQuestTracking() {
      return useQuestTracking;
   }
}
