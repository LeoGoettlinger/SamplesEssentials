package dev.demora.demoralegendaryweapons.weapons.golemhammer;

import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class GolemHammerListener implements Listener {
   private final DemoraLegendaryWeapons plugin;
   private final GolemHammer golemHammer;
   private final Map<UUID, Long> cooldowns = new HashMap<>();
   private final Map<UUID, Boolean> inAir = new HashMap<>();

   public GolemHammerListener(DemoraLegendaryWeapons plugin, GolemHammer golemHammer) {
      this.plugin = plugin;
      this.golemHammer = golemHammer;
   }

   @EventHandler
   public void onInteract(PlayerInteractEvent event) {
      if (event.getAction().isRightClick()) {
         final Player player = event.getPlayer();
         ItemStack item = player.getInventory().getItemInMainHand();
         if (this.golemHammer.isGolemHammer(item)) {
            event.setCancelled(true);
            if (this.isOnCooldown(player)) {
               long remaining = this.getRemainingCooldown(player);
               player.sendMessage(this.plugin.getMessageWithPrefix("weapon-messages.golem-hammer-cooldown").replace("{time}", String.valueOf(remaining)));
            } else {
               double launchHeight = this.plugin.getConfig().getDouble("weapons.golem_hammer.launch_height", 5.0);
               Vector velocity = new Vector(0.0, launchHeight / 2.5, 0.0);
               player.setVelocity(velocity);
               player.getWorld().playSound(player.getLocation(), Sound.ENTITY_IRON_GOLEM_ATTACK, 1.5F, 0.8F);
               player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1.0F, 0.5F);
               player.getWorld().spawnParticle(Particle.EXPLOSION, player.getLocation(), 5, 0.5, 0.5, 0.5, 0.0);
               player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 20, 0.5, 0.2, 0.5, 0.1);
               this.inAir.put(player.getUniqueId(), true);
               int cooldownSeconds = this.plugin.getConfig().getInt("weapons.golem_hammer.cooldown", 15);
               this.cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + cooldownSeconds * 1000L);
               (new BukkitRunnable() {
                     private int ticks = 0;
                     private final int maxTicks = 200;

                     public void run() {
                        if (player.isOnline() && this.ticks < 200) {
                           if (GolemHammerListener.this.inAir.getOrDefault(player.getUniqueId(), false) && player.isOnGround()) {
                              GolemHammerListener.this.createShockwave(player);
                              Bukkit.getScheduler()
                                 .runTaskLater(GolemHammerListener.this.plugin, () -> GolemHammerListener.this.inAir.remove(player.getUniqueId()), 20L);
                              this.cancel();
                           } else {
                              this.ticks++;
                           }
                        } else {
                           GolemHammerListener.this.inAir.remove(player.getUniqueId());
                           this.cancel();
                        }
                     }
                  })
                  .runTaskTimer(this.plugin, 5L, 1L);
            }
         }
      }
   }

   private void createShockwave(Player player) {
      final Location loc = player.getLocation();
      final double radius = this.plugin.getConfig().getDouble("weapons.golem_hammer.shockwave_radius", 6.0);
      final double damage = this.plugin.getConfig().getDouble("weapons.golem_hammer.shockwave_damage", 10.0);
      final double knockback = this.plugin.getConfig().getDouble("weapons.golem_hammer.knockback_strength", 2.0);
      player.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2.0F, 0.7F);
      player.getWorld().playSound(loc, Sound.ENTITY_IRON_GOLEM_DEATH, 1.5F, 0.8F);
      player.getWorld().playSound(loc, Sound.BLOCK_ANVIL_LAND, 2.0F, 0.5F);
      player.getWorld().spawnParticle(Particle.CLOUD, loc, 30, 0.3, 0.1, 0.3, 0.1);
      player.getWorld().spawnParticle(Particle.CRIT, loc, 20, 0.3, 0.1, 0.3, 0.05);
      (new BukkitRunnable() {
         private double currentRadius = 0.0;
         private final double maxRadius = radius;
         private final double speed = 1.5;
         private boolean damageDealt = false;

         public void run() {
            if (this.currentRadius >= this.maxRadius) {
               if (!this.damageDealt) {
                  for (LivingEntity entity : loc.getWorld().getNearbyLivingEntities(loc, this.maxRadius, e -> !e.equals(player))) {
                     entity.damage(damage, player);
                     Vector direction = entity.getLocation().toVector().subtract(loc.toVector()).normalize();
                     direction.setY(0.5);
                     direction.multiply(knockback);
                     entity.setVelocity(direction);
                     if (entity instanceof Player targetPlayer) {
                        targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 1, false, false));
                     }
                  }

                  this.damageDealt = true;
               }

               this.cancel();
            } else {
               int particles = (int)(this.currentRadius * 15.0);
               if (particles < 8) {
                  particles = 8;
               }

               for (int i = 0; i < particles; i++) {
                  double angle = (Math.PI * 2) * i / particles;
                  double x = Math.cos(angle) * this.currentRadius;
                  double z = Math.sin(angle) * this.currentRadius;
                  Location particleLoc = loc.clone().add(x, 0.1, z);
                  player.getWorld().spawnParticle(Particle.CLOUD, particleLoc, 2, 0.1, 0.1, 0.1, 0.02);
                  player.getWorld().spawnParticle(Particle.CRIT, particleLoc, 1, 0.05, 0.05, 0.05, 0.0);
               }

               this.currentRadius++;
            }
         }
      }).runTaskTimer(this.plugin, 0L, 1L);
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

   @EventHandler(
      priority = EventPriority.HIGH
   )
   public void onEntityDamage(EntityDamageEvent event) {
      if (event.getEntity() instanceof Player) {
         Player player = (Player)event.getEntity();
         if (event.getCause() == DamageCause.FALL && this.inAir.containsKey(player.getUniqueId())) {
            event.setCancelled(true);
         }
      }
   }

   @EventHandler
   public void onPlayerQuit(PlayerQuitEvent event) {
      UUID playerId = event.getPlayer().getUniqueId();
      this.cooldowns.remove(playerId);
      this.inAir.remove(playerId);
   }
}
