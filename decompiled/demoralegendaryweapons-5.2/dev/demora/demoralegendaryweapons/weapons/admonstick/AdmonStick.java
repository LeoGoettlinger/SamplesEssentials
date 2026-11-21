package dev.demora.demoralegendaryweapons.weapons.admonstick;

import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class AdmonStick {
   private final DemoraLegendaryWeapons plugin;
   private final NamespacedKey admonStickKey;
   private final NamespacedKey abilityKey;
   private final List<String> abilityNames = Arrays.asList("прототип1", "прототип2");

   public AdmonStick(DemoraLegendaryWeapons plugin) {
      this.plugin = plugin;
      this.admonStickKey = new NamespacedKey(plugin, "admon_stick");
      this.abilityKey = new NamespacedKey(plugin, "admon_stick_ability");
   }

   public ItemStack createAdmonStick() {
      ItemStack stick = new ItemStack(Material.STICK);
      ItemMeta meta = stick.getItemMeta();
      if (meta != null) {
         meta.setDisplayName("§c§lADMIN STICK");
         meta.setUnbreakable(true);
         meta.getPersistentDataContainer().set(this.admonStickKey, PersistentDataType.STRING, "admon_stick");
         meta.getPersistentDataContainer().set(this.abilityKey, PersistentDataType.INTEGER, 0);
         stick.setItemMeta(meta);
      }

      return stick;
   }

   public boolean isAdmonStick(ItemStack item) {
      if (item != null && item.hasItemMeta()) {
         ItemMeta meta = item.getItemMeta();
         return meta == null ? false : meta.getPersistentDataContainer().has(this.admonStickKey, PersistentDataType.STRING);
      } else {
         return false;
      }
   }

   public int getAbilityIndex(ItemStack item) {
      if (!this.isAdmonStick(item)) {
         return 0;
      } else {
         ItemMeta meta = item.getItemMeta();
         return (Integer)meta.getPersistentDataContainer().getOrDefault(this.abilityKey, PersistentDataType.INTEGER, 0);
      }
   }

   public int cycleAbility(ItemStack item) {
      ItemMeta meta = item.getItemMeta();
      if (meta == null) {
         return 0;
      } else {
         int current = (Integer)meta.getPersistentDataContainer().getOrDefault(this.abilityKey, PersistentDataType.INTEGER, 0);
         int next = (current + 1) % this.abilityNames.size();
         meta.getPersistentDataContainer().set(this.abilityKey, PersistentDataType.INTEGER, next);
         item.setItemMeta(meta);
         return next;
      }
   }

   public String getAbilityName(int index) {
      return index >= 0 && index < this.abilityNames.size() ? this.abilityNames.get(index) : "";
   }

   public int getAbilityCount() {
      return this.abilityNames.size();
   }
}
