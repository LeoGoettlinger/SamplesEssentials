package me.chancesd.pvpmanager.integration.hook.worldguard;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.FlagValueChangeHandler;
import me.chancesd.pvpmanager.PvPManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.sdutils.utils.Log;
import me.chancesd.pvpmanager.setting.Lang;
import org.bukkit.ChatColor;

public class WorldGuardFlagHandler extends FlagValueChangeHandler<State> {
   public static final WorldGuardFlagHandler.Factory FACTORY = new WorldGuardFlagHandler.Factory();
   private static StateFlag noProtectionFlag;
   private final PvPManager plugin = PvPManager.getInstance();

   public WorldGuardFlagHandler(Session session) {
      super(session, noProtectionFlag);
   }

   public static void initializeFlags() {
      Log.infoColor(ChatColor.DARK_PURPLE + "Registering WorldGuard flags...");
      noProtectionFlag = registerFlag("pvp-protection", false);
   }

   public static void shutdown() {
      WorldGuard.getInstance().getPlatform().getSessionManager().unregisterHandler(FACTORY);
   }

   protected void onInitialValue(LocalPlayer player, ApplicableRegionSet set, State value) {
      Log.debug("Initial value: " + value);
   }

   protected boolean onSetValue(
      LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet, State currentValue, State lastValue, MoveType moveType
   ) {
      Log.debug("Changed value: " + currentValue + " from " + lastValue);
      if (currentValue != State.DENY) {
         return true;
      } else {
         CombatPlayer cplayer = this.plugin.getPlayerManager().get(BukkitAdapter.adapt(player));
         if (!cplayer.hasPvPEnabled()) {
            cplayer.setPvP(true);
            cplayer.message(Lang.PVP_FORCE_ENABLED_WG, new Object[0]);
         }

         return true;
      }
   }

   protected boolean onAbsentValue(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet, State lastValue, MoveType moveType) {
      Log.debug("Absent value, last was: " + lastValue);
      return true;
   }

   private static StateFlag registerFlag(String name, boolean def) {
      FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
      StateFlag flag = new StateFlag(name, def);

      try {
         registry.register(flag);
      } catch (IllegalStateException | FlagConflictException var7) {
         if (registry.get(name) instanceof StateFlag stateFlag) {
            return stateFlag;
         }
      }

      return flag;
   }

   public static StateFlag getNoProtectionFlag() {
      return noProtectionFlag;
   }

   public static class Factory extends com.sk89q.worldguard.session.handler.Handler.Factory<WorldGuardFlagHandler> {
      public WorldGuardFlagHandler create(Session session) {
         return new WorldGuardFlagHandler(session);
      }
   }
}
