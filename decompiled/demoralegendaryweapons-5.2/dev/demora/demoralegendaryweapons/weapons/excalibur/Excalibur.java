package dev.demora.demoralegendaryweapons.weapons.excalibur;

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
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class Excalibur {
   private final DemoraLegendaryWeapons plugin;
   private final NamespacedKey excaliburKey;
   private final Map<UUID, Long> cooldowns;
   private final Map<UUID, Long> activeProtection;

   public Excalibur(DemoraLegendaryWeapons plugin) {
      this.plugin = plugin;
      this.excaliburKey = new NamespacedKey(plugin, "excalibur");
      this.cooldowns = new HashMap<>();
      this.activeProtection = new HashMap<>();
   }

   public ItemStack createExcalibur() {
      ItemStack sword = new ItemStack(Material.NETHERITE_SWORD);
      ItemMeta meta = sword.getItemMeta();
      if (meta != null) {
         String weaponName = this.plugin.getLanguageManager().getWeaponName("excalibur");
         if (weaponName.isEmpty()) {
            weaponName = "§6§lЭкскалибур";
         }

         meta.setDisplayName(weaponName);
         meta.setCustomModelData(3);
         meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES});
         meta.setUnbreakable(true);
         double damage = this.plugin.getConfig().getDouble("weapons.excalibur.attribute_damage", 11.0);
         AttributeModifier damageModifier = new AttributeModifier(UUID.randomUUID(), "generic.attack_damage", damage, Operation.ADD_NUMBER, EquipmentSlot.HAND);
         if (AttributeCompat.getAttackDamage() != null) {
            meta.addAttributeModifier(AttributeCompat.getAttackDamage(), damageModifier);
         }

         double attackSpeed = this.plugin.getConfig().getDouble("weapons.excalibur.attack_speed", -2.4);
         AttributeModifier attackSpeedModifier = new AttributeModifier(
            UUID.randomUUID(), "generic.attack_speed", attackSpeed, Operation.ADD_NUMBER, EquipmentSlot.HAND
         );
         if (AttributeCompat.getAttackSpeed() != null) {
            meta.addAttributeModifier(AttributeCompat.getAttackSpeed(), attackSpeedModifier);
         }

         PersistentDataContainer pdc = meta.getPersistentDataContainer();
         pdc.set(this.excaliburKey, PersistentDataType.BYTE, (byte)1);
         this.plugin.applyWeaponLore(meta, "excalibur");
         sword.setItemMeta(meta);
      }

      return sword;
   }

   public boolean isExcalibur(ItemStack item) {
      if (item != null && item.getType() == Material.NETHERITE_SWORD && item.hasItemMeta()) {
         ItemMeta meta = item.getItemMeta();
         PersistentDataContainer pdc = meta.getPersistentDataContainer();
         Byte mark = (Byte)pdc.get(this.excaliburKey, PersistentDataType.BYTE);
         return mark != null && mark == 1;
      } else {
         return false;
      }
   }

   public boolean isOnCooldown(Player player) {
      UUID playerId = player.getUniqueId();
      if (!this.cooldowns.containsKey(playerId)) {
         return false;
      } else {
         long cooldownTime = this.plugin.getConfig().getInt("weapons.excalibur.cooldown", 20) * 1000L;
         long lastUse = this.cooldowns.get(playerId);
         return System.currentTimeMillis() - lastUse < cooldownTime;
      }
   }

   public long getRemainingCooldown(Player player) {
      UUID playerId = player.getUniqueId();
      if (!this.cooldowns.containsKey(playerId)) {
         return 0L;
      } else {
         long cooldownTime = this.plugin.getConfig().getInt("weapons.excalibur.cooldown", 20) * 1000L;
         long lastUse = this.cooldowns.get(playerId);
         long remaining = cooldownTime - (System.currentTimeMillis() - lastUse);
         return Math.max(0L, remaining / 1000L + 1L);
      }
   }

   public void activateProtection(Player player) {
      UUID playerId = player.getUniqueId();
      int duration = this.plugin.getConfig().getInt("weapons.excalibur.duration", 7);
      this.activeProtection.put(playerId, System.currentTimeMillis());
      this.cooldowns.put(playerId, System.currentTimeMillis());
      player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, duration * 20, 255, false, false, false));
      Location playerLoc = player.getLocation();

      for (Player nearbyPlayer : player.getWorld().getNearbyPlayers(playerLoc, 10.0)) {
         nearbyPlayer.playSound(playerLoc, Sound.ITEM_TOTEM_USE, 1.0F, 1.0F);
      }

      player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, playerLoc.clone().add(0.0, 1.0, 0.0), 100, 0.5, 0.8, 0.5, 0.3);
   }

   public boolean hasActiveProtection(Player player) {
      UUID playerId = player.getUniqueId();
      if (!this.activeProtection.containsKey(playerId)) {
         return false;
      } else {
         long duration = this.plugin.getConfig().getInt("weapons.excalibur.duration", 7) * 1000L;
         long activatedTime = this.activeProtection.get(playerId);
         long currentTime = System.currentTimeMillis();
         if (currentTime - activatedTime > duration) {
            this.activeProtection.remove(playerId);
            return false;
         } else {
            return true;
         }
      }
   }

   public void removePlayerData(UUID playerId) {
      this.cooldowns.remove(playerId);
      this.activeProtection.remove(playerId);
   }

   public Vector resolveLungeDir(Vector dir, float forward, Vector rightDir, float sideways) {
      return dir != null && rightDir != null ? dir.clone().multiply(forward).add(rightDir.clone().multiply(-sideways)) : null;
   }
}
