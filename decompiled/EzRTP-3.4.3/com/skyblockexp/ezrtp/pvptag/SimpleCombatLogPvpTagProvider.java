package com.skyblockexp.ezrtp.pvptag;

import de.nikey.combatLog.CombatLog;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class SimpleCombatLogPvpTagProvider implements PvpTagProvider {
   @Override
   public String getName() {
      return "Simple Combat Log";
   }

   @Override
   public boolean isAvailable() {
      return Bukkit.getPluginManager().isPluginEnabled("CombatLog");
   }

   @Override
   public boolean isInCombat(Player player) {
      if (player == null) {
         return false;
      } else {
         try {
            return Bukkit.getPluginManager().getPlugin("CombatLog") instanceof CombatLog combatLog ? combatLog.getCombatManager().isInCombat(player) : false;
         } catch (Throwable var4) {
            return false;
         }
      }
   }
}
