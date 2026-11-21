package com.magmaguy.elitemobs.magmacore.thirdparty.worldguard;

import com.magmaguy.elitemobs.magmacore.location.RegionProtectionProvider;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class WorldGuardProtectionProvider implements RegionProtectionProvider {
   private final RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();

   @Override
   public boolean isProtected(Location location) {
      RegionQuery query = this.container.createQuery();
      ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(location));
      return set.size() > 0;
   }

   @Override
   public boolean canBuild(Player player, Location location) {
      RegionQuery query = this.container.createQuery();
      LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
      World world = BukkitAdapter.adapt(location.getWorld());
      return WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(localPlayer, world)
         ? true
         : query.testBuild(BukkitAdapter.adapt(location), localPlayer, new StateFlag[0]);
   }

   @Override
   public String providerName() {
      return "WorldGuard";
   }
}
