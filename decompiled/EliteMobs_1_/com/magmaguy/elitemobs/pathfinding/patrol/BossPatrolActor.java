package com.magmaguy.elitemobs.pathfinding.patrol;

import com.magmaguy.elitemobs.magmacore.util.AttributeManager;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.InstancedBossEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.RegionalBossEntity;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

final class BossPatrolActor implements PatrolActor {
   private final CustomBossEntity boss;
   private final PatrolOrigin origin;
   private final String canonicalIdentity;

   BossPatrolActor(CustomBossEntity boss) {
      this.boss = boss;
      this.origin = PatrolOrigin.from(boss.getSpawnLocation(), boss.getWorldName());
      this.canonicalIdentity = PatrolIdentity.canonical("boss", boss.getCustomBossesConfigFields().getFilename(), this.origin);
   }

   @Override
   public UUID runtimeId() {
      return this.boss.getEliteUUID();
   }

   @Override
   public String canonicalIdentity() {
      return this.canonicalIdentity;
   }

   @Override
   public String displayName() {
      return this.boss.getCustomBossesConfigFields().getFilename();
   }

   @Override
   public PatrolOrigin origin() {
      return this.origin;
   }

   @Override
   public PatrolRoute route() {
      return this.boss.getCustomBossesConfigFields().getPatrolRoute();
   }

   @Override
   public LivingEntity body() {
      return this.boss.getLivingEntity();
   }

   @Override
   public boolean persistsWhileDetached() {
      return Boolean.TRUE.equals(this.boss.getIsPersistent()) && !(this.boss instanceof InstancedBossEntity);
   }

   @Override
   public boolean isInCombat() {
      return this.boss.isInCombat();
   }

   @Override
   public boolean prepareBody() {
      LivingEntity body = this.body();
      return body != null
         && body.isValid()
         && this.boss.getCustomBossesConfigFields().isAi()
         && this.boss.getMovementSpeedAttribute() > 0.0
         && !body.isInsideVehicle();
   }

   @Override
   public String ineligibleReason() {
      if (!this.boss.getCustomBossesConfigFields().isAi()) {
         return "ai is disabled";
      } else if (this.boss.getMovementSpeedAttribute() <= 0.0) {
         return "movement speed is zero";
      } else {
         LivingEntity body = this.body();
         return body != null && body.isInsideVehicle() ? "the routed boss is a passenger" : "the entity cannot use native pathfinding";
      }
   }

   @Override
   public double currentMovementSpeed() {
      LivingEntity body = this.body();
      return body == null ? this.boss.getMovementSpeedAttribute() : AttributeManager.getAttributeBaseValue(body, "generic_movement_speed");
   }

   @Override
   public void updatePersistentLocation(Location safeLocation) {
      this.boss.updatePersistentLocation(safeLocation);
   }

   @Override
   public void materialize() {
      if (!(this.boss instanceof RegionalBossEntity regionalBoss && regionalBoss.isRespawning())) {
         this.boss.chunkLoad();
      }
   }

   @Override
   public void syncVisuals() {
   }

   @Override
   public Object owner() {
      return this.boss;
   }
}
