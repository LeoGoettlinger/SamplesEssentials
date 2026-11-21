package me.byteful.plugin.leveltools.api.trigger;

import java.util.Locale;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum TriggerSlot {
   HAND,
   OFF_HAND,
   HELMET,
   CHESTPLATE,
   LEGGINGS,
   BOOTS;

   @Nullable
   public static TriggerSlot fromName(@Nullable String value) {
      if (value != null && !value.trim().isEmpty()) {
         try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT).replace('-', '_'));
         } catch (IllegalArgumentException var2) {
            return null;
         }
      } else {
         return null;
      }
   }

   public static TriggerSlot fromBukkit(@NotNull EquipmentSlot bukkit) {
      switch (bukkit) {
         case CHEST:
            return CHESTPLATE;
         case FEET:
            return BOOTS;
         case HAND:
            return HAND;
         case OFF_HAND:
            return OFF_HAND;
         case HEAD:
            return HELMET;
         case LEGS:
            return LEGGINGS;
         default:
            throw new IllegalArgumentException("LevelTools needs to be updated for the new MC update.");
      }
   }
}
