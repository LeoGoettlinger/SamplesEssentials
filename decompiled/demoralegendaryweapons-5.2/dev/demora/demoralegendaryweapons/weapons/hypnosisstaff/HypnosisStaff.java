package dev.demora.demoralegendaryweapons.weapons.hypnosisstaff;

import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class HypnosisStaff {
   private final NamespacedKey hypnosisStaffKey;
   private final Plugin plugin;

   public HypnosisStaff(Plugin plugin) {
      this.plugin = plugin;
      this.hypnosisStaffKey = new NamespacedKey(plugin, "hypnosis_staff");
   }

   public ItemStack createHypnosisStaff() {
      ItemStack staff = new ItemStack(Material.STONE_SWORD);
      ItemMeta meta = staff.getItemMeta();
      if (meta != null) {
         String weaponName = "";
         if (this.plugin instanceof DemoraLegendaryWeapons) {
            weaponName = ((DemoraLegendaryWeapons)this.plugin).getLanguageManager().getWeaponName("hypnosis_staff");
         }

         if (weaponName.isEmpty()) {
            weaponName = "§6§lПосох гипноза";
         }

         meta.displayName(LegacyComponentSerializer.legacySection().deserialize(weaponName));
         meta.setCustomModelData(6);
         meta.setUnbreakable(true);
         meta.getPersistentDataContainer().set(this.hypnosisStaffKey, PersistentDataType.BOOLEAN, true);
         if (this.plugin instanceof DemoraLegendaryWeapons) {
            ((DemoraLegendaryWeapons)this.plugin).applyWeaponLore(meta, "hypnosis_staff");
         }

         staff.setItemMeta(meta);
      }

      return staff;
   }

   public boolean isHypnosisStaff(ItemStack item) {
      return item != null && item.hasItemMeta()
         ? item.getItemMeta().getPersistentDataContainer().has(this.hypnosisStaffKey, PersistentDataType.BOOLEAN)
         : false;
   }
}
