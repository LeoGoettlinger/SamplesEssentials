package dev.demora.demoralegendaryweapons.weapons.reaperscythe;

import dev.demora.demoralegendaryweapons.AttributeCompat;
import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class ReaperScythe {
   private final DemoraLegendaryWeapons plugin;
   private final NamespacedKey reaperScytheKey;
   private final Map<UUID, Long> cooldowns;

   public ReaperScythe(DemoraLegendaryWeapons plugin) {
      this.plugin = plugin;
      this.reaperScytheKey = new NamespacedKey(plugin, "reaper_scythe");
      this.cooldowns = new HashMap<>();
   }

   public ItemStack createReaperScythe() {
      ItemStack scythe = new ItemStack(Material.STONE_HOE);
      ItemMeta meta = scythe.getItemMeta();
      if (meta != null) {
         String weaponName = this.plugin.getLanguageManager().getWeaponName("reaper_scythe");
         if (weaponName.isEmpty()) {
            weaponName = "§5§lКоса жнеца";
         }

         meta.setDisplayName(weaponName);
         meta.setCustomModelData(2);
         meta.setUnbreakable(true);
         double damage = this.plugin.getConfig().getDouble("weapons.reaper_scythe.attribute_damage", 11.0);
         AttributeModifier damageModifier = new AttributeModifier(UUID.randomUUID(), "generic.attack_damage", damage, Operation.ADD_NUMBER, EquipmentSlot.HAND);
         if (AttributeCompat.getAttackDamage() != null) {
            meta.addAttributeModifier(AttributeCompat.getAttackDamage(), damageModifier);
         }

         double attackSpeed = this.plugin.getConfig().getDouble("weapons.reaper_scythe.attack_speed", -2.4);
         AttributeModifier attackSpeedModifier = new AttributeModifier(
            UUID.randomUUID(), "generic.attack_speed", attackSpeed, Operation.ADD_NUMBER, EquipmentSlot.HAND
         );
         if (AttributeCompat.getAttackSpeed() != null) {
            meta.addAttributeModifier(AttributeCompat.getAttackSpeed(), attackSpeedModifier);
         }

         meta.getPersistentDataContainer().set(this.reaperScytheKey, PersistentDataType.BOOLEAN, true);
         this.plugin.applyWeaponLore(meta, "reaper_scythe");
         scythe.setItemMeta(meta);
      }

      return scythe;
   }

   public boolean isReaperScythe(ItemStack item) {
      if (item != null && item.getType() == Material.STONE_HOE) {
         ItemMeta meta = item.getItemMeta();
         return meta == null ? false : meta.getPersistentDataContainer().has(this.reaperScytheKey, PersistentDataType.BOOLEAN);
      } else {
         return false;
      }
   }

   public boolean isOnCooldown(Player player) {
      UUID playerId = player.getUniqueId();
      if (!this.cooldowns.containsKey(playerId)) {
         return false;
      } else {
         long cooldownTime = this.plugin.getConfig().getInt("weapons.reaper_scythe.cooldown", 15) * 1000L;
         long lastUse = this.cooldowns.get(playerId);
         long currentTime = System.currentTimeMillis();
         return currentTime - lastUse < cooldownTime;
      }
   }

   public long getRemainingCooldown(Player player) {
      UUID playerId = player.getUniqueId();
      if (!this.cooldowns.containsKey(playerId)) {
         return 0L;
      } else {
         long cooldownTime = this.plugin.getConfig().getInt("weapons.reaper_scythe.cooldown", 15) * 1000L;
         long lastUse = this.cooldowns.get(playerId);
         long remaining = cooldownTime - (System.currentTimeMillis() - lastUse);
         return Math.max(0L, remaining / 1000L);
      }
   }

   public void setCooldown(Player player) {
      this.cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
   }

   public void performReaperAttack(Player attacker, Player target) {
      Collection<PotionEffect> targetEffects = target.getActivePotionEffects();
      List<PotionEffect> positiveEffects = new ArrayList<>();

      for (PotionEffect effect : targetEffects) {
         PotionEffectType type = effect.getType();
         if (this.isPositiveEffect(type)) {
            positiveEffects.add(effect);
         }
      }

      if (!positiveEffects.isEmpty()) {
         for (PotionEffect effectx : positiveEffects) {
            target.removePotionEffect(effectx.getType());
         }

         for (PotionEffect effectx : positiveEffects) {
            attacker.addPotionEffect(effectx);
         }

         int healHearts = this.plugin.getConfig().getInt("weapons.reaper_scythe.heal_hearts", 2);
         double healAmount = healHearts * 2.0;
         double maxHealth = AttributeCompat.getMaxHealth() != null && attacker.getAttribute(AttributeCompat.getMaxHealth()) != null
            ? attacker.getAttribute(AttributeCompat.getMaxHealth()).getValue()
            : 20.0;
         double newHealth = Math.min(attacker.getHealth() + healAmount, maxHealth);
         attacker.setHealth(newHealth);
         this.setCooldown(attacker);
         World world = target.getWorld();
         Location loc = target.getLocation().add(0.0, 1.0, 0.0);
         world.spawnParticle(Particle.SOUL, loc, 60, 0.85, 0.9, 0.85, 0.15);
         world.spawnParticle(Particle.SOUL_FIRE_FLAME, loc, 40, 0.75, 1.0, 0.75, 0.08);
         world.playSound(loc, Sound.ENTITY_VEX_DEATH, 1.0F, 0.7F);
      }
   }

   private boolean isPositiveEffect(PotionEffectType type) {
      return type == PotionEffectType.SPEED
         || type == PotionEffectType.HASTE
         || type == PotionEffectType.STRENGTH
         || type == PotionEffectType.JUMP_BOOST
         || type == PotionEffectType.REGENERATION
         || type == PotionEffectType.RESISTANCE
         || type == PotionEffectType.FIRE_RESISTANCE
         || type == PotionEffectType.WATER_BREATHING
         || type == PotionEffectType.INVISIBILITY
         || type == PotionEffectType.NIGHT_VISION
         || type == PotionEffectType.HEALTH_BOOST
         || type == PotionEffectType.ABSORPTION
         || type == PotionEffectType.SATURATION
         || type == PotionEffectType.LUCK
         || type == PotionEffectType.SLOW_FALLING
         || type == PotionEffectType.CONDUIT_POWER
         || type == PotionEffectType.DOLPHINS_GRACE
         || type == PotionEffectType.HERO_OF_THE_VILLAGE;
   }

   public void applyReapMultiplier(DemoraLegendaryWeapons plugin, Vector finalMove, boolean sprint, boolean jump, Entity mount) {
      if (finalMove != null && !(finalMove.length() <= 0.0)) {
         double normalSpeed = plugin.getConfig().getDouble("weapons.ravager_horn.movement.normal_speed", 0.25);
         double sprintSpeed = plugin.getConfig().getDouble("weapons.ravager_horn.movement.sprint_speed", 0.4);
         double speed = sprint ? sprintSpeed : normalSpeed;
         finalMove.normalize().multiply(speed);
         finalMove.setY(mount.getVelocity().getY());
         if (jump && mount.isOnGround()) {
            double jumpPower = plugin.getConfig().getDouble("weapons.ravager_horn.movement.jump_power", 0.5);
            finalMove.setY(jumpPower);
         }
      }
   }
}
