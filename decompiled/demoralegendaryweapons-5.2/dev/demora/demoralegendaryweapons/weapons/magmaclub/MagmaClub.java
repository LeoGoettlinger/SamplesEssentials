package dev.demora.demoralegendaryweapons.weapons.magmaclub;

import dev.demora.demoralegendaryweapons.AttributeCompat;
import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class MagmaClub {
   private final DemoraLegendaryWeapons plugin;
   private final NamespacedKey magmaClubKey;
   private final Map<Player, Long> cooldowns;
   private final Set<Player> fireResistancePlayers;

   public MagmaClub(DemoraLegendaryWeapons plugin) {
      this.plugin = plugin;
      this.magmaClubKey = new NamespacedKey(plugin, "magma_club");
      this.cooldowns = new HashMap<>();
      this.fireResistancePlayers = new HashSet<>();
      this.startFireResistanceTask();
   }

   public ItemStack createMagmaClub() {
      ItemStack club = new ItemStack(Material.DIAMOND_SWORD);
      ItemMeta meta = club.getItemMeta();
      String weaponName = this.plugin.getLanguageManager().getWeaponName("magma_club");
      if (weaponName.isEmpty()) {
         weaponName = "§6§lМагмовая бита";
      }

      meta.setDisplayName(weaponName);
      meta.setCustomModelData(5);
      meta.setUnbreakable(true);
      double damage = this.plugin.getConfig().getDouble("weapons.magma_club.attribute_damage", 11.0);
      AttributeModifier damageModifier = new AttributeModifier(UUID.randomUUID(), "generic.attack_damage", damage, Operation.ADD_NUMBER, EquipmentSlot.HAND);
      if (AttributeCompat.getAttackDamage() != null) {
         meta.addAttributeModifier(AttributeCompat.getAttackDamage(), damageModifier);
      }

      double attackSpeed = this.plugin.getConfig().getDouble("weapons.magma_club.attack_speed", -2.4);
      AttributeModifier attackSpeedModifier = new AttributeModifier(
         UUID.randomUUID(), "generic.attack_speed", attackSpeed, Operation.ADD_NUMBER, EquipmentSlot.HAND
      );
      if (AttributeCompat.getAttackSpeed() != null) {
         meta.addAttributeModifier(AttributeCompat.getAttackSpeed(), attackSpeedModifier);
      }

      meta.getPersistentDataContainer().set(this.magmaClubKey, PersistentDataType.BYTE, (byte)1);
      this.plugin.applyWeaponLore(meta, "magma_club");
      club.setItemMeta(meta);
      return club;
   }

   public boolean isMagmaClub(ItemStack item) {
      if (item != null && item.getType() == Material.DIAMOND_SWORD) {
         ItemMeta meta = item.getItemMeta();
         return meta == null ? false : meta.getPersistentDataContainer().has(this.magmaClubKey, PersistentDataType.BYTE);
      } else {
         return false;
      }
   }

   public boolean isOnCooldown(Player player) {
      return this.cooldowns.containsKey(player) && System.currentTimeMillis() < this.cooldowns.get(player);
   }

   public long getRemainingCooldown(Player player) {
      return !this.isOnCooldown(player) ? 0L : (this.cooldowns.get(player) - System.currentTimeMillis()) / 1000L;
   }

   public void fireBlast(Player player) {
      if (this.isOnCooldown(player)) {
         long remaining = this.getRemainingCooldown(player);
         player.sendMessage(this.plugin.getMessageWithPrefix("weapon-messages.magma-club-cooldown").replace("{time}", String.valueOf(remaining)));
      } else {
         Location playerLoc = player.getLocation();
         World world = player.getWorld();
         this.createFireParticles(playerLoc, world);
         double damage = this.plugin.getConfig().getDouble("weapons.magma_club.damage", 8.0);

         for (Entity entity : world.getNearbyEntities(playerLoc, 5.0, 5.0, 5.0)) {
            if (entity instanceof LivingEntity && entity != player) {
               LivingEntity target = (LivingEntity)entity;
               target.damage(damage);
               target.setFireTicks(100);
               Vector knockback = target.getLocation().toVector().subtract(playerLoc.toVector()).normalize();
               knockback.setY(0.5);
               target.setVelocity(knockback.multiply(1.5));
               if (target instanceof Player targetPlayer) {
                  targetPlayer.playSound(targetPlayer.getLocation(), Sound.ENTITY_PLAYER_HURT_ON_FIRE, 1.0F, 1.0F);
               }
            }
         }

         world.playSound(playerLoc, Sound.ENTITY_GENERIC_EXPLODE, 1.0F, 0.8F);
         world.playSound(playerLoc, Sound.BLOCK_FIRE_EXTINGUISH, 1.0F, 0.5F);
         int cooldownSeconds = this.plugin.getConfig().getInt("weapons.magma_club.cooldown", 15);
         this.cooldowns.put(player, System.currentTimeMillis() + cooldownSeconds * 1000L);
      }
   }

   private void createFireParticles(Location center, World world) {
      (new BukkitRunnable() {
         int ticks = 0;

         public void run() {
            if (this.ticks >= 20) {
               this.cancel();
            } else {
               for (int i = 0; i < 50; i++) {
                  double angle = Math.random() * 2.0 * Math.PI;
                  double radius = Math.random() * 5.0;
                  double x = center.getX() + Math.cos(angle) * radius;
                  double z = center.getZ() + Math.sin(angle) * radius;
                  double y = center.getY() + Math.random() * 2.0;
                  Location particleLoc = new Location(world, x, y, z);
                  world.spawnParticle(Particle.FLAME, particleLoc, 1, 0.0, 0.0, 0.0, 0.1);
               }

               this.ticks++;
            }
         }
      }).runTaskTimer(this.plugin, 0L, 1L);
   }

   private void startFireResistanceTask() {
      (new BukkitRunnable() {
         public void run() {
            for (Player player : Bukkit.getOnlinePlayers()) {
               ItemStack mainHand = player.getInventory().getItemInMainHand();
               if (MagmaClub.this.isMagmaClub(mainHand)) {
                  if (!MagmaClub.this.fireResistancePlayers.contains(player)) {
                     MagmaClub.this.fireResistancePlayers.add(player);
                  }

                  player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 60, 0, true, false));
               } else {
                  MagmaClub.this.fireResistancePlayers.remove(player);
               }
            }
         }
      }).runTaskTimer(this.plugin, 0L, 20L);
   }

   public void removePlayerData(Player player) {
      this.cooldowns.remove(player);
      this.fireResistancePlayers.remove(player);
   }
}
