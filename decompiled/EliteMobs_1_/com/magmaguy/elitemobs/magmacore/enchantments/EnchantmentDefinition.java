package com.magmaguy.elitemobs.magmacore.enchantments;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Pattern;

public record EnchantmentDefinition(
   String id,
   String name,
   String description,
   int maxLevel,
   boolean curse,
   Set<EnchantmentDefinition.Slot> validSlots,
   Set<EnchantmentDefinition.ItemType> itemTypes,
   Set<String> attackKinds,
   Set<String> requires,
   Set<String> conflicts,
   EnchantmentDefinition.Stacking stacking,
   String script,
   Map<String, Object> parameters
) {
   private static final Pattern ID = Pattern.compile("[a-z0-9._-]{1,64}:[a-z0-9._-]{1,128}");
   private static final Pattern CAPABILITY = Pattern.compile("[a-z0-9._:-]{1,128}");
   public static final Set<String> ATTACK_KINDS = Set.of("WAND_MISSILE", "STAFF_FIREBALL", "STAFF_MELEE");

   public EnchantmentDefinition(
      String id,
      String name,
      String description,
      int maxLevel,
      boolean curse,
      Set<EnchantmentDefinition.Slot> validSlots,
      Set<EnchantmentDefinition.ItemType> itemTypes,
      Set<String> attackKinds,
      Set<String> requires,
      Set<String> conflicts,
      EnchantmentDefinition.Stacking stacking,
      String script,
      Map<String, Object> parameters
   ) {
      requireId(id);
      if (id.startsWith("minecraft:")) {
         throw new IllegalArgumentException("Custom definitions cannot own minecraft");
      } else if (name == null || name.isBlank() || name.length() > 128 || name.indexOf(167) >= 0 || name.chars().anyMatch(Character::isISOControl)) {
         throw new IllegalArgumentException("name must be plain, nonempty text of at most 128 characters");
      } else if (description == null || description.length() > 8192) {
         throw new IllegalArgumentException("Invalid description");
      } else if (maxLevel < 1) {
         throw new IllegalArgumentException("maxLevel must be positive");
      } else {
         validSlots = Set.copyOf(validSlots);
         itemTypes = Set.copyOf(itemTypes);
         attackKinds = Set.copyOf(attackKinds);
         if (!ATTACK_KINDS.containsAll(attackKinds)) {
            throw new IllegalArgumentException("Unknown attackKinds");
         } else {
            requires = Set.copyOf(requires);
            if (requires.stream().anyMatch(key -> !CAPABILITY.matcher(key).matches())) {
               throw new IllegalArgumentException("Invalid required capability");
            } else {
               conflicts = Set.copyOf(conflicts);
               conflicts.forEach(EnchantmentDefinition::requireId);
               if (conflicts.contains(id)) {
                  throw new IllegalArgumentException("An enchantment cannot conflict with itself");
               } else {
                  Objects.requireNonNull(stacking, "stacking");
                  if (script != null && script.matches("[a-z0-9._-]{1,128}\\.lua")) {
                     parameters = EnchantmentValues.copy(parameters);

                     for (Entry<String, Object> entry : parameters.entrySet()) {
                        if (!entry.getKey().matches("[a-zA-Z_][a-zA-Z0-9_]{0,127}")) {
                           throw new IllegalArgumentException("Invalid parameter name");
                        }

                        if (entry.getValue() instanceof List<?> levels) {
                           if (levels.size() != maxLevel) {
                              throw new IllegalArgumentException("Per-level parameter must match maxLevel");
                           }

                           levels.forEach(EnchantmentDefinition::requireScalar);
                        } else {
                           requireScalar(entry.getValue());
                        }
                     }

                     this.id = id;
                     this.name = name;
                     this.description = description;
                     this.maxLevel = maxLevel;
                     this.curse = curse;
                     this.validSlots = validSlots;
                     this.itemTypes = itemTypes;
                     this.attackKinds = attackKinds;
                     this.requires = requires;
                     this.conflicts = conflicts;
                     this.stacking = stacking;
                     this.script = script;
                     this.parameters = parameters;
                  } else {
                     throw new IllegalArgumentException("script must be a lowercase Lua filename without a directory");
                  }
               }
            }
         }
      }
   }

   public Map<String, Object> parametersAt(int level) {
      if (level < 1) {
         throw new IllegalArgumentException("Level must be positive");
      } else {
         Map<String, Object> selected = new LinkedHashMap<>();
         this.parameters.forEach((key, value) -> selected.put(key, value instanceof List<?> values ? values.get(Math.min(level, values.size()) - 1) : value));
         return Map.copyOf(selected);
      }
   }

   public static String requireId(String id) {
      if (id != null && ID.matcher(id).matches()) {
         return id;
      } else {
         throw new IllegalArgumentException("Expected a lowercase namespaced enchantment ID");
      }
   }

   private static void requireScalar(Object value) {
      if (!(value instanceof String) && !(value instanceof Boolean) && !(value instanceof Number)) {
         throw new IllegalArgumentException("Parameters must contain scalars or per-level scalar lists");
      }
   }

   public static enum ItemType {
      SWORD,
      AXE,
      PICKAXE,
      SHOVEL,
      HOE,
      BOW,
      CROSSBOW,
      TRIDENT,
      MACE,
      SPEAR,
      HELMET,
      CHESTPLATE,
      LEGGINGS,
      BOOTS,
      SHIELD,
      FISHING_ROD,
      SHEARS,
      ELYTRA,
      WAND,
      STAFF,
      OTHER;
   }

   public static enum Slot {
      HEAD,
      CHEST,
      LEGS,
      FEET,
      MAINHAND,
      OFFHAND;
   }

   public static enum Stacking {
      SOURCE_ITEM,
      SUM_EQUIPPED_LEVELS;
   }
}
