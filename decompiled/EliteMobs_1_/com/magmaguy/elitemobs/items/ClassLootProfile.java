package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfig;
import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfigFields;
import com.magmaguy.elitemobs.items.itemconstructor.MagicEnchantmentGeneration;
import com.magmaguy.elitemobs.magmacore.enchantments.EnchantmentDefinition;
import com.magmaguy.elitemobs.skills.SkillType;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;

public record ClassLootProfile(
   String primary, List<ClassLootProfile.Rule> enchantments, List<ClassLootProfile.Rule> rareEnchantments, List<String> potionEffects
) {
   public ClassLootProfile(String primary, List<ClassLootProfile.Rule> enchantments, List<ClassLootProfile.Rule> rareEnchantments, List<String> potionEffects) {
      enchantments = List.copyOf(enchantments);
      rareEnchantments = List.copyOf(rareEnchantments);
      potionEffects = List.copyOf(potionEffects);
      this.primary = primary;
      this.enchantments = enchantments;
      this.rareEnchantments = rareEnchantments;
      this.potionEffects = potionEffects;
   }

   public ClassLootProfile(String primary, List<ClassLootProfile.Rule> enchantments, List<ClassLootProfile.Rule> rareEnchantments) {
      this(primary, enchantments, rareEnchantments, List.of());
   }

   public ClassLootProfile.Roll roll(double fraction, int minimumPrimaryLevel) {
      HashMap<Enchantment, Integer> nativeLevels = new HashMap<>();
      HashMap<String, Integer> customLevels = new HashMap<>();

      for (ClassLootProfile.Rule rule : this.enchantments) {
         add(rule, nativeLevels, customLevels);
      }

      for (ClassLootProfile.Rule rule : this.rareEnchantments) {
         add(rule, nativeLevels, customLevels);
      }

      Enchantment primaryEnchantment = nativeEnchantment(this.primary);
      int primaryFloor = Math.min(minimumPrimaryLevel, nativeLevels.getOrDefault(primaryEnchantment, 0));
      return new ClassLootProfile.Roll(ScalableItemConstructor.rollEnchantments(nativeLevels, fraction, primaryEnchantment, primaryFloor), customLevels);
   }

   private static void add(ClassLootProfile.Rule rule, HashMap<Enchantment, Integer> nativeLevels, HashMap<String, Integer> customLevels) {
      if (EliteEnchantmentCatalog.ownsFilename(rule.key)) {
         String id = "elitemobs:" + rule.key;
         EnchantmentDefinition definition = EliteEnchantmentCatalog.definition(id);
         if (definition != null && Boolean.TRUE.equals(definition.parameters().get("procedural")) && !(ThreadLocalRandom.current().nextDouble() >= rule.chance)
            )
          {
            int level = rule.level;
            if (level > 0) {
               customLevels.merge(id, level, Math::max);
            }
         }
      } else {
         EnchantmentsConfigFields config = EnchantmentsConfig.getEnchantment(rule.key);
         if (config != null && config.isEnabled() && config.isEnabledForProcedurallyGeneratedItems()) {
            if (!(ThreadLocalRandom.current().nextDouble() >= rule.chance)) {
               int level = rule.level;
               if (level > 0) {
                  Enchantment nativeEnchantment = nativeEnchantment(rule.key);
                  if (nativeEnchantment != null) {
                     nativeLevels.merge(nativeEnchantment, level, Math::max);
                  } else {
                     customLevels.merge(MagicEnchantmentGeneration.KEYS.contains(rule.key) ? "freeminecraftmodels:" + rule.key : rule.key, level, Math::max);
                  }
               }
            }
         }
      }
   }

   public static Enchantment nativeEnchantment(String key) {
      return Enchantment.getByKey(NamespacedKey.minecraft(key.toLowerCase(Locale.ROOT)));
   }

   public static boolean supports(ClassLootFamily family, String key) {
      if (family.isWeapon()) {
         return supports(family.skill(), key);
      } else if (Set.of("sharpness", "smite", "bane_of_arthropods").contains(key)) {
         return true;
      } else if (Set.of("unbreaking", "mending", "vanishing_curse", "protection").contains(key)) {
         return true;
      } else if (family == ClassLootFamily.SHIELDS) {
         return false;
      } else if (Set.of("fire_protection", "blast_protection", "projectile_protection", "thorns", "binding_curse").contains(key)) {
         return true;
      } else {
         String var2 = family.slot();

         return switch (var2) {
            case "HELMET" -> Set.of("aqua_affinity", "respiration").contains(key);
            case "LEGGINGS" -> key.equals("swift_sneak");
            case "BOOTS" -> Set.of("feather_falling", "depth_strider", "frost_walker", "soul_speed").contains(key);
            default -> false;
         };
      }
   }

   public static boolean supports(SkillType skill, String key) {
      if (Set.of("unbreaking", "mending", "vanishing_curse").contains(key)) {
         return true;
      } else if (skill != SkillType.STAVES && skill != SkillType.WANDS) {
         if (key.equals("critical_strikes")) {
            return true;
         } else {
            return switch (skill) {
               case BOWS -> Set.of("power", "flame", "punch", "infinity").contains(key);
               case CROSSBOWS -> Set.of("power", "quick_charge", "multishot", "piercing").contains(key);
               case TRIDENTS -> Set.of("sharpness", "impaling", "loyalty", "channeling").contains(key);
               case SWORDS -> Set.of("sharpness", "fire_aspect", "knockback", "looting", "sweeping_edge").contains(key);
               case AXES -> Set.of("sharpness", "fire_aspect", "looting").contains(key);
               case HOES, MACES, SPEARS -> Set.of("sharpness", "fire_aspect").contains(key);
               default -> false;
            };
         }
      } else {
         return key.equals("power") || MagicEnchantmentGeneration.supports(skill, key);
      }
   }

   public record Roll(HashMap<Enchantment, Integer> nativeEnchantments, HashMap<String, Integer> customEnchantments) {
   }

   public record Rule(String key, int level, double chance) {
   }
}
