package me.chancesd.pvpmanager.integration.hook.worldguard;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.SessionManager;
import com.sk89q.worldguard.session.handler.Handler;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Set;
import me.chancesd.pvpmanager.integration.BaseDependency;
import me.chancesd.pvpmanager.integration.Hook;
import me.chancesd.pvpmanager.integration.type.ForceToggleDependency;
import me.chancesd.pvpmanager.integration.type.WorldGuardDependency;
import me.chancesd.pvpmanager.manager.PlayerManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.player.ProtectionType;
import me.chancesd.pvpmanager.sdutils.utils.Log;
import me.chancesd.pvpmanager.setting.Conf;
import me.chancesd.pvpmanager.setting.Lang;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class WorldGuardModernHook extends BaseDependency implements WorldGuardDependency, ForceToggleDependency {
   private final RegionQuery regionQuery = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();

   public WorldGuardModernHook(Hook hook) {
      super(hook);
      SessionManager sessionManager = WorldGuard.getInstance().getPlatform().getSessionManager();

      for (Player player : Bukkit.getOnlinePlayers()) {
         Session session = sessionManager.get(WorldGuardPlugin.inst().wrapPlayer(player));

         try {
            Field handlersField = session.getClass().getDeclaredField("handlers");
            handlersField.setAccessible(true);
            HashMap<Class<?>, Handler> handlers = (HashMap<Class<?>, Handler>)handlersField.get(session);
            handlers.entrySet().removeIf(entry -> entry.getValue().getClass().getName().equals(WorldGuardFlagHandler.class.getName()));
         } catch (SecurityException | IllegalArgumentException | IllegalAccessException | NoSuchFieldException var8) {
            Log.severe("Failed to remove old WG FlagHandler, flags might not work correctly until the player rejoins", var8);
         }

         session.register(WorldGuardFlagHandler.FACTORY.create(session));
      }

      sessionManager.registerHandler(WorldGuardFlagHandler.FACTORY, null);
   }

   @Override
   public boolean canAttack(Player attacker, Player defender) {
      return true;
   }

   @Override
   public boolean canAttackAt(Player p, Location loc) {
      return this.getWGPvPState(loc) != State.DENY;
   }

   @Override
   public boolean hasAllowPvPFlag(Player defender) {
      return this.getWGPvPState(defender.getLocation()) == State.ALLOW;
   }

   @Override
   public boolean hasDenyPvPFlag(Player player) {
      return this.getWGPvPState(player.getLocation()) == State.DENY;
   }

   @Override
   public Set<ProtectedRegion> getRegionsAt(Location loc) {
      return this.regionQuery.getApplicableRegions(BukkitAdapter.adapt(loc)).getRegions();
   }

   @Override
   public boolean containsRegionsAt(Location loc, Set<String> regionIDs) {
      for (ProtectedRegion r : this.getRegionsAt(loc)) {
         if (regionIDs.contains(r.getId())) {
            return true;
         }
      }

      return false;
   }

   @Override
   public State getWGPvPState(Location loc) {
      return this.getStateFlagAt(loc, Flags.PVP);
   }

   @Override
   public State getStateFlagAt(Location loc, StateFlag flag) {
      return this.regionQuery.queryState(BukkitAdapter.adapt(loc), null, new StateFlag[]{flag});
   }

   @Override
   public void startListener(PlayerManager ph) {
      Bukkit.getPluginManager().registerEvents(new WGListener(ph), ph.getPlugin());
   }

   @Override
   public boolean shouldDisable(Player player) {
      if (!this.shouldCheckPvPFlag(player) && this.getStateFlagAt(player.getLocation(), WorldGuardFlagHandler.getNoProtectionFlag()) != State.DENY) {
         return false;
      } else {
         CombatPlayer cplayer = CombatPlayer.get(player);
         this.disablePvPProtection(cplayer);
         return true;
      }
   }

   @Override
   public boolean shouldDisable(Player damager, Player defender, ProtectionType reason) {
      if (this.shouldCheckPvPFlag(defender)) {
         CombatPlayer attacker = CombatPlayer.get(damager);
         CombatPlayer attacked = CombatPlayer.get(defender);
         if (reason == ProtectionType.PVPDISABLED) {
            this.disablePvPProtection(attacker);
            this.disablePvPProtection(attacked);
         } else {
            this.disableNewbieProtection(attacker);
            this.disableNewbieProtection(attacked);
         }

         return true;
      } else {
         return false;
      }
   }

   private boolean shouldCheckPvPFlag(Player defender) {
      return Conf.WORLDGUARD_OVERRIDES.asBool() && this.hasAllowPvPFlag(defender);
   }

   private void disableNewbieProtection(CombatPlayer player) {
      if (player.isNewbie()) {
         player.setNewbie(false);
         player.message(Lang.NEWBIE_FORCE_REMOVED_WG, new Object[0]);
      }
   }

   private void disablePvPProtection(CombatPlayer player) {
      if (!player.hasPvPEnabled()) {
         player.setPvP(true);
         player.message(Lang.PVP_FORCE_ENABLED_WG, new Object[0]);
      }
   }

   @Override
   public boolean shouldDisableProtection() {
      return true;
   }
}
