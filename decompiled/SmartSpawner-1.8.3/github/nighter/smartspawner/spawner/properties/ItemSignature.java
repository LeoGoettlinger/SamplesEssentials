package github.nighter.smartspawner.spawner.properties;

import lombok.Generated;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemSignature {
   private final ItemStack template;
   private final int hashCode;
   private final Material material;
   private final int maxStackSize;
   private final int damage;
   private final boolean hasItemMeta;

   public ItemSignature(ItemStack item) {
      this.template = item.asQuantity(1);
      this.material = this.template.getType();
      this.maxStackSize = this.template.getMaxStackSize();
      ItemMeta meta = this.template.hasItemMeta() ? this.template.getItemMeta() : null;
      this.hasItemMeta = meta != null;
      this.damage = this.extractDamage(meta);
      this.hashCode = this.calculateHashCode(meta);
   }

   private int calculateHashCode(ItemMeta meta) {
      int result = 31 * this.material.ordinal();
      result = 31 * result + this.damage;
      if (this.hasItemMeta) {
         result = 31 * result + (meta.hasDisplayName() ? meta.displayName().hashCode() : 0);
         result = 31 * result + (meta.hasLore() ? meta.lore().hashCode() : 0);
         result = 31 * result + (meta.hasEnchants() ? meta.getEnchants().hashCode() : 0);
      }

      return result;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o instanceof ItemSignature that) {
         if (this.material != that.material || this.damage != that.damage) {
            return false;
         } else if (this.hasItemMeta != that.hasItemMeta) {
            return false;
         } else {
            return !this.hasItemMeta ? true : this.template.isSimilar(that.template);
         }
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return this.hashCode;
   }

   public ItemStack getTemplate() {
      return this.template.clone();
   }

   public ItemStack getUnsafeTemplateRef() {
      return this.template;
   }

   public String getMaterialName() {
      return this.material.name();
   }

   private int extractDamage(ItemMeta meta) {
      return meta instanceof Damageable damageable ? damageable.getDamage() : 0;
   }

   @Generated
   public Material getMaterial() {
      return this.material;
   }

   @Generated
   public int getMaxStackSize() {
      return this.maxStackSize;
   }

   @Generated
   public int getDamage() {
      return this.damage;
   }

   @Generated
   public boolean hasItemMeta() {
      return this.hasItemMeta;
   }
}
