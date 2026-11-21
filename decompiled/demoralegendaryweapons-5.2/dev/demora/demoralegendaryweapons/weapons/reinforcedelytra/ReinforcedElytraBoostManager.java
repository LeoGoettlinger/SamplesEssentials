package dev.demora.demoralegendaryweapons.weapons.reinforcedelytra;

import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class ReinforcedElytraBoostManager {
   private final DemoraLegendaryWeapons plugin;
   private final ReinforcedElytra reinforcedElytra;

   public ReinforcedElytraBoostManager(DemoraLegendaryWeapons plugin, ReinforcedElytra reinforcedElytra) {
      this.plugin = plugin;
      this.reinforcedElytra = reinforcedElytra;
      this.startBoostTrailTask();
   }

   public void activateBoost(Player player) {
      if (this.reinforcedElytra.isOnBoostCooldown(player.getUniqueId())) {
         long remaining = this.reinforcedElytra.getRemainingBoostCooldownSeconds(player.getUniqueId());
         player.sendMessage(this.plugin.getMessageWithPrefix("weapon-messages.reinforced-elytra-boost-cooldown").replace("{time}", String.valueOf(remaining)));
      } else {
         this.reinforcedElytra.setBoostCooldown(player.getUniqueId());
         Vector direction = player.getEyeLocation().getDirection().normalize();
         double boostPower = this.reinforcedElytra.getBoostPower();
         player.setVelocity(direction.multiply(boostPower));
         player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.0F, 0.9F);
         player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1.0F, 0.7F);
         this.spawnBoostActivationParticles(player);
      }
   }

   private void spawnBoostActivationParticles(Player player) {
      Location loc = player.getLocation();
      DustOptions blueDust = new DustOptions(Color.fromRGB(100, 150, 255), 1.5F);
      DustOptions lightBlueDust = new DustOptions(Color.fromRGB(173, 216, 230), 1.2F);
      player.getWorld().spawnParticle(Particle.CLOUD, loc, 20, 0.5, 0.2, 0.5, 0.05);
      player.getWorld().spawnParticle(Particle.SMOKE, loc, 15, 0.3, 0.3, 0.3, 0.05);
      player.getWorld().spawnParticle(Particle.DUST, loc, 30, 0.4, 0.2, 0.4, 0.0, blueDust);
      player.getWorld().spawnParticle(Particle.DUST, loc, 25, 0.3, 0.15, 0.3, 0.0, lightBlueDust);
      player.getWorld().spawnParticle(Particle.SPLASH, loc, 20, 0.4, 0.2, 0.4, 0.1);
      player.getWorld().spawnParticle(Particle.END_ROD, loc, 15, 0.3, 0.15, 0.3, 0.1);
   }

   private void startBoostTrailTask() {
      (new BukkitRunnable() {
         public void run() {
            for (Player player : Bukkit.getOnlinePlayers()) {
               ItemStack chest = player.getInventory().getChestplate();
               if (ReinforcedElytraBoostManager.this.reinforcedElytra.isReinforcedElytra(chest) && player.isGliding()) {
                  ReinforcedElytraBoostManager.this.spawnBoostTrailParticles(player);
               }
            }
         }
      }).runTaskTimer(this.plugin, 0L, 2L);
   }

   private void spawnBoostTrailParticles(Player player) {
      Location loc = player.getLocation();
      Vector velocity = player.getVelocity();
      if (velocity.length() > 0.1) {
         Vector normalized = velocity.normalize();
         Location trailLoc = loc.clone().subtract(normalized.multiply(0.8));
         DustOptions blueDust = new DustOptions(Color.fromRGB(100, 150, 255), 1.5F);
         DustOptions lightBlueDust = new DustOptions(Color.fromRGB(173, 216, 230), 1.2F);
         DustOptions cyanDust = new DustOptions(Color.fromRGB(0, 255, 255), 1.3F);

         for (int i = 0; i < 12; i++) {
            double offsetX = (Math.random() - 0.5) * 0.6;
            double offsetY = (Math.random() - 0.5) * 0.6;
            double offsetZ = (Math.random() - 0.5) * 0.6;
            Location particleLoc = trailLoc.clone().add(offsetX, offsetY, offsetZ);
            double rand = Math.random();
            if (rand < 0.25) {
               loc.getWorld().spawnParticle(Particle.CLOUD, particleLoc, 2, 0.1, 0.1, 0.1, 0.02);
            } else if (rand < 0.5) {
               loc.getWorld().spawnParticle(Particle.SPLASH, particleLoc, 3, 0.15, 0.15, 0.15, 0.05);
            } else if (rand < 0.7) {
               loc.getWorld().spawnParticle(Particle.DUST, particleLoc, 2, 0.1, 0.1, 0.1, 0.0, blueDust);
            } else {
               loc.getWorld().spawnParticle(Particle.DUST, particleLoc, 2, 0.1, 0.1, 0.1, 0.0, lightBlueDust);
            }
         }

         for (int ix = 0; ix < 8; ix++) {
            double offsetX = (Math.random() - 0.5) * 0.5;
            double offsetY = (Math.random() - 0.5) * 0.5;
            double offsetZ = (Math.random() - 0.5) * 0.5;
            Location particleLoc = trailLoc.clone().add(offsetX, offsetY, offsetZ);
            double rand = Math.random();
            if (rand < 0.5) {
               loc.getWorld().spawnParticle(Particle.END_ROD, particleLoc, 2, 0.05, 0.05, 0.05, 0.08);
            } else {
               loc.getWorld().spawnParticle(Particle.DUST, particleLoc, 2, 0.08, 0.08, 0.08, 0.0, cyanDust);
            }
         }

         loc.getWorld().spawnParticle(Particle.DUST, trailLoc, 15, 0.3, 0.3, 0.3, 0.0, blueDust);
         loc.getWorld().spawnParticle(Particle.DUST, trailLoc, 12, 0.25, 0.25, 0.25, 0.0, lightBlueDust);
         loc.getWorld().spawnParticle(Particle.SPLASH, trailLoc, 10, 0.2, 0.2, 0.2, 0.1);
      }
   }

   public void cleanupPlayer(UUID playerId) {
   }
}
