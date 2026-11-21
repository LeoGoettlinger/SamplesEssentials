package dev.demora.demoralegendaryweapons.weapons.midassword;

import dev.demora.demoralegendaryweapons.AttributeCompat;
import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class MidasSword {
   private final DemoraLegendaryWeapons plugin;
   private final NamespacedKey midasSwordKey;
   private final NamespacedKey damageLevelKey;

   public MidasSword(DemoraLegendaryWeapons plugin) {
      this.plugin = plugin;
      this.midasSwordKey = new NamespacedKey(plugin, "midas_sword");
      this.damageLevelKey = new NamespacedKey(plugin, "damage_level");
   }

   public ItemStack createMidasSword() {
      ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
      ItemMeta meta = sword.getItemMeta();
      if (meta != null) {
         String weaponName = this.plugin.getLanguageManager().getWeaponName("midas_sword");
         if (weaponName.isEmpty()) {
            weaponName = "§6§lМеч Мидаса";
         }

         meta.setDisplayName(weaponName);
         int initialDamage = this.plugin.getConfig().getInt("weapons.midas_sword.initial-damage", 5);
         String damageLabel = this.plugin.getLanguageManager().getMessage("weapon-messages.midas-damage-label");
         if (damageLabel.isEmpty()) {
            damageLabel = ChatColor.YELLOW + "Урон: ";
         }

         List<String> lore = Arrays.asList(damageLabel + initialDamage);
         meta.setLore(lore);
         meta.setCustomModelData(1);
         meta.setUnbreakable(true);
         meta.getPersistentDataContainer().set(this.midasSwordKey, PersistentDataType.BOOLEAN, true);
         meta.getPersistentDataContainer().set(this.damageLevelKey, PersistentDataType.INTEGER, initialDamage);
         if (initialDamage > 0) {
            AttributeModifier damageModifier = new AttributeModifier(
               UUID.randomUUID(), "generic.attack_damage", initialDamage, Operation.ADD_NUMBER, EquipmentSlot.HAND
            );
            if (AttributeCompat.getAttackDamage() != null) {
               meta.addAttributeModifier(AttributeCompat.getAttackDamage(), damageModifier);
            }
         }

         double attackSpeed = this.plugin.getConfig().getDouble("weapons.midas_sword.attack-speed", 1.6);
         AttributeModifier speedModifier = new AttributeModifier(
            UUID.randomUUID(), "generic.attack_speed", attackSpeed, Operation.ADD_NUMBER, EquipmentSlot.HAND
         );
         if (AttributeCompat.getAttackSpeed() != null) {
            meta.addAttributeModifier(AttributeCompat.getAttackSpeed(), speedModifier);
         }

         this.plugin.applyWeaponLore(meta, "midas_sword");
         sword.setItemMeta(meta);
      }

      return sword;
   }

   public boolean isMidasSword(ItemStack item) {
      if (item != null && item.getType() == Material.DIAMOND_SWORD) {
         ItemMeta meta = item.getItemMeta();
         return meta == null ? false : meta.getPersistentDataContainer().has(this.midasSwordKey, PersistentDataType.BOOLEAN);
      } else {
         return false;
      }
   }

   public int getDamageLevel(ItemStack sword) {
      if (!this.isMidasSword(sword)) {
         return 0;
      } else {
         ItemMeta meta = sword.getItemMeta();
         return meta == null ? 0 : (Integer)meta.getPersistentDataContainer().getOrDefault(this.damageLevelKey, PersistentDataType.INTEGER, 0);
      }
   }

   public void increaseDamage(ItemStack sword) {
      if (this.isMidasSword(sword)) {
         ItemMeta meta = sword.getItemMeta();
         if (meta != null) {
            int currentDamage = this.getDamageLevel(sword);
            int maxDamage = this.plugin.getConfig().getInt("weapons.midas_sword.max-damage", 25);
            if (currentDamage < maxDamage) {
               int damagePerKill = this.plugin.getConfig().getInt("weapons.midas_sword.damage-per-kill", 2);
               int popa = currentDamage + damagePerKill;
               int newDamage = popa;
               if (popa > maxDamage) {
                  newDamage = maxDamage;
               }

               if (AttributeCompat.getAttackDamage() != null) {
                  meta.removeAttributeModifier(AttributeCompat.getAttackDamage());
                  if (newDamage > 0) {
                     AttributeModifier damageModifier = new AttributeModifier(
                        UUID.randomUUID(), "generic.attack_damage", newDamage, Operation.ADD_NUMBER, EquipmentSlot.HAND
                     );
                     meta.addAttributeModifier(AttributeCompat.getAttackDamage(), damageModifier);
                  }
               }

               meta.getPersistentDataContainer().set(this.damageLevelKey, PersistentDataType.INTEGER, newDamage);
               List<String> lore = meta.getLore();
               if (lore != null && lore.size() > 0) {
                  String damageLabel = this.plugin.getLanguageManager().getMessage("weapon-messages.midas-damage-label");
                  if (damageLabel.isEmpty()) {
                     damageLabel = ChatColor.YELLOW + "Урон: ";
                  }

                  lore.set(0, damageLabel + newDamage);
                  meta.setLore(lore);
               }

               sword.setItemMeta(meta);
            }
         }
      }
   }

   public void createKillEffects(Player victim, Player killer) {
      Location location = victim.getLocation();
      World world = victim.getWorld();
      world.createExplosion(location, 0.0F, false, false);

      for (int i = 0; i < 50; i++) {
         double x = location.getX() + (Math.random() - 0.5) * 4.0;
         double y = location.getY() + Math.random() * 3.0;
         double z = location.getZ() + (Math.random() - 0.5) * 4.0;
         world.spawnParticle(Particle.HAPPY_VILLAGER, x, y, z, 1, 0.0, 0.0, 0.0, 0.0);
      }

      for (int i = 0; i < 30; i++) {
         double x = location.getX() + (Math.random() - 0.5) * 6.0;
         double y = location.getY() + Math.random() * 4.0;
         double z = location.getZ() + (Math.random() - 0.5) * 6.0;
         world.spawnParticle(Particle.ENCHANT, x, y, z, 1, 0.0, 0.0, 0.0, 0.0);
      }

      world.playSound(location, Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
      world.playSound(location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 0.5F);
      world.playSound(location, Sound.BLOCK_ANVIL_LAND, 0.5F, 1.5F);
      world.strikeLightningEffect(location);
   }
}
