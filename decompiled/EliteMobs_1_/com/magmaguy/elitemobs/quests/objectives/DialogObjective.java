package com.magmaguy.elitemobs.quests.objectives;

import com.magmaguy.elitemobs.npcs.NPCEntity;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.quests.Quest;
import com.magmaguy.elitemobs.quests.dialogue.QuestDialogueBossBarManager;
import java.util.List;
import lombok.Generated;
import org.bukkit.entity.Player;

public class DialogObjective extends Objective {
   private final String targetLocation;
   private final String npcFilename;
   private final List<String> dialog;

   public DialogObjective(String npcFilename, String npcName, String targetLocation, List<String> dialog) {
      super(1, npcName);
      this.targetLocation = targetLocation;
      this.npcFilename = npcFilename;
      this.dialog = dialog;
   }

   public boolean checkProgress(Player player, QuestObjectives questObjectives) {
      if (super.currentAmount >= super.targetAmount) {
         return false;
      } else {
         this.progressObjective(questObjectives);
         if (this.dialog != null && !this.dialog.isEmpty() && !QuestDialogueBossBarManager.showRawDialogue(player, this.getObjectiveName(), this.dialog, null)) {
            this.dialog.forEach(player::sendMessage);
         }

         return true;
      }
   }

   public static boolean progressAtNPC(Player player, NPCEntity npcEntity) {
      boolean progressed = false;

      for (Quest quest : List.copyOf(PlayerData.getQuests(player.getUniqueId()))) {
         for (Objective objective : quest.getQuestObjectives().getObjectives()) {
            if (objective instanceof DialogObjective dialogue && dialogue.getNpcFilename().equals(npcEntity.getNPCsConfigFields().getFilename())) {
               progressed |= dialogue.checkProgress(player, quest.getQuestObjectives());
            }
         }
      }

      return progressed;
   }

   @Generated
   public String getTargetLocation() {
      return this.targetLocation;
   }

   @Generated
   public String getNpcFilename() {
      return this.npcFilename;
   }

   @Generated
   public List<String> getDialog() {
      return this.dialog;
   }
}
