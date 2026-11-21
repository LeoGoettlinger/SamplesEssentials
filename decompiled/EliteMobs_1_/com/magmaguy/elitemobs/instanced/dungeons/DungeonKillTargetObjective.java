package com.magmaguy.elitemobs.instanced.dungeons;

import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.mobconstructor.custombosses.InstancedBossEntity;
import lombok.Generated;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class DungeonKillTargetObjective extends DungeonObjective {
   private String bossFilename;
   private int targetAmount = 1;
   private int currentAmount = 0;

   public DungeonKillTargetObjective(DungeonInstance dungeonInstance, String objectiveString) {
      this(dungeonInstance, DungeonObjective.parse(objectiveString));
   }

   DungeonKillTargetObjective(DungeonInstance dungeonInstance, DungeonObjective.TargetDefinition definition) {
      super(dungeonInstance, null);
      this.bossFilename = definition.filename();
      this.targetAmount = definition.amount();
   }

   public void incrementKills() {
      if (!this.completed && !this.dungeonInstance.isDefunct()) {
         this.currentAmount++;
         if (this.currentAmount >= this.targetAmount) {
            super.completed = true;
            this.dungeonInstance.checkCompletionStatus();
            this.unregister();
         }
      }
   }

   @Generated
   public String getBossFilename() {
      return this.bossFilename;
   }

   @Generated
   public int getTargetAmount() {
      return this.targetAmount;
   }

   @Generated
   public int getCurrentAmount() {
      return this.currentAmount;
   }

   public static class DungeonKillTargetObjectiveListener implements Listener {
      @EventHandler
      public void onEliteDeath(EliteMobDeathEvent event) {
         if (event.getEliteEntity() instanceof InstancedBossEntity instancedBossEntity) {
            if (instancedBossEntity.getDungeonInstance() != null) {
               for (DungeonObjective objective : instancedBossEntity.getDungeonInstance().objectiveSnapshot()) {
                  if (objective instanceof DungeonKillTargetObjective dungeonKillTargetObjective
                     && !dungeonKillTargetObjective.isCompleted()
                     && dungeonKillTargetObjective.getDungeonInstance() == instancedBossEntity.getDungeonInstance()
                     && (
                        instancedBossEntity.getCustomBossesConfigFields().getFilename().equals(dungeonKillTargetObjective.getBossFilename())
                           || instancedBossEntity.getPhaseBossEntity() != null
                              && instancedBossEntity.getPhaseBossEntity().getPhase1Config().getFilename().equals(dungeonKillTargetObjective.getBossFilename())
                     )) {
                     dungeonKillTargetObjective.incrementKills();
                  }
               }
            }
         }
      }
   }
}
