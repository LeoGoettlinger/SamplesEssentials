package dev.demora.demoralegendaryweapons.weapons.artemisbow;

import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class ArtemisBowListener implements Listener {
   private final DemoraLegendaryWeapons plugin;
   private final ArtemisBow artemisBow;
   private final Set<Arrow> specialArrows;
   private final Set<Arrow> normalArtemisArrows;
   private final Set<Arrow> lightningStruckArrows;
   private final Random random;
   private final NamespacedKey specialArrowKey;

   public ArtemisBowListener(DemoraLegendaryWeapons plugin) {
      this.plugin = plugin;
      this.artemisBow = new ArtemisBow(plugin);
      this.specialArrows = new HashSet<>();
      this.normalArtemisArrows = new HashSet<>();
      this.lightningStruckArrows = new HashSet<>();
      this.random = new Random();
      this.specialArrowKey = new NamespacedKey(plugin, "artemis_special_arrow");
   }

   public ArtemisBow getArtemisBow() {
      return this.artemisBow;
   }

   @EventHandler
   public void onEntityShootBow(EntityShootBowEvent event) {
      if (event.getEntity() instanceof Player) {
         Player player = (Player)event.getEntity();
         ItemStack bow = event.getBow();
         if (this.artemisBow.isArtemisBow(bow)) {
            if (event.getProjectile() instanceof Arrow) {
               Arrow arrow = (Arrow)event.getProjectile();
               arrow.setGlowing(true);
               double chance = this.artemisBow.getSpecialArrowChance();
               this.normalArtemisArrows.add(arrow);
               if (this.random.nextDouble() < chance) {
                  this.specialArrows.add(arrow);
                  arrow.getPersistentDataContainer().set(this.specialArrowKey, PersistentDataType.BOOLEAN, true);
                  this.startArrowTracking(arrow, player);
               } else {
                  this.startArrowTracking(arrow, player);
               }
            }
         }
      }
   }

   private void startArrowTracking(Arrow arrow, Player shooter) {
      (new BukkitRunnable() {
            private int ticks = 0;
            private final int maxTicks = 200;

            public void run() {
               if (this.ticks < 200 && !arrow.isDead() && arrow.isValid() && !arrow.isOnGround()) {
                  boolean isSpecial = ArtemisBowListener.this.specialArrows.contains(arrow)
                     || arrow.getPersistentDataContainer().has(ArtemisBowListener.this.specialArrowKey, PersistentDataType.BOOLEAN);
                  if (isSpecial) {
                     Location arrowLoc = arrow.getLocation();
                     LivingEntity target = ArtemisBowListener.this.findNearestTarget(arrowLoc, shooter);
                     if (target != null && target != shooter) {
                        Vector direction = target.getLocation().add(0.0, 1.0, 0.0).subtract(arrowLoc).toVector().normalize();
                        arrow.setVelocity(direction.multiply(arrow.getVelocity().length()));
                        if (arrowLoc.distance(target.getLocation()) < 2.0 && !ArtemisBowListener.this.lightningStruckArrows.contains(arrow)) {
                           target.getWorld().strikeLightning(target.getLocation());
                           target.getWorld().playSound(target.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0F, 1.0F);
                           ArtemisBowListener.this.lightningStruckArrows.add(arrow);
                           ArtemisBowListener.this.specialArrows.remove(arrow);
                           ArtemisBowListener.this.normalArtemisArrows.remove(arrow);
                           this.cancel();
                           return;
                        }
                     }
                  }

                  this.ticks++;
               } else {
                  ArtemisBowListener.this.specialArrows.remove(arrow);
                  ArtemisBowListener.this.normalArtemisArrows.remove(arrow);
                  ArtemisBowListener.this.lightningStruckArrows.remove(arrow);
                  this.cancel();
               }
            }
         })
         .runTaskTimer(this.plugin, 0L, 1L);
   }

   private LivingEntity findNearestTarget(Location arrowLoc, Player shooter) {
      double searchRadius = 5.0;
      LivingEntity nearestTarget = null;
      double nearestDistance = Double.MAX_VALUE;
      boolean foundPlayer = false;

      for (Entity entity : arrowLoc.getWorld().getNearbyEntities(arrowLoc, searchRadius, searchRadius, searchRadius)) {
         if (entity instanceof LivingEntity && entity != shooter) {
            LivingEntity livingEntity = (LivingEntity)entity;
            double distance = arrowLoc.distance(livingEntity.getLocation());
            if (entity instanceof Player) {
               if (!foundPlayer || distance < nearestDistance) {
                  nearestTarget = livingEntity;
                  nearestDistance = distance;
                  foundPlayer = true;
               }
            } else if (!foundPlayer && distance < nearestDistance) {
               nearestTarget = livingEntity;
               nearestDistance = distance;
            }
         }
      }

      return nearestTarget;
   }

   @EventHandler
   public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
      if (event.getDamager() instanceof Arrow) {
         Arrow arrow = (Arrow)event.getDamager();
         if (arrow.getShooter() instanceof Player) {
            Player shooter = (Player)arrow.getShooter();
            boolean isArtemisArrow = this.normalArtemisArrows.contains(arrow) || this.specialArrows.contains(arrow);
            boolean isSpecialArrow = this.specialArrows.contains(arrow)
               || arrow.getPersistentDataContainer().has(this.specialArrowKey, PersistentDataType.BOOLEAN);
            if (!isArtemisArrow) {
               ItemStack mainHand = shooter.getInventory().getItemInMainHand();
               ItemStack offHand = shooter.getInventory().getItemInOffHand();
               boolean hasArtemisBow = this.artemisBow.isArtemisBow(mainHand) || this.artemisBow.isArtemisBow(offHand);
               if (!hasArtemisBow) {
                  return;
               }

               this.normalArtemisArrows.add(arrow);
               isSpecialArrow = arrow.getPersistentDataContainer().has(this.specialArrowKey, PersistentDataType.BOOLEAN);
            }

            if (isSpecialArrow) {
               double damage = this.artemisBow.getSpecialArrowDamage();
               event.setDamage(damage);
               if (!this.lightningStruckArrows.contains(arrow) && event.getEntity() instanceof LivingEntity) {
                  LivingEntity target = (LivingEntity)event.getEntity();
                  if (target != shooter) {
                     Location targetLoc = target.getLocation();
                     target.getWorld().strikeLightning(targetLoc);
                     target.getWorld().playSound(targetLoc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0F, 1.0F);
                     this.lightningStruckArrows.add(arrow);
                  }
               }

               this.specialArrows.remove(arrow);
               this.normalArtemisArrows.remove(arrow);
               this.lightningStruckArrows.remove(arrow);
               if (arrow.isValid()) {
                  arrow.getPersistentDataContainer().remove(this.specialArrowKey);
               }
            } else {
               if (this.normalArtemisArrows.contains(arrow)) {
                  double damagex = this.artemisBow.getArrowDamage();
                  event.setDamage(damagex);
                  this.normalArtemisArrows.remove(arrow);
               }
            }
         }
      }
   }
}
