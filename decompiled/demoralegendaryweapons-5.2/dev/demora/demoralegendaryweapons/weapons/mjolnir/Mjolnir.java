package dev.demora.demoralegendaryweapons.weapons.mjolnir;

import dev.demora.demoralegendaryweapons.AttributeCompat;
import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;

public class Mjolnir {
   private final DemoraLegendaryWeapons plugin;
   private final NamespacedKey mjolnirKey;
   private final Map<String, Long> cooldowns;
   private final Set<UUID> processingMjolnirDamage;
   private final List<Runnable> throwChargeDecayTicks = new ArrayList<>();
   private boolean throwChargeDecayLoopStarted;

   public Mjolnir(DemoraLegendaryWeapons plugin) {
      this.plugin = plugin;
      this.mjolnirKey = new NamespacedKey(plugin, "mjolnir");
      this.cooldowns = new HashMap<>();
      this.processingMjolnirDamage = new HashSet<>();
   }

   public void registerThrowChargeDecayTick(Runnable tick) {
      if (tick != null) {
         this.throwChargeDecayTicks.add(tick);
      }
   }

   public void startThrowChargeDecayLoop() {
      if (!this.throwChargeDecayLoopStarted) {
         this.throwChargeDecayLoopStarted = true;
         Bukkit.getScheduler().runTaskTimer(this.plugin, () -> {
            for (Runnable r : this.throwChargeDecayTicks) {
               try {
                  r.run();
               } catch (Exception var4) {
               }
            }
         }, 1L, 1L);
      }
   }

   public ItemStack createMjolnir() {
      ItemStack item = new ItemStack(Material.STONE_AXE);
      ItemMeta meta = item.getItemMeta();
      if (meta != null) {
         String weaponName = this.plugin.getLanguageManager().getWeaponName("mjolnir");
         if (weaponName.isEmpty()) {
            weaponName = "§6§lМьёльнир";
         }

         meta.setDisplayName(weaponName);
         meta.setCustomModelData(1);
         meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES});
         meta.setUnbreakable(true);
         double damage = this.plugin.getConfig().getDouble("weapons.mjolnir.attribute_damage", 11.0);
         AttributeModifier damageModifier = new AttributeModifier(UUID.randomUUID(), "generic.attack_damage", damage, Operation.ADD_NUMBER, EquipmentSlot.HAND);
         if (AttributeCompat.getAttackDamage() != null) {
            meta.addAttributeModifier(AttributeCompat.getAttackDamage(), damageModifier);
         }

         double attackSpeed = this.plugin.getConfig().getDouble("weapons.mjolnir.attack_speed", -2.4);
         AttributeModifier attackSpeedModifier = new AttributeModifier(
            UUID.randomUUID(), "generic.attack_speed", attackSpeed, Operation.ADD_NUMBER, EquipmentSlot.HAND
         );
         if (AttributeCompat.getAttackSpeed() != null) {
            meta.addAttributeModifier(AttributeCompat.getAttackSpeed(), attackSpeedModifier);
         }

         meta.getPersistentDataContainer().set(this.mjolnirKey, PersistentDataType.STRING, "mjolnir");
         this.plugin.applyWeaponLore(meta, "mjolnir");
         item.setItemMeta(meta);
      }

      return item;
   }

   public boolean isMjolnir(ItemStack item) {
      if (item != null && item.hasItemMeta()) {
         ItemMeta meta = item.getItemMeta();
         return meta == null ? false : meta.getPersistentDataContainer().has(this.mjolnirKey, PersistentDataType.STRING);
      } else {
         return false;
      }
   }

   public boolean isOnThrowCooldown(Player player) {
      String playerId = player.getUniqueId().toString();
      if (!this.cooldowns.containsKey(playerId + "_throw")) {
         return false;
      } else {
         long lastUse = this.cooldowns.get(playerId + "_throw");
         int cooldownDuration = this.plugin.getConfig().getInt("weapons.mjolnir.cooldown.throw_duration", 10);
         long currentTime = System.currentTimeMillis();
         return currentTime - lastUse < cooldownDuration * 1000L;
      }
   }

   public boolean isOnMeleeCooldown(Player player) {
      String playerId = player.getUniqueId().toString();
      if (!this.cooldowns.containsKey(playerId + "_melee")) {
         return false;
      } else {
         long lastUse = this.cooldowns.get(playerId + "_melee");
         int cooldownDuration = this.plugin.getConfig().getInt("weapons.mjolnir.cooldown.melee_duration", 5);
         long currentTime = System.currentTimeMillis();
         return currentTime - lastUse < cooldownDuration * 1000L;
      }
   }

   public void setThrowCooldown(Player player) {
      this.cooldowns.put(player.getUniqueId().toString() + "_throw", System.currentTimeMillis());
   }

   public void setMeleeCooldown(Player player) {
      this.cooldowns.put(player.getUniqueId().toString() + "_melee", System.currentTimeMillis());
   }

   public long getRemainingThrowCooldown(Player player) {
      String playerId = player.getUniqueId().toString();
      if (!this.cooldowns.containsKey(playerId + "_throw")) {
         return 0L;
      } else {
         long lastUse = this.cooldowns.get(playerId + "_throw");
         int cooldownDuration = this.plugin.getConfig().getInt("weapons.mjolnir.cooldown.throw_duration", 10);
         long currentTime = System.currentTimeMillis();
         long remaining = cooldownDuration * 1000L - (currentTime - lastUse);
         return Math.max(0L, remaining / 1000L);
      }
   }

   public long getRemainingMeleeCooldown(Player player) {
      String playerId = player.getUniqueId().toString();
      if (!this.cooldowns.containsKey(playerId + "_melee")) {
         return 0L;
      } else {
         long lastUse = this.cooldowns.get(playerId + "_melee");
         int cooldownDuration = this.plugin.getConfig().getInt("weapons.mjolnir.cooldown.melee_duration", 5);
         long currentTime = System.currentTimeMillis();
         long remaining = cooldownDuration * 1000L - (currentTime - lastUse);
         return Math.max(0L, remaining / 1000L);
      }
   }

   public void throwMjolnir(Player player) {
      final Location loc = player.getEyeLocation();
      final ItemDisplay mjolnirDisplay = (ItemDisplay)player.getWorld().spawnEntity(loc, EntityType.ITEM_DISPLAY);
      ItemStack displayItem = new ItemStack(Material.STONE_AXE);
      ItemMeta displayMeta = displayItem.getItemMeta();
      displayMeta.setCustomModelData(1);
      displayItem.setItemMeta(displayMeta);
      mjolnirDisplay.setItemStack(displayItem);

      try {
         mjolnirDisplay.setInterpolationDelay(0);
         mjolnirDisplay.setInterpolationDuration(2);
      } catch (NoSuchMethodError var14) {
      }

      Transformation transformation = mjolnirDisplay.getTransformation();
      transformation.getLeftRotation().rotationX((float)Math.toRadians(90.0));
      mjolnirDisplay.setTransformation(transformation);
      mjolnirDisplay.setRotation(loc.getYaw(), loc.getPitch());
      player.getInventory().setItemInMainHand(null);
      final Vector direction = loc.getDirection().normalize();
      player.getWorld().playSound(loc, Sound.ENTITY_ENDER_DRAGON_SHOOT, 1.0F, 1.0F);
      final double flightSpeed = this.plugin.getConfig().getDouble("weapons.mjolnir.animation.flight_speed", 1.8);
      final double returnSpeed = this.plugin.getConfig().getDouble("weapons.mjolnir.animation.return_speed", 1.6);
      final float rotationSpeed = (float)this.plugin.getConfig().getDouble("weapons.mjolnir.animation.rotation_speed", 25.0);
      final float returnRotationMultiplier = (float)this.plugin.getConfig().getDouble("weapons.mjolnir.animation.return_rotation_multiplier", 1.5);
      (new BukkitRunnable() {
         private final Location startLoc = loc.clone();
         private Location currentLoc = Mjolnir.super.startLoc.clone();
         private boolean isReturning = false;
         private int ticks = 0;
         private float rotationAngle = 0.0F;
         private Vector currentVelocity = direction.clone().normalize().multiply(flightSpeed);

         public void run() {
            if (!mjolnirDisplay.isDead() && mjolnirDisplay.isValid()) {
               if (!this.isReturning) {
                  this.currentLoc.add(this.currentVelocity);
                  double wobble = Math.sin(this.ticks * 0.3) * 0.05;
                  Location displayLoc = this.currentLoc.clone().add(0.0, wobble, 0.0);
                  mjolnirDisplay.teleport(displayLoc);
                  this.rotationAngle = this.rotationAngle + rotationSpeed;
                  Transformation trans = mjolnirDisplay.getTransformation();
                  Quaternionf rotation = new Quaternionf();
                  rotation.rotateX((float)Math.toRadians(90.0));
                  rotation.rotateY((float)Math.toRadians(this.rotationAngle));
                  trans.getLeftRotation().set(rotation);
                  mjolnirDisplay.setTransformation(trans);
                  Block targetBlock = this.currentLoc.getBlock();
                  if (!targetBlock.isPassable() || this.ticks >= 80) {
                     this.currentLoc.getWorld().strikeLightning(this.currentLoc);
                     double areaDamage = Mjolnir.this.plugin.getConfig().getDouble("weapons.mjolnir.damage.area_damage", 12.0);

                     for (Entity entity : this.currentLoc.getWorld().getNearbyEntities(this.currentLoc, 2.0, 2.0, 2.0)) {
                        if (entity instanceof LivingEntity && entity != player) {
                           ((LivingEntity)entity).damage(areaDamage, player);
                        }
                     }

                     this.isReturning = true;
                     return;
                  }

                  for (Entity entityx : this.currentLoc.getWorld().getNearbyEntities(this.currentLoc, 1.0, 1.0, 1.0)) {
                     if (entityx instanceof LivingEntity && entityx != player) {
                        if (entityx instanceof Player targetPlayer) {
                           targetPlayer.getWorld().strikeLightning(targetPlayer.getLocation());
                           double throwDamage = Mjolnir.this.plugin.getConfig().getDouble("weapons.mjolnir.damage.throw_damage", 15.0);
                           targetPlayer.damage(throwDamage, player);
                        } else {
                           this.currentLoc.getWorld().strikeLightning(this.currentLoc);
                           double areaDamage = Mjolnir.this.plugin.getConfig().getDouble("weapons.mjolnir.damage.area_damage", 12.0);

                           for (Entity nearbyEntity : this.currentLoc.getWorld().getNearbyEntities(this.currentLoc, 2.0, 2.0, 2.0)) {
                              if (nearbyEntity instanceof LivingEntity && nearbyEntity != player) {
                                 ((LivingEntity)nearbyEntity).damage(areaDamage, player);
                              }
                           }
                        }

                        this.isReturning = true;
                        return;
                     }
                  }
               } else {
                  Location playerTarget = player.getLocation().add(0.0, 1.0, 0.0);
                  Vector returnVector = playerTarget.subtract(this.currentLoc).toVector().normalize().multiply(returnSpeed);
                  this.currentLoc.add(returnVector);
                  mjolnirDisplay.teleport(this.currentLoc);
                  this.rotationAngle = this.rotationAngle + rotationSpeed * returnRotationMultiplier;
                  Transformation trans = mjolnirDisplay.getTransformation();
                  Quaternionf rotation = new Quaternionf();
                  rotation.rotateX((float)Math.toRadians(90.0));
                  rotation.rotateY((float)Math.toRadians(this.rotationAngle));
                  trans.getLeftRotation().set(rotation);
                  mjolnirDisplay.setTransformation(trans);
                  if (this.currentLoc.distance(player.getLocation()) < 1.5) {
                     player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_SHOOT, 1.0F, 0.5F);
                     ItemStack mjolnir = Mjolnir.this.createMjolnir();
                     HashMap<Integer, ItemStack> leftOver = player.getInventory().addItem(new ItemStack[]{mjolnir});
                     if (!leftOver.isEmpty()) {
                        for (ItemStack item : leftOver.values()) {
                           player.getWorld().dropItem(player.getLocation(), item);
                        }
                     }

                     mjolnirDisplay.remove();
                     this.cancel();
                     return;
                  }
               }

               this.currentLoc.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, this.currentLoc, 5, 0.1, 0.1, 0.1, 0.05);
               if (this.ticks % 3 == 0) {
                  this.currentLoc.getWorld().spawnParticle(Particle.CLOUD, this.currentLoc, 2, 0.2, 0.2, 0.2, 0.01);
               }

               this.ticks++;
            } else {
               this.cancel();
            }
         }
      }).runTaskTimer(this.plugin, 0L, 1L);
   }

   public boolean isProcessingDamage(UUID playerId) {
      return this.processingMjolnirDamage.contains(playerId);
   }

   public void addProcessingDamage(UUID playerId) {
      this.processingMjolnirDamage.add(playerId);
   }

   public void removeProcessingDamage(UUID playerId) {
      this.processingMjolnirDamage.remove(playerId);
   }

   public void performMeleeAttack(Player target, Player attacker) {
      (new BukkitRunnable() {
         public void run() {
            Location lightningLoc = target.getLocation().add(0.0, 1.0, 0.0);
            target.getWorld().strikeLightningEffect(lightningLoc);
            target.getWorld().playSound(target.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0F, 1.0F);
            target.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, lightningLoc, 15, 0.3, 0.3, 0.3, 0.05);
         }
      }).runTaskLater(this.plugin, 1L);
   }
}
