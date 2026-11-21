package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.utils.CommandRunner;
import java.util.ArrayList;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class EliteMobExitCombatEvent extends Event {
   private static final HandlerList handlers = new HandlerList();
   private final EliteEntity eliteEntity;
   private final EliteMobExitCombatEvent.EliteMobExitCombatReason eliteMobExitCombatReason;

   public EliteMobExitCombatEvent(EliteEntity eliteEntity, EliteMobExitCombatEvent.EliteMobExitCombatReason reason) {
      this.eliteEntity = eliteEntity;
      this.eliteMobExitCombatReason = reason;
      eliteEntity.setInCombat(false);
      if (eliteEntity.getUnsyncedLivingEntity() != null && !eliteEntity.getUnsyncedLivingEntity().isDead()) {
         if (eliteEntity instanceof CustomBossEntity) {
            CommandRunner.runCommandFromList(((CustomBossEntity)eliteEntity).getCustomBossesConfigFields().getOnCombatLeaveCommands(), new ArrayList<>());
         }

         if (eliteEntity.isValid()
            && MobCombatSettingsConfig.isRegenerateCustomBossHealthOnCombatEnd()
            && !reason.equals(EliteMobExitCombatEvent.EliteMobExitCombatReason.PHASE_SWITCH)
            && !eliteEntity.getUnsyncedLivingEntity().getType().equals(EntityType.PHANTOM)) {
            eliteEntity.fullHeal();
         }

         if (!DefaultConfig.isAlwaysShowNametags()) {
            eliteEntity.setNameVisible(false);
         }
      }
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   public EliteEntity getEliteMobEntity() {
      return this.eliteEntity;
   }

   public EliteMobExitCombatEvent.EliteMobExitCombatReason getEliteMobExitCombatReason() {
      return this.eliteMobExitCombatReason;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public static enum EliteMobExitCombatReason {
      NO_NEARBY_PLAYERS,
      SPIRIT_WALK,
      ELITE_NOT_VALID,
      PHASE_SWITCH;
   }
}
