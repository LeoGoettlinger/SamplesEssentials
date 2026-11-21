package dev.demora.demoralegendaryweapons.weapons.shadowblade;

import dev.demora.demoralegendaryweapons.AttributeCompat;
import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ShadowBlade {
   private final DemoraLegendaryWeapons plugin;
   private final NamespacedKey shadowBladeKey;
   private final Map<UUID, Integer> shadowTimers = new HashMap<>();
   private final Map<UUID, Integer> shadowCooldowns = new HashMap<>();
   private final List<Player> vanishedPlayers = new ArrayList<>();
   private int shadowDuration;
   private int cooldownTime;

   public ShadowBlade(DemoraLegendaryWeapons plugin) {
      this.plugin = plugin;
      this.shadowBladeKey = new NamespacedKey(plugin, "shadow_blade");
      this.loadConfig();
   }

   public void loadConfig() {
      this.shadowDuration = this.plugin.getConfig().getInt("weapons.shadow_blade.duration", 200);
      this.cooldownTime = this.plugin.getConfig().getInt("weapons.shadow_blade.cooldown", 20) * 20;
   }

   public void reloadConfig() {
      this.loadConfig();
   }

   public ItemStack createShadowBlade() {
      ItemStack shadowBlade = new ItemStack(Material.DIAMOND_SWORD);
      ItemMeta meta = shadowBlade.getItemMeta();
      if (meta != null) {
         String weaponName = this.plugin.getLanguageManager().getWeaponName("shadow_blade");
         if (weaponName.isEmpty()) {
            weaponName = "§8§lТеневой клинок";
         }

         meta.displayName(LegacyComponentSerializer.legacySection().deserialize(weaponName));
         meta.setCustomModelData(4);
         meta.getPersistentDataContainer().set(this.shadowBladeKey, PersistentDataType.BOOLEAN, true);
         meta.setUnbreakable(true);
         double damage = this.plugin.getConfig().getDouble("weapons.shadow_blade.attribute_damage", 11.0);
         AttributeModifier damageModifier = new AttributeModifier(UUID.randomUUID(), "generic.attack_damage", damage, Operation.ADD_NUMBER, EquipmentSlot.HAND);
         if (AttributeCompat.getAttackDamage() != null) {
            meta.addAttributeModifier(AttributeCompat.getAttackDamage(), damageModifier);
         }

         double attackSpeed = this.plugin.getConfig().getDouble("weapons.shadow_blade.attack_speed", -2.4);
         AttributeModifier attackSpeedModifier = new AttributeModifier(
            UUID.randomUUID(), "generic.attack_speed", attackSpeed, Operation.ADD_NUMBER, EquipmentSlot.HAND
         );
         if (AttributeCompat.getAttackSpeed() != null) {
            meta.addAttributeModifier(AttributeCompat.getAttackSpeed(), attackSpeedModifier);
         }

         this.plugin.applyWeaponLore(meta, "shadow_blade");
         shadowBlade.setItemMeta(meta);
      }

      return shadowBlade;
   }

   public boolean isShadowBlade(ItemStack item) {
      return item != null && item.hasItemMeta() ? item.getItemMeta().getPersistentDataContainer().has(this.shadowBladeKey, PersistentDataType.BOOLEAN) : false;
   }

   public boolean isOnCooldown(Player player) {
      UUID playerId = player.getUniqueId();
      return this.shadowCooldowns.containsKey(playerId) && this.shadowCooldowns.get(playerId) > 0;
   }

   public int getRemainingCooldown(Player player) {
      UUID playerId = player.getUniqueId();
      return !this.shadowCooldowns.containsKey(playerId) ? 0 : (int)Math.ceil(this.shadowCooldowns.get(playerId).intValue() / 20.0);
   }

   public boolean isInShadowMode(Player player) {
      UUID playerId = player.getUniqueId();
      return this.shadowTimers.containsKey(playerId) && this.shadowTimers.get(playerId) > 0;
   }

   public void activateShadowBlade(Player player) {
      UUID playerId = player.getUniqueId();

      for (Player onlinePlayer : this.plugin.getServer().getOnlinePlayers()) {
         if (!onlinePlayer.equals(player)) {
            onlinePlayer.hidePlayer(this.plugin, player);
         }
      }

      this.vanishedPlayers.add(player);
      this.shadowTimers.put(playerId, this.shadowDuration);
      player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 0.8F);
      Location loc = player.getLocation();
      loc.add(0.0, 0.5, 0.0);
      World world = player.getWorld();
      world.spawnParticle(Particle.SMOKE, loc, 100, 0.5, 0.5, 0.5, 0.1);
   }

   public void updateTimers() {
      Iterator<Entry<UUID, Integer>> timerIterator = this.shadowTimers.entrySet().iterator();

      while (timerIterator.hasNext()) {
         Entry<UUID, Integer> entry = timerIterator.next();
         UUID playerId = entry.getKey();
         int timer = entry.getValue();
         Player player = this.plugin.getServer().getPlayer(playerId);
         if (player == null || !player.isOnline()) {
            timerIterator.remove();
         } else if (timer > 0) {
            if (timer >= 20) {
               player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 25, 2, true, false));
            }

            this.shadowTimers.put(playerId, timer - 1);
         } else {
            this.returnFromShadow(player);
            timerIterator.remove();
         }
      }

      Iterator<Entry<UUID, Integer>> cooldownIterator = this.shadowCooldowns.entrySet().iterator();

      while (cooldownIterator.hasNext()) {
         Entry<UUID, Integer> entry = cooldownIterator.next();
         UUID playerId = entry.getKey();
         int cooldown = entry.getValue();
         if (cooldown > 0) {
            this.shadowCooldowns.put(playerId, cooldown - 1);
         } else {
            Player player = this.plugin.getServer().getPlayer(playerId);
            if (player != null && player.isOnline()) {
               player.playSound(player.getLocation(), Sound.BLOCK_END_PORTAL_FRAME_FILL, 1.0F, 1.5F);
            }

            cooldownIterator.remove();
         }
      }
   }

   public void returnFromShadow(Player player) {
      UUID playerId = player.getUniqueId();
      this.vanishedPlayers.remove(player);

      for (Player onlinePlayer : this.plugin.getServer().getOnlinePlayers()) {
         if (!onlinePlayer.equals(player)) {
            onlinePlayer.showPlayer(this.plugin, player);
         }
      }

      player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_AMBIENT, 1.0F, 1.2F);
      player.sendMessage(DemoraLegendaryWeapons.getInstance().getMessageWithPrefix("weapon-messages.shadow-blade-visible"));
      this.shadowCooldowns.put(playerId, this.cooldownTime);
   }

   public void hidePlayerFromNewJoiner(Player newPlayer) {
      for (Player vanishedPlayer : this.vanishedPlayers) {
         newPlayer.hidePlayer(this.plugin, vanishedPlayer);
      }
   }

   public void removePlayerData(Player player) {
      UUID playerId = player.getUniqueId();
      this.shadowTimers.remove(playerId);
      this.shadowCooldowns.remove(playerId);
      this.vanishedPlayers.remove(player);
   }
}
