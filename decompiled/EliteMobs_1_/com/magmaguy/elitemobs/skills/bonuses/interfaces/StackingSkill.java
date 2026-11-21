package com.magmaguy.elitemobs.skills.bonuses.interfaces;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import org.bukkit.entity.Player;

public interface StackingSkill {
   int getMaxStacks();

   int getCurrentStacks(Player var1);

   void addStack(Player var1);

   void resetStacks(Player var1);

   double getBonusPerStack(int var1);

   default double getTotalStackBonus(Player player, int skillLevel) {
      return this.getCurrentStacks(player) * this.getBonusPerStack(skillLevel);
   }

   default boolean stacksOnHit(EliteMobDamagedByPlayerEvent event) {
      return true;
   }

   default boolean banksStacksExternally() {
      return false;
   }
}
