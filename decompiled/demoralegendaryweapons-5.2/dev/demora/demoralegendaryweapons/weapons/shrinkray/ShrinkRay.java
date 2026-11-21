package dev.demora.demoralegendaryweapons.weapons.shrinkray;

import dev.demora.demoralegendaryweapons.AttributeCompat;
import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class ShrinkRay {
   private final DemoraLegendaryWeapons plugin;
   private final NamespacedKey shrinkRayKey;
   private final Map<UUID, Long> shrinkEffects;
   private final Map<UUID, Long> enlargeEffects;
   private final Map<UUID, Long> cooldowns;

   public ShrinkRay(DemoraLegendaryWeapons plugin) {
      this.plugin = plugin;
      this.shrinkRayKey = new NamespacedKey(plugin, "shrink_ray");
      this.shrinkEffects = new HashMap<>();
      this.enlargeEffects = new HashMap<>();
      this.cooldowns = new HashMap<>();
   }

   public ItemStack createShrinkRay() {
      ItemStack bow = new ItemStack(Material.BOW);
      ItemMeta meta = bow.getItemMeta();
      String weaponName = this.plugin.getLanguageManager().getWeaponName("shrink_ray");
      if (weaponName.isEmpty()) {
         weaponName = "§9§lУменьшающий луч";
      }

      meta.setDisplayName(weaponName);
      meta.setCustomModelData(12);
      meta.setUnbreakable(true);
      meta.getPersistentDataContainer().set(this.shrinkRayKey, PersistentDataType.BYTE, (byte)1);
      this.plugin.applyWeaponLore(meta, "shrink_ray");
      bow.setItemMeta(meta);
      return bow;
   }

   public boolean isShrinkRay(ItemStack item) {
      if (item != null && item.getType() == Material.BOW) {
         ItemMeta meta = item.getItemMeta();
         return meta == null ? false : meta.getPersistentDataContainer().has(this.shrinkRayKey, PersistentDataType.BYTE);
      } else {
         return false;
      }
   }

   public boolean isOnCooldown(Player player) {
      UUID playerId = player.getUniqueId();
      return this.cooldowns.containsKey(playerId) && System.currentTimeMillis() < this.cooldowns.get(playerId);
   }

   public long getRemainingCooldown(Player player) {
      if (!this.isOnCooldown(player)) {
         return 0L;
      } else {
         UUID playerId = player.getUniqueId();
         return (this.cooldowns.get(playerId) - System.currentTimeMillis()) / 1000L;
      }
   }

   public void shrinkPlayer(Player player) {
      if (this.isOnCooldown(player)) {
         long remaining = this.getRemainingCooldown(player);
         player.sendMessage(this.plugin.getMessageWithPrefix("weapon-messages.shrink-ray-cooldown").replace("{time}", String.valueOf(remaining)));
      } else {
         final UUID playerId = player.getUniqueId();
         if (this.shrinkEffects.containsKey(playerId)) {
            long remaining = (this.shrinkEffects.get(playerId) - System.currentTimeMillis()) / 1000L;
            player.sendMessage(this.plugin.getMessageWithPrefix("weapon-messages.shrink-ray-already-small").replace("{time}", String.valueOf(remaining)));
         } else if (this.enlargeEffects.containsKey(playerId)) {
            player.sendMessage(this.plugin.getMessageWithPrefix("weapon-messages.shrink-ray-cannot-shrink"));
         } else {
            if (AttributeCompat.getScale() != null) {
               AttributeInstance scaleAttr = player.getAttribute(AttributeCompat.getScale());
               if (scaleAttr != null) {
                  scaleAttr.setBaseValue(0.5);
               }
            }

            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0F, 2.0F);
            player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, player.getLocation().add(0.0, 1.0, 0.0), 20, 0.3, 0.5, 0.3, 0.0);
            int duration = this.plugin.getConfig().getInt("weapons.shrink_ray.shrink_duration", 12);
            final long endTime = System.currentTimeMillis() + duration * 1000L;
            this.shrinkEffects.put(playerId, endTime);
            int cooldown = this.plugin.getConfig().getInt("weapons.shrink_ray.cooldown", 20);
            this.setCooldown(player, cooldown);
            (new BukkitRunnable() {
               public void run() {
                  if (System.currentTimeMillis() >= endTime) {
                     ShrinkRay.this.resetPlayerSize(player);
                     ShrinkRay.this.shrinkEffects.remove(playerId);
                     this.cancel();
                  }
               }
            }).runTaskTimer(this.plugin, 0L, 20L);
         }
      }
   }

   public void shootRay(Player shooter) {
      if (this.isOnCooldown(shooter)) {
         long remaining = this.getRemainingCooldown(shooter);
         shooter.sendMessage(this.plugin.getMessageWithPrefix("weapon-messages.shrink-ray-ray-cooldown").replace("{time}", String.valueOf(remaining)));
      } else {
         final Location eyeLoc = shooter.getEyeLocation();
         final Vector direction = eyeLoc.getDirection();
         final World world = shooter.getWorld();
         final double raySpeed = this.plugin.getConfig().getDouble("weapons.shrink_ray.ray_speed", 0.5);
         final int cooldown = this.plugin.getConfig().getInt("weapons.shrink_ray.cooldown", 20);
         world.playSound(eyeLoc, Sound.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM, 1.0F, 1.5F);
         final boolean[] hitTarget = new boolean[]{false};
         (new BukkitRunnable() {
               Location currentLoc = eyeLoc.clone();
               double distance = 0.0;
               final double maxDistance = 50.0;
               final double step = raySpeed;

               public void run() {
                  if (this.distance >= 50.0) {
                     if (!hitTarget[0]) {
                        ShrinkRay.this.setCooldown(shooter, 5);
                     }

                     this.cancel();
                  } else {
                     this.currentLoc.add(direction.clone().multiply(this.step));
                     this.distance = this.distance + this.step;
                     world.spawnParticle(Particle.HAPPY_VILLAGER, this.currentLoc, 2, 0.0, 0.0, 0.0, 0.0);
                     world.spawnParticle(Particle.COMPOSTER, this.currentLoc, 1, 0.0, 0.0, 0.0, 0.0);
                     RayTraceResult result = world.rayTraceEntities(
                        this.currentLoc, direction, this.step, entity -> entity instanceof LivingEntity && entity != shooter
                     );
                     if (result != null && result.getHitEntity() != null) {
                        Entity hitEntity = result.getHitEntity();
                        if (hitEntity instanceof LivingEntity) {
                           hitTarget[0] = true;
                           ShrinkRay.this.enlargeEntity((LivingEntity)hitEntity);
                           ShrinkRay.this.setCooldown(shooter, cooldown);
                           this.cancel();
                           return;
                        }
                     }

                     if (this.currentLoc.getBlock().getType().isSolid()) {
                        world.spawnParticle(Particle.HAPPY_VILLAGER, this.currentLoc, 30, 0.2, 0.2, 0.2, 0.0);
                        world.playSound(this.currentLoc, Sound.BLOCK_GLASS_BREAK, 0.5F, 1.5F);
                        if (!hitTarget[0]) {
                           ShrinkRay.this.setCooldown(shooter, 5);
                        }

                        this.cancel();
                     }
                  }
               }
            })
            .runTaskTimer(this.plugin, 0L, 1L);
      }
   }

   private void setCooldown(Player player, int seconds) {
      UUID playerId = player.getUniqueId();
      this.cooldowns.put(playerId, System.currentTimeMillis() + seconds * 1000L);
   }

   private void enlargeEntity(LivingEntity entity) {
      final UUID entityId = entity.getUniqueId();
      if (!this.enlargeEffects.containsKey(entityId)) {
         if (entity instanceof Player player && this.shrinkEffects.containsKey(entityId)) {
            player.sendMessage(this.plugin.getMessageWithPrefix("weapon-messages.shrink-ray-cannot-grow"));
         } else {
            if (AttributeCompat.getScale() != null) {
               AttributeInstance scaleAttr = entity.getAttribute(AttributeCompat.getScale());
               if (scaleAttr != null) {
                  scaleAttr.setBaseValue(2.0);
               }
            }

            entity.getWorld().playSound(entity.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0F, 0.5F);
            entity.getWorld().spawnParticle(Particle.COMPOSTER, entity.getLocation().add(0.0, 1.0, 0.0), 30, 0.5, 1.0, 0.5, 0.0);
            int duration = this.plugin.getConfig().getInt("weapons.shrink_ray.enlarge_duration", 12);
            final long endTime = System.currentTimeMillis() + duration * 1000L;
            this.enlargeEffects.put(entityId, endTime);
            (new BukkitRunnable() {
               public void run() {
                  if (System.currentTimeMillis() >= endTime) {
                     ShrinkRay.this.resetEntitySize(entity);
                     ShrinkRay.this.enlargeEffects.remove(entityId);
                     this.cancel();
                  }
               }
            }).runTaskTimer(this.plugin, 0L, 20L);
         }
      }
   }

   private void resetPlayerSize(Player player) {
      if (AttributeCompat.getScale() != null) {
         AttributeInstance scaleAttr = player.getAttribute(AttributeCompat.getScale());
         if (scaleAttr != null) {
            scaleAttr.setBaseValue(1.0);
         }
      }

      player.getWorld().playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5F, 1.0F);
   }

   private void resetEntitySize(LivingEntity entity) {
      if (AttributeCompat.getScale() != null) {
         AttributeInstance scaleAttr = entity.getAttribute(AttributeCompat.getScale());
         if (scaleAttr != null) {
            scaleAttr.setBaseValue(1.0);
         }
      }

      entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5F, 1.0F);
   }

   public void removePlayerData(Player player) {
      UUID playerId = player.getUniqueId();
      this.shrinkEffects.remove(playerId);
      this.enlargeEffects.remove(playerId);
      this.cooldowns.remove(playerId);
      this.resetPlayerSize(player);
   }
}
