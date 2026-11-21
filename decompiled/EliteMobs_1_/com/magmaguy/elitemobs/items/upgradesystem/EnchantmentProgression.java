package com.magmaguy.elitemobs.items.upgradesystem;

import com.magmaguy.elitemobs.config.SpecialItemSystemsConfig;
import com.magmaguy.elitemobs.items.ItemTagger;
import com.magmaguy.elitemobs.magmacore.util.Round;
import java.util.List;
import java.util.Map;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

public final class EnchantmentProgression {
   private EnchantmentProgression() {
   }

   public static int weight(ItemStack item) {
      if (item == null) {
         return 0;
      } else {
         ItemMeta meta = item.getItemMeta();
         if (meta == null) {
            throw new IllegalArgumentException("Missing item metadata");
         } else {
            Map<Enchantment, Integer> nativeLevels = meta instanceof EnchantmentStorageMeta book ? book.getStoredEnchants() : meta.getEnchants();
            int total = 0;

            for (int value : nativeLevels.values()) {
               total = Math.addExact(total, value);
            }

            for (Enchantment enchantment : List.of(
               Enchantment.SHARPNESS,
               Enchantment.POWER,
               Enchantment.PROTECTION,
               Enchantment.BANE_OF_ARTHROPODS,
               Enchantment.SMITE,
               Enchantment.BLAST_PROTECTION,
               Enchantment.FIRE_PROTECTION,
               Enchantment.PROJECTILE_PROTECTION,
               Enchantment.THORNS
            )) {
               total = Math.addExact(total, Math.max(0, ItemTagger.getEnchantment(meta, enchantment.getKey()) - enchantment.getMaxLevel()));
            }

            for (int value : EliteEnchantmentItems.custom(item).values()) {
               total = Math.addExact(total, value);
            }

            if (total < 0) {
               throw new IllegalArgumentException("Invalid enchantment progression weight");
            } else {
               return total;
            }
         }
      }
   }

   public static EnchantmentProgression.Quote quote(ItemStack item, boolean lucky) {
      return quote(
         weight(item),
         lucky,
         SpecialItemSystemsConfig.getLuckyTicketMultiplier(),
         SpecialItemSystemsConfig.getCriticalFailureChance(),
         SpecialItemSystemsConfig.getChallengeChance()
      );
   }

   static EnchantmentProgression.Quote quote(int weight, boolean lucky, double luckyMultiplier, double criticalChance, double challengeChance) {
      if (weight >= 0 && Double.isFinite(luckyMultiplier) && !(luckyMultiplier <= 0.0) && probability(criticalChance) && probability(challengeChance)) {
         double price = Math.pow(weight + 1.0, 4.0);
         if (Double.isFinite(price) && !(price > 2.147483647E9)) {
            double chance = 100.0 / (weight + 1.0) * 4.0;
            if (lucky) {
               chance *= luckyMultiplier;
            }

            double success = Round.twoDecimalPlaces(Math.min(1.0, chance / 100.0));
            double critical = Round.twoDecimalPlaces(criticalChance / (lucky ? 2.0 : 1.0) * (1.0 - success));
            double challenge = Round.twoDecimalPlaces((1.0 - success - critical) * challengeChance);
            double failure = Round.twoDecimalPlaces(1.0 - success - critical - challenge);
            if (probability(critical) && probability(challenge) && probability(failure)) {
               return new EnchantmentProgression.Quote((int)price, success, critical, challenge, failure);
            } else {
               throw new IllegalArgumentException("Enchantment outcome probabilities are invalid");
            }
         } else {
            throw new IllegalArgumentException("Enchantment price exceeds the supported range");
         }
      } else {
         throw new IllegalArgumentException("Invalid enchantment economy settings");
      }
   }

   private static boolean probability(double value) {
      return Double.isFinite(value) && value >= 0.0 && value <= 1.0;
   }

   public record Quote(int price, double success, double criticalFailure, double challenge, double failure) {
   }
}
