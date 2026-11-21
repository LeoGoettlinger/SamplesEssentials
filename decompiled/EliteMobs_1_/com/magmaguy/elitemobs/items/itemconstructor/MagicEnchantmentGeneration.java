package com.magmaguy.elitemobs.items.itemconstructor;

import com.magmaguy.elitemobs.config.ProceduralItemGenerationSettingsConfig;
import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfig;
import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfigFields;
import com.magmaguy.elitemobs.config.enchantments.premade.BlastRadiusConfig;
import com.magmaguy.elitemobs.config.enchantments.premade.IgnitionConfig;
import com.magmaguy.elitemobs.config.enchantments.premade.MulticastConfig;
import com.magmaguy.elitemobs.skills.SkillType;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class MagicEnchantmentGeneration {
   public static final String MULTICAST = "multicast";
   public static final String BLAST_RADIUS = "blast_radius";
   public static final String IGNITION = "ignition";
   public static final List<String> KEYS = List.of("multicast", "blast_radius", "ignition");

   private MagicEnchantmentGeneration() {
   }

   public static boolean supports(SkillType skill, String key) {
      if ("multicast".equals(key)) {
         return skill == SkillType.WANDS;
      } else {
         return !"blast_radius".equals(key) && !"ignition".equals(key) ? false : skill == SkillType.STAVES;
      }
   }

   public static HashMap<String, Integer> generate(double itemLevel, SkillType skill) {
      HashMap<String, Integer> result = new HashMap<>();
      if (itemLevel < 10.0) {
         return result;
      } else if (ThreadLocalRandom.current().nextDouble() >= ProceduralItemGenerationSettingsConfig.getCustomEnchantmentChance()) {
         return result;
      } else {
         for (String key : KEYS) {
            if (supports(skill, key)) {
               EnchantmentsConfigFields config = EnchantmentsConfig.getEnchantment(key);
               if (config != null && config.isEnabled() && config.isEnabledForProcedurallyGeneratedItems()) {
                  double chance = switch (key) {
                     case "multicast" -> MulticastConfig.generationChance();
                     case "blast_radius" -> BlastRadiusConfig.generationChance();
                     default -> IgnitionConfig.generationChance();
                  };
                  if (!(ThreadLocalRandom.current().nextDouble() >= chance)) {
                     int maximum = Math.min(3, Math.min(config.getMaxLevel(), config.getMaxEnchantmentLevel()));
                     if (maximum > 0) {
                        int level;
                        if (key.equals("multicast")) {
                           level = maximum >= 3
                                 && itemLevel >= MulticastConfig.rareMinimumLevel()
                                 && ThreadLocalRandom.current().nextDouble() < MulticastConfig.rareLevelChance()
                              ? 3
                              : (itemLevel >= 35.0 && maximum >= 2 && ThreadLocalRandom.current().nextDouble() < 0.25 ? 2 : 1);
                        } else {
                           level = ThreadLocalRandom.current().nextInt(1, Math.min(maximum, itemLevel >= 65.0 ? 3 : (itemLevel >= 30.0 ? 2 : 1)) + 1);
                        }

                        result.put("freeminecraftmodels:" + key, level);
                     }
                  }
               }
            }
         }

         return result;
      }
   }
}
