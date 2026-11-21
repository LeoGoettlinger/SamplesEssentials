package com.magmaguy.elitemobs.quests.objectives;

import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import lombok.Generated;
import org.bukkit.entity.EntityType;

public class DynamicKillObjective extends KillObjective {
   private final EntityType entityType;
   private int minMobLevel;

   public DynamicKillObjective(int targetAmount, EntityType entityType, int minMobLevel) {
      super(targetAmount, EliteMobProperties.getPluginData(entityType).getName(minMobLevel));
      this.minMobLevel = minMobLevel;
      this.entityType = entityType;
   }

   public void adaptToLevel(int newMobLevel) {
      this.minMobLevel = newMobLevel;
      this.objectiveName = EliteMobProperties.getPluginData(this.entityType).getName(newMobLevel);
   }

   @Override
   public void checkProgress(EliteMobDeathEvent event, QuestObjectives questObjectives) {
      if (event.getEliteEntity().getLevel() >= this.minMobLevel) {
         if (event.getEliteEntity().getUnsyncedLivingEntity().getType() == this.entityType) {
            this.progressObjective(questObjectives);
         }
      }
   }

   @Generated
   public EntityType getEntityType() {
      return this.entityType;
   }

   @Generated
   public int getMinMobLevel() {
      return this.minMobLevel;
   }

   @Generated
   public void setMinMobLevel(int minMobLevel) {
      this.minMobLevel = minMobLevel;
   }
}
