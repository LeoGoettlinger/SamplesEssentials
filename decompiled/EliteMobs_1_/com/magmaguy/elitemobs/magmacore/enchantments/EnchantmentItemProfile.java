package com.magmaguy.elitemobs.magmacore.enchantments;

import java.util.Objects;
import java.util.Set;
import org.bukkit.inventory.ItemStack;

public record EnchantmentItemProfile(EnchantmentDefinition.ItemType type, Set<EnchantmentDefinition.Slot> slots, Set<String> attackKinds) {
   public EnchantmentItemProfile(EnchantmentDefinition.ItemType type, Set<EnchantmentDefinition.Slot> slots, Set<String> attackKinds) {
      Objects.requireNonNull(type, "type");
      slots = Set.copyOf(slots);
      attackKinds = Set.copyOf(attackKinds);
      if (!EnchantmentDefinition.ATTACK_KINDS.containsAll(attackKinds)) {
         throw new IllegalArgumentException("Unknown attack kind");
      } else {
         this.type = type;
         this.slots = slots;
         this.attackKinds = attackKinds;
      }
   }

   public static EnchantmentItemProfile vanilla(ItemStack item) {
      String material = item.getType().name();
      EnchantmentDefinition.ItemType type = EnchantmentDefinition.ItemType.OTHER;

      for (EnchantmentDefinition.ItemType candidate : EnchantmentDefinition.ItemType.values()) {
         if (candidate != EnchantmentDefinition.ItemType.WAND
            && candidate != EnchantmentDefinition.ItemType.STAFF
            && candidate != EnchantmentDefinition.ItemType.OTHER
            && (material.equals(candidate.name()) || material.endsWith("_" + candidate.name()))) {
            type = candidate;
            break;
         }
      }
      Set<EnchantmentDefinition.Slot> slots = switch (type) {
         case HELMET -> Set.of(EnchantmentDefinition.Slot.HEAD);
         case CHESTPLATE, ELYTRA -> Set.of(EnchantmentDefinition.Slot.CHEST);
         case LEGGINGS -> Set.of(EnchantmentDefinition.Slot.LEGS);
         case BOOTS -> Set.of(EnchantmentDefinition.Slot.FEET);
         default -> Set.of(EnchantmentDefinition.Slot.MAINHAND, EnchantmentDefinition.Slot.OFFHAND);
      };
      return new EnchantmentItemProfile(type, slots, Set.of());
   }
}
