package org.skriptlang.skript.bukkit.loottables;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.Lootable;

public class LootTableUtils {
   public static boolean isLootable(Object object) {
      if (object instanceof Block block) {
         object = block.getState();
      }

      return object instanceof Lootable;
   }

   public static Lootable getAsLootable(Object object) {
      if (object instanceof Block block) {
         object = block.getState();
      }

      return object instanceof Lootable lootable ? lootable : null;
   }

   public static LootTable getLootTable(Object object) {
      return getAsLootable(object).getLootTable();
   }

   public static void updateState(Lootable lootable) {
      if (lootable instanceof BlockState blockState) {
         blockState.update(true, false);
      }
   }
}
