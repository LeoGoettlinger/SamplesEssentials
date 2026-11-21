package me.byteful.plugin.leveltools.api.trigger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class TriggerIds {
   public static final String BLOCK_BREAK = "block_break";
   public static final String ENTITY_KILL = "entity_kill";
   public static final String FISHING = "fishing";
   public static final String RIGHT_CLICK = "right_click";
   public static final String LEFT_CLICK = "left_click";
   public static final String CONSUME = "consume";
   public static final String FARMING = "farming";
   public static final String ARMOR_DURABILITY = "armor_durability";

   private TriggerIds() {
   }

   @Nullable
   public static String normalize(@Nullable String id) {
      return id == null ? null : id.toLowerCase().replace("-", "_").trim();
   }

   public static boolean isBuiltIn(@NotNull String id) {
      String normalized = normalize(id);
      return "block_break".equals(normalized)
         || "entity_kill".equals(normalized)
         || "fishing".equals(normalized)
         || "right_click".equals(normalized)
         || "left_click".equals(normalized)
         || "consume".equals(normalized)
         || "farming".equals(normalized)
         || "armor_durability".equals(normalized);
   }
}
