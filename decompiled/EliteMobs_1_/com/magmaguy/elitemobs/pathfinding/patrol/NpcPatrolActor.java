package com.magmaguy.elitemobs.pathfinding.patrol;

import com.magmaguy.elitemobs.easyminecraftgoals.NMSManager;
import com.magmaguy.elitemobs.magmacore.util.AttributeManager;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import com.magmaguy.elitemobs.thirdparty.custommodels.CustomModel;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

final class NpcPatrolActor implements PatrolActor {
   private final NPCEntity npc;
   private final PatrolOrigin origin;
   private final String canonicalIdentity;
   private long nextProximityCheck;
   private UUID nearestPlayerId;
   private CustomModel facingModel;

   NpcPatrolActor(NPCEntity npc) {
      this.npc = npc;
      this.origin = PatrolOrigin.from(npc.getSpawnLocation(), npc.getWorldName());
      this.canonicalIdentity = PatrolIdentity.canonical("npc", npc.getNPCsConfigFields().getFilename(), this.origin);
   }

   @Override
   public UUID runtimeId() {
      return this.npc.getUuid();
   }

   @Override
   public String canonicalIdentity() {
      return this.canonicalIdentity;
   }

   @Override
   public String displayName() {
      return this.npc.getNPCsConfigFields().getFilename();
   }

   @Override
   public PatrolOrigin origin() {
      return this.origin;
   }

   @Override
   public PatrolRoute route() {
      return this.npc.getNPCsConfigFields().getPatrolRoute();
   }

   @Override
   public LivingEntity body() {
      return this.npc.getVillager();
   }

   @Override
   public boolean persistsWhileDetached() {
      return !this.npc.getNPCsConfigFields().isInstanced();
   }

   @Override
   public boolean isInCombat() {
      return false;
   }

   @Override
   public boolean isNearPlayer(long tick) {
      LivingEntity body = this.body();
      double radius = this.npc.getNPCsConfigFields().getPatrolPauseNearPlayersRadius();
      if (!(radius <= 0.0) && body != null && body.isValid() && !body.isDead()) {
         if (tick < this.nextProximityCheck) {
            return this.nearestPlayerId != null;
         } else {
            this.nextProximityCheck = tick + 20L;
            this.nearestPlayerId = null;
            Location location = body.getLocation();
            double nearestDistance = radius * radius;

            for (Entity entity : body.getNearbyEntities(radius, radius, radius)) {
               if (entity instanceof Player player && player.isValid() && !player.isDead() && player.getGameMode() != GameMode.SPECTATOR) {
                  double distance = player.getLocation().distanceSquared(location);
                  if (distance < nearestDistance
                     || distance == nearestDistance && (this.nearestPlayerId == null || player.getUniqueId().compareTo(this.nearestPlayerId) < 0)) {
                     nearestDistance = distance;
                     this.nearestPlayerId = player.getUniqueId();
                  }
               }
            }

            return this.nearestPlayerId != null;
         }
      } else {
         this.nearestPlayerId = null;
         this.nextProximityCheck = 0L;
         return false;
      }
   }

   @Override
   public void tickFacing(boolean paused) {
      CustomModel model = this.npc.getCustomModel();
      if (this.facingModel != null && this.facingModel != model) {
         this.facingModel.setLookTarget(null);
         this.facingModel = null;
      }

      Location target = null;
      LivingEntity body = this.body();
      if (paused
         && this.npc.getNPCsConfigFields().isPatrolFaceNearbyPlayers()
         && this.nearestPlayerId != null
         && !PatrolEditor.isEditing(this.npc)
         && body != null
         && body.isValid()
         && !body.isDead()) {
         Player player = Bukkit.getPlayer(this.nearestPlayerId);
         double radius = this.npc.getNPCsConfigFields().getPatrolPauseNearPlayersRadius();
         if (player != null
            && player.isValid()
            && !player.isDead()
            && player.getGameMode() != GameMode.SPECTATOR
            && player.getWorld() == body.getWorld()
            && player.getLocation().distanceSquared(body.getLocation()) <= radius * radius) {
            target = player.getEyeLocation();
         }
      }

      if (target != null && model != null && model.setLookTarget(target)) {
         this.facingModel = model;
      } else {
         if (this.facingModel != null) {
            this.facingModel.setLookTarget(null);
            this.facingModel = null;
         }

         if (target != null) {
            Location eyes = body.getEyeLocation();
            Vector direction = target.toVector().subtract(eyes.toVector());
            if (direction.lengthSquared() > 1.0E-8) {
               eyes.setDirection(direction);
               body.setRotation(eyes.getYaw(), eyes.getPitch());
            }
         }
      }
   }

   @Override
   public boolean prepareBody() {
      this.npc.enablePatrolMovementSync();
      LivingEntity body = this.body();
      if (body instanceof Mob mob && body.isValid() && !body.isInsideVehicle() && NMSManager.getAdapter() != null) {
         mob.setAI(true);
         mob.setAware(true);
         return NMSManager.getAdapter().removeFreeWill(body);
      } else {
         return false;
      }
   }

   @Override
   public String ineligibleReason() {
      LivingEntity body = this.body();
      return body != null && body.isInsideVehicle() ? "the routed NPC is a passenger" : "the NPC body cannot use native pathfinding";
   }

   @Override
   public double currentMovementSpeed() {
      LivingEntity body = this.body();
      return body == null ? 0.0 : AttributeManager.getAttributeBaseValue(body, "generic_movement_speed");
   }

   @Override
   public void updatePersistentLocation(Location safeLocation) {
      this.npc.updatePatrolPersistentLocation(safeLocation);
   }

   @Override
   public void materialize() {
      this.npc.chunkLoad();
   }

   @Override
   public void syncVisuals() {
      this.npc.syncPatrolVisuals();
   }

   @Override
   public Object owner() {
      return this.npc;
   }
}
