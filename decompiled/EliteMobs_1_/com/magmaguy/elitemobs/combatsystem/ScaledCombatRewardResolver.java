package com.magmaguy.elitemobs.combatsystem;

import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.skills.CombatLevelCalculator;
import org.bukkit.entity.Player;

public class ScaledCombatRewardResolver {
   private ScaledCombatRewardResolver() {
   }

   public static int getRewardLevel(EliteEntity eliteEntity, Player player) {
      if (eliteEntity == null) {
         return 1;
      } else {
         return player != null && eliteEntity.isScaledCombat()
            ? Math.max(1, CombatLevelCalculator.calculateCombatLevel(player.getUniqueId()))
            : Math.max(1, eliteEntity.getLevel());
      }
   }
}
