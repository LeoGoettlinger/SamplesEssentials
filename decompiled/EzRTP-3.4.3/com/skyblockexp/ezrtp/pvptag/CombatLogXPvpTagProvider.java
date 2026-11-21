package com.skyblockexp.ezrtp.pvptag;

import com.github.sirblobman.combatlogx.api.ICombatLogX;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class CombatLogXPvpTagProvider implements PvpTagProvider {
   private final ICombatLogX api = (ICombatLogX)Bukkit.getPluginManager().getPlugin("CombatLogX");

   @Override
   public String getName() {
      return "CombatLogX";
   }

   @Override
   public boolean isAvailable() {
      return this.api != null && Bukkit.getPluginManager().isPluginEnabled("CombatLogX");
   }

   @Override
   public boolean isInCombat(Player player) {
      if (this.api != null && player != null) {
         try {
            return this.api.getCombatManager().isInCombat(player);
         } catch (Throwable var3) {
            return false;
         }
      } else {
         return false;
      }
   }
}
