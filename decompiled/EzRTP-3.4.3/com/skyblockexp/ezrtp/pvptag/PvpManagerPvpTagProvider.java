package com.skyblockexp.ezrtp.pvptag;

import me.chancesd.pvpmanager.player.CombatPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class PvpManagerPvpTagProvider implements PvpTagProvider {
   @Override
   public String getName() {
      return "PvPManager";
   }

   @Override
   public boolean isAvailable() {
      return Bukkit.getPluginManager().isPluginEnabled("PvPManager");
   }

   @Override
   public boolean isInCombat(Player player) {
      if (player == null) {
         return false;
      } else {
         try {
            CombatPlayer combatPlayer = CombatPlayer.get(player);
            return combatPlayer != null && combatPlayer.isInCombat();
         } catch (Throwable var3) {
            return false;
         }
      }
   }
}
