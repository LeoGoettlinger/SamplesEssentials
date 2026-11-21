package com.magmaguy.elitemobs.powerstances;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Item;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class VisualItemProcessor implements AutoCloseable {
   private boolean hasValidEffect;
   private final Object[][] trailTracker;
   private BukkitTask task;
   private boolean closed;

   public VisualItemProcessor(
      Object[][] multiDimensionalTrailTracker, Vector[][] cachedVectorPositions, boolean visualEffectBoolean, int pointsPerRotation, EliteEntity eliteEntity
   ) {
      this.hasValidEffect = visualEffectBoolean;
      this.trailTracker = multiDimensionalTrailTracker;
      if (multiDimensionalTrailTracker.length >= 1) {
         this.task = this.rotateExistingEffects(multiDimensionalTrailTracker, cachedVectorPositions, pointsPerRotation, eliteEntity);
      }
   }

   public static int adjustTrackPosition(double pointsPerRotation, int totalEffectQuantity, int sectionCounter, int globalCounter) {
      int location = (int)(pointsPerRotation / totalEffectQuantity * sectionCounter + globalCounter);
      if (location >= 30) {
         location -= 30;
      }

      return location;
   }

   private BukkitTask rotateExistingEffects(
      final Object[][] multiDimensionalTrailTracker, final Vector[][] cachedVectorPositions, final int pointsPerRotation, final EliteEntity eliteEntity
   ) {
      return (new BukkitRunnable() {
            final boolean isObfuscated = eliteEntity.isVisualEffectObfuscated();
            int counter = 0;

            public void run() {
               try {
                  if (!eliteEntity.isValid() || !VisualItemProcessor.this.hasValidEffect) {
                     VisualItemRemover.removeItems(multiDimensionalTrailTracker);
                     this.cancel();
                     return;
                  }

                  for (int i = 0; i < multiDimensionalTrailTracker.length; i++) {
                     int sectionCounter = 0;

                     for (int j = 0; j < multiDimensionalTrailTracker[i].length; j++) {
                        int adjustedEffectPositionInRotation = VisualItemProcessor.adjustTrackPosition(
                           pointsPerRotation, multiDimensionalTrailTracker[i].length, sectionCounter, this.counter
                        );
                        Vector vector = cachedVectorPositions[i][adjustedEffectPositionInRotation];
                        if (multiDimensionalTrailTracker[i][j] instanceof Item) {
                           VisualItemProcessor.this.rotateItem(multiDimensionalTrailTracker[i][j], vector, eliteEntity);
                        }

                        if (multiDimensionalTrailTracker[i][j] instanceof Particle) {
                           VisualItemProcessor.this.rotateParticle(multiDimensionalTrailTracker[i][j], vector, eliteEntity);
                        }

                        if (++sectionCounter >= pointsPerRotation) {
                           sectionCounter = 0;
                        }
                     }
                  }

                  this.counter++;
                  if (this.counter >= pointsPerRotation) {
                     this.counter = 0;
                  }

                  if (this.isObfuscated != eliteEntity.isVisualEffectObfuscated()) {
                     VisualItemRemover.removeItems(multiDimensionalTrailTracker);
                     this.cancel();
                     eliteEntity.requestPowerStanceRefreshAfterObfuscation();
                  }
               } catch (RuntimeException var6) {
                  this.cancel();
                  Logger.warn("Stopped failed power stance for " + eliteEntity.getEliteUUID() + ": " + var6);
                  VisualItemProcessor.this.close();
               }
            }
         })
         .runTaskTimer(MetadataHandler.PLUGIN, 0L, 5L);
   }

   private void rotateItem(Object itemObject, Vector vector, EliteEntity eliteEntity) {
      Item item = (Item)itemObject;
      Location currentLocation = item.getLocation();
      Location newLocation = eliteEntity.getLivingEntity().getLocation().add(0.0, 1.0, 0.0).add(vector);
      if (newLocation.getWorld() != currentLocation.getWorld()) {
         this.hasValidEffect = false;
      } else {
         Vector movementVector = newLocation.subtract(currentLocation).toVector();
         movementVector = movementVector.multiply(0.3);
         item.setVelocity(movementVector);
      }
   }

   private void rotateParticle(Object particleObject, Vector vector, EliteEntity eliteEntity) {
      Particle particle = (Particle)particleObject;
      if (eliteEntity.isValid()) {
         eliteEntity.getLivingEntity()
            .getWorld()
            .spawnParticle(particle, eliteEntity.getLivingEntity().getLocation().add(0.0, 1.0, 0.0).add(vector), 1, 0.0, 0.0, 0.0, 0.01);
      }
   }

   @Override
   public void close() {
      if (!this.closed) {
         this.closed = true;
         this.hasValidEffect = false;
         if (this.task != null) {
            this.task.cancel();
         }

         VisualItemRemover.removeItemsNow(this.trailTracker);
      }
   }
}
