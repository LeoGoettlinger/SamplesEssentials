package com.magmaguy.elitemobs.powers.lua;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.bukkit.inventory.EquipmentSlot;

final class EquipmentDamagePolicy {
   static final int MAXIMUM_DAMAGE_PER_CALL = 64;
   private static final Map<String, EquipmentSlot> SLOTS = Map.of(
      "HAND",
      EquipmentSlot.HAND,
      "OFF_HAND",
      EquipmentSlot.OFF_HAND,
      "HEAD",
      EquipmentSlot.HEAD,
      "CHEST",
      EquipmentSlot.CHEST,
      "LEGS",
      EquipmentSlot.LEGS,
      "FEET",
      EquipmentSlot.FEET
   );

   private EquipmentDamagePolicy() {
   }

   static EquipmentSlot parseSlot(String token) {
      String normalized = Objects.requireNonNull(token, "token").trim().toUpperCase(Locale.ROOT);
      EquipmentSlot slot = SLOTS.get(normalized);
      if (slot == null) {
         throw new IllegalArgumentException("Equipment slot must be one of " + SLOTS.keySet());
      } else {
         return slot;
      }
   }

   static int requestedDamage(int requested, int currentDamage, int maximumDamage) {
      requireRequestedDamage(requested);
      validateDurability(currentDamage, maximumDamage);
      return Math.min(requested, Math.max(0, maximumDamage - currentDamage));
   }

   static boolean canDamage(boolean hasItem, boolean damageable, boolean unbreakable, int maximumDamage) {
      return hasItem && damageable && !unbreakable && maximumDamage > 0;
   }

   static EquipmentDamagePolicy.Result applyEventDamage(int eventDamage, int currentDamage, int maximumDamage) {
      return applyEventDamage(true, true, eventDamage, currentDamage, maximumDamage);
   }

   static EquipmentDamagePolicy.Result applyEventDamage(boolean targetActive, boolean permitted, int eventDamage, int currentDamage, int maximumDamage) {
      validateDurability(currentDamage, maximumDamage);
      if (targetActive && permitted) {
         int actual = Math.min(Math.max(0, eventDamage), Math.max(0, maximumDamage - currentDamage));
         int resulting = Math.addExact(currentDamage, actual);
         return new EquipmentDamagePolicy.Result(actual, resulting, actual > 0 && resulting >= maximumDamage);
      } else {
         return new EquipmentDamagePolicy.Result(0, currentDamage, false);
      }
   }

   static void requireRequestedDamage(int requested) {
      if (requested <= 0 || requested > 64) {
         throw new IllegalArgumentException("Equipment damage must be between 1 and 64");
      }
   }

   private static void validateDurability(int currentDamage, int maximumDamage) {
      if (maximumDamage <= 0 || currentDamage < 0) {
         throw new IllegalArgumentException("Durability bounds require non-negative damage and a positive maximum");
      }
   }

   record Result(int actualDamage, int resultingDamage, boolean broke) {
   }
}
