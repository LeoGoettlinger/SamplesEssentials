package dev.demora.demoralegendaryweapons.weapons.reinforcedelytra;

import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class ReinforcedElytraExplosionManager {
   private final DemoraLegendaryWeapons plugin;
   private final ReinforcedElytra reinforcedElytra;
   private final Set<UUID> immuneToLandingExplosion = new HashSet<>();

   public ReinforcedElytraExplosionManager(DemoraLegendaryWeapons plugin, ReinforcedElytra reinforcedElytra) {
      this.plugin = plugin;
      this.reinforcedElytra = reinforcedElytra;
   }

   public void triggerExplosiveLanding(Player player, double maxSpeed) {
      if (!this.reinforcedElytra.isOnExplosionLandingCooldown(player.getUniqueId())) {
         double minSpeed = this.reinforcedElytra.getMinTriggerSpeed();
         if (!(maxSpeed < minSpeed)) {
            double maxSpeedConfig = this.reinforcedElytra.getMaxSpeedForExplosion();
            float minExplosion = this.reinforcedElytra.getMinExplosionPower();
            float maxExplosion = this.reinforcedElytra.getMaxExplosionPower();
            double t = Math.min(1.0, (maxSpeed - minSpeed) / (maxSpeedConfig - minSpeed));
            float explosionPower = (float)(minExplosion + t * (maxExplosion - minExplosion));
            boolean breakBlocks = this.reinforcedElytra.isExplosionBreakBlocks();
            boolean setFire = this.reinforcedElytra.isExplosionSetFire();
            boolean damagePlayer = this.reinforcedElytra.isDamagePlayer();
            Location loc = player.getLocation();
            if (!damagePlayer) {
               this.immuneToLandingExplosion.add(player.getUniqueId());
            }

            this.reinforcedElytra.setExplosionLandingCooldown(player.getUniqueId());
            loc.getWorld().createExplosion(loc, explosionPower, setFire, breakBlocks && !this.plugin.isRegionProtected(loc), player);
            Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.immuneToLandingExplosion.remove(player.getUniqueId()), 3L);
            double radius = explosionPower * 2.5;

            for (Entity entity : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
               if (entity instanceof LivingEntity && entity != player) {
                  LivingEntity target = (LivingEntity)entity;
                  double dist = entity.getLocation().distance(loc);
                  double damage = explosionPower * 2.0 * Math.max(0.0, 1.0 - dist / radius);
                  if (damage > 0.5) {
                     target.setNoDamageTicks(0);
                     target.damage(damage, player);
                  }
               }
            }

            loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.5F, 0.7F);
            loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.0F, 1.2F);
            this.spawnExplosionParticles(loc, explosionPower);
         }
      }
   }

   private void spawnExplosionParticles(Location loc, float explosionPower) {
      DustOptions blueDust = new DustOptions(Color.fromRGB(100, 150, 255), 1.5F);
      DustOptions lightBlueDust = new DustOptions(Color.fromRGB(173, 216, 230), 1.2F);
      DustOptions cyanDust = new DustOptions(Color.fromRGB(0, 255, 255), 1.3F);
      DustOptions darkBlueDust = new DustOptions(Color.fromRGB(0, 100, 200), 1.4F);
      double particleRadius = explosionPower * 4.0;
      int particleCount = (int)(explosionPower * 120.0F);

      for (int i = 0; i < particleCount; i++) {
         double angle = Math.random() * Math.PI * 2.0;
         double verticalAngle = Math.random() * Math.PI;
         double distance = Math.random() * particleRadius;
         double x = Math.sin(verticalAngle) * Math.cos(angle) * distance;
         double y = Math.cos(verticalAngle) * distance;
         double z = Math.sin(verticalAngle) * Math.sin(angle) * distance;
         Location particleLoc = loc.clone().add(x, y, z);
         double rand = Math.random();
         if (rand < 0.15) {
            loc.getWorld().spawnParticle(Particle.CLOUD, particleLoc, 3, 0.15, 0.15, 0.15, 0.05);
         } else if (rand < 0.3) {
            loc.getWorld().spawnParticle(Particle.SPLASH, particleLoc, 4, 0.2, 0.2, 0.2, 0.1);
         } else if (rand < 0.45) {
            loc.getWorld().spawnParticle(Particle.END_ROD, particleLoc, 3, 0.1, 0.1, 0.1, 0.1);
         } else if (rand < 0.6) {
            loc.getWorld().spawnParticle(Particle.DUST, particleLoc, 3, 0.15, 0.15, 0.15, 0.0, blueDust);
         } else if (rand < 0.75) {
            loc.getWorld().spawnParticle(Particle.DUST, particleLoc, 3, 0.15, 0.15, 0.15, 0.0, lightBlueDust);
         } else if (rand < 0.85) {
            loc.getWorld().spawnParticle(Particle.DUST, particleLoc, 2, 0.12, 0.12, 0.12, 0.0, cyanDust);
         } else {
            loc.getWorld().spawnParticle(Particle.DUST, particleLoc, 2, 0.15, 0.15, 0.15, 0.0, darkBlueDust);
         }
      }

      for (int ix = 0; ix < 15; ix++) {
         double angle = Math.random() * Math.PI * 2.0;
         double verticalAngle = Math.random() * Math.PI;
         double distance = explosionPower * 2.0;
         double x = Math.sin(verticalAngle) * Math.cos(angle) * distance;
         double y = Math.cos(verticalAngle) * distance;
         double z = Math.sin(verticalAngle) * Math.sin(angle) * distance;
         Location particleLoc = loc.clone().add(x, y, z);
         loc.getWorld().spawnParticle(Particle.EXPLOSION, particleLoc, 1, 0.0, 0.0, 0.0, 0.0);
      }

      loc.getWorld().spawnParticle(Particle.EXPLOSION, loc, 10, 1.0, 1.0, 1.0, 0.1);
      loc.getWorld().spawnParticle(Particle.SMOKE, loc, 40, 1.5, 1.5, 1.5, 0.05);
      loc.getWorld().spawnParticle(Particle.CLOUD, loc, 30, 1.5, 1.5, 1.5, 0.1);
      loc.getWorld().spawnParticle(Particle.SPLASH, loc, 35, 1.5, 1.5, 1.5, 0.1);
      loc.getWorld().spawnParticle(Particle.END_ROD, loc, 25, 1.0, 1.0, 1.0, 0.1);
      loc.getWorld().spawnParticle(Particle.DUST, loc, 50, 1.2, 1.2, 1.2, 0.0, blueDust);
      loc.getWorld().spawnParticle(Particle.DUST, loc, 40, 1.0, 1.0, 1.0, 0.0, lightBlueDust);
      loc.getWorld().spawnParticle(Particle.DUST, loc, 30, 0.8, 0.8, 0.8, 0.0, cyanDust);
      loc.getWorld().spawnParticle(Particle.DUST, loc, 25, 1.0, 1.0, 1.0, 0.0, darkBlueDust);
   }

   public boolean isImmuneToLandingExplosion(UUID playerId) {
      return this.immuneToLandingExplosion.contains(playerId);
   }

   public void cleanupPlayer(UUID playerId) {
      this.immuneToLandingExplosion.remove(playerId);
   }
}
