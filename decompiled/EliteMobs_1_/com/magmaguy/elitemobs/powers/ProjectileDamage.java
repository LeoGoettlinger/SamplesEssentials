package com.magmaguy.elitemobs.powers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.easyminecraftgoals.NMSManager;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.FakeItem;
import com.magmaguy.elitemobs.events.BossCustomAttackDamage;
import com.magmaguy.elitemobs.magmacore.util.ItemStackGenerator;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.utils.GameClock;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

public class ProjectileDamage {
   public static void doGoldNuggetDamage(final List<ProjectileDamage.FakeProjectile> projectiles, final EliteEntity eliteEntity) {
      (new BukkitRunnable() {
         int timer = 0;

         public void run() {
            this.timer++;
            if (projectiles.isEmpty()) {
               this.cancel();
            } else {
               Iterator<ProjectileDamage.FakeProjectile> iterator = projectiles.iterator();

               while (iterator.hasNext()) {
                  ProjectileDamage.FakeProjectile projectile = iterator.next();
                  if (projectile != null && !projectile.isRemoved()) {
                     projectile.tick();
                     boolean removed = false;

                     for (LivingEntity livingEntity : projectile.getNearbyLivingEntities(0.5)) {
                        if (livingEntity != eliteEntity.getLivingEntity() && !(livingEntity instanceof Player player && player.isBlocking())) {
                           BossCustomAttackDamage.dealCustomDamage(eliteEntity.getLivingEntity(), livingEntity, 1.0);
                           removed = true;
                        }
                     }

                     if (removed) {
                        projectile.remove();
                        iterator.remove();
                     } else if (projectile.isOnGround()) {
                        projectile.remove();
                        iterator.remove();
                     }
                  } else {
                     iterator.remove();
                  }
               }

               if (this.timer >= 100) {
                  for (ProjectileDamage.FakeProjectile projectile : projectiles) {
                     projectile.remove();
                  }

                  projectiles.clear();
                  this.cancel();
               }
            }
         }
      }).runTaskTimer(MetadataHandler.PLUGIN, 0L, 1L);
   }

   public static ProjectileDamage.FakeProjectile createGoldNuggetProjectile(Location location, Vector velocity) {
      return new ProjectileDamage.GoldNuggetBurst().create(location, velocity);
   }

   public static class FakeProjectile {
      private final FakeItem fakeItem;
      private Location location;
      private Vector velocity;
      private boolean removed = false;
      private boolean hasGravity = false;

      public FakeProjectile(Location spawnLocation, ItemStack itemStack, Vector velocity) {
         this.location = spawnLocation.clone();
         this.velocity = velocity.clone();
         this.fakeItem = NMSManager.getAdapter().fakeItemBuilder().itemStack(itemStack).scale(0.5F).build(spawnLocation);
      }

      public void displayToNearbyPlayers(double radius) {
         for (Entity entity : this.location.getWorld().getNearbyEntities(this.location, radius, radius, radius)) {
            if (entity instanceof Player player) {
               this.fakeItem.displayTo(player);
            }
         }
      }

      public void setGravity(boolean gravity) {
         this.hasGravity = gravity;
      }

      public void tick() {
         if (!this.removed) {
            if (this.hasGravity) {
               this.velocity.setY(this.velocity.getY() - 0.04);
            }

            this.location.add(this.velocity);
            this.fakeItem.teleport(this.location);
         }
      }

      public Location getLocation() {
         return this.location.clone();
      }

      public boolean isOnGround() {
         Location below = this.location.clone().subtract(0.0, 0.1, 0.0);
         return below.getBlock().getType().isSolid();
      }

      public void remove() {
         if (!this.removed) {
            this.removed = true;
            this.fakeItem.remove();
         }
      }

      public boolean isRemoved() {
         return this.removed;
      }

      public List<LivingEntity> getNearbyLivingEntities(double radius) {
         List<LivingEntity> nearby = new ArrayList<>();

         for (Entity entity : this.location.getWorld().getNearbyEntities(this.location, radius, radius, radius)) {
            if (entity instanceof LivingEntity livingEntity) {
               nearby.add(livingEntity);
            }
         }

         return nearby;
      }
   }

   public static final class GoldNuggetBurst {
      private long tick = Long.MIN_VALUE;
      private Location center;
      private List<Player> candidates = List.of();

      public ProjectileDamage.FakeProjectile create(Location location, Vector velocity) {
         if (location != null && location.getWorld() != null && velocity != null) {
            long now = GameClock.getCurrentTick();
            if (this.center == null
               || this.center.getWorld() != location.getWorld()
               || now != this.tick
               || now == ProjectileDamage.ViewerEvents.changedAtTick
               || Math.abs(this.center.getX() - location.getX()) > 4.0
               || Math.abs(this.center.getY() - location.getY()) > 4.0
               || Math.abs(this.center.getZ() - location.getZ()) > 4.0) {
               this.center = location.clone();
               this.tick = now;
               this.candidates = location.getWorld()
                  .getNearbyEntities(location, 68.0, 68.0, 68.0, entity -> entity instanceof Player)
                  .stream()
                  .map(Player.class::cast)
                  .toList();
            }

            ItemStack goldNugget = ItemStackGenerator.generateItemStack(
               Material.GOLD_NUGGET, "visual projectile", List.of(ThreadLocalRandom.current().nextDouble() + "")
            );
            ProjectileDamage.FakeProjectile projectile = new ProjectileDamage.FakeProjectile(location, goldNugget, velocity);
            BoundingBox range = BoundingBox.of(location, 64.0, 64.0, 64.0);

            for (Player player : this.candidates) {
               if (player.isOnline() && player.getWorld() == location.getWorld() && range.overlaps(player.getBoundingBox())) {
                  projectile.fakeItem.displayTo(player);
               }
            }

            return projectile;
         } else {
            return null;
         }
      }
   }

   public static final class ViewerEvents implements Listener {
      private static long changedAtTick = Long.MIN_VALUE;

      private static void changed() {
         changedAtTick = GameClock.getCurrentTick();
      }

      @EventHandler(
         priority = EventPriority.MONITOR,
         ignoreCancelled = true
      )
      public void onTeleport(PlayerTeleportEvent event) {
         changed();
      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onJoin(PlayerJoinEvent event) {
         changed();
      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onQuit(PlayerQuitEvent event) {
         changed();
      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onRespawn(PlayerRespawnEvent event) {
         changed();
      }
   }
}
