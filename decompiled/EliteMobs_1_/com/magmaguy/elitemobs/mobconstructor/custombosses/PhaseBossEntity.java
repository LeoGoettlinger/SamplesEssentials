package com.magmaguy.elitemobs.mobconstructor.custombosses;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedEvent;
import com.magmaguy.elitemobs.api.EliteMobRemoveEvent;
import com.magmaguy.elitemobs.api.ElitePhaseSwitchEvent;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.easyminecraftgoals.NMSManager;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.mobconstructor.custombosses.transitiveblocks.TransitiveBossBlock;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import com.magmaguy.elitemobs.utils.EventCaller;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class PhaseBossEntity {
   private static final int DESTINATION_WAIT_TICKS = 100;
   private static final Map<PhaseBossEntity.DestinationTicket, Integer> destinationTickets = new HashMap<>();
   private final CustomBossEntity customBossEntity;
   private final List<PhaseBossEntity.BossPhase> bossPhases;
   private PhaseBossEntity.BossPhase currentPhase = null;
   private Location originalSpawnLocation;
   private Double pendingHealthFraction;
   private PhaseBossEntity.PendingTransition pendingTransition;
   private final Set<PhaseBossEntity.BossPhase> reportedInvalidLocations = new HashSet<>();

   public PhaseBossEntity(CustomBossEntity customBossEntity) {
      this.customBossEntity = customBossEntity;
      ArrayList<PhaseBossEntity.BossPhase> parsed = new ArrayList<>();
      parsed.add(new PhaseBossEntity.BossPhase(customBossEntity.getCustomBossesConfigFields(), 1.0));

      for (String entry : customBossEntity.getCustomBossesConfigFields().getPhases()) {
         try {
            String[] parts = entry.split(":", -1);
            if (parts.length != 2) {
               throw new IllegalArgumentException("Expected filename:healthFraction");
            }

            CustomBossesConfigFields fields = CustomBossesConfig.getCustomBoss(parts[0].trim());
            if (fields == null) {
               throw new IllegalArgumentException("Missing phase boss " + parts[0]);
            }

            double fraction = Double.parseDouble(parts[1].trim());
            if (!Double.isFinite(fraction) || fraction < 0.0 || fraction >= 1.0) {
               throw new IllegalArgumentException("Phase health fraction must be between 0 inclusive and 1 exclusive");
            }

            parsed.add(new PhaseBossEntity.BossPhase(fields, fraction));
         } catch (RuntimeException var9) {
            throw new IllegalArgumentException(
               "Invalid phase '" + entry + "' in " + customBossEntity.getCustomBossesConfigFields().getFilename() + ": " + var9.getMessage(), var9
            );
         }
      }

      parsed.sort((first, second) -> Double.compare(second.healthPercentage, first.healthPercentage));
      this.bossPhases = List.copyOf(parsed);
      this.currentPhase = this.bossPhases.get(0);
   }

   public boolean isInFirstPhase() {
      return this.bossPhases.get(0).equals(this.currentPhase);
   }

   private void switchPhase(PhaseBossEntity.BossPhase bossPhase, RemovalReason removalReason, double healthPercentage) {
      if (bossPhase.equals(this.currentPhase)) {
         Logger.warn("Attempted to change the boss phase to what it already was.", true);
      } else {
         Location origin = this.customBossEntity.getLocation();
         boolean reset = removalReason == RemovalReason.PHASE_BOSS_RESET;
         String authored = bossPhase.customBossesConfigFields.getPhaseSpawnLocation();
         Location destination;
         if (reset) {
            destination = this.originalSpawnLocation == null ? this.customBossEntity.getSpawnLocation() : this.originalSpawnLocation;
         } else if (authored == null) {
            destination = origin;
         } else {
            destination = ConfigurationLocation.serialize(authored, true);
            if (destination != null && origin != null && authored.split(",", 2)[0].equalsIgnoreCase("same_as_boss")) {
               destination.setWorld(origin.getWorld());
            }
         }

         PhaseBossEntity.PendingTransition preparation = null;

         try {
            if (destination == null || destination.getWorld() == null) {
               throw new IllegalArgumentException("Phase destination has no loaded world");
            }

            destination.checkFinite();
            destination = destination.clone();
            if (NMSManager.getAdapter() == null) {
               throw new IllegalStateException("Native chunk readiness is unavailable");
            }

            if (!NMSManager.getAdapter().isPositionEntityTicking(destination)) {
               preparation = new PhaseBossEntity.PendingTransition(bossPhase, removalReason, healthPercentage, destination);
               this.pendingTransition = preparation;
               preparation.acquireTicket();
               if (this.pendingTransition == preparation && preparation.stillCurrent()) {
                  preparation.task = Bukkit.getScheduler().runTaskTimer(preparation.plugin, preparation, 1L, 1L);
                  return;
               }

               if (this.pendingTransition == preparation) {
                  this.pendingTransition = null;
               }

               preparation.close();
               return;
            }
         } catch (RuntimeException var11) {
            if (preparation != null) {
               if (this.pendingTransition == preparation) {
                  this.pendingTransition = null;
               }

               preparation.close();
            }

            this.reportDestinationFailure(bossPhase, var11.getMessage());
            return;
         }

         this.completeTransition(bossPhase, removalReason, healthPercentage, destination);
      }
   }

   private void completeTransition(PhaseBossEntity.BossPhase bossPhase, RemovalReason removalReason, double healthPercentage, Location destination) {
      boolean reset = removalReason == RemovalReason.PHASE_BOSS_RESET;
      String authored = bossPhase.customBossesConfigFields.getPhaseSpawnLocation();
      if (this.isInFirstPhase()) {
         this.originalSpawnLocation = this.customBossEntity.getSpawnLocation().clone();
      }

      if (removalReason == RemovalReason.PHASE_BOSS_PHASE_END && this.customBossEntity instanceof RegionalBossEntity regional) {
         TransitiveBossBlock.clearPhaseBlocks(regional);
      }

      this.customBossEntity.remove(removalReason);
      if (this.customBossEntity.getCustomModel() != null) {
         this.customBossEntity.getCustomModel().switchPhase();
      }

      this.customBossEntity.setCustomBossesConfigFields(bossPhase.customBossesConfigFields);
      if (reset) {
         if (bossPhase.customBossesConfigFields.getSong() != null) {
            this.customBossEntity.setBossMusic(new CustomMusic(bossPhase.customBossesConfigFields.getSong(), this.customBossEntity));
         }

         this.customBossEntity.setSpawnLocation(destination.clone());
      } else {
         if (authored != null) {
            this.customBossEntity.setSpawnLocation(destination.clone());
         }

         if (bossPhase.customBossesConfigFields.getSong() != null) {
            if (this.currentPhase.customBossesConfigFields.getSong() == null) {
               this.customBossEntity.setBossMusic(new CustomMusic(bossPhase.customBossesConfigFields.getSong(), this.customBossEntity));
               this.customBossEntity.getBossMusic().start(this.customBossEntity);
            }

            if (!bossPhase.customBossesConfigFields.getSong().equals(this.currentPhase.customBossesConfigFields.getSong())) {
               if (this.customBossEntity.getBossMusic() != null) {
                  this.customBossEntity.getBossMusic().stop();
               }

               this.customBossEntity.setBossMusic(new CustomMusic(bossPhase.customBossesConfigFields.getSong(), this.customBossEntity));
            }
         }
      }

      this.customBossEntity.setRespawnOverrideLocation(destination.clone());
      this.customBossEntity.setPersistentLocation(destination);
      this.currentPhase = bossPhase;
      this.pendingHealthFraction = healthPercentage;
      this.customBossEntity.spawn(true);
   }

   private void reportDestinationFailure(PhaseBossEntity.BossPhase phase, String problem) {
      if (this.reportedInvalidLocations.add(phase)) {
         Logger.warn(
            "Refused phase transition from "
               + this.currentPhase.customBossesConfigFields.getFilename()
               + " to "
               + phase.customBossesConfigFields.getFilename()
               + ": "
               + problem
               + ". The current phase was retained."
         );
      }
   }

   private void cancelPendingTransition() {
      PhaseBossEntity.PendingTransition pending = this.pendingTransition;
      this.pendingTransition = null;
      if (pending != null) {
         pending.close();
      }
   }

   void onBodyMaterialized() {
      if (this.pendingHealthFraction != null && this.customBossEntity.isValid()) {
         double fraction = this.pendingHealthFraction;
         this.customBossEntity.setHealth(this.customBossEntity.getMaxHealth() * fraction);
         this.pendingHealthFraction = null;
         if (this.customBossEntity.isValid()) {
            this.customBossEntity.setCombatGracePeriod(20);
            new EventCaller(new ElitePhaseSwitchEvent(this.customBossEntity, this));
         }
      }
   }

   public void resetToFirstPhase() {
      this.cancelPendingTransition();
      if (!this.isInFirstPhase()) {
         this.switchPhase(this.bossPhases.get(0), RemovalReason.PHASE_BOSS_RESET, 1.0);
      }
   }

   public void silentReset() {
      this.cancelPendingTransition();
      this.pendingHealthFraction = null;
      this.currentPhase = this.bossPhases.get(0);
      this.customBossEntity.setCustomBossesConfigFields(this.currentPhase.customBossesConfigFields);
   }

   public CustomBossesConfigFields getPhase1Config() {
      return this.bossPhases.get(0).customBossesConfigFields;
   }

   public void checkPhaseBossSwitch(EliteMobDamagedEvent event) {
      if (this.pendingTransition != null) {
         event.setCancelled(true);
      } else if (this.bossPhases.indexOf(this.currentPhase) + 1 < this.bossPhases.size()) {
         PhaseBossEntity.BossPhase nextBossPhase = this.bossPhases.get(this.bossPhases.indexOf(this.currentPhase) + 1);
         double newHealth = Math.max((this.customBossEntity.getHealth() - event.getDamage()) / this.customBossEntity.getMaxHealth(), 0.0);
         if (!(newHealth > nextBossPhase.healthPercentage)) {
            event.setCancelled(true);
            this.switchPhase(nextBossPhase, RemovalReason.PHASE_BOSS_PHASE_END, nextBossPhase.healthPercentage);
         }
      }
   }

   @Generated
   public List<PhaseBossEntity.BossPhase> getBossPhases() {
      return this.bossPhases;
   }

   public class BossPhase {
      public final CustomBossesConfigFields customBossesConfigFields;
      public final double healthPercentage;

      public BossPhase(CustomBossesConfigFields customBossesConfigFields, double healthPercentage) {
         this.customBossesConfigFields = customBossesConfigFields;
         this.healthPercentage = healthPercentage;
      }
   }

   private record DestinationTicket(UUID worldId, int x, int z, Plugin plugin) {
   }

   private final class PendingTransition implements Runnable {
      private final PhaseBossEntity.BossPhase expectedPhase = PhaseBossEntity.this.currentPhase;
      private final LivingEntity expectedBody = PhaseBossEntity.this.customBossEntity.getLivingEntity();
      private final PhaseBossEntity.BossPhase nextPhase;
      private final RemovalReason removalReason;
      private final double healthPercentage;
      private final Location destination;
      private final Plugin plugin = MetadataHandler.PLUGIN;
      private final PhaseBossEntity.DestinationTicket ticket;
      private BukkitTask task;
      private boolean ownedTicket;
      private int elapsedTicks;

      private PendingTransition(PhaseBossEntity.BossPhase nextPhase, RemovalReason removalReason, double healthPercentage, Location destination) {
         this.nextPhase = nextPhase;
         this.removalReason = removalReason;
         this.healthPercentage = healthPercentage;
         this.destination = destination;
         this.ticket = new PhaseBossEntity.DestinationTicket(
            destination.getWorld().getUID(), destination.getBlockX() >> 4, destination.getBlockZ() >> 4, this.plugin
         );
      }

      private void acquireTicket() {
         Integer owners = PhaseBossEntity.destinationTickets.get(this.ticket);
         if (owners != null) {
            PhaseBossEntity.destinationTickets.put(this.ticket, owners + 1);
            this.ownedTicket = true;
         } else if (!this.destination.getWorld().getPluginChunkTickets(this.ticket.x(), this.ticket.z()).contains(this.plugin)) {
            PhaseBossEntity.destinationTickets.put(this.ticket, 1);
            this.ownedTicket = true;
            if (!this.destination.getWorld().addPluginChunkTicket(this.ticket.x(), this.ticket.z(), this.plugin)) {
               throw new IllegalStateException("Phase destination chunk ticket could not be acquired");
            }
         }
      }

      private boolean stillCurrent() {
         return PhaseBossEntity.this.customBossEntity.isValid()
            && PhaseBossEntity.this.customBossEntity.getLivingEntity() == this.expectedBody
            && PhaseBossEntity.this.currentPhase == this.expectedPhase
            && this.plugin.isEnabled()
            && Bukkit.getWorld(this.ticket.worldId()) == this.destination.getWorld();
      }

      @Override
      public void run() {
         if (PhaseBossEntity.this.pendingTransition == this) {
            if (!this.stillCurrent()) {
               PhaseBossEntity.this.cancelPendingTransition();
            } else {
               boolean ready;
               try {
                  if (NMSManager.getAdapter() == null) {
                     throw new IllegalStateException("Native chunk readiness is unavailable");
                  }

                  ready = NMSManager.getAdapter().isPositionEntityTicking(this.destination);
               } catch (RuntimeException var7) {
                  PhaseBossEntity.this.cancelPendingTransition();
                  PhaseBossEntity.this.reportDestinationFailure(this.nextPhase, var7.getMessage());
                  return;
               }

               if (ready) {
                  PhaseBossEntity.this.pendingTransition = null;
                  if (this.task != null) {
                     this.task.cancel();
                  }

                  try {
                     PhaseBossEntity.this.completeTransition(this.nextPhase, this.removalReason, this.healthPercentage, this.destination);
                  } finally {
                     this.close();
                  }
               } else if (++this.elapsedTicks >= 100) {
                  PhaseBossEntity.this.cancelPendingTransition();
                  PhaseBossEntity.this.reportDestinationFailure(this.nextPhase, "Destination did not become entity-ticking within 100 ticks");
               }
            }
         }
      }

      private void close() {
         if (this.task != null) {
            this.task.cancel();
            this.task = null;
         }

         if (this.ownedTicket) {
            this.ownedTicket = false;
            Integer owners = PhaseBossEntity.destinationTickets.get(this.ticket);
            if (owners != null) {
               if (owners > 1) {
                  PhaseBossEntity.destinationTickets.put(this.ticket, owners - 1);
               } else {
                  PhaseBossEntity.destinationTickets.remove(this.ticket);
                  this.destination.getWorld().removePluginChunkTicket(this.ticket.x(), this.ticket.z(), this.plugin);
               }
            }
         }
      }
   }

   public static class PhaseBossEntityListener implements Listener {
      @EventHandler(
         ignoreCancelled = true
      )
      public void onEliteDamaged(EliteMobDamagedEvent event) {
         if (event.getEliteEntity() instanceof CustomBossEntity) {
            if (((CustomBossEntity)event.getEliteEntity()).getPhaseBossEntity() != null) {
               ((CustomBossEntity)event.getEliteEntity()).getPhaseBossEntity().checkPhaseBossSwitch(event);
            }
         }
      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onEliteRemove(EliteMobRemoveEvent event) {
         if (event.getEliteMobEntity() instanceof CustomBossEntity customBossEntity) {
            PhaseBossEntity phases = customBossEntity.getPhaseBossEntity();
            if (phases != null) {
               if (event.getRemovalReason() != RemovalReason.PHASE_BOSS_PHASE_END && event.getRemovalReason() != RemovalReason.PHASE_BOSS_RESET) {
                  phases.cancelPendingTransition();
               }

               if (event.getRemovalReason() == RemovalReason.DEATH) {
                  phases.silentReset();
               }
            }
         }
      }
   }
}
