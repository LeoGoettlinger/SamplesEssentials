package dev.demora.demoralegendaryweapons.weapons.reinforcedelytra.rework;

import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class ReworkElytraBoostManager {
   private final DemoraLegendaryWeapons plugin;
   private final ReworkElytra reworkElytra;

   public ReworkElytraBoostManager(DemoraLegendaryWeapons plugin, ReworkElytra reworkElytra) {
      this.plugin = plugin;
      this.reworkElytra = reworkElytra;
      this.startBoostTrailTask();
   }

   public void activateBoost(Player player) {
      if (this.reworkElytra.isOnBoostCooldown(player.getUniqueId())) {
         long remaining = this.reworkElytra.getRemainingBoostCooldownSeconds(player.getUniqueId());
         player.sendMessage(this.plugin.getMessageWithPrefix("weapon-messages.rework-elytra-boost-cooldown").replace("{time}", String.valueOf(remaining)));
      } else {
         this.reworkElytra.setBoostCooldown(player.getUniqueId());
         Vector direction = player.getEyeLocation().getDirection().normalize();
         double boostPower = this.reworkElytra.getBoostPower();
         player.setVelocity(direction.multiply(boostPower));
         player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.0F, 0.9F);
         player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1.0F, 0.7F);
         this.spawnBloodyBoostActivationParticles(player);
         this.spawnCrimsonSpirits(player);
      }
   }

   private void spawnBloodyBoostActivationParticles(Player player) {
      Location loc = player.getLocation();
      DustOptions redDust = new DustOptions(Color.fromRGB(180, 0, 0), 1.5F);
      DustOptions darkRedDust = new DustOptions(Color.fromRGB(100, 0, 0), 1.2F);
      player.getWorld().spawnParticle(Particle.DUST, loc, 30, 0.4, 0.2, 0.4, 0.0, redDust);
      player.getWorld().spawnParticle(Particle.DUST, loc, 25, 0.3, 0.15, 0.3, 0.0, darkRedDust);
      player.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, loc, 15, 0.3, 0.2, 0.3, 0.1);
   }

   private void spawnCrimsonSpirits(Player owner) {
      int count = this.reworkElytra.getCrimsonSpiritsCount();
      final int durationTicks = this.reworkElytra.getCrimsonSpiritsDurationTicks();
      double damage = this.reworkElytra.getCrimsonSpiritDamage();
      double speed = this.reworkElytra.getCrimsonSpiritSpeed();
      double spawnRadius = this.reworkElytra.getCrimsonSpiritSpawnRadius();
      Location base = owner.getLocation();
      final World world = owner.getWorld();
      if (world != null) {
         final List<ReworkElytraBoostManager.CrimsonSpirit> spirits = new ArrayList<>();

         for (int i = 0; i < count; i++) {
            double angle = (Math.PI * 2) * i / count + Math.random() * 0.5;
            double r = spawnRadius * (0.5 + Math.random() * 0.5);
            Location start = base.clone().add(Math.cos(angle) * r, 0.5, Math.sin(angle) * r);
            LivingEntity target = this.findNearestTarget(start, owner, 12.0);
            if (target != null) {
               spirits.add(new ReworkElytraBoostManager.CrimsonSpirit(start.clone(), target.getUniqueId(), damage, speed, owner));
            }
         }

         if (spirits.isEmpty()) {
            for (LivingEntity e : world.getNearbyLivingEntities(base, 15.0, 15.0, 15.0).stream().filter(ex -> ex != owner).limit(count).toList()) {
               spirits.add(
                  new ReworkElytraBoostManager.CrimsonSpirit(
                     base.clone().add((Math.random() - 0.5) * spawnRadius * 2.0, 0.5, (Math.random() - 0.5) * spawnRadius * 2.0),
                     e.getUniqueId(),
                     damage,
                     speed,
                     owner
                  )
               );
            }
         }

         (new BukkitRunnable() {
            int tick = 0;

            public void run() {
               if (this.tick >= durationTicks) {
                  this.cancel();
               } else {
                  Iterator<ReworkElytraBoostManager.CrimsonSpirit> it = spirits.iterator();

                  while (it.hasNext()) {
                     ReworkElytraBoostManager.CrimsonSpirit spirit = it.next();
                     LivingEntity target = Bukkit.getEntity(spirit.targetUuid) instanceof LivingEntity le ? le : null;
                     if (target != null && target.isValid()) {
                        Vector toTarget = target.getLocation().add(0.0, 0.5, 0.0).toVector().subtract(spirit.position.toVector()).normalize();
                        spirit.position.add(toTarget.multiply(spirit.speed));
                        double wave = Math.sin(this.tick * 0.3 + spirit.phase) * 0.15;
                        Vector perp = ReworkElytraBoostManager.this.getPerpendicular(toTarget).multiply(wave);
                        Location particleLoc = spirit.position.clone().add(perp);
                        DustOptions dust = new DustOptions(Color.fromRGB(180, 0, 0), 1.0F);
                        world.spawnParticle(Particle.DUST, particleLoc, 4, 0.1, 0.1, 0.1, 0.0, dust);
                        world.spawnParticle(Particle.DAMAGE_INDICATOR, particleLoc, 1, 0.05, 0.05, 0.05, 0.0);
                        if (spirit.position.distance(target.getLocation().add(0.0, 0.5, 0.0)) < 1.5) {
                           target.setNoDamageTicks(0);
                           target.damage(spirit.damage, spirit.owner);
                           it.remove();
                        }
                     } else {
                        it.remove();
                     }
                  }

                  this.tick++;
               }
            }
         }).runTaskTimer(this.plugin, 0L, 1L);
      }
   }

   private LivingEntity findNearestTarget(Location from, Player exclude, double maxDist) {
      World world = from.getWorld();
      if (world == null) {
         return null;
      } else {
         LivingEntity nearest = null;
         double nearestDist = maxDist * maxDist;

         for (LivingEntity e : world.getNearbyLivingEntities(from, maxDist)) {
            if (e != exclude && e.isValid()) {
               double d = e.getLocation().distanceSquared(from);
               if (d < nearestDist) {
                  nearestDist = d;
                  nearest = e;
               }
            }
         }

         return nearest;
      }
   }

   private Vector getPerpendicular(Vector v) {
      return Math.abs(v.getY()) < 0.9 ? new Vector(-v.getZ(), 0.0, v.getX()).normalize() : new Vector(1, 0, 0);
   }

   private void spawnBoostTrailParticles(Player player) {
      Location loc = player.getLocation();
      Vector velocity = player.getVelocity();
      if (!(velocity.length() < 0.1)) {
         Vector normalized = velocity.normalize();
         Location trailLoc = loc.clone().subtract(normalized.clone().multiply(0.8));
         DustOptions redDust = new DustOptions(Color.fromRGB(180, 0, 0), 1.5F);
         DustOptions darkRedDust = new DustOptions(Color.fromRGB(100, 0, 0), 1.2F);

         for (int i = 0; i < 12; i++) {
            double ox = (Math.random() - 0.5) * 0.6;
            double oy = (Math.random() - 0.5) * 0.6;
            double oz = (Math.random() - 0.5) * 0.6;
            Location pl = trailLoc.clone().add(ox, oy, oz);
            if (Math.random() < 0.5) {
               loc.getWorld().spawnParticle(Particle.DUST, pl, 2, 0.1, 0.1, 0.1, 0.0, redDust);
            } else {
               loc.getWorld().spawnParticle(Particle.DUST, pl, 2, 0.1, 0.1, 0.1, 0.0, darkRedDust);
            }
         }

         loc.getWorld().spawnParticle(Particle.DUST, trailLoc, 15, 0.3, 0.3, 0.3, 0.0, redDust);
         loc.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, trailLoc, 5, 0.2, 0.2, 0.2, 0.05);
      }
   }

   private void startBoostTrailTask() {
      (new BukkitRunnable() {
         public void run() {
            for (Player player : Bukkit.getOnlinePlayers()) {
               ItemStack chest = player.getInventory().getChestplate();
               if (ReworkElytraBoostManager.this.reworkElytra.isReworkElytra(chest) && player.isGliding()) {
                  ReworkElytraBoostManager.this.spawnBoostTrailParticles(player);
               }
            }
         }
      }).runTaskTimer(this.plugin, 0L, 2L);
   }

   public void cleanupPlayer(UUID playerId) {
   }

   private static class CrimsonSpirit {
      final Location position;
      final UUID targetUuid;
      final double damage;
      final double speed;
      final Player owner;
      final double phase = Math.random() * Math.PI * 2.0;

      CrimsonSpirit(Location position, UUID targetUuid, double damage, double speed, Player owner) {
         this.position = position;
         this.targetUuid = targetUuid;
         this.damage = damage;
         this.speed = speed;
         this.owner = owner;
      }
   }
}
