package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.skills.WeaponIdentityResolver;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

public final class ItemDurability {
   public static final int MAGIC_WEAPON_DURABILITY = 384;

   private ItemDurability() {
   }

   public static int maximum(ItemStack item) {
      if (item != null && !item.getType().isAir()) {
         if (item.getItemMeta() instanceof Damageable meta && meta.hasMaxDamage()) {
            return meta.getMaxDamage();
         } else {
            return WeaponIdentityResolver.isMagicWeapon(item) ? 384 : item.getType().getMaxDurability();
         }
      } else {
         return 0;
      }
   }

   public static int maximum(ItemStack item, boolean magicWeapon) {
      if (item != null && !item.getType().isAir()) {
         if (item.getItemMeta() instanceof Damageable meta && meta.hasMaxDamage()) {
            return meta.getMaxDamage();
         } else {
            return magicWeapon ? 384 : item.getType().getMaxDurability();
         }
      } else {
         return 0;
      }
   }

   public static void prepareMagicWeapon(ItemStack item) {
      prepareMagicWeapon(item, WeaponIdentityResolver.isMagicWeapon(item));
   }

   public static void prepareMagicWeapon(ItemStack item, boolean magicWeapon) {
      if (magicWeapon && item.getItemMeta() instanceof Damageable meta) {
         if (!meta.hasMaxDamage()) {
            meta.setMaxDamage(384);
         }

         meta.setMaxStackSize(1);
         item.setItemMeta(meta);
      }
   }
}
