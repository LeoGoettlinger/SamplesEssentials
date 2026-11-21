package dev.demora.demoralegendaryweapons.weapons.withersickles;

import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import java.util.Iterator;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;

public class SickleThrowManager {
   private final DemoraLegendaryWeapons plugin;
   private final WitherSickles witherSickles;
   private final SickleCooldownManager cooldownManager;
   private final SickleInventoryManager inventoryManager;
   private final Random random = new Random();

   public SickleThrowManager(
      DemoraLegendaryWeapons plugin, WitherSickles witherSickles, SickleCooldownManager cooldownManager, SickleInventoryManager inventoryManager
   ) {
      this.plugin = plugin;
      this.witherSickles = witherSickles;
      this.cooldownManager = cooldownManager;
      this.inventoryManager = inventoryManager;
   }

   public void throwSickle(Player player) {
      final Location startLoc = player.getEyeLocation();
      final Vector direction = startLoc.getDirection().normalize();
      final ItemDisplay display = (ItemDisplay)player.getWorld().spawnEntity(startLoc, EntityType.ITEM_DISPLAY);
      display.setItemStack(this.witherSickles.createAdditionalSickle());
      display.setBillboard(Billboard.FIXED);
      double rotationX = 90.0;
      double rotationY = 0.0;
      double rotationZ = 0.0;
      Transformation transformation = display.getTransformation();
      transformation.getScale().set(1.0F, 1.0F, 1.0F);
      float angleX = (float)Math.toRadians(rotationX);
      float angleY = (float)Math.toRadians(rotationY);
      float angleZ = (float)Math.toRadians(rotationZ);
      Quaternionf rotation = new Quaternionf();
      rotation.rotateXYZ(angleX, angleY, angleZ);
      transformation.getLeftRotation().set(rotation);
      display.setTransformation(transformation);
      player.getWorld().playSound(startLoc, Sound.ENTITY_ITEM_PICKUP, 0.8F, 0.5F);
      player.getWorld().playSound(startLoc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0F, 1.2F);
      (new BukkitRunnable() {
         private int ticks = 0;
         private final int maxTicks = 100;
         private final double speed = 1.2;
         private Location currentLoc = startLoc.clone();

         public void run() {
            if (this.ticks < 100 && display.isValid()) {
               this.currentLoc.add(direction.clone().multiply(1.2));
               display.teleport(this.currentLoc);
               Iterator var1 = this.currentLoc.getWorld().getNearbyLivingEntities(this.currentLoc, 0.8, e -> !e.equals(player)).iterator();
               if (var1.hasNext()) {
                  LivingEntity entity = (LivingEntity)var1.next();
                  double throwDamage = SickleThrowManager.this.plugin.getConfig().getDouble("weapons.wither_sickles.throw_damage", 8.0);
                  entity.damage(throwDamage, player);
                  this.currentLoc.getWorld().playSound(this.currentLoc, Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0F, 0.8F);
                  this.currentLoc.getWorld().playSound(this.currentLoc, Sound.ENTITY_SKELETON_HORSE_DEATH, 0.7F, 1.5F);
                  this.currentLoc.getWorld().playSound(this.currentLoc, Sound.ENTITY_WITHER_SHOOT, 0.5F, 1.2F);
                  double throwChance = SickleThrowManager.this.plugin.getConfig().getDouble("weapons.wither_sickles.wither_chance_throw", 0.2);
                  if (SickleThrowManager.this.random.nextDouble() < throwChance) {
                     int duration = SickleThrowManager.this.plugin.getConfig().getInt("weapons.wither_sickles.wither_duration_ticks", 60);
                     entity.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, duration, 0, true, true));
                     this.currentLoc.getWorld().playSound(this.currentLoc, Sound.ENTITY_WITHER_AMBIENT, 0.8F, 0.9F);
                  }

                  display.remove();
                  SickleThrowManager.this.returnSickleToPlayer(player);
                  this.cancel();
               } else if (this.currentLoc.getBlock().getType().isSolid()) {
                  this.currentLoc.getWorld().playSound(this.currentLoc, Sound.BLOCK_STONE_HIT, 0.8F, 0.7F);
                  this.currentLoc.getWorld().playSound(this.currentLoc, Sound.ENTITY_ITEM_BREAK, 0.6F, 1.0F);
                  display.remove();
                  SickleThrowManager.this.returnSickleToPlayer(player);
                  this.cancel();
               } else {
                  this.ticks++;
               }
            } else {
               this.currentLoc.getWorld().playSound(this.currentLoc, Sound.ENTITY_ITEM_BREAK, 0.6F, 1.0F);
               this.currentLoc.getWorld().playSound(this.currentLoc, Sound.BLOCK_LAVA_EXTINGUISH, 0.4F, 1.5F);
               display.remove();
               SickleThrowManager.this.returnSickleToPlayer(player);
               this.cancel();
            }
         }
      }).runTaskTimer(this.plugin, 0L, 1L);
   }

   private void returnSickleToPlayer(Player player) {
      if (player.isOnline()) {
         long cooldownEnd = this.cooldownManager.getCooldownEndTime(player);
         long remaining = cooldownEnd - System.currentTimeMillis();
         if (remaining > 0L) {
            Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
               if (player.isOnline() && !this.cooldownManager.isOnCooldown(player)) {
                  ItemStack mainHand = player.getInventory().getItemInMainHand();
                  if (this.witherSickles.isWitherSickle(mainHand)) {
                     player.getInventory().setItemInOffHand(this.witherSickles.createAdditionalSickle());
                     this.inventoryManager.setOffhandSickle(player, true);
                  }
               }
            }, remaining / 50L);
         } else {
            Bukkit.getScheduler().runTask(this.plugin, () -> {
               ItemStack mainHand = player.getInventory().getItemInMainHand();
               if (this.witherSickles.isWitherSickle(mainHand)) {
                  player.getInventory().setItemInOffHand(this.witherSickles.createAdditionalSickle());
                  this.inventoryManager.setOffhandSickle(player, true);
               }
            });
         }
      }
   }
}
