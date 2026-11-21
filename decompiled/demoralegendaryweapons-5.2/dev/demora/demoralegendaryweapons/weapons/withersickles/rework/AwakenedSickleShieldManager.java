package dev.demora.demoralegendaryweapons.weapons.withersickles.rework;

import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class AwakenedSickleShieldManager {
   private final DemoraLegendaryWeapons plugin;
   private final Map<UUID, Long> shieldCooldowns = new HashMap<>();
   private final Map<UUID, Boolean> activeShields = new HashMap<>();
   private final Map<UUID, BukkitRunnable> shieldTasks = new HashMap<>();
   private FileConfiguration reworkConfig;

   public AwakenedSickleShieldManager(DemoraLegendaryWeapons plugin) {
      this.plugin = plugin;
      this.loadReworkConfig();
   }

   private void loadReworkConfig() {
      File reworkFile = new File(this.plugin.getDataFolder(), "rework.yml");
      this.reworkConfig = YamlConfiguration.loadConfiguration(reworkFile);
   }

   public void reloadConfig() {
      this.loadReworkConfig();
   }

   public void activateShield(Player player) {
      if (this.isOnCooldown(player)) {
         long remaining = this.getRemainingCooldown(player);
         player.sendMessage(
            DemoraLegendaryWeapons.getInstance()
               .getMessageWithPrefix("weapon-messages.awakened-sickles-shield-cooldown")
               .replace("{time}", String.valueOf(remaining))
         );
      } else if (!this.isShieldActive(player)) {
         final int duration = this.reworkConfig.getInt("awakened_wither_sickles.shield_duration_seconds", 10);
         final int flameCount = this.reworkConfig.getInt("awakened_wither_sickles.shield_flame_count", 6);
         final double radius = this.reworkConfig.getDouble("awakened_wither_sickles.shield_radius", 2.5);
         this.activeShields.put(player.getUniqueId(), true);
         player.getWorld().playSound(player.getLocation(), Sound.BLOCK_PORTAL_TRIGGER, 1.0F, 1.5F);
         player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_AMBIENT, 1.0F, 0.8F);
         player.getWorld().spawnParticle(Particle.FLAME, player.getLocation().add(0.0, 1.0, 0.0), 30, 0.5, 0.5, 0.5, 0.1);
         BukkitRunnable task = new BukkitRunnable() {
            private int ticks = 0;
            private final int maxTicks = duration * 20;

            public void run() {
               if (player.isOnline() && this.ticks < this.maxTicks && AwakenedSickleShieldManager.this.activeShields.getOrDefault(player.getUniqueId(), false)) {
                  Location center = player.getLocation().add(0.0, 1.0, 0.0);
                  double angle = this.ticks * 0.15;

                  for (int i = 0; i < flameCount; i++) {
                     double currentAngle = angle + (Math.PI * 2) * i / flameCount;
                     double x = Math.cos(currentAngle) * radius;
                     double z = Math.sin(currentAngle) * radius;
                     Location flameLoc = center.clone().add(x, 0.0, z);
                     player.getWorld().spawnParticle(Particle.FLAME, flameLoc, 3, 0.1, 0.1, 0.1, 0.02);
                     player.getWorld().spawnParticle(Particle.LAVA, flameLoc, 1, 0.05, 0.05, 0.05, 0.0);
                  }

                  this.ticks++;
               } else {
                  AwakenedSickleShieldManager.this.deactivateShield(player);
                  this.cancel();
               }
            }
         };
         task.runTaskTimer(this.plugin, 0L, 1L);
         this.shieldTasks.put(player.getUniqueId(), task);
         Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            int cooldownSeconds = this.reworkConfig.getInt("awakened_wither_sickles.shield_cooldown_seconds", 30);
            this.shieldCooldowns.put(player.getUniqueId(), System.currentTimeMillis() + cooldownSeconds * 1000L);
         }, duration * 20L);
      }
   }

   public void deactivateShield(Player player) {
      this.activeShields.remove(player.getUniqueId());
      BukkitRunnable task = this.shieldTasks.remove(player.getUniqueId());
      if (task != null && !task.isCancelled()) {
         task.cancel();
      }

      if (player.isOnline()) {
         player.getWorld().playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.8F, 1.0F);
      }
   }

   public boolean isShieldActive(Player player) {
      return this.activeShields.getOrDefault(player.getUniqueId(), false);
   }

   public double getDamageReduction() {
      return this.reworkConfig.getDouble("awakened_wither_sickles.shield_damage_reduction", 0.2);
   }

   private boolean isOnCooldown(Player player) {
      Long end = this.shieldCooldowns.get(player.getUniqueId());
      if (end == null) {
         return false;
      } else if (System.currentTimeMillis() >= end) {
         this.shieldCooldowns.remove(player.getUniqueId());
         return false;
      } else {
         return true;
      }
   }

   private long getRemainingCooldown(Player player) {
      Long end = this.shieldCooldowns.get(player.getUniqueId());
      if (end == null) {
         return 0L;
      } else {
         long diff = end - System.currentTimeMillis();
         if (diff <= 0L) {
            this.shieldCooldowns.remove(player.getUniqueId());
            return 0L;
         } else {
            return (long)Math.ceil(diff / 1000.0);
         }
      }
   }

   public void removePlayerData(UUID playerId) {
      this.activeShields.remove(playerId);
      this.shieldCooldowns.remove(playerId);
      BukkitRunnable task = this.shieldTasks.remove(playerId);
      if (task != null && !task.isCancelled()) {
         task.cancel();
      }
   }
}
