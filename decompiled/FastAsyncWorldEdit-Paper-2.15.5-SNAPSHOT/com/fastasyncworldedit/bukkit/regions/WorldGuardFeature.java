package com.fastasyncworldedit.bukkit.regions;

import com.fastasyncworldedit.core.configuration.Settings;
import com.fastasyncworldedit.core.regions.FaweMask;
import com.fastasyncworldedit.core.regions.FaweMaskManager;
import com.fastasyncworldedit.core.regions.RegionWrapper;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.AbstractRegion;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionIntersection;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.GlobalProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class WorldGuardFeature extends BukkitMaskManager implements Listener {
   private static final Logger LOGGER = LogManagerCompat.getLogger();
   private final WorldGuardPlugin worldguard = this.getWorldGuard();

   public WorldGuardFeature(Plugin plugin) {
      super(plugin.getName());
      LOGGER.info("Plugin 'WorldGuard' found. Using it now.");
   }

   private static Region adapt(ProtectedRegion region) {
      if (region instanceof ProtectedCuboidRegion) {
         return new CuboidRegion(region.getMinimumPoint(), region.getMaximumPoint());
      } else if (region instanceof GlobalProtectedRegion) {
         return RegionWrapper.GLOBAL();
      } else if (region instanceof ProtectedPolygonalRegion casted) {
         BlockVector3 max = region.getMaximumPoint();
         BlockVector3 min = region.getMinimumPoint();
         return new Polygonal2DRegion(null, casted.getPoints(), min.y(), max.y());
      } else {
         return new WorldGuardFeature.AdaptedRegion(region);
      }
   }

   private WorldGuardPlugin getWorldGuard() {
      Plugin plugin = Bukkit.getPluginManager().getPlugin("WorldGuard");
      return !(plugin instanceof WorldGuardPlugin) ? null : (WorldGuardPlugin)plugin;
   }

   public Set<ProtectedRegion> getRegions(LocalPlayer player, Location location, boolean isWhitelist) {
      RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
      if (container == null) {
         LOGGER.info("Region capability is not enabled for WorldGuard.");
         return Collections.emptySet();
      } else {
         RegionManager manager = container.get(BukkitAdapter.adapt(location.getWorld()));
         if (manager == null) {
            LOGGER.info("Region capability is not enabled for that world.");
            return Collections.emptySet();
         } else if (isWhitelist) {
            ProtectedRegion global = manager.getRegion("__global__");
            if (global != null && this.isAllowed(player, global)) {
               return Collections.singleton(global);
            } else {
               ApplicableRegionSet regions = manager.getApplicableRegions(BlockVector3.at(location.getX(), location.getY(), location.getZ()));
               if (player.hasPermission("fawe.worldguardflag")
                  && !regions.testState(player, new StateFlag[]{Flags.BUILD, Flags.BLOCK_PLACE, Flags.BLOCK_BREAK})) {
                  return Collections.emptySet();
               } else {
                  Set<ProtectedRegion> protectedRegions = new HashSet<>();

                  for (ProtectedRegion region : regions) {
                     if (this.isAllowed(player, region)) {
                        protectedRegions.add(region);
                     }
                  }

                  return Collections.unmodifiableSet(protectedRegions);
               }
            }
         } else {
            Collection<ProtectedRegion> regions = manager.getRegions().values();
            Set<ProtectedRegion> protectedRegions = new HashSet<>();

            for (ProtectedRegion regionx : regions) {
               if (!regionx.getId().equals("__global__") && !this.isAllowed(player, regionx)) {
                  protectedRegions.add(regionx);
               }
            }

            return Collections.unmodifiableSet(protectedRegions);
         }
      }
   }

   public boolean isAllowed(LocalPlayer localplayer, ProtectedRegion region) {
      if (!region.isOwner(localplayer) && !region.isOwner(localplayer.getName())) {
         if (region.getId().toLowerCase(Locale.ROOT).equals(localplayer.getName().toLowerCase(Locale.ROOT))) {
            return true;
         } else if (region.getId().toLowerCase(Locale.ROOT).contains(localplayer.getName().toLowerCase(Locale.ROOT) + "//")) {
            return true;
         } else if (region.isOwner("*")) {
            return true;
         } else if (localplayer.hasPermission("worldguard.region.bypass")
            || localplayer.hasPermission("worldguard.region.bypass." + localplayer.getWorld().getName().toLowerCase(Locale.ROOT))) {
            return true;
         } else if (!localplayer.hasPermission("fawe.worldguard.member")) {
            return false;
         } else {
            return !region.isMember(localplayer) && !region.isMember(localplayer.getName()) ? region.isMember("*") : true;
         }
      } else {
         return true;
      }
   }

   @Override
   public FaweMask getMask(Player wePlayer, FaweMaskManager.MaskType type, boolean isWhitelist) {
      if (isWhitelist && Settings.settings().REGION_RESTRICTIONS_OPTIONS.WORLDGUARD_REGION_BLACKLIST) {
         return new FaweMask(RegionWrapper.GLOBAL());
      } else {
         org.bukkit.entity.Player player = BukkitAdapter.adapt(wePlayer);
         LocalPlayer localplayer = this.worldguard.wrapPlayer(player);
         Location location = player.getLocation();
         final Set<ProtectedRegion> regions = this.getRegions(localplayer, location, isWhitelist);
         if (!regions.isEmpty()) {
            final RegionManager manager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(location.getWorld()));
            if (manager == null) {
               return null;
            } else {
               Set<Region> result = new HashSet<>();

               for (final ProtectedRegion myRegion : regions) {
                  if (myRegion.getId().equals("__global__")) {
                     return new FaweMask(RegionWrapper.GLOBAL()) {
                        @Override
                        public boolean isValid(Player player, FaweMaskManager.MaskType typex) {
                           return manager.hasRegion(myRegion.getId())
                              && WorldGuardFeature.this.isAllowed(WorldGuardFeature.this.worldguard.wrapPlayer(BukkitAdapter.adapt(player)), myRegion);
                        }
                     };
                  }

                  if (myRegion instanceof ProtectedCuboidRegion) {
                     result.add(new CuboidRegion(myRegion.getMinimumPoint(), myRegion.getMaximumPoint()));
                  } else {
                     result.add(adapt(myRegion));
                  }
               }

               return new FaweMask(new RegionIntersection(wePlayer.getWorld(), result)) {
                  @Override
                  public boolean isValid(Player player, FaweMaskManager.MaskType type) {
                     LocalPlayer localplayer = WorldGuardFeature.this.worldguard.wrapPlayer(BukkitAdapter.adapt(player));

                     for (ProtectedRegion myRegion : regions) {
                        if (!manager.hasRegion(myRegion.getId()) || !WorldGuardFeature.this.isAllowed(localplayer, myRegion)) {
                           return false;
                        }
                     }

                     return true;
                  }
               };
            }
         } else {
            return null;
         }
      }
   }

   private static class AdaptedRegion extends AbstractRegion {
      private final ProtectedRegion region;

      public AdaptedRegion(ProtectedRegion region) {
         super(null);
         this.region = region;
      }

      @Override
      public BlockVector3 getMinimumPoint() {
         return this.region.getMinimumPoint();
      }

      @Override
      public BlockVector3 getMaximumPoint() {
         return this.region.getMaximumPoint();
      }

      @Override
      public void expand(BlockVector3... changes) {
         throw new UnsupportedOperationException("Region is immutable");
      }

      @Override
      public void contract(BlockVector3... changes) {
         throw new UnsupportedOperationException("Region is immutable");
      }

      @Override
      public boolean contains(BlockVector3 position) {
         return this.region.contains(position);
      }
   }
}
