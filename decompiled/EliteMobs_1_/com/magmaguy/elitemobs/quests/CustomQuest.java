package com.magmaguy.elitemobs.quests;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.QuestAcceptEvent;
import com.magmaguy.elitemobs.api.QuestRewardEvent;
import com.magmaguy.elitemobs.config.QuestsConfig;
import com.magmaguy.elitemobs.config.customquests.CustomQuestsConfig;
import com.magmaguy.elitemobs.config.customquests.CustomQuestsConfigFields;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.quests.dialogue.QuestDialogueBossBarManager;
import com.magmaguy.elitemobs.quests.objectives.QuestObjectives;
import com.magmaguy.elitemobs.quests.playercooldowns.PlayerQuestCooldowns;
import com.magmaguy.elitemobs.quests.rewards.QuestReward;
import com.magmaguy.elitemobs.utils.EventCaller;
import java.util.List;
import java.util.UUID;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.permissions.PermissionAttachment;

public class CustomQuest extends Quest {
   private static final long serialVersionUID = -797964699830430999L;
   private final String configurationFilename;
   private transient CustomQuestsConfigFields customQuestsConfigFields;
   private transient PermissionAttachment temporaryPermissions;

   public CustomQuest(Player player, CustomQuestsConfigFields customQuestsConfigFields) {
      super(player, new QuestObjectives(new QuestReward(customQuestsConfigFields, player)), customQuestsConfigFields.getQuestLevel());
      this.customQuestsConfigFields = customQuestsConfigFields;
      this.configurationFilename = customQuestsConfigFields.getFilename();
      super.questObjectives.setQuest(this);
      super.questName = customQuestsConfigFields.getQuestName();
      super.questTaker = customQuestsConfigFields.getTurnInNPC();
   }

   public static CustomQuest getQuest(String questFilename, Player player) {
      if (!PlayerData.isInMemory(player)) {
         return null;
      } else if (CustomQuestsConfig.getCustomQuests().get(questFilename) == null) {
         return null;
      } else {
         Quest quest = null;

         for (Quest iteratedQuest : PlayerData.getQuests(player.getUniqueId())) {
            if (iteratedQuest instanceof CustomQuest && ((CustomQuest)iteratedQuest).getConfigurationFilename().equals(questFilename)) {
               quest = iteratedQuest;
               break;
            }
         }

         if (quest != null) {
            return (CustomQuest)quest;
         } else {
            CustomQuestsConfigFields fields = CustomQuestsConfig.getCustomQuests().get(questFilename);
            if (!hasPermissionForQuest(player, fields)) {
               return null;
            } else {
               for (Quest pending : pendingPlayerQuests.getOrDefault(player.getUniqueId(), List.of())) {
                  if (pending instanceof CustomQuest custom && custom.configurationFilename.equals(questFilename)) {
                     return custom;
                  }
               }

               return new CustomQuest(player, fields);
            }
         }
      }
   }

   public static Quest startQuest(String questID, Player player) {
      if (!PlayerData.isInMemory(player)) {
         return null;
      } else {
         List<Quest> pendingQuests = pendingPlayerQuests.get(player.getUniqueId());

         UUID parsedQuestID;
         try {
            parsedQuestID = UUID.fromString(questID);
         } catch (NullPointerException | IllegalArgumentException var10) {
            player.sendMessage(QuestsConfig.getInvalidQuestIdMessage().replace("$questId", String.valueOf(questID)));
            return null;
         }

         if (pendingQuests == null) {
            player.sendMessage(QuestsConfig.getInvalidQuestIdMessage().replace("$questId", questID));
            return null;
         } else {
            Quest quest = null;

            for (Quest iteratedQuest : pendingQuests) {
               if (iteratedQuest.getQuestID().equals(parsedQuestID)) {
                  quest = iteratedQuest;
                  break;
               }
            }

            if (!canAccept(player, quest)) {
               player.sendMessage(QuestsConfig.getInvalidQuestIdMessage().replace("$questId", questID));
               return null;
            } else {
               pendingQuests.remove(quest);

               Quest var13;
               try {
                  QuestAcceptEvent questAcceptEvent = new QuestAcceptEvent(player, quest);
                  new EventCaller(questAcceptEvent);
                  var13 = !questAcceptEvent.isCancelled() && quest.isAccepted() ? quest : null;
               } finally {
                  if (!quest.isAccepted() && pendingPlayerQuests.get(player.getUniqueId()) == pendingQuests && canAccept(player, quest)) {
                     pendingQuests.add(quest);
                  }

                  if (pendingQuests.isEmpty()) {
                     pendingPlayerQuests.remove(player.getUniqueId(), pendingQuests);
                  }
               }

               return var13;
            }
         }
      }
   }

   public CustomQuestsConfigFields getCustomQuestsConfigFields() {
      if (this.customQuestsConfigFields == null) {
         this.customQuestsConfigFields = CustomQuestsConfig.getCustomQuests().get(this.configurationFilename);
      }

      if (this.customQuestsConfigFields == null) {
         Logger.warn(
            "Detected that Custom Quest "
               + this.configurationFilename
               + " got removed even though player "
               + this.getPlayerUUID()
               + " is still trying to complete it. This player's quest will now be wiped."
         );
         PlayerData.removeQuest(this.getPlayerUUID(), this);
         return null;
      } else {
         return this.customQuestsConfigFields;
      }
   }

   public void applyTemporaryPermissions(Player player) {
      if (this.temporaryPermissions == null) {
         CustomQuestsConfigFields fields = this.getCustomQuestsConfigFields();
         if (fields != null && !fields.getTemporaryPermissions().isEmpty()) {
            this.temporaryPermissions = player.addAttachment(MetadataHandler.PLUGIN);

            for (String permission : fields.getTemporaryPermissions()) {
               this.temporaryPermissions.setPermission(permission, true);
            }
         }
      }
   }

   public void releaseTemporaryPermissions() {
      PermissionAttachment owned = this.temporaryPermissions;
      this.temporaryPermissions = null;
      if (owned != null) {
         owned.remove();
      }
   }

   public void applyEndPermissions(Player player) {
      this.releaseTemporaryPermissions();
      CustomQuestsConfigFields fields = this.getCustomQuestsConfigFields();
      if (fields != null && !fields.getQuestLockoutPermission().isEmpty()) {
         PlayerQuestCooldowns.addCooldown(
            player, this.getCustomQuestsConfigFields().getQuestLockoutPermission(), this.getCustomQuestsConfigFields().getQuestLockoutMinutes()
         );
      }
   }

   public boolean hasPermissionForQuest(Player player) {
      return hasPermissionForQuest(player, this.getCustomQuestsConfigFields());
   }

   public static boolean hasPermissionForQuest(Player player, CustomQuestsConfigFields customQuestsConfigFields) {
      if (customQuestsConfigFields == null || !customQuestsConfigFields.isEnabled()) {
         return false;
      } else if (PlayerQuestCooldowns.bypassesQuestRestrictions(player)) {
         return true;
      } else {
         if (customQuestsConfigFields.getQuestAcceptPermissions() != null && !customQuestsConfigFields.getQuestAcceptPermissions().isEmpty()) {
            for (String permission : customQuestsConfigFields.getQuestAcceptPermissions()) {
               if (!player.hasMetadata(permission)) {
                  return false;
               }
            }
         }

         if (!customQuestsConfigFields.getQuestAcceptPermission().isEmpty() && !player.hasMetadata(customQuestsConfigFields.getQuestAcceptPermission())) {
            return false;
         } else if (customQuestsConfigFields.getQuestLockoutMinutes() <= 0) {
            String lockoutPermission = customQuestsConfigFields.getQuestLockoutPermission();
            return lockoutPermission != null && !lockoutPermission.isEmpty() ? !player.hasMetadata(lockoutPermission) : true;
         } else {
            QuestLockout lockout = PlayerData.getQuestLockout(player.getUniqueId());
            return lockout == null || !lockout.isLockedOut(customQuestsConfigFields.getFilename());
         }
      }
   }

   @Generated
   public String getConfigurationFilename() {
      return this.configurationFilename;
   }

   public static class CustomQuestEvents implements Listener {
      @EventHandler
      public void onQuestReward(QuestRewardEvent event) {
         if (event.getQuest() instanceof CustomQuest customQuest) {
            CustomQuestsConfigFields customQuestsConfigFields = customQuest.getCustomQuestsConfigFields();
            customQuest.applyEndPermissions(event.getPlayer());
            if (customQuestsConfigFields == null) {
               return;
            }

            List<String> completeDialog = customQuestsConfigFields.getQuestCompleteDialog();
            if (completeDialog != null
               && !completeDialog.isEmpty()
               && !QuestDialogueBossBarManager.consumeRecentlyShownQuestCompleteDialog(event.getPlayer(), customQuest)
               && !QuestDialogueBossBarManager.showRawDialogue(event.getPlayer(), customQuest.getQuestName(), completeDialog, null)) {
               completeDialog.forEach(event.getPlayer()::sendMessage);
            }

            for (String command : customQuestsConfigFields.getQuestCompleteCommands()) {
               Bukkit.dispatchCommand(
                  Bukkit.getConsoleSender(),
                  command.replace("$player", event.getPlayer().getName())
                     .replace("$getWorld", event.getPlayer().getWorld().getName())
                     .replace("$getX", event.getPlayer().getLocation().getX() + "")
                     .replace("$getY", event.getPlayer().getLocation().getY() + "")
                     .replace("$getZ", event.getPlayer().getLocation().getZ() + "")
               );
            }
         }
      }
   }
}
