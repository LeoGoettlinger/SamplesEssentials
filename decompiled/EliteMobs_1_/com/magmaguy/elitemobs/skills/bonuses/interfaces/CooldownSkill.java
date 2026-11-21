package com.magmaguy.elitemobs.skills.bonuses.interfaces;

import org.bukkit.entity.Player;

public interface CooldownSkill {
   long getCooldownSeconds(int var1);

   boolean isOnCooldown(Player var1);

   void startCooldown(Player var1, int var2);

   long getRemainingCooldown(Player var1);

   void endCooldown(Player var1);

   default void onActivate(Player player, Object event) {
   }

   default boolean tryActivate(Player player, Object event) {
      this.onActivate(player, event);
      return true;
   }

   default boolean triggersOnOffensiveHit() {
      return true;
   }

   default boolean sharesActivation(Player player, Object event) {
      return false;
   }
}
