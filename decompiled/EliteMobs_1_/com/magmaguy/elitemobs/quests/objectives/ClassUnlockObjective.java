package com.magmaguy.elitemobs.quests.objectives;

import com.magmaguy.elitemobs.advancedcombat.AdvancedCombatModule;
import com.magmaguy.elitemobs.api.QuestAcceptEvent;
import com.magmaguy.elitemobs.api.QuestCompleteEvent;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.quests.Quest;
import java.util.List;
import lombok.Generated;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public final class ClassUnlockObjective extends Objective {
   private static final long serialVersionUID = 1L;
   private final String classId;
   private final String npcFilename;

   public ClassUnlockObjective(String classId, String className, String npcFilename) {
      super(1, className);
      this.classId = classId;
      this.npcFilename = npcFilename;
   }

   private boolean isUnlocked(Player player) {
      return this.classId != null && !this.classId.isBlank() && AdvancedCombatModule.isInitialized()
         ? AdvancedCombatModule.get()
            .profile(player.getUniqueId())
            .map(profile -> profile.forms().get(this.classId))
            .map(form -> form.unlocked())
            .orElse(false)
         : false;
   }

   @Override
   public void progressNonlinearObjective(QuestObjectives objectives, Player player) {
      if (!this.isObjectiveCompleted() && this.isUnlocked(player)) {
         this.progressObjective(objectives);
      }
   }

   private static void refresh(Quest quest, Player player) {
      for (Objective objective : quest.getQuestObjectives().getObjectives()) {
         if (objective instanceof ClassUnlockObjective) {
            objective.progressNonlinearObjective(quest.getQuestObjectives(), player);
         }
      }
   }

   public static void refresh(Player player) {
      if (PlayerData.isInMemory(player.getUniqueId())) {
         for (Quest quest : List.copyOf(PlayerData.getQuests(player.getUniqueId()))) {
            refresh(quest, player);
         }
      }
   }

   @Generated
   public String getClassId() {
      return this.classId;
   }

   @Generated
   public String getNpcFilename() {
      return this.npcFilename;
   }

   public static final class Events implements Listener {
      @EventHandler(
         priority = EventPriority.MONITOR,
         ignoreCancelled = true
      )
      public void onAccept(QuestAcceptEvent event) {
         ClassUnlockObjective.refresh(event.getQuest(), event.getPlayer());
      }

      @EventHandler(
         priority = EventPriority.HIGHEST,
         ignoreCancelled = true
      )
      public void onComplete(QuestCompleteEvent event) {
         for (Objective objective : event.getQuest().getQuestObjectives().getObjectives()) {
            if (objective instanceof ClassUnlockObjective unlock && !unlock.isUnlocked(event.getPlayer())) {
               event.setCancelled(true);
               return;
            }
         }
      }
   }
}
