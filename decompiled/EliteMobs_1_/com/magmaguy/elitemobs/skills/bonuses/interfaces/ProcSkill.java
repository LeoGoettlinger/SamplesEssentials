package com.magmaguy.elitemobs.skills.bonuses.interfaces;

import org.bukkit.entity.Player;

public interface ProcSkill {
   double getProcChance(int var1);

   void onProc(Player var1, Object var2);

   default long getInternalCooldownMillis() {
      return 0L;
   }

   default boolean canProc(Player player, Object context) {
      return true;
   }
}
