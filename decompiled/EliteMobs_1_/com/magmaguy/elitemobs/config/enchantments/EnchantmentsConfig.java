package com.magmaguy.elitemobs.config.enchantments;

import com.magmaguy.elitemobs.config.LegacyValueConverter;
import com.magmaguy.elitemobs.items.EliteEnchantmentCatalog;
import com.magmaguy.elitemobs.magmacore.config.CustomConfig;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import lombok.Generated;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;

public class EnchantmentsConfig extends CustomConfig {
   private static HashMap<String, EnchantmentsConfigFields> enchantments = new HashMap<>();
   private static final Set<String> missingEnchantmentWarnings = new HashSet<>();

   public EnchantmentsConfig() {
      super("enchantments", "com.magmaguy.elitemobs.config.enchantments.premade", EnchantmentsConfigFields.class);
      enchantments = new HashMap<>();

      for (String key : super.getCustomConfigFieldsHashMap().keySet()) {
         enchantments.put(key, (EnchantmentsConfigFields)super.getCustomConfigFieldsHashMap().get(key));
      }
   }

   @Override
   protected boolean ownsFilename(String filename) {
      String stem = filename.toLowerCase(Locale.ROOT).replaceFirst("\\.ya?ml$", "");
      return EliteEnchantmentCatalog.HOST_SETTINGS.contains(stem) || Enchantment.getByKey(NamespacedKey.minecraft(stem)) != null;
   }

   public static EnchantmentsConfigFields getEnchantment(String string) {
      String newString = LegacyValueConverter.parseEnchantment(string.replace(".yml", "")) + ".yml";
      newString = newString.toLowerCase(Locale.ROOT);
      EnchantmentsConfigFields test = enchantments.get(newString);
      if (test == null && missingEnchantmentWarnings.add(newString)) {
         Logger.warn("Failed to find enchant file " + newString);
      }

      return enchantments.get(newString);
   }

   public static EnchantmentsConfigFields getEnchantment(Enchantment enchantment) {
      return getEnchantment(enchantment.getKey().getKey().toLowerCase(Locale.ROOT) + ".yml");
   }

   @Generated
   public static HashMap<String, EnchantmentsConfigFields> getEnchantments() {
      return enchantments;
   }
}
