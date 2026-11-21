package com.magmaguy.elitemobs.thirdparty.worldguard;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import javax.annotation.Nullable;
import org.bukkit.Location;

public class WorldGuardFlagChecker {
   private WorldGuardFlagChecker() {
   }

   public static Boolean checkNullableFlag(Location location, StateFlag stateFlag) {
      com.sk89q.worldedit.util.Location wgLocation = BukkitAdapter.adapt(location);
      RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
      RegionQuery query = container.createQuery();
      ApplicableRegionSet set = query.getApplicableRegions(wgLocation);
      State state = set.queryState(null, new StateFlag[]{stateFlag});
      return state == null ? null : state == State.ALLOW;
   }

   public static Boolean checkFlag(Location location, StateFlag stateFlag) {
      com.sk89q.worldedit.util.Location wgLocation = BukkitAdapter.adapt(location);
      RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
      RegionQuery query = container.createQuery();
      ApplicableRegionSet set = query.getApplicableRegions(wgLocation);
      return set.testState(null, new StateFlag[]{stateFlag});
   }

   @Nullable
   public static Integer getIntegerFlagValue(Location location, Flag flag) {
      com.sk89q.worldedit.util.Location wgLocation = BukkitAdapter.adapt(location);
      RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
      RegionQuery query = container.createQuery();
      ApplicableRegionSet set = query.getApplicableRegions(wgLocation);
      Object object = set.queryValue(null, flag);
      return object == null ? null : (Integer)object;
   }

   @Nullable
   public static Integer getRegionMinimumLevel(Location location) {
      return getIntegerFlagValue(location, WorldGuardCompatibility.getELITEMOBS_MINIMUM_LEVEL());
   }

   @Nullable
   public static Integer getRegionMaximumLevel(Location location) {
      return getIntegerFlagValue(location, WorldGuardCompatibility.getELITEMOBS_MAXIMUM_LEVEL());
   }

   public static boolean doExplosionRegenFlag(Location location) {
      com.sk89q.worldedit.util.Location wgLocation = BukkitAdapter.adapt(location);
      RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
      RegionQuery query = container.createQuery();
      ApplicableRegionSet set = query.getApplicableRegions(wgLocation);
      return set.testState(null, new StateFlag[]{WorldGuardCompatibility.getELITEMOBS_EXPLOSION_REGEN()});
   }

   public static boolean doEventFlag(Location location) {
      com.sk89q.worldedit.util.Location wgLocation = BukkitAdapter.adapt(location);
      RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
      RegionQuery query = container.createQuery();
      ApplicableRegionSet set = query.getApplicableRegions(wgLocation);
      return set.testState(null, new StateFlag[]{WorldGuardCompatibility.getELITEMOBS_EVENTS()});
   }

   public static boolean doEliteMobsSpawnFlag(Location location) {
      com.sk89q.worldedit.util.Location wgLocation = BukkitAdapter.adapt(location);
      RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
      RegionQuery query = container.createQuery();
      ApplicableRegionSet set = query.getApplicableRegions(wgLocation);
      return set.testState(null, new StateFlag[]{WorldGuardCompatibility.getELITEMOBS_SPAWN_FLAG()});
   }

   public static boolean doMobSpawnFlag(Location location) {
      com.sk89q.worldedit.util.Location wgLocation = BukkitAdapter.adapt(location);
      RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
      RegionQuery query = container.createQuery();
      ApplicableRegionSet set = query.getApplicableRegions(wgLocation);
      return set.testState(null, new StateFlag[]{Flags.MOB_SPAWNING});
   }
}
