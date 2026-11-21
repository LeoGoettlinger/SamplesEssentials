package me.chancesd.pvpmanager.integration.hook.worldguard;

import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.bukkit.RegionQuery;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.lang.reflect.Method;
import java.util.Set;
import me.chancesd.pvpmanager.integration.BaseDependency;
import me.chancesd.pvpmanager.integration.DependencyException;
import me.chancesd.pvpmanager.integration.Hook;
import me.chancesd.pvpmanager.integration.type.WorldGuardDependency;
import me.chancesd.pvpmanager.manager.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class WorldGuardLegacyHook extends BaseDependency implements WorldGuardDependency {
   private RegionQuery regionQuery;

   public WorldGuardLegacyHook(Hook hook) {
      super(hook);
      WorldGuardPlugin wgPlugin = WorldGuardPlugin.inst();

      try {
         Method method = WorldGuardPlugin.class.getMethod("getRegionContainer");
         this.regionQuery = ((RegionContainer)method.invoke(wgPlugin)).createQuery();
      } catch (ReflectiveOperationException var4) {
         this.regionQuery = null;
         throw new DependencyException("Error loading WorldGuard! Update WorldGuard to a newer version", var4, hook);
      }
   }

   @Override
   public boolean canAttack(Player attacker, Player defender) {
      return true;
   }

   @Override
   public boolean canAttackAt(Player p, Location l) {
      return this.getWGPvPState(l) != State.DENY;
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
   public Set<ProtectedRegion> getRegionsAt(Location l) {
      return this.regionQuery.getApplicableRegions(l).getRegions();
   }

   @Override
   public boolean containsRegionsAt(Location l, Set<String> regionIDs) {
      for (ProtectedRegion r : this.getRegionsAt(l)) {
         if (regionIDs.contains(r.getId())) {
            return true;
         }
      }

      return false;
   }

   @Override
   public State getWGPvPState(Location l) {
      return this.regionQuery.queryState(l, (Player)null, new StateFlag[]{DefaultFlag.PVP});
   }

   @Override
   public State getStateFlagAt(Location loc, StateFlag flag) {
      return this.regionQuery.queryState(loc, (Player)null, new StateFlag[]{flag});
   }

   @Override
   public void startListener(PlayerManager ph) {
      Bukkit.getPluginManager().registerEvents(new WGListenerLegacy(ph), ph.getPlugin());
   }

   @Override
   public String getName() {
      return "WorldGuardLegacy";
   }
}
