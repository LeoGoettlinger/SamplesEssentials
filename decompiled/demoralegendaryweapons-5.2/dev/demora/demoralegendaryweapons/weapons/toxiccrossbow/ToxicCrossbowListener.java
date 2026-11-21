package dev.demora.demoralegendaryweapons.weapons.toxiccrossbow;

import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class ToxicCrossbowListener implements Listener {
   private final DemoraLegendaryWeapons plugin;
   private final ToxicCrossbow toxicCrossbow;
   private final Map<UUID, Long> cooldowns = new HashMap<>();
   private final Map<UUID, UUID> arrowOwners = new HashMap<>();

   public ToxicCrossbowListener(DemoraLegendaryWeapons plugin, ToxicCrossbow toxicCrossbow) {
      this.plugin = plugin;
      this.toxicCrossbow = toxicCrossbow;
   }

   @EventHandler
   public void onShoot(EntityShootBowEvent event) {
      if (event.getEntity() instanceof Player) {
         Player player = (Player)event.getEntity();
         ItemStack bow = event.getBow();
         if (this.toxicCrossbow.isToxicCrossbow(bow)) {
            if (this.isOnCooldown(player)) {
               long remaining = this.getRemainingCooldown(player);
               player.sendMessage(this.plugin.getMessageWithPrefix("weapon-messages.toxic-crossbow-cooldown").replace("{time}", String.valueOf(remaining)));
               event.setCancelled(true);
            } else {
               if (event.getProjectile() instanceof AbstractArrow) {
                  AbstractArrow arrow = (AbstractArrow)event.getProjectile();
                  this.arrowOwners.put(arrow.getUniqueId(), player.getUniqueId());
                  this.startArrowParticles(arrow);
               }

               int cooldownSeconds = this.plugin.getConfig().getInt("weapons.toxic_crossbow.cooldown", 10);
               this.cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + cooldownSeconds * 1000L);
               player.playSound(player.getLocation(), Sound.ENTITY_PHANTOM_FLAP, 1.0F, 0.7F);
            }
         }
      }
   }

   @EventHandler
   public void onProjectileHit(ProjectileHitEvent event) {
      Projectile projectile = event.getEntity();
      if (this.arrowOwners.containsKey(projectile.getUniqueId())) {
         UUID shooterId = this.arrowOwners.remove(projectile.getUniqueId());
         Player shooter = this.plugin.getServer().getPlayer(shooterId);
         if (shooter != null) {
            Location hitLocation = projectile.getLocation();
            this.createToxicCloud(hitLocation, shooter);
         }
      }
   }

   private void createToxicCloud(Location location, Player shooter) {
      final double radius = this.plugin.getConfig().getDouble("weapons.toxic_crossbow.cloud_radius", 4.0);
      int durationSeconds = this.plugin.getConfig().getInt("weapons.toxic_crossbow.cloud_duration_seconds", 5);
      final int durationTicks = durationSeconds * 20;
      final double damagePerTick = this.plugin.getConfig().getDouble("weapons.toxic_crossbow.damage_per_tick", 0.5);
      int poisonDurationSeconds = this.plugin.getConfig().getInt("weapons.toxic_crossbow.poison_duration_seconds", 3);
      final int poisonDuration = poisonDurationSeconds * 20;
      final int poisonAmplifier = 1;
      location.getWorld().playSound(location, Sound.BLOCK_BREWING_STAND_BREW, 1.5F, 0.8F);
      location.getWorld().playSound(location, Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED, 1.0F, 1.5F);
      (new BukkitRunnable() {
         private int ticksElapsed = 0;

         public void run() {
            if (this.ticksElapsed >= durationTicks) {
               this.cancel();
            } else {
               ToxicCrossbowListener.this.spawnCloudParticles(location, radius);
               if (this.ticksElapsed % 10 == 0) {
                  for (LivingEntity entity : location.getWorld().getNearbyLivingEntities(location, radius)) {
                     if (!entity.equals(shooter)) {
                        entity.damage(damagePerTick, shooter);
                        entity.addPotionEffect(new PotionEffect(PotionEffectType.POISON, poisonDuration, poisonAmplifier, false, true));
                     }
                  }
               }

               this.ticksElapsed++;
            }
         }
      }).runTaskTimer(this.plugin, 0L, 1L);
   }

   private void startArrowParticles(AbstractArrow arrow) {
      (new BukkitRunnable() {
            public void run() {
               if (!arrow.isDead() && arrow.isValid() && !arrow.isOnGround()) {
                  Location arrowLoc = arrow.getLocation();
                  arrowLoc.getWorld().spawnParticle(Particle.DUST, arrowLoc, 3, 0.1, 0.1, 0.1, 0.0, new DustOptions(Color.fromRGB(0, 255, 0), 1.0F));
                  arrowLoc.getWorld()
                     .spawnParticle(
                        Particle.DUST, arrowLoc.clone().add(0.0, -0.2, 0.0), 2, 0.05, 0.05, 0.05, 0.0, new DustOptions(Color.fromRGB(34, 139, 34), 1.2F)
                     );
               } else {
                  this.cancel();
               }
            }
         })
         .runTaskTimer(this.plugin, 0L, 1L);
   }

   private void spawnCloudParticles(Location center, double radius) {
      int particleCount = 15;

      for (int i = 0; i < particleCount; i++) {
         double angle = Math.random() * 2.0 * Math.PI;
         double distance = Math.random() * radius;
         double height = (Math.random() - 0.5) * radius * 0.5;
         double x = Math.cos(angle) * distance;
         double z = Math.sin(angle) * distance;
         Location particleLoc = center.clone().add(x, height, z);
         if (particleLoc.distance(center) <= radius) {
            center.getWorld().spawnParticle(Particle.DUST, particleLoc, 1, 0.0, 0.0, 0.0, 0.0, new DustOptions(Color.fromRGB(0, 255, 0), 1.0F));
         }
      }
   }

   public boolean isOnCooldown(Player player) {
      Long end = this.cooldowns.get(player.getUniqueId());
      if (end == null) {
         return false;
      } else if (System.currentTimeMillis() >= end) {
         this.cooldowns.remove(player.getUniqueId());
         return false;
      } else {
         return true;
      }
   }

   public long getRemainingCooldown(Player player) {
      Long end = this.cooldowns.get(player.getUniqueId());
      if (end == null) {
         return 0L;
      } else {
         long diff = end - System.currentTimeMillis();
         if (diff <= 0L) {
            this.cooldowns.remove(player.getUniqueId());
            return 0L;
         } else {
            return (long)Math.ceil(diff / 1000.0);
         }
      }
   }
}
