package com.magmaguy.elitemobs.quests.objectives;

import com.magmaguy.elitemobs.api.QuestProgressionEvent;
import com.magmaguy.elitemobs.utils.EventCaller;
import java.io.Serializable;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public abstract class Objective implements Serializable {
   protected boolean objectiveCompleted = false;
   protected int currentAmount = 0;
   protected int targetAmount;
   protected String objectiveName;

   public Objective(int targetAmount, String objectiveName) {
      this.targetAmount = targetAmount;
      this.objectiveName = objectiveName;
   }

   public void progressObjective(QuestObjectives questObjectives) {
      if (!this.objectiveCompleted) {
         this.currentAmount++;
         if (this.currentAmount >= this.targetAmount) {
            this.objectiveCompleted = true;
         }

         this.fireProgressionEvent(questObjectives);
      }
   }

   private void fireProgressionEvent(QuestObjectives questObjectives) {
      QuestProgressionEvent questProgressionEvent = new QuestProgressionEvent(
         Bukkit.getPlayer(questObjectives.getQuest().getPlayerUUID()), questObjectives.getQuest(), this
      );
      new EventCaller(questProgressionEvent);
   }

   public void progressNonlinearObjective(QuestObjectives questObjectives, Player player) {
   }

   @Generated
   public boolean isObjectiveCompleted() {
      return this.objectiveCompleted;
   }

   @Generated
   public int getCurrentAmount() {
      return this.currentAmount;
   }

   @Generated
   public void setCurrentAmount(int currentAmount) {
      this.currentAmount = currentAmount;
   }

   @Generated
   public int getTargetAmount() {
      return this.targetAmount;
   }

   @Generated
   public String getObjectiveName() {
      return this.objectiveName;
   }

   @Generated
   public void setObjectiveName(String objectiveName) {
      this.objectiveName = objectiveName;
   }
}
