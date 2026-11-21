package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfig;
import com.magmaguy.elitemobs.items.potioneffects.ElitePotionEffect;
import com.magmaguy.elitemobs.items.potioneffects.ElitePotionEffectContainer;
import com.magmaguy.elitemobs.magmacore.enchantments.EnchantmentDefinition;
import com.magmaguy.elitemobs.magmacore.enchantments.EnchantmentItems;
import com.magmaguy.elitemobs.magmacore.util.Round;
import java.util.Map.Entry;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemWorthCalculator {
   public static double determineItemWorth(ItemStack itemStack, Player player) {
      return itemStack != null && itemStack.getItemMeta() != null && !itemStack.getType().equals(Material.AIR)
         ? 1.0
            + Round.twoDecimalPlaces(
               EconomySettingsConfig.getMaterialWorth(itemStack.getType())
                  + getAllEnchantmentValues(itemStack)
                  + getAllPotionEffectValues(itemStack.getItemMeta())
            )
         : 0.0;
   }

   public static double writeItemWorth(ItemStack itemStack, Player player) {
      return determineItemWorth(itemStack, player);
   }

   public static double determineResaleWorth(ItemStack itemStack, Player player) {
      double value = ItemTagger.getItemValue(itemStack);
      return value == -1.0
         ? Round.twoDecimalPlaces(determineItemWorth(itemStack, player) * (EconomySettingsConfig.getResaleValue() / 100.0))
         : Round.twoDecimalPlaces(value * (EconomySettingsConfig.getResaleValue() / 100.0));
   }

   private static double getAllEnchantmentValues(ItemStack itemStack) {
      double value = 0.0;

      for (Enchantment enchantment : itemStack.getEnchantments().keySet()) {
         if (EliteEnchantments.isPotentialEliteEnchantment(enchantment)) {
            if (ItemTagger.getEnchantment(itemStack.getItemMeta(), enchantment.getKey()) != 0) {
               value += EnchantmentsConfig.getEnchantment(enchantment).getValue() * ItemTagger.getEnchantment(itemStack.getItemMeta(), enchantment.getKey());
            }
         } else {
            value += EnchantmentsConfig.getEnchantment(enchantment).getValue() * ((Integer)itemStack.getEnchantments().get(enchantment)).intValue();
         }
      }

      for (Entry<String, Integer> entry : EnchantmentItems.inspectCustom(itemStack.getItemMeta()).entrySet()) {
         EnchantmentDefinition definition = EliteEnchantmentCatalog.definition(entry.getKey());
         if ((definition == null ? null : definition.parameters().get("value")) instanceof Number number
            && Double.isFinite(number.doubleValue())
            && number.doubleValue() >= 0.0) {
            value += number.doubleValue() * entry.getValue().intValue();
         }
      }

      return value;
   }

   private static double getAllPotionEffectValues(ItemMeta itemMeta) {
      double value = 0.0;

      for (ElitePotionEffect elitePotionEffect : ElitePotionEffectContainer.getElitePotionEffectContainer(itemMeta, ItemTagger.continuousPotionEffectKey)) {
         value += elitePotionEffect.getValue() * (elitePotionEffect.getPotionEffect().getAmplifier() + 1);
      }

      for (ElitePotionEffect elitePotionEffect : ElitePotionEffectContainer.getElitePotionEffectContainer(itemMeta, ItemTagger.onHitPotionEffectKey)) {
         value += elitePotionEffect.getValue() * (elitePotionEffect.getPotionEffect().getAmplifier() + 1);
      }

      return value;
   }
}
