package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.config.customarenas.CustomArenasConfig;
import com.magmaguy.elitemobs.config.customarenas.CustomArenasConfigFields;
import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import com.magmaguy.elitemobs.magmacore.config.ConfigurationFile;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.quests.objectives.ArenaObjective;
import com.magmaguy.elitemobs.quests.objectives.ClassUnlockObjective;
import com.magmaguy.elitemobs.quests.objectives.CustomFetchObjective;
import com.magmaguy.elitemobs.quests.objectives.DialogObjective;
import com.magmaguy.elitemobs.quests.objectives.KillObjective;
import com.magmaguy.elitemobs.quests.objectives.Objective;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Generated;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

public class QuestsConfig extends ConfigurationFile {
   private static final String QUEST_TURN_IN_OBJECTIVE_KEY = "questTurnInObjective";
   static final String DEFAULT_QUEST_TURN_IN_OBJECTIVE = "&aTalk to $npcName";
   private static boolean requireQuestTurnIn;
   private static String questJoinMessage;
   private static String questLeaveMessage;
   private static String questCompleteMessage;
   private static String leaveWhenNoActiveQuestsExist;
   private static boolean useQuestAcceptTitles;
   private static String questStartTitle;
   private static String questStartSubtitle;
   private static boolean useQuestCompleteTitles;
   private static String questCompleteTitle;
   private static String questCompleteSubtitle;
   private static boolean useQuestLeaveTitles;
   private static String questLeaveTitle;
   private static String questLeaveSubtitle;
   private static boolean doQuestChatProgression;
   private static String ongoingColorCode;
   private static String completedColorCode;
   private static boolean binaryObjectiveStatusEnabled;
   private static String binaryObjectiveIncompleteSymbol;
   private static String binaryObjectiveCompleteSymbol;
   private static String killQuestChatProgressionMessage;
   private static String fetchQuestChatProgressionMessage;
   private static String dialogQuestChatProgressionMessage;
   private static String classUnlockQuestChatProgressionMessage;
   private static boolean useQuestScoreboards;
   private static String killQuestScoreboardProgressionLine;
   private static String fetchQuestScoreboardProgressionLine;
   private static String dialogQuestScoreboardProgressionLine;
   private static String classUnlockQuestScoreboardProgressionLine;
   private static String arenaQuestScoreboardProgressionLine;
   private static int maximumActiveQuests;
   private static String questCapMessage;
   private static List<EntityType> questEntityTypes = new ArrayList<>();
   private static String chatTrackMessage;
   private static String chatTrackHover;
   private static String chatTrackCommand;
   private static String chatTrackingMessage;
   private static String chatTrackingHover;
   private static String chatTrackingCommand;
   private static boolean autoTrackQuestsOnAccept;
   private static boolean autoTrackNextQuestOnCompletion;
   private static String questAutoTrackNextMessage;
   private static String questAbandonConfirmationText;
   private static String questAbandonConfirmationHover;
   private static String noQuestDestinationFound;
   private static String questDestinationInOtherWorld;
   private static String questAlreadyCompletedMessage;
   private static String questPrerequisitesMissingMessage;
   private static String lowRankDynamicQuestWarning;
   private static String questTurnInObjective;
   private static int horizontalCharacterLimitBedrockMenu;
   private static int itemEntryCharacterLimitBedrockMenu;
   private static String previousInventoryPage;
   private static String nextInventoryPage;
   private static String rewardPreviewUnavailable;
   private static String questLockoutSubtitle;
   private static String questLockoutChatMessage;
   private static String invalidQuestIdMessage;
   private static String invalidQuestNpcMessage;
   private static String invalidQuestMessage;
   private static String questTrackingInvalidMessage;
   private static String questTurnInStatus;
   private static String questAcceptedStatus;
   private static String questBackToStatusMenu;
   private static String questAbandonText;
   private static boolean useQuestDialogueBossBars;
   private static boolean hideQuestScoreboardDuringQuestDialogue;
   private static int questDialogueCharactersPerLine;
   private static int questDialogueCharactersPerTick;
   private static String questDialogueNamePrefix;
   private static String questDialogueNameSuffix;
   private static String questDialogueLinePrefix;
   private static String questDialogueLineSuffix;
   private static int questDialogueBoxOffsetX;
   private static int questDialogueNameOffsetX;
   private static int questDialogueLineOffsetX;
   private static int questDialoguePromptOffsetX;
   private static String questDialoguePromptText;

   public QuestsConfig() {
      super("Quests.yml");
   }

   private static List<EntityType> setEntityTypes(FileConfiguration fileConfiguration, File file) {
      List<String> entityTypes = new ArrayList<>(
         new ArrayList<>(
            List.of(
               EntityType.BLAZE.toString(),
               EntityType.CAVE_SPIDER.toString(),
               EntityType.DROWNED.toString(),
               EntityType.ELDER_GUARDIAN.toString(),
               EntityType.ENDERMAN.toString(),
               EntityType.ENDERMITE.toString(),
               EntityType.EVOKER.toString(),
               EntityType.GHAST.toString(),
               EntityType.GUARDIAN.toString(),
               EntityType.HUSK.toString(),
               EntityType.ILLUSIONER.toString(),
               EntityType.IRON_GOLEM.toString(),
               EntityType.PILLAGER.toString(),
               EntityType.RAVAGER.toString(),
               EntityType.SILVERFISH.toString(),
               EntityType.SKELETON.toString(),
               EntityType.SPIDER.toString(),
               EntityType.STRAY.toString(),
               EntityType.VINDICATOR.toString(),
               EntityType.WITCH.toString(),
               EntityType.WITHER_SKELETON.toString(),
               EntityType.WOLF.toString(),
               EntityType.ZOMBIE.toString()
            )
         )
      );
      List<String> laterEntities = new ArrayList<>(
         List.of(
            EntityType.HOGLIN.toString(),
            EntityType.ZOGLIN.toString(),
            EntityType.PIGLIN_BRUTE.toString(),
            EntityType.PIGLIN.toString(),
            EntityType.ZOMBIFIED_PIGLIN.toString()
         )
      );
      entityTypes.addAll(laterEntities);
      List<?> configuredTypes = ConfigurationEngine.setList(file, fileConfiguration, "questEntityTypes", entityTypes, false);
      List<EntityType> parsedTypes = new ArrayList<>();

      for (Object configured : configuredTypes) {
         try {
            EntityType type = EntityType.valueOf(String.valueOf(configured));
            if (!MobPropertiesConfig.getMobProperties().containsKey(type)) {
               throw new IllegalArgumentException("Not supported for dynamic kill quests");
            }

            parsedTypes.add(type);
         } catch (IllegalArgumentException var9) {
            Logger.warn("Entity type " + configured + " is not supported for dynamic quests.");
         }
      }

      if (parsedTypes.isEmpty()) {
         Logger.warn("questEntityTypes has no usable entries. Dynamic quest offers are disabled until this is corrected.");
      }

      return parsedTypes;
   }

   public static String getQuestChatProgressionMessage(Objective objective) {
      String newString = "";
      if (objective instanceof KillObjective) {
         newString = killQuestChatProgressionMessage;
      } else if (objective instanceof CustomFetchObjective) {
         newString = fetchQuestChatProgressionMessage;
      } else if (objective instanceof DialogObjective) {
         newString = dialogQuestChatProgressionMessage;
      } else if (objective instanceof ClassUnlockObjective) {
         newString = classUnlockQuestChatProgressionMessage;
      }

      newString = newString.replace("$name", safeObjectiveName(objective));
      newString = newString.replace("$current", objective.getCurrentAmount() + "");
      newString = newString.replace("$target", objective.getTargetAmount() + "");
      return !objective.isObjectiveCompleted() ? newString.replace("$color", ongoingColorCode) : newString.replace("$color", completedColorCode);
   }

   public static String getQuestScoreboardProgressionLine(Objective objective) {
      if (objective == null) {
         Logger.warn("Objective is null!");
      }

      String newString = "";
      if (objective instanceof KillObjective) {
         newString = killQuestScoreboardProgressionLine;
      } else if (objective instanceof CustomFetchObjective) {
         newString = fetchQuestScoreboardProgressionLine;
      } else if (objective instanceof DialogObjective) {
         newString = dialogQuestScoreboardProgressionLine;
      } else if (objective instanceof ClassUnlockObjective) {
         newString = classUnlockQuestScoreboardProgressionLine;
      } else if (objective instanceof ArenaObjective arenaObjective) {
         CustomArenasConfigFields arenaFields = CustomArenasConfig.getCustomArena(arenaObjective.getArenaFilename());
         String arenaDisplayName = arenaFields != null && arenaFields.getArenaName() != null ? arenaFields.getArenaName() : arenaObjective.getArenaFilename();
         newString = arenaQuestScoreboardProgressionLine.replace("$arenaName", arenaDisplayName);
      }

      if (binaryObjectiveStatusEnabled && objective != null && !(objective instanceof ArenaObjective) && objective.getTargetAmount() <= 1) {
         String label = binaryObjectiveLabel(newString);
         String body = (label.isEmpty() ? "" : label + " ") + ChatColor.stripColor(safeObjectiveName(objective));
         return objective.isObjectiveCompleted()
            ? binaryObjectiveCompleteSymbol + " " + completedColorCode + ChatColor.STRIKETHROUGH + body
            : binaryObjectiveIncompleteSymbol + " " + body;
      } else {
         newString = newString.replace("$name", safeObjectiveName(objective));
         newString = newString.replace("$current", objective.getCurrentAmount() + "");
         newString = newString.replace("$target", objective.getTargetAmount() + "");
         return !objective.isObjectiveCompleted() ? newString.replace("$color", ongoingColorCode) : newString.replace("$color", completedColorCode);
      }
   }

   private static String binaryObjectiveLabel(String template) {
      if (template == null) {
         return "";
      } else {
         int index = template.indexOf("$name");
         String before = index >= 0 ? template.substring(0, index) : template;
         before = ChatColor.stripColor(before).replaceAll("^[^\\p{L}]+", "");
         return before.trim();
      }
   }

   private static String safeObjectiveName(Objective objective) {
      if (objective != null && objective.getObjectiveName() != null) {
         String strippedName = ChatColor.stripColor(objective.getObjectiveName());
         return ChatColor.WHITE + (strippedName == null ? "" : strippedName);
      } else {
         return "";
      }
   }

   static String repairLegacyTurnInObjective(String value) {
      if (value == null || !value.contains("$npcName")) {
         return value;
      } else if (value.startsWith("&a2")) {
         return "&a" + value.substring(3);
      } else {
         String convertedPrefix = ChatColor.GREEN + "2";
         return value.startsWith(convertedPrefix) ? ChatColor.GREEN + value.substring(convertedPrefix.length()) : value;
      }
   }

   static void repairStoredLegacyTurnInObjective(FileConfiguration configuration) {
      String configured = configuration.getString("questTurnInObjective");
      String repaired = repairLegacyTurnInObjective(configured);
      if (!Objects.equals(configured, repaired)) {
         configuration.set("questTurnInObjective", repaired);
      }
   }

   @Override
   public void initializeValues() {
      this.fileConfiguration.set("hideKeyedBossBarsDuringQuestDialogue", null);
      requireQuestTurnIn = ConfigurationEngine.setBoolean(
         List.of("Sets if quests have to be returned to quest givers to complete the quest."), this.fileConfiguration, "requireQuestTurnIn", true
      );
      questJoinMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when a quest is accepted."),
         this.file,
         this.fileConfiguration,
         "questJoinMessage",
         "&aYou have accepted the quest $questName &a!",
         true
      );
      questLeaveMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when a quest is abandoned."),
         this.file,
         this.fileConfiguration,
         "questLeaveMessage",
         "&cYou have abandoned the quest $questName &c!",
         true
      );
      questCompleteMessage = ConfigurationEngine.setString(
         List.of("Sets message sent when a quest is completed."),
         this.file,
         this.fileConfiguration,
         "questCompleteMessage",
         "&2You completed the quest $questName &2!",
         true
      );
      leaveWhenNoActiveQuestsExist = ConfigurationEngine.setString(
         List.of("Sets the message sent when a player tried to abandon a quest when none are active."),
         this.file,
         this.fileConfiguration,
         "leaveWhenNoActiveQuestsExist",
         "&cYou don't currently have an active quest!",
         true
      );
      useQuestAcceptTitles = ConfigurationEngine.setBoolean(
         List.of("Sets if some of the messages related to quests will use titles instead of chat messages."),
         this.fileConfiguration,
         "useQuestAcceptTitles",
         true
      );
      questStartTitle = ConfigurationEngine.setString(
         List.of("Sets the title sent when a players starts a quest."), this.file, this.fileConfiguration, "questStartTitle", "&aQuest Accepted!", true
      );
      questStartSubtitle = ConfigurationEngine.setString(
         List.of("Sets the subtitle sent when a players starts a quest."), this.file, this.fileConfiguration, "questStartSubtitle", "$questName", false
      );
      useQuestCompleteTitles = ConfigurationEngine.setBoolean(
         List.of("Sets if titles will be sent when players complete a quest."), this.fileConfiguration, "useQuestCompleteTitles", true
      );
      questCompleteTitle = ConfigurationEngine.setString(
         List.of("Sets the title sent when a player completes a quest."), this.file, this.fileConfiguration, "questCompleteTitle", "&2Quest Completed!", true
      );
      questCompleteSubtitle = ConfigurationEngine.setString(
         List.of("Sets the subtitle sent when a player completes a quest."), this.file, this.fileConfiguration, "questCompleteSubtitle", "$questName", true
      );
      useQuestLeaveTitles = ConfigurationEngine.setBoolean(
         List.of("Sets if titles are sent when players leave quests.."), this.fileConfiguration, "useQuestLeaveTitles", true
      );
      questLeaveTitle = ConfigurationEngine.setString(
         List.of("Sets the title sent when a player leaves a quest."), this.file, this.fileConfiguration, "questLeaveTitle", "&cQuest Abandoned!", true
      );
      questLeaveSubtitle = ConfigurationEngine.setString(
         List.of("Sets the subtitle sent when a player leaves a quest."), this.file, this.fileConfiguration, "questLeaveSubtitle", "$questName", false
      );
      doQuestChatProgression = ConfigurationEngine.setBoolean(
         List.of("Sets if messages are sent on chat reporting quest objective progression."), this.fileConfiguration, "doQuestChatProgression", true
      );
      ongoingColorCode = ConfigurationEngine.setString(
         List.of("Sets the color codes for ongoing objectives."), this.file, this.fileConfiguration, "ongoingQuestColorCode", "&c", false
      );
      completedColorCode = ConfigurationEngine.setString(
         List.of("Sets the color codes for completed objectives."), this.file, this.fileConfiguration, "completedQuestColorCode", "&a", false
      );
      binaryObjectiveStatusEnabled = ConfigurationEngine.setBoolean(
         List.of(
            "Sets if single-step scoreboard objectives (target of 1, e.g. 'talk to') show a status symbol",
            "instead of a meaningless '0/1' counter. Completed objectives are shown struck through."
         ),
         this.fileConfiguration,
         "binaryObjectiveStatusEnabled",
         true
      );
      binaryObjectiveIncompleteSymbol = ConfigurationEngine.setString(
         List.of("Symbol shown before an incomplete single-step objective when binaryObjectiveStatusEnabled is true."),
         this.file,
         this.fileConfiguration,
         "binaryObjectiveIncompleteSymbol",
         "&7➤",
         false
      );
      binaryObjectiveCompleteSymbol = ConfigurationEngine.setString(
         List.of("Symbol shown before a completed single-step objective when binaryObjectiveStatusEnabled is true."),
         this.file,
         this.fileConfiguration,
         "binaryObjectiveCompleteSymbol",
         "&a✔",
         false
      );
      killQuestChatProgressionMessage = ConfigurationEngine.setString(
         List.of("Sets the formatting for progression messages of kill quests."),
         this.file,
         this.fileConfiguration,
         "killQuestChatProgressionMessage",
         "&8[EliteMobs] &7➤ Kill &f$name&7: $color$current&7/$color$target",
         true
      );
      fetchQuestChatProgressionMessage = ConfigurationEngine.setString(
         List.of("Sets the formatting for progression messages of fetch quests."),
         this.file,
         this.fileConfiguration,
         "fetchQuestChatProgressionMessage",
         "&8[EliteMobs] &7➤ Get &f$name&7: $color$current&7/$color$target",
         true
      );
      dialogQuestChatProgressionMessage = ConfigurationEngine.setString(
         List.of("Sets the formatting for progression messages of dialog quests."),
         this.file,
         this.fileConfiguration,
         "dialogQuestChatProgressionMessage",
         "&8[EliteMobs] &7➤ Talk to &f$name&7: $color$current&7/$color$target",
         true
      );
      maximumActiveQuests = ConfigurationEngine.setInt(
         List.of("Sets the maximum amount of accepted quests a player can have."), this.fileConfiguration, "maximumActiveQuests", 10
      );
      questCapMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent to players when trying to accept an amount of quests that exceed the active quest cap."),
         this.file,
         this.fileConfiguration,
         "questCapMessage",
         "&8[EliteMobs] &cYou have reached the maximum amount of active quests (10)! &4Abandon or complete at least one active quest if you want to get more quests!",
         true
      );
      useQuestScoreboards = ConfigurationEngine.setBoolean(
         List.of("Sets if scoreboards messages will be used for quests."), this.fileConfiguration, "useQuestScoreboards", true
      );
      killQuestScoreboardProgressionLine = ConfigurationEngine.setString(
         List.of("Sets the formatting for scoreboard progression messages of kill quests."),
         this.file,
         this.fileConfiguration,
         "killQuestScoreboardProgressionMessage",
         "&7➤ Kill &f$name&7: $color$current&7/$color$target",
         true
      );
      fetchQuestScoreboardProgressionLine = ConfigurationEngine.setString(
         List.of("Sets the formatting for scoreboard progression message of fetch quests."),
         this.file,
         this.fileConfiguration,
         "fetchQuestScoreboardProgressionMessage",
         "&7➤ Get &f$name&7: $color$current&7/$color$target",
         true
      );
      dialogQuestScoreboardProgressionLine = ConfigurationEngine.setString(
         List.of("Sets the formatting for scoreboard progression messages of dialog quests."),
         this.file,
         this.fileConfiguration,
         "dialogQuestScoreboardProgressionMessage",
         "&7➤ Talk to &f$name&7: $color$current&7/$color$target",
         true
      );
      classUnlockQuestChatProgressionMessage = ConfigurationEngine.setString(
         List.of("Progress message for unlocking a class."),
         this.file,
         this.fileConfiguration,
         "classUnlockQuestChatProgressionMessage",
         "&8[EliteMobs] &7Unlock &f$name&7: $color$current&7/$color$target",
         true
      );
      classUnlockQuestScoreboardProgressionLine = ConfigurationEngine.setString(
         List.of("Scoreboard objective for unlocking a class."),
         this.file,
         this.fileConfiguration,
         "classUnlockQuestScoreboardProgressionMessage",
         "&7Unlock &f$name&7: $color$current&7/$color$target",
         true
      );
      arenaQuestScoreboardProgressionLine = ConfigurationEngine.setString(
         List.of("Sets the formatting for scoreboard progression messages of arena quests."),
         this.file,
         this.fileConfiguration,
         "arenaQuestScoreboardProgressionMessage",
         "&7➤ Complete &f$arenaName",
         true
      );
      questEntityTypes = setEntityTypes(this.fileConfiguration, this.file);
      chatTrackMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent to players to activate quest tracking."),
         this.file,
         this.fileConfiguration,
         "chatTrackMessage",
         "&8[EliteMobs]&2 Click here to track your quest!",
         true
      );
      chatTrackHover = ConfigurationEngine.setString(
         List.of("Sets the hover message of the quest tracking message."), this.file, this.fileConfiguration, "chatTrackHover", "&2Click to track!", true
      );
      chatTrackCommand = ConfigurationEngine.setString(
         List.of("Sets the command sent when clicking on the tracking message. You really should not modify this."),
         this.file,
         this.fileConfiguration,
         "chatTrackCommand",
         "/elitemobs quest track $questID",
         false
      );
      chatTrackingMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent to players when activating quest tracking."),
         this.file,
         this.fileConfiguration,
         "chatTrackingMessage",
         "&8[EliteMobs]&9 You are now tracking a quest!",
         true
      );
      chatTrackingHover = ConfigurationEngine.setString(
         List.of("Sets the hover message of the quest tracking enabled message."),
         this.file,
         this.fileConfiguration,
         "chatTrackingHover",
         "&2Click to untrack/track! /em -> Quests to track a different quest!",
         true
      );
      chatTrackingCommand = ConfigurationEngine.setString(
         List.of("Sets the command sent when click on the quest tracking activation message. You really should not modify this."),
         this.file,
         this.fileConfiguration,
         "chatTrackingCommand",
         "/elitemobs quest track $questID",
         false
      );
      autoTrackQuestsOnAccept = ConfigurationEngine.setBoolean(
         List.of("Sets if quests are automatically tracked when accepted."), this.fileConfiguration, "autoTrackQuestsOnAccept", true
      );
      autoTrackNextQuestOnCompletion = ConfigurationEngine.setBoolean(
         List.of("Sets if completing the tracked quest automatically starts tracking another active quest."),
         this.fileConfiguration,
         "autoTrackNextQuestOnCompletion",
         true
      );
      questAutoTrackNextMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when tracking switches to the next active quest after completing one."),
         this.file,
         this.fileConfiguration,
         "questAutoTrackNextMessage",
         "&8[EliteMobs]&9 Now tracking: &f$questName",
         true
      );
      questAbandonConfirmationText = ConfigurationEngine.setString(
         List.of(
            "Sets the confirmation message shown when a player clicks to abandon a quest.",
            "Abandoning starts the quest's full cooldown, so it requires a second click to confirm."
         ),
         this.file,
         this.fileConfiguration,
         "questAbandonConfirmationText",
         "&cAbandoning &f$questName &cstarts its full cooldown! &4&l[Click here to confirm abandoning]",
         true
      );
      questAbandonConfirmationHover = ConfigurationEngine.setString(
         List.of("Sets the hover message of the quest abandon confirmation."),
         this.file,
         this.fileConfiguration,
         "questAbandonConfirmationHover",
         "&cThis cannot be undone!",
         true
      );
      noQuestDestinationFound = ConfigurationEngine.setString(
         List.of("Sets the message that appears when the destination of the quest could not be found."),
         this.file,
         this.fileConfiguration,
         "noQuestDestinationFound",
         "[EM] No quest destination found!",
         true
      );
      questDestinationInOtherWorld = ConfigurationEngine.setString(
         List.of("Sets the message that appears when the quest target is in a different world."),
         this.file,
         this.fileConfiguration,
         "questDestinationInOtherWorld",
         "[EM] Go to world $world!",
         true
      );
      questAlreadyCompletedMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent to players when trying to accept a quest they have already completed."),
         this.file,
         this.fileConfiguration,
         "questAlreadyCompletedMessage",
         "&8[EliteMobs] &cYou already completed this quest!",
         true
      );
      questPrerequisitesMissingMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent to players when trying to accept a quest they do not have the permission for."),
         this.file,
         this.fileConfiguration,
         "questPrerequisitesMissingMessage",
         "&8[EliteMobs] &cThis NPC has some quest(s) you can't accept yet!",
         true
      );
      lowRankDynamicQuestWarning = ConfigurationEngine.setString(
         List.of("Sets the message sent to players trying to get a quest for which they do not have the correct guild rank activated."),
         this.file,
         this.fileConfiguration,
         "lowRankDynamicQuestWarning",
         "&8[EliteMobs] &cYou can't take these quests with your current guild rank! Increase your guild rank to accept these quests.",
         true
      );
      repairStoredLegacyTurnInObjective(this.fileConfiguration);
      questTurnInObjective = repairLegacyTurnInObjective(
         ConfigurationEngine.setString(
            List.of("Sets the formatting for the quest turn-in message."),
            this.file,
            this.fileConfiguration,
            "questTurnInObjective",
            "&aTalk to $npcName",
            true
         )
      );
      horizontalCharacterLimitBedrockMenu = ConfigurationEngine.setInt(
         List.of("Sets the maximum amount of characters inventory-based menus for quests will have before breaking the line."),
         this.fileConfiguration,
         "horizontalCharacterLimitBedrockMenu",
         30
      );
      itemEntryCharacterLimitBedrockMenu = ConfigurationEngine.setInt(
         List.of("Sets the maximum amount of characters per item entry in inventory-based menus for quests before creating another item to continue the entry."),
         this.fileConfiguration,
         "itemEntryCharacterLimitBedrockMenu",
         300
      );
      previousInventoryPage = ConfigurationEngine.setString(
         List.of("Previous page button in inventory quest menus"), this.file, this.fileConfiguration, "previousInventoryPage", "Previous page", true
      );
      nextInventoryPage = ConfigurationEngine.setString(
         List.of("Next page button in inventory quest menus"), this.file, this.fileConfiguration, "nextInventoryPage", "Next page", true
      );
      rewardPreviewUnavailable = ConfigurationEngine.setString(
         List.of("Shown when a quest reward preview cannot be generated"),
         this.file,
         this.fileConfiguration,
         "rewardPreviewUnavailable",
         "Reward preview unavailable.",
         true
      );
      questLockoutSubtitle = ConfigurationEngine.setString(
         List.of("Sets the subtitle shown when a player tries to accept a quest they are locked out from"),
         this.file,
         this.fileConfiguration,
         "questLockoutSubtitle",
         "&cQuest Lockout!",
         true
      );
      questLockoutChatMessage = ConfigurationEngine.setString(
         List.of(
            "Sets the chat message shown when a player tries to accept a quest they are locked out from",
            "$questName is the placeholder for the quest name",
            "$remainingTime is the placeholder for the remaining lockout time"
         ),
         this.file,
         this.fileConfiguration,
         "questLockoutChatMessage",
         "&c[EliteMobs] &7You completed &c$questName &7recently and must wait another &e$remainingTime &7before you can accept it again.",
         true
      );
      invalidQuestIdMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when a player tries to start a quest with an invalid ID.", "$questId is the placeholder for the quest ID"),
         this.file,
         this.fileConfiguration,
         "invalidQuestIdMessage",
         "&8[EliteMobs] &cInvalid quest ID for ID $questId",
         true
      );
      invalidQuestNpcMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when an NPC has an invalid quest configuration."),
         this.file,
         this.fileConfiguration,
         "invalidQuestNpcMessage",
         "[EliteMobs] This NPC's quest is not valid! This might be a configuration error on the NPC or on the quest.",
         true
      );
      invalidQuestMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent to admins when a specific quest filename is invalid.", "$quest is the placeholder for the quest filename"),
         this.file,
         this.fileConfiguration,
         "invalidQuestMessage",
         "Invalid quest: $quest",
         true
      );
      questTrackingInvalidMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when quest tracking fails due to an invalid quest ID."),
         this.file,
         this.fileConfiguration,
         "questTrackingInvalidMessage",
         "[EliteMobs] Failed to get a valid quest with that quest ID!",
         true
      );
      questTurnInStatus = ConfigurationEngine.setString(
         List.of("Sets the status suffix shown when a quest is ready to turn in."),
         this.file,
         this.fileConfiguration,
         "questTurnInStatus",
         " §f| §2Turn in!",
         true
      );
      questAcceptedStatus = ConfigurationEngine.setString(
         List.of("Sets the status suffix shown when a quest is accepted."), this.file, this.fileConfiguration, "questAcceptedStatus", " §f| §aAccepted", true
      );
      questBackToStatusMenu = ConfigurationEngine.setString(
         List.of("Sets the text for the back button to return to the status menu."),
         this.file,
         this.fileConfiguration,
         "questBackToStatusMenu",
         "← Back to Status Menu",
         true
      );
      questAbandonText = ConfigurationEngine.setString(
         List.of("Sets the text shown on the abandon quest button."), this.file, this.fileConfiguration, "questAbandonText", "§l§c[Abandon]", true
      );
      useQuestDialogueBossBars = ConfigurationEngine.setBoolean(
         List.of("Experimental: shows quest NPC text through top-anchored boss bars before opening the quest menu."),
         this.fileConfiguration,
         "useQuestDialogueBossBars",
         true
      );
      hideQuestScoreboardDuringQuestDialogue = ConfigurationEngine.setBoolean(
         List.of(
            "Experimental: hides the quest tracking scoreboard while quest dialogue is active.",
            "Boss bars are always hidden because the dialogue layout requires exclusive control of that area."
         ),
         this.fileConfiguration,
         "hideQuestScoreboardDuringQuestDialogue",
         true
      );
      questDialogueCharactersPerLine = ConfigurationEngine.setInt(
         List.of("Experimental: maximum visible characters per boss-bar dialogue line before wrapping."),
         this.fileConfiguration,
         "questDialogueCharactersPerLineV2",
         52
      );
      questDialogueCharactersPerTick = ConfigurationEngine.setInt(
         List.of("Experimental: visible characters added per tick while quest dialogue types in."), this.fileConfiguration, "questDialogueCharactersPerTick", 2
      );
      questDialogueNamePrefix = ConfigurationEngine.setString(
         List.of("Experimental: prefix placed before the NPC name boss-bar title. Use this for custom font offset glyphs."),
         this.file,
         this.fileConfiguration,
         "questDialogueNamePrefix",
         "",
         false
      );
      questDialogueNameSuffix = ConfigurationEngine.setString(
         List.of("Experimental: suffix placed after the NPC name boss-bar title. Use this for custom font offset glyphs."),
         this.file,
         this.fileConfiguration,
         "questDialogueNameSuffix",
         "",
         false
      );
      questDialogueLinePrefix = ConfigurationEngine.setString(
         List.of("Experimental: prefix placed before each dialogue line boss-bar title. Use this for custom font offset glyphs."),
         this.file,
         this.fileConfiguration,
         "questDialogueLinePrefix",
         "",
         false
      );
      questDialogueLineSuffix = ConfigurationEngine.setString(
         List.of("Experimental: suffix placed after each dialogue line boss-bar title. Use this for custom font offset glyphs."),
         this.file,
         this.fileConfiguration,
         "questDialogueLineSuffix",
         "",
         false
      );
      questDialogueBoxOffsetX = ConfigurationEngine.setInt(
         List.of(
            "Experimental: horizontal pixel shift of the dialogue box image (positive = right).",
            "Default 0 keeps the box aligned with the shift-click quest dialogue menu art."
         ),
         this.fileConfiguration,
         "questDialogueBoxOffetXV2",
         0
      );
      questDialogueNameOffsetX = ConfigurationEngine.setInt(
         List.of("Experimental: horizontal pixel shift of the NPC name line (negative = left)."), this.fileConfiguration, "questDialogueNameOffsetXV2", -90
      );
      questDialogueLineOffsetX = ConfigurationEngine.setInt(
         List.of("Experimental: horizontal pixel shift of each dialogue body line (negative = left)."), this.fileConfiguration, "questDialogueLineOffsetX", 0
      );
      questDialoguePromptOffsetX = ConfigurationEngine.setInt(
         List.of("Experimental: horizontal pixel shift of the 'sneak to continue' prompt line (positive = right)."),
         this.fileConfiguration,
         "questDialoguePromptOffsetXV2",
         74
      );
      questDialoguePromptText = ConfigurationEngine.setString(
         List.of("Experimental: text shown on the prompt line of the dialogue box telling players how to advance."),
         this.file,
         this.fileConfiguration,
         "questDialoguePromptText",
         "&7&oSneak to continue",
         true
      );
   }

   @Generated
   public static boolean isRequireQuestTurnIn() {
      return requireQuestTurnIn;
   }

   @Generated
   public static String getQuestJoinMessage() {
      return questJoinMessage;
   }

   @Generated
   public static String getQuestLeaveMessage() {
      return questLeaveMessage;
   }

   @Generated
   public static String getQuestCompleteMessage() {
      return questCompleteMessage;
   }

   @Generated
   public static String getLeaveWhenNoActiveQuestsExist() {
      return leaveWhenNoActiveQuestsExist;
   }

   @Generated
   public static boolean isUseQuestAcceptTitles() {
      return useQuestAcceptTitles;
   }

   @Generated
   public static String getQuestStartTitle() {
      return questStartTitle;
   }

   @Generated
   public static String getQuestStartSubtitle() {
      return questStartSubtitle;
   }

   @Generated
   public static boolean isUseQuestCompleteTitles() {
      return useQuestCompleteTitles;
   }

   @Generated
   public static String getQuestCompleteTitle() {
      return questCompleteTitle;
   }

   @Generated
   public static String getQuestCompleteSubtitle() {
      return questCompleteSubtitle;
   }

   @Generated
   public static boolean isUseQuestLeaveTitles() {
      return useQuestLeaveTitles;
   }

   @Generated
   public static String getQuestLeaveTitle() {
      return questLeaveTitle;
   }

   @Generated
   public static String getQuestLeaveSubtitle() {
      return questLeaveSubtitle;
   }

   @Generated
   public static boolean isDoQuestChatProgression() {
      return doQuestChatProgression;
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
   public static boolean isBinaryObjectiveStatusEnabled() {
      return binaryObjectiveStatusEnabled;
   }

   @Generated
   public static String getBinaryObjectiveIncompleteSymbol() {
      return binaryObjectiveIncompleteSymbol;
   }

   @Generated
   public static String getBinaryObjectiveCompleteSymbol() {
      return binaryObjectiveCompleteSymbol;
   }

   @Generated
   public static String getKillQuestChatProgressionMessage() {
      return killQuestChatProgressionMessage;
   }

   @Generated
   public static String getFetchQuestChatProgressionMessage() {
      return fetchQuestChatProgressionMessage;
   }

   @Generated
   public static String getDialogQuestChatProgressionMessage() {
      return dialogQuestChatProgressionMessage;
   }

   @Generated
   public static String getClassUnlockQuestChatProgressionMessage() {
      return classUnlockQuestChatProgressionMessage;
   }

   @Generated
   public static boolean isUseQuestScoreboards() {
      return useQuestScoreboards;
   }

   @Generated
   public static String getKillQuestScoreboardProgressionLine() {
      return killQuestScoreboardProgressionLine;
   }

   @Generated
   public static String getFetchQuestScoreboardProgressionLine() {
      return fetchQuestScoreboardProgressionLine;
   }

   @Generated
   public static String getDialogQuestScoreboardProgressionLine() {
      return dialogQuestScoreboardProgressionLine;
   }

   @Generated
   public static String getClassUnlockQuestScoreboardProgressionLine() {
      return classUnlockQuestScoreboardProgressionLine;
   }

   @Generated
   public static String getArenaQuestScoreboardProgressionLine() {
      return arenaQuestScoreboardProgressionLine;
   }

   @Generated
   public static int getMaximumActiveQuests() {
      return maximumActiveQuests;
   }

   @Generated
   public static String getQuestCapMessage() {
      return questCapMessage;
   }

   @Generated
   public static List<EntityType> getQuestEntityTypes() {
      return questEntityTypes;
   }

   @Generated
   public static String getChatTrackMessage() {
      return chatTrackMessage;
   }

   @Generated
   public static String getChatTrackHover() {
      return chatTrackHover;
   }

   @Generated
   public static String getChatTrackCommand() {
      return chatTrackCommand;
   }

   @Generated
   public static String getChatTrackingMessage() {
      return chatTrackingMessage;
   }

   @Generated
   public static String getChatTrackingHover() {
      return chatTrackingHover;
   }

   @Generated
   public static String getChatTrackingCommand() {
      return chatTrackingCommand;
   }

   @Generated
   public static boolean isAutoTrackQuestsOnAccept() {
      return autoTrackQuestsOnAccept;
   }

   @Generated
   public static boolean isAutoTrackNextQuestOnCompletion() {
      return autoTrackNextQuestOnCompletion;
   }

   @Generated
   public static String getQuestAutoTrackNextMessage() {
      return questAutoTrackNextMessage;
   }

   @Generated
   public static String getQuestAbandonConfirmationText() {
      return questAbandonConfirmationText;
   }

   @Generated
   public static String getQuestAbandonConfirmationHover() {
      return questAbandonConfirmationHover;
   }

   @Generated
   public static String getNoQuestDestinationFound() {
      return noQuestDestinationFound;
   }

   @Generated
   public static String getQuestDestinationInOtherWorld() {
      return questDestinationInOtherWorld;
   }

   @Generated
   public static String getQuestAlreadyCompletedMessage() {
      return questAlreadyCompletedMessage;
   }

   @Generated
   public static String getQuestPrerequisitesMissingMessage() {
      return questPrerequisitesMissingMessage;
   }

   @Generated
   public static String getLowRankDynamicQuestWarning() {
      return lowRankDynamicQuestWarning;
   }

   @Generated
   public static String getQuestTurnInObjective() {
      return questTurnInObjective;
   }

   @Generated
   public static int getHorizontalCharacterLimitBedrockMenu() {
      return horizontalCharacterLimitBedrockMenu;
   }

   @Generated
   public static int getItemEntryCharacterLimitBedrockMenu() {
      return itemEntryCharacterLimitBedrockMenu;
   }

   @Generated
   public static String getPreviousInventoryPage() {
      return previousInventoryPage;
   }

   @Generated
   public static String getNextInventoryPage() {
      return nextInventoryPage;
   }

   @Generated
   public static String getRewardPreviewUnavailable() {
      return rewardPreviewUnavailable;
   }

   @Generated
   public static String getQuestLockoutSubtitle() {
      return questLockoutSubtitle;
   }

   @Generated
   public static String getQuestLockoutChatMessage() {
      return questLockoutChatMessage;
   }

   @Generated
   public static String getInvalidQuestIdMessage() {
      return invalidQuestIdMessage;
   }

   @Generated
   public static String getInvalidQuestNpcMessage() {
      return invalidQuestNpcMessage;
   }

   @Generated
   public static String getInvalidQuestMessage() {
      return invalidQuestMessage;
   }

   @Generated
   public static String getQuestTrackingInvalidMessage() {
      return questTrackingInvalidMessage;
   }

   @Generated
   public static String getQuestTurnInStatus() {
      return questTurnInStatus;
   }

   @Generated
   public static String getQuestAcceptedStatus() {
      return questAcceptedStatus;
   }

   @Generated
   public static String getQuestBackToStatusMenu() {
      return questBackToStatusMenu;
   }

   @Generated
   public static String getQuestAbandonText() {
      return questAbandonText;
   }

   @Generated
   public static boolean isUseQuestDialogueBossBars() {
      return useQuestDialogueBossBars;
   }

   @Generated
   public static boolean isHideQuestScoreboardDuringQuestDialogue() {
      return hideQuestScoreboardDuringQuestDialogue;
   }

   @Generated
   public static int getQuestDialogueCharactersPerLine() {
      return questDialogueCharactersPerLine;
   }

   @Generated
   public static int getQuestDialogueCharactersPerTick() {
      return questDialogueCharactersPerTick;
   }

   @Generated
   public static String getQuestDialogueNamePrefix() {
      return questDialogueNamePrefix;
   }

   @Generated
   public static String getQuestDialogueNameSuffix() {
      return questDialogueNameSuffix;
   }

   @Generated
   public static String getQuestDialogueLinePrefix() {
      return questDialogueLinePrefix;
   }

   @Generated
   public static String getQuestDialogueLineSuffix() {
      return questDialogueLineSuffix;
   }

   @Generated
   public static int getQuestDialogueBoxOffsetX() {
      return questDialogueBoxOffsetX;
   }

   @Generated
   public static int getQuestDialogueNameOffsetX() {
      return questDialogueNameOffsetX;
   }

   @Generated
   public static int getQuestDialogueLineOffsetX() {
      return questDialogueLineOffsetX;
   }

   @Generated
   public static int getQuestDialoguePromptOffsetX() {
      return questDialoguePromptOffsetX;
   }

   @Generated
   public static String getQuestDialoguePromptText() {
      return questDialoguePromptText;
   }
}
