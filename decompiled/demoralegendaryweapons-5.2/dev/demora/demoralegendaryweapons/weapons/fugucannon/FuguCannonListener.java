package dev.demora.demoralegendaryweapons.weapons.fugucannon;

import dev.demora.demoralegendaryweapons.AttributeCompat;
import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import io.papermc.paper.event.entity.EntityLoadCrossbowEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.Particle.DustOptions;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.PufferFish;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class FuguCannonListener implements Listener {
   private final DemoraLegendaryWeapons plugin;
   private final FuguCannon fuguCannon;
   private final NamespacedKey projectileKey;
   private final Map<UUID, Long> cooldowns = new HashMap<>();
   private final Map<UUID, UUID> fishOwners = new HashMap<>();
   private final Random random = new Random();

   public FuguCannonListener(DemoraLegendaryWeapons plugin) {
      this.plugin = plugin;
      this.fuguCannon = new FuguCannon(plugin);
      this.projectileKey = new NamespacedKey(plugin, "fugu_projectile");
   }

   public FuguCannon getFuguCannon() {
      return this.fuguCannon;
   }

   @EventHandler
   public void onLoad(EntityLoadCrossbowEvent event) {
      if (this.fuguCannon.isFuguCannon(event.getCrossbow())) {
         event.setConsumeItem(false);
      }
   }

   @EventHandler
   public void onShoot(EntityShootBowEvent event) {
      if (event.getEntity() instanceof Player player) {
         if (this.fuguCannon.isFuguCannon(event.getBow())) {
            Entity var4 = event.getProjectile();
            if (var4 instanceof Entity) {
               var4.remove();
            }

            if (this.isOnCooldown(player)) {
               event.setCancelled(true);
               long remaining = this.getRemainingCooldownSeconds(player.getUniqueId());
               player.sendMessage(this.plugin.getMessageWithPrefix("weapon-messages.fugu-cannon-cooldown").replace("{time}", String.valueOf(remaining)));
            } else {
               this.setCooldown(player);
               this.fire(player);
            }
         }
      }
   }

   private void fire(Player player) {
      Location eye = player.getEyeLocation();
      Vector base = eye.getDirection().normalize();
      this.playMuzzleEffect(player, eye, base);
      int count = this.fuguCannon.getProjectileCount();
      double spread = this.fuguCannon.getSpread();

      for (int i = 0; i < count; i++) {
         Vector direction = base.clone()
            .add(new Vector((this.random.nextDouble() - 0.5) * spread, (this.random.nextDouble() - 0.5) * spread, (this.random.nextDouble() - 0.5) * spread))
            .normalize();
         this.spawnFish(player, eye, direction);
      }
   }

   private void playMuzzleEffect(Player player, Location eye, Vector base) {
      World world = player.getWorld();
      Location muzzle = eye.clone().add(base.clone().multiply(0.9));
      Vector right = base.clone().crossProduct(new Vector(0, 1, 0)).normalize();
      Vector up = right.clone().crossProduct(base).normalize();

      for (int i = 0; i < 24; i++) {
         double angle = (Math.PI / 12) * i;
         Vector ring = right.clone().multiply(Math.cos(angle) * 0.35).add(up.clone().multiply(Math.sin(angle) * 0.35));
         Location ringLoc = muzzle.clone().add(ring);
         world.spawnParticle(Particle.BUBBLE, ringLoc, 1, 0.02, 0.02, 0.02, 0.0);
         world.spawnParticle(Particle.DUST, ringLoc, 1, 0.0, 0.0, 0.0, 0.0, new DustOptions(Color.fromRGB(255, 224, 64), 1.1F));
      }

      world.spawnParticle(Particle.SPLASH, muzzle, 35, 0.25, 0.25, 0.25, 0.2);
      world.spawnParticle(Particle.ITEM, muzzle, 18, 0.2, 0.2, 0.2, 0.12, new ItemStack(Material.PUFFERFISH));
      world.spawnParticle(Particle.CLOUD, muzzle, 10, 0.1, 0.1, 0.1, 0.03);
      world.playSound(eye, Sound.ITEM_CROSSBOW_SHOOT, 1.0F, 0.8F);
      world.playSound(eye, Sound.ENTITY_PUFFER_FISH_BLOW_OUT, 1.3F, 0.9F);
      world.playSound(eye, Sound.ENTITY_GENERIC_SPLASH, 1.0F, 1.2F);
   }

   private void spawnFish(Player player, Location eye, Vector direction) {
      World world = player.getWorld();
      Location spawnLoc = eye.clone().add(direction.clone().multiply(1.0));
      double health = this.fuguCannon.getFishHealth();
      PufferFish fish = (PufferFish)world.spawn(spawnLoc, PufferFish.class, spawned -> {
         spawned.setPuffState(2);
         spawned.setPersistent(false);
         spawned.setGravity(false);
         spawned.setSilent(true);
         AttributeInstance maxHealth = AttributeCompat.getMaxHealth() != null ? spawned.getAttribute(AttributeCompat.getMaxHealth()) : null;
         if (maxHealth != null) {
            maxHealth.setBaseValue(health);
         }

         spawned.setHealth(Math.min(health, spawned.getMaxHealth()));
         spawned.getPersistentDataContainer().set(this.projectileKey, PersistentDataType.BYTE, (byte)1);
      });
      Vector flightDirection = direction.clone();
      double flightSpeed = this.fuguCannon.getFlightSpeed();
      fish.setVelocity(flightDirection.clone().multiply(flightSpeed));
      this.fishOwners.put(fish.getUniqueId(), player.getUniqueId());
      this.trackFish(fish, player.getUniqueId(), flightDirection, flightSpeed);
   }

   private void trackFish(PufferFish fish, UUID ownerId, Vector direction, double flightSpeed) {
      final double radius = this.fuguCannon.getTouchRadius();
      final int lifetimeTicks = this.fuguCannon.getLifetimeSeconds() * 20;
      final long hitCooldownMs = this.fuguCannon.getHitCooldownSeconds() * 1000L;
      final int poweredTicks = flightSpeed > 0.0 ? (int)Math.max(1L, Math.round(this.fuguCannon.getFlightDistance() / flightSpeed)) : 0;
      (new BukkitRunnable() {
            private final Map<UUID, Long> lastHit = new HashMap<>();
            private int ticks = 0;

            public void run() {
               if (fish.isValid() && !fish.isDead()) {
                  if (this.ticks >= lifetimeTicks) {
                     FuguCannonListener.this.despawnFish(fish);
                     this.cancel();
                  } else {
                     fish.setPuffState(2);
                     Location loc = fish.getLocation();
                     World world = fish.getWorld();
                     boolean flying = this.ticks < poweredTicks;
                     if (flying) {
                        fish.setVelocity(direction.clone().multiply(flightSpeed));
                     } else {
                        fish.setGravity(true);
                     }

                     int bubbles = flying ? 6 : 3;
                     world.spawnParticle(Particle.BUBBLE, loc, bubbles, 0.12, 0.12, 0.12, 0.02);
                     world.spawnParticle(Particle.BUBBLE_POP, loc, flying ? 2 : 1, 0.1, 0.1, 0.1, 0.01);
                     if (this.ticks % 2 == 0) {
                        world.spawnParticle(Particle.SPLASH, loc, 1, 0.05, 0.05, 0.05, 0.0);
                     }

                     Player shooter = Bukkit.getPlayer(ownerId);
                     long now = System.currentTimeMillis();

                     for (Entity entity : world.getNearbyEntities(loc, radius, radius, radius)) {
                        if (entity instanceof LivingEntity living
                           && !entity.getUniqueId().equals(ownerId)
                           && !entity.getPersistentDataContainer().has(FuguCannonListener.this.projectileKey, PersistentDataType.BYTE)) {
                           Long last = this.lastHit.get(entity.getUniqueId());
                           if (last == null || now - last >= hitCooldownMs) {
                              this.lastHit.put(entity.getUniqueId(), now);
                              FuguCannonListener.this.applyEffects(living, shooter, loc);
                           }
                        }
                     }

                     this.ticks++;
                  }
               } else {
                  FuguCannonListener.this.fishOwners.remove(fish.getUniqueId());
                  this.cancel();
               }
            }
         })
         .runTaskTimer(this.plugin, 1L, 1L);
   }

   @EventHandler
   public void onFishSting(EntityDamageByEntityEvent event) {
      if (event.getDamager() instanceof PufferFish fish) {
         if (fish.getPersistentDataContainer().has(this.projectileKey, PersistentDataType.BYTE)) {
            event.setCancelled(true);
         }
      }
   }

   private void applyEffects(LivingEntity target, Player shooter, Location source) {
      int poisonTicks = this.fuguCannon.getPoisonDurationSeconds() * 20;
      target.removePotionEffect(PotionEffectType.POISON);
      target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, poisonTicks, this.fuguCannon.getPoisonAmplifier()));
      double damage = this.fuguCannon.getImpactDamage();
      if (damage > 0.0) {
         target.setNoDamageTicks(0);
         if (shooter != null) {
            target.damage(damage, shooter);
         } else {
            target.damage(damage);
         }
      }

      World world = source.getWorld();
      Location hit = target.getLocation().add(0.0, target.getHeight() * 0.6, 0.0);
      world.spawnParticle(Particle.CRIT, hit, 25, 0.3, 0.4, 0.3, 0.25);
      world.spawnParticle(Particle.DAMAGE_INDICATOR, hit, 8, 0.2, 0.3, 0.2, 0.05);
      world.spawnParticle(Particle.ITEM, hit, 12, 0.2, 0.3, 0.2, 0.05, new ItemStack(Material.PUFFERFISH));
      world.playSound(hit, Sound.ENTITY_PUFFER_FISH_STING, 1.2F, 1.0F);
   }

   private void despawnFish(PufferFish fish) {
      this.fishOwners.remove(fish.getUniqueId());
      if (fish.isValid() && !fish.isDead()) {
         World world = fish.getWorld();
         world.spawnParticle(Particle.BUBBLE_POP, fish.getLocation(), 12, 0.3, 0.3, 0.3, 0.05);
         fish.remove();
      }
   }

   @EventHandler
   public void onQuit(PlayerQuitEvent event) {
      UUID playerId = event.getPlayer().getUniqueId();
      this.cooldowns.remove(playerId);
      Iterator<Entry<UUID, UUID>> iterator = this.fishOwners.entrySet().iterator();

      while (iterator.hasNext()) {
         Entry<UUID, UUID> entry = iterator.next();
         if (entry.getValue().equals(playerId)) {
            if (Bukkit.getEntity(entry.getKey()) instanceof PufferFish fish && fish.isValid() && !fish.isDead()) {
               fish.remove();
            }

            iterator.remove();
         }
      }
   }

   private boolean isOnCooldown(Player player) {
      Long last = this.cooldowns.get(player.getUniqueId());
      return last == null ? false : System.currentTimeMillis() - last < this.fuguCannon.getCooldownSeconds() * 1000L;
   }

   private void setCooldown(Player player) {
      this.cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
   }

   public long getRemainingCooldownSeconds(UUID playerId) {
      Long last = this.cooldowns.get(playerId);
      if (last == null) {
         return 0L;
      } else {
         long remaining = this.fuguCannon.getCooldownSeconds() * 1000L - (System.currentTimeMillis() - last);
         return Math.max(0L, remaining / 1000L);
      }
   }
}
