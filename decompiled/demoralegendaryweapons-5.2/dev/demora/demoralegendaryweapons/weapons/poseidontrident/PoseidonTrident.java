package dev.demora.demoralegendaryweapons.weapons.poseidontrident;

import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class PoseidonTrident {
   private final DemoraLegendaryWeapons plugin;
   private final NamespacedKey poseidonTridentKey;
   private final Map<UUID, Long> throwCooldowns;
   private final Map<UUID, Long> riptideCooldowns;

   public PoseidonTrident(DemoraLegendaryWeapons plugin) {
      this.plugin = plugin;
      this.poseidonTridentKey = new NamespacedKey(plugin, "poseidon_trident");
      this.throwCooldowns = new HashMap<>();
      this.riptideCooldowns = new HashMap<>();
   }

   public ItemStack createPoseidonTrident() {
      ItemStack trident = new ItemStack(Material.BOW);
      ItemMeta meta = trident.getItemMeta();
      if (meta != null) {
         String weaponName = this.plugin.getLanguageManager().getWeaponName("poseidon_trident");
         if (weaponName.isEmpty()) {
            weaponName = "§9§lТрезубец Посейдона";
         }

         meta.setDisplayName(weaponName);
         meta.setCustomModelData(8);
         meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES});
         meta.setUnbreakable(true);
         meta.getPersistentDataContainer().set(this.poseidonTridentKey, PersistentDataType.STRING, "poseidon_trident");
         this.plugin.applyWeaponLore(meta, "poseidon_trident");
         trident.setItemMeta(meta);
      }

      return trident;
   }

   public boolean isPoseidonTrident(ItemStack item) {
      if (item != null && item.hasItemMeta()) {
         ItemMeta meta = item.getItemMeta();
         return meta == null ? false : meta.getPersistentDataContainer().has(this.poseidonTridentKey, PersistentDataType.STRING);
      } else {
         return false;
      }
   }

   public boolean isOnThrowCooldown(UUID playerId) {
      if (!this.throwCooldowns.containsKey(playerId)) {
         return false;
      } else {
         long lastUse = this.throwCooldowns.get(playerId);
         long cooldownDuration = this.plugin.getConfig().getInt("weapons.poseidon_trident.throw_cooldown", 10) * 1000L;
         return System.currentTimeMillis() - lastUse < cooldownDuration;
      }
   }

   public boolean isOnRiptideCooldown(UUID playerId) {
      if (!this.riptideCooldowns.containsKey(playerId)) {
         return false;
      } else {
         long lastUse = this.riptideCooldowns.get(playerId);
         long cooldownDuration = this.plugin.getConfig().getInt("weapons.poseidon_trident.riptide_cooldown", 15) * 1000L;
         return System.currentTimeMillis() - lastUse < cooldownDuration;
      }
   }

   public void setThrowCooldown(UUID playerId) {
      this.throwCooldowns.put(playerId, System.currentTimeMillis());
   }

   public void setRiptideCooldown(UUID playerId) {
      this.riptideCooldowns.put(playerId, System.currentTimeMillis());
   }

   public long getRemainingThrowCooldown(UUID playerId) {
      if (!this.throwCooldowns.containsKey(playerId)) {
         return 0L;
      } else {
         long lastUse = this.throwCooldowns.get(playerId);
         long cooldownDuration = this.plugin.getConfig().getInt("weapons.poseidon_trident.throw_cooldown", 10) * 1000L;
         long remaining = cooldownDuration - (System.currentTimeMillis() - lastUse);
         return Math.max(0L, remaining / 1000L);
      }
   }

   public long getRemainingRiptideCooldown(UUID playerId) {
      if (!this.riptideCooldowns.containsKey(playerId)) {
         return 0L;
      } else {
         long lastUse = this.riptideCooldowns.get(playerId);
         long cooldownDuration = this.plugin.getConfig().getInt("weapons.poseidon_trident.riptide_cooldown", 15) * 1000L;
         long remaining = cooldownDuration - (System.currentTimeMillis() - lastUse);
         return Math.max(0L, remaining / 1000L);
      }
   }

   public double getThrowDamage() {
      return this.plugin.getConfig().getDouble("weapons.poseidon_trident.throw_damage", 12.0);
   }

   public double getRiptideDamage() {
      return this.plugin.getConfig().getDouble("weapons.poseidon_trident.riptide_damage", 8.0);
   }

   public double getMeleeDamage() {
      return this.plugin.getConfig().getDouble("weapons.poseidon_trident.melee_damage", 11.0);
   }
}
