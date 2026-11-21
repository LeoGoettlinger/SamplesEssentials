package com.magmaguy.elitemobs.utils;

import com.magmaguy.elitemobs.magmacore.util.Logger;
import org.bukkit.NamespacedKey;
import org.bukkit.potion.PotionEffectType;

public class PotionEffectTypeUtil {
   public static PotionEffectType getByKey(String key) {
      try {
         PotionEffectType potionEffectType = PotionEffectType.getByKey(NamespacedKey.fromString(key));
         return potionEffectType != null ? potionEffectType : getByName(key);
      } catch (NoSuchMethodError var2) {
         return getByName(key);
      }
   }

   private static PotionEffectType getByName(String name) {
      PotionEffectType potionEffectType = PotionEffectType.getByName(name);
      if (potionEffectType != null) {
         return potionEffectType;
      } else {
         Logger.warn("Failed to get potion effect type in script for key " + name + "!");
         return null;
      }
   }
}
