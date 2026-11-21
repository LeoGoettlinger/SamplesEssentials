package com.magmaguy.elitemobs.skills.bonuses.interfaces;

import org.bukkit.entity.Player;

public interface ConditionalSkill {
   boolean conditionMet(Player var1, Object var2);

   double getConditionalBonus(int var1);

   default double getConditionalBonus(Player player, int skillLevel) {
      return this.getConditionalBonus(skillLevel);
   }

   default void onConditionMet(Player player, Object context) {
   }
}
