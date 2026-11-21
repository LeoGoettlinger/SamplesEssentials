package me.chancesd.pvpmanager.tasks;

import me.chancesd.pvpmanager.manager.PlayerManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.sdutils.scheduler.ScheduleUtils;
import me.chancesd.pvpmanager.sdutils.utils.Log;
import me.chancesd.pvpmanager.setting.Conf;
import me.chancesd.pvpmanager.setting.Lang;
import org.bukkit.ChatColor;

public class PvPToggleFeeTask implements Runnable {
   private final PlayerManager ph;

   public PvPToggleFeeTask(PlayerManager ph) {
      this.ph = ph;
   }

   @Override
   public final void run() {
      int i = 0;

      for (CombatPlayer combatPlayer : this.ph.getPlayers().values()) {
         if (!combatPlayer.hasPvPEnabled() && !combatPlayer.applyPvPDisabledFee()) {
            ScheduleUtils.ensureMainThread(() -> combatPlayer.setPvP(true), combatPlayer.getPlayer());
            combatPlayer.message(Lang.PVP_FEE_NOT_ENOUGH, new Object[0]);
            i++;
         }
      }

      Log.infoColor(ChatColor.GREEN + "Applied the PvP disabled fee of " + Conf.PVP_DISABLED_FEE.asInt() + " to " + i + " players.");
   }
}
