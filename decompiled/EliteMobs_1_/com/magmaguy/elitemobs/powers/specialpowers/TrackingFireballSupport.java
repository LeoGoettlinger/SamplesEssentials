package com.magmaguy.elitemobs.powers.specialpowers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.combatsystem.EliteProjectile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class TrackingFireballSupport {
   private static final Map<UUID, TrackingFireballSupport.TrackingFireballTask> trackingFireballs = new HashMap<>();

   private TrackingFireballSupport() {
   }

   public static void fireAtNearbyPlayers(Monster monster, double fireballSpeed) {
      for (Entity nearbyEntity : monster.getNearbyEntities(20.0, 20.0, 20.0)) {
         if (nearbyEntity instanceof Player player && (player.getGameMode() == GameMode.ADVENTURE || player.getGameMode() == GameMode.SURVIVAL)) {
            new TrackingFireballSupport.TrackingFireballTask(monster, player, fireballSpeed);
         }
      }
   }

   public static void shutdown() {
      new ArrayList<>(trackingFireballs.values()).forEach(TrackingFireballSupport.TrackingFireballTask::close);
   }

   public static class Events implements Listener {
      @EventHandler(
         ignoreCancelled = true
      )
      public void onFireballPunched(EntityDamageByEntityEvent event) {
         TrackingFireballSupport.TrackingFireballTask trackingFireballTask = TrackingFireballSupport.trackingFireballs.get(event.getEntity().getUniqueId());
         if (trackingFireballTask != null) {
            trackingFireballTask.isAfterPlayer = !trackingFireballTask.isAfterPlayer;
         }
      }
   }

   private static final class TrackingFireballTask {
      private boolean isAfterPlayer = true;
      private final Fireball repeatingFireball;
      private BukkitTask task;

      private TrackingFireballTask(final Entity entity, final Player player, final double fireballSpeed) {
         Vector targetterToTargetted = player.getLocation().clone().toVector().subtract(entity.getLocation().toVector()).normalize().multiply(1.0E-4);
         this.repeatingFireball = (Fireball)EliteProjectile.create(EntityType.FIREBALL, entity, player, targetterToTargetted, true);
         this.repeatingFireball.setYield(3.0F);
         this.repeatingFireball.setIsIncendiary(true);
         this.repeatingFireball.setShooter((ProjectileSource)entity);
         TrackingFireballSupport.trackingFireballs.put(this.repeatingFireball.getUniqueId(), this);
         this.task = (new BukkitRunnable() {
               int counter = 0;

               public void run() {
                  if (TrackingFireballTask.this.repeatingFireball != null
                     && TrackingFireballTask.this.repeatingFireball.isValid()
                     && entity.isValid()
                     && !player.isDead()
                     && this.counter <= 3600
                     && TrackingFireballTask.this.repeatingFireball.getLocation().getWorld() == player.getWorld()) {
                     if (TrackingFireballTask.this.isAfterPlayer) {
                        TrackingFireballTask.this.repeatingFireball.setDirection(TrackingFireballTask.this.setFireballDirection(player, fireballSpeed));
                        TrackingFireballTask.this.repeatingFireball.setVelocity(TrackingFireballTask.this.setFireballDirection(player, fireballSpeed));
                     } else {
                        TrackingFireballTask.this.repeatingFireball.setDirection(TrackingFireballTask.this.setFireballDirection(entity, fireballSpeed));
                        TrackingFireballTask.this.repeatingFireball.setVelocity(TrackingFireballTask.this.setFireballDirection(entity, fireballSpeed));
                     }

                     this.counter++;
                  } else {
                     TrackingFireballTask.this.close();
                  }
               }
            })
            .runTaskTimer(MetadataHandler.PLUGIN, 1L, 1L);
      }

      private void close() {
         if (this.task != null) {
            this.task.cancel();
         }

         this.task = null;
         TrackingFireballSupport.trackingFireballs.remove(this.repeatingFireball.getUniqueId(), this);
      }

      private Vector setFireballDirection(Entity target, double fireballSpeed) {
         return target.getLocation().distanceSquared(this.repeatingFireball.getLocation()) > 1.0
            ? target.getLocation()
               .clone()
               .add(new Vector(0, 1, 0))
               .toVector()
               .subtract(this.repeatingFireball.getLocation().toVector())
               .normalize()
               .multiply(fireballSpeed)
            : target.getLocation().clone().toVector().subtract(this.repeatingFireball.getLocation().toVector()).normalize().multiply(fireballSpeed);
      }
   }
}
