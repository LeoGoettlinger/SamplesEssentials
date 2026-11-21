package dev.cwhead.GravesX.manager;

import com.ranull.graves.Graves;
import com.ranull.graves.type.Grave;
import com.ranull.graves.util.LocationUtil;
import com.ranull.graves.util.MaterialUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public final class SafeLocationManager {
   private final Graves plugin;
   private static final List<String> DEFAULT_POWDER_SNOW_BLOCKS = List.of("POWDER_SNOW");
   private static final List<String> DEFAULT_NON_FULL_SUPPORT_BLOCKS = List.of(
      "*_SLAB",
      "*_CARPET",
      "STEP",
      "DOUBLE_STEP",
      "CARPET",
      "LEGACY_STEP",
      "LEGACY_DOUBLE_STEP",
      "LEGACY_CARPET",
      "SCAFFOLDING",
      "LEGACY_SCAFFOLDING",
      "TRIPWIRE",
      "TRIPWIRE_HOOK",
      "LEGACY_TRIPWIRE",
      "LEGACY_TRIPWIRE_HOOK"
   );
   private static final List<String> DEFAULT_REPLACE_AVOIDED_BLOCKS = List.of(
      "*_SLAB",
      "*_CARPET",
      "*_PRESSURE_PLATE",
      "*_BUTTON",
      "*_SIGN",
      "*_WALL_SIGN",
      "*_BANNER",
      "*_WALL_BANNER",
      "*_TORCH",
      "STEP",
      "DOUBLE_STEP",
      "CARPET",
      "LEGACY_STEP",
      "LEGACY_DOUBLE_STEP",
      "LEGACY_CARPET",
      "SNOW",
      "LEGACY_SNOW",
      "REDSTONE_WIRE",
      "LEGACY_REDSTONE_WIRE",
      "TORCH",
      "LEGACY_TORCH"
   );

   public SafeLocationManager(Graves plugin) {
      this.plugin = plugin;
   }

   public int getNetherRoofLimit(World world, Grave grave) {
      return this.getNetherRoofYInternal(world, grave);
   }

   public boolean isAboveNetherRoof(Location location, Grave grave) {
      return this.isAboveNetherRoofInternal(location, grave);
   }

   public void setLastSolidLocation(Entity entity, Location location) {
      if (entity != null && location != null) {
         this.plugin.getCacheManager().getLastLocationMap().put(entity.getUniqueId(), location.clone());
      }
   }

   public Location getLastSolidLocation(Entity entity) {
      if (entity == null) {
         return null;
      } else {
         Location location = this.plugin.getCacheManager().getLastLocationMap().get(entity.getUniqueId());
         if (location != null && location.getWorld() != null) {
            entity.getWorld();
            if (!location.getWorld().equals(entity.getWorld())) {
               return null;
            } else {
               Block feet = location.getBlock();
               if (!MaterialUtil.isWater(feet.getType()) && !MaterialUtil.isLava(feet.getType())) {
                  Block below = location.getBlock().getRelative(BlockFace.DOWN);
                  return !below.getType().isSolid() ? null : location.clone();
               } else {
                  return null;
               }
            }
         } else {
            return null;
         }
      }
   }

   public void removeLastSolidLocation(Entity entity) {
      if (entity != null) {
         this.plugin.getCacheManager().getLastLocationMap().remove(entity.getUniqueId());
      }
   }

   public Location getSafeTeleportLocation(Entity entity, Location location, Grave grave, Graves plugin) {
      if (location != null && entity != null) {
         Location rounded = LocationUtil.roundLocation(location);
         if (rounded != null && rounded.getWorld() != null) {
            String prefix = "[SafeLocationManager] getSafeTeleportLocation: ";
            if (plugin.getConfigManager().getConfigSection("teleport.unsafe", grave).getBoolean("teleport.unsafe")) {
               plugin.debugMessage(prefix + "teleport.unsafe=true, using requested location as-is.", 1);
               return rounded;
            } else if (this.isLocationSafePlayer(rounded)) {
               plugin.debugMessage(prefix + "requested location already safe for player, using as-is.", 1);
               return rounded;
            } else {
               if (plugin.getConfigManager().getConfigSection("teleport.top", grave).getBoolean("teleport.top")) {
                  Location top = this.resolveTeleportTop(rounded);
                  if (top != null) {
                     plugin.debugMessage(prefix + "resolved top teleport location to " + this.fmtLoc(top) + ".", 1);
                     if (entity instanceof Player player) {
                        plugin.getEntityManager().sendMessage("message.teleport-top", player, top, grave);
                     }

                     return top;
                  }

                  plugin.debugMessage(prefix + "no safe top teleport location found.", 1);
               }

               plugin.debugMessage(prefix + "no safe teleport location could be resolved, returning null.", 1);
               return null;
            }
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   private Location resolveTeleportTop(Location base) {
      if (base != null && base.getWorld() != null) {
         World world = base.getWorld();
         int x = base.getBlockX();
         int z = base.getBlockZ();
         int minY = world.getMinHeight();
         int maxY = world.getMaxHeight() - 1;

         for (int y = maxY; y >= minY; y--) {
            Location candidate = new Location(world, x + 0.5, y, z + 0.5, base.getYaw(), base.getPitch());
            if (this.isLocationSafePlayer(candidate)) {
               return candidate;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   public Location getSafeGraveLocation(LivingEntity livingEntity, Location location, Grave grave) {
      SafeLocationManager.GravePlacementResult r = this.resolveSafeGravePlacement(livingEntity, location, grave);
      return r != null ? r.getLocation() : null;
   }

   public SafeLocationManager.GravePlacementResult resolveSafeGravePlacement(LivingEntity livingEntity, Location location, Grave grave) {
      String prefix = "[SafeLocationManager] resolveSafeGravePlacement: ";
      if (location == null) {
         this.plugin.debugMessage(prefix + "input location is null; returning null.", 1);
         return null;
      } else {
         Location origin = LocationUtil.roundLocation(location);
         if (origin == null) {
            origin = location.clone();
         }

         if (origin.getWorld() == null) {
            this.plugin.debugMessage(prefix + "origin world is null; returning original as-is.", 1);
            return SafeLocationManager.GravePlacementResult.of(origin, SafeLocationManager.GravePlacementReason.FALLBACK_ORIGINAL);
         } else {
            this.plugin.debugMessage(prefix + "origin=" + this.fmtLoc(origin) + " block=" + origin.getBlock().getType(), 1);
            boolean useGround = this.plugin.getConfigManager().getConfigSection("placement.ground", grave).getBoolean("placement.ground");
            boolean useRoof = this.plugin.getConfigManager().getConfigSection("placement.roof", grave).getBoolean("placement.roof");
            boolean allowNetherRoof = this.plugin.getConfigManager().getConfigSection("placement.nether-roof", grave).getBoolean("placement.nether-roof");
            World world = origin.getWorld();
            int worldMaxY = world.getMaxHeight() - 1;
            if (origin.getBlockY() > worldMaxY) {
               Location scanBase = new Location(world, origin.getBlockX() + 0.5, worldMaxY, origin.getBlockZ() + 0.5, origin.getYaw(), origin.getPitch());
               Location down = this.searchDownForSafeGrave(scanBase, grave, worldMaxY);
               if (down != null) {
                  this.plugin
                     .debugMessage(
                        prefix
                           + "CHOSEN="
                           + SafeLocationManager.GravePlacementReason.ABOVE_BUILD_LIMIT
                           + " loc="
                           + this.fmtLoc(down)
                           + " distSq="
                           + this.distSq(scanBase, down)
                           + ".",
                        1
                     );
                  return SafeLocationManager.GravePlacementResult.of(down, SafeLocationManager.GravePlacementReason.ABOVE_BUILD_LIMIT);
               }

               this.plugin.debugMessage(prefix + "origin above build limit; no safe placement found below build limit; continuing.", 1);
            }

            if (this.isNether(world) && this.isAboveNetherRoofInternal(origin, grave) && !allowNetherRoof && !useRoof) {
               int roofY = this.getNetherRoofYInternal(world, grave);
               this.plugin.debugMessage(prefix + "nether-roof handling: roofLimitY=" + roofY + " roof=false nether-roof=false.", 1);
               Location scanStart = new Location(world, origin.getBlockX() + 0.5, roofY, origin.getBlockZ() + 0.5, origin.getYaw(), origin.getPitch());
               Location below = this.searchDownForSafeGrave(scanStart, grave, roofY);
               if (below != null) {
                  this.plugin
                     .debugMessage(
                        prefix
                           + "CHOSEN="
                           + SafeLocationManager.GravePlacementReason.GROUND
                           + " loc="
                           + this.fmtLoc(below)
                           + " distSq="
                           + this.distSq(origin, below)
                           + ".",
                        1
                     );
                  return SafeLocationManager.GravePlacementResult.of(below, SafeLocationManager.GravePlacementReason.GROUND);
               } else {
                  int roofTopY = Math.min(worldMaxY, roofY + 1);
                  Location roofTop = new Location(world, origin.getBlockX() + 0.5, roofTopY, origin.getBlockZ() + 0.5, origin.getYaw(), origin.getPitch());
                  if (!this.hasGrave(roofTop) && this.isLocationSafeGrave(roofTop)) {
                     this.plugin
                        .debugMessage(
                           prefix
                              + "CHOSEN="
                              + SafeLocationManager.GravePlacementReason.NETHER_ROOF
                              + " loc="
                              + this.fmtLoc(roofTop)
                              + " distSq="
                              + this.distSq(origin, roofTop)
                              + ".",
                           1
                        );
                     return SafeLocationManager.GravePlacementResult.of(roofTop, SafeLocationManager.GravePlacementReason.NETHER_ROOF);
                  } else {
                     Location roofUp = this.searchUpForSafeGrave(roofTop, roofTop, grave, roofTop.getBlockY() + 1);
                     if (roofUp != null) {
                        this.plugin
                           .debugMessage(
                              prefix
                                 + "CHOSEN="
                                 + SafeLocationManager.GravePlacementReason.NETHER_ROOF
                                 + " loc="
                                 + this.fmtLoc(roofUp)
                                 + " distSq="
                                 + this.distSq(origin, roofUp)
                                 + ".",
                              1
                           );
                        return SafeLocationManager.GravePlacementResult.of(roofUp, SafeLocationManager.GravePlacementReason.NETHER_ROOF);
                     } else {
                        this.plugin
                           .debugMessage(
                              prefix + "CHOSEN=" + SafeLocationManager.GravePlacementReason.NETHER_ROOF + " loc=" + this.fmtLoc(origin) + " distSq=0.0.", 1
                           );
                        return SafeLocationManager.GravePlacementResult.of(origin, SafeLocationManager.GravePlacementReason.NETHER_ROOF);
                     }
                  }
               }
            } else {
               Location powderSnow = this.resolvePowderSnowPlacement(livingEntity, origin, grave);
               if (powderSnow != null) {
                  this.plugin
                     .debugMessage(
                        prefix
                           + "CHOSEN="
                           + SafeLocationManager.GravePlacementReason.POWDER_SNOW
                           + " loc="
                           + this.fmtLoc(powderSnow)
                           + " distSq="
                           + this.distSq(origin, powderSnow)
                           + ".",
                        1
                     );
                  return SafeLocationManager.GravePlacementResult.of(powderSnow, SafeLocationManager.GravePlacementReason.POWDER_SNOW);
               } else {
                  SafeLocationManager.Candidate nonFullSupport = this.resolveNonFullSupportPlacement(origin, grave);
                  if (nonFullSupport != null) {
                     this.plugin
                        .debugMessage(
                           prefix
                              + "CHOSEN="
                              + nonFullSupport.reason
                              + " loc="
                              + this.fmtLoc(nonFullSupport.loc)
                              + " distSq="
                              + this.distSq(origin, nonFullSupport.loc)
                              + ".",
                           1
                        );
                     return SafeLocationManager.GravePlacementResult.of(nonFullSupport.loc, nonFullSupport.reason);
                  } else {
                     if (!this.hasGrave(origin) && this.isLocationSafeGrave(origin)) {
                        Material t = origin.getBlock().getType();
                        if (!MaterialUtil.isWater(t) && !MaterialUtil.isLava(t)) {
                           this.plugin.debugMessage(prefix + "origin already safe+empty (non-fluid); using ORIGINAL_SAFE.", 1);
                           return SafeLocationManager.GravePlacementResult.of(origin, SafeLocationManager.GravePlacementReason.ORIGINAL_SAFE);
                        }
                     }

                     List<SafeLocationManager.Candidate> candidates = new ArrayList<>();
                     if (!this.isVoid(origin) && this.isInsideBorder(origin)) {
                        Material originType = origin.getBlock().getType();
                        Material belowType = origin.getBlock().getRelative(BlockFace.DOWN).getType();
                        boolean directlyAboveWater = MaterialUtil.isAir(originType) && MaterialUtil.isWater(belowType);
                        boolean directlyAboveLava = MaterialUtil.isAir(originType) && MaterialUtil.isLava(belowType);
                        boolean inWaterOrAbove = MaterialUtil.isWater(originType) || directlyAboveWater;
                        boolean inLavaOrAbove = MaterialUtil.isLava(originType) || directlyAboveLava;
                        boolean wallSuffocation = this.isPlacementEnabled("placement.suffocation", grave, true) && this.isWallSuffocation(origin);
                        if (wallSuffocation) {
                           Location lastSolid = this.resolveClearedLastSolid(livingEntity, origin, grave);
                           if (lastSolid != null) {
                              this.plugin
                                 .debugMessage(
                                    prefix
                                       + "CHOSEN="
                                       + SafeLocationManager.GravePlacementReason.SUFFOCATION
                                       + " loc="
                                       + this.fmtLoc(lastSolid)
                                       + " distSq="
                                       + this.distSq(origin, lastSolid)
                                       + ".",
                                    1
                                 );
                              return SafeLocationManager.GravePlacementResult.of(lastSolid, SafeLocationManager.GravePlacementReason.SUFFOCATION);
                           }

                           Location suffocation = this.resolveSuffocationLocation(origin, grave);
                           if (suffocation != null) {
                              this.plugin
                                 .debugMessage(
                                    prefix
                                       + "CHOSEN="
                                       + SafeLocationManager.GravePlacementReason.SUFFOCATION
                                       + " loc="
                                       + this.fmtLoc(suffocation)
                                       + " distSq="
                                       + this.distSq(origin, suffocation)
                                       + ".",
                                    1
                                 );
                              return SafeLocationManager.GravePlacementResult.of(suffocation, SafeLocationManager.GravePlacementReason.SUFFOCATION);
                           }

                           this.plugin.debugMessage(prefix + "wall suffocation detected but no above/below placement found; continuing.", 1);
                        } else if (this.isPlacementEnabled("placement.feet-blocked", grave, true) && this.isFeetBlocked(origin, grave)) {
                           Location feetBlocked = this.resolveBlockedPlacement(origin, grave, SafeLocationManager.GravePlacementReason.FEET_BLOCKED);
                           if (feetBlocked != null) {
                              this.plugin
                                 .debugMessage(
                                    prefix
                                       + "CHOSEN="
                                       + SafeLocationManager.GravePlacementReason.FEET_BLOCKED
                                       + " loc="
                                       + this.fmtLoc(feetBlocked)
                                       + " distSq="
                                       + this.distSq(origin, feetBlocked)
                                       + ".",
                                    1
                                 );
                              return SafeLocationManager.GravePlacementResult.of(feetBlocked, SafeLocationManager.GravePlacementReason.FEET_BLOCKED);
                           }
                        } else if (this.isPlacementEnabled("placement.head-blocked", grave, true) && this.isHeadBlocked(origin, grave)) {
                           Location headBlocked = this.resolveBlockedPlacement(origin, grave, SafeLocationManager.GravePlacementReason.HEAD_BLOCKED);
                           if (headBlocked != null) {
                              this.plugin
                                 .debugMessage(
                                    prefix
                                       + "CHOSEN="
                                       + SafeLocationManager.GravePlacementReason.HEAD_BLOCKED
                                       + " loc="
                                       + this.fmtLoc(headBlocked)
                                       + " distSq="
                                       + this.distSq(origin, headBlocked)
                                       + ".",
                                    1
                                 );
                              return SafeLocationManager.GravePlacementResult.of(headBlocked, SafeLocationManager.GravePlacementReason.HEAD_BLOCKED);
                           }
                        } else if (inWaterOrAbove) {
                           Location smart = this.resolveSmartFromLastSolid(livingEntity, origin, grave, useGround, useRoof);
                           if (smart != null) {
                              this.plugin
                                 .debugMessage(
                                    prefix
                                       + "CHOSEN="
                                       + SafeLocationManager.GravePlacementReason.WATER_SMART
                                       + " loc="
                                       + this.fmtLoc(smart)
                                       + " distSq="
                                       + this.distSq(origin, smart)
                                       + ".",
                                    1
                                 );
                              return SafeLocationManager.GravePlacementResult.of(smart, SafeLocationManager.GravePlacementReason.WATER_SMART);
                           }

                           if (directlyAboveWater) {
                              Location above = this.centerOnBlock(origin);
                              if (above != null
                                 && !this.isVoid(above)
                                 && this.isInsideBorder(above)
                                 && !this.hasGrave(above)
                                 && this.isLocationSafeGraveAboveFluid(above, true)) {
                                 this.plugin
                                    .debugMessage(
                                       prefix
                                          + "CHOSEN="
                                          + SafeLocationManager.GravePlacementReason.WATER_ABOVE
                                          + " loc="
                                          + this.fmtLoc(above)
                                          + " distSq="
                                          + this.distSq(origin, above)
                                          + ".",
                                       1
                                    );
                                 return SafeLocationManager.GravePlacementResult.of(above, SafeLocationManager.GravePlacementReason.WATER_ABOVE);
                              }
                           }

                           boolean waterTop = this.plugin.getConfigManager().getConfigSection("placement.water-top", grave).getBoolean("placement.water-top");
                           if (waterTop) {
                              Location start = MaterialUtil.isWater(originType) ? origin : origin.clone().add(0.0, -1.0, 0.0);
                              Location surface = this.resolveFluidSurfaceTop(start, grave, true);
                              if (surface != null) {
                                 this.plugin
                                    .debugMessage(
                                       prefix
                                          + "CHOSEN="
                                          + SafeLocationManager.GravePlacementReason.WATER_TOP
                                          + " loc="
                                          + this.fmtLoc(surface)
                                          + " distSq="
                                          + this.distSq(origin, surface)
                                          + ".",
                                       1
                                    );
                                 return SafeLocationManager.GravePlacementResult.of(surface, SafeLocationManager.GravePlacementReason.WATER_TOP);
                              }

                              this.plugin.debugMessage(prefix + "water-top enabled but no AIR surface found; continuing.", 1);
                           }

                           boolean waterBottom = this.plugin
                                 .getConfigManager()
                                 .getConfigSection("placement.water-bottom", grave)
                                 .contains("placement.water-bottom")
                              ? this.plugin.getConfigManager().getConfigSection("placement.water-bottom", grave).getBoolean("placement.water-bottom")
                              : useGround;
                           if (waterBottom) {
                              Location bottom = this.findWaterBottom(origin);
                              if (bottom != null && !this.hasGrave(bottom) && this.isLocationSafeGrave(bottom)) {
                                 this.plugin
                                    .debugMessage(
                                       prefix
                                          + "CHOSEN="
                                          + SafeLocationManager.GravePlacementReason.WATER_BOTTOM
                                          + " loc="
                                          + this.fmtLoc(bottom)
                                          + " distSq="
                                          + this.distSq(origin, bottom)
                                          + ".",
                                       1
                                    );
                                 return SafeLocationManager.GravePlacementResult.of(bottom, SafeLocationManager.GravePlacementReason.WATER_BOTTOM);
                              }
                           }

                           if (useRoof) {
                              Location roof = this.resolveRoofLocation(origin, origin, grave);
                              if (roof != null) {
                                 candidates.add(new SafeLocationManager.Candidate(roof, SafeLocationManager.GravePlacementReason.ROOF));
                              }
                           }

                           if (useGround) {
                              Location ground = this.resolveGroundLocation(origin, origin, grave);
                              if (ground != null) {
                                 candidates.add(new SafeLocationManager.Candidate(ground, SafeLocationManager.GravePlacementReason.GROUND));
                              }
                           }
                        } else if (inLavaOrAbove) {
                           Location smartx = this.resolveSmartFromLastSolid(livingEntity, origin, grave, useGround, useRoof);
                           if (smartx != null) {
                              this.plugin
                                 .debugMessage(
                                    prefix
                                       + "CHOSEN="
                                       + SafeLocationManager.GravePlacementReason.LAVA_SMART
                                       + " loc="
                                       + this.fmtLoc(smartx)
                                       + " distSq="
                                       + this.distSq(origin, smartx)
                                       + ".",
                                    1
                                 );
                              return SafeLocationManager.GravePlacementResult.of(smartx, SafeLocationManager.GravePlacementReason.LAVA_SMART);
                           }

                           boolean lavaTop = this.plugin.getConfigManager().getConfigSection("placement.lava-top", grave).getBoolean("placement.lava-top");
                           if (lavaTop) {
                              if (directlyAboveLava) {
                                 Location above = this.centerOnBlock(origin);
                                 if (above != null
                                    && !this.isVoid(above)
                                    && this.isInsideBorder(above)
                                    && !this.hasGrave(above)
                                    && this.isLocationSafeGraveAboveFluid(above, false)) {
                                    this.plugin
                                       .debugMessage(
                                          prefix
                                             + "CHOSEN="
                                             + SafeLocationManager.GravePlacementReason.LAVA_ABOVE
                                             + " loc="
                                             + this.fmtLoc(above)
                                             + " distSq="
                                             + this.distSq(origin, above)
                                             + ".",
                                          1
                                       );
                                    return SafeLocationManager.GravePlacementResult.of(above, SafeLocationManager.GravePlacementReason.LAVA_ABOVE);
                                 }
                              }

                              Location start = MaterialUtil.isLava(originType) ? origin : origin.clone().add(0.0, -1.0, 0.0);
                              Location surface = this.resolveFluidSurfaceTop(start, grave, false);
                              if (surface != null) {
                                 this.plugin
                                    .debugMessage(
                                       prefix
                                          + "CHOSEN="
                                          + SafeLocationManager.GravePlacementReason.LAVA_TOP
                                          + " loc="
                                          + this.fmtLoc(surface)
                                          + " distSq="
                                          + this.distSq(origin, surface)
                                          + ".",
                                       1
                                    );
                                 return SafeLocationManager.GravePlacementResult.of(surface, SafeLocationManager.GravePlacementReason.LAVA_TOP);
                              }

                              this.plugin.debugMessage(prefix + "lava-top enabled but no AIR surface found; continuing.", 1);
                           }

                           if (useRoof) {
                              Location roof = this.resolveRoofLocation(origin, origin, grave);
                              if (roof != null) {
                                 candidates.add(new SafeLocationManager.Candidate(roof, SafeLocationManager.GravePlacementReason.ROOF));
                              }
                           }

                           if (useGround) {
                              Location ground = this.resolveGroundLocation(origin, origin, grave);
                              if (ground != null) {
                                 candidates.add(new SafeLocationManager.Candidate(ground, SafeLocationManager.GravePlacementReason.GROUND));
                              }
                           }
                        } else {
                           if (useRoof) {
                              Location roof = this.resolveRoofLocation(origin, origin, grave);
                              if (roof != null) {
                                 candidates.add(new SafeLocationManager.Candidate(roof, SafeLocationManager.GravePlacementReason.ROOF));
                              }
                           }

                           if (useGround) {
                              Location ground = this.resolveGroundLocation(origin, origin, grave);
                              if (ground != null) {
                                 candidates.add(new SafeLocationManager.Candidate(ground, SafeLocationManager.GravePlacementReason.GROUND));
                              }
                           }
                        }

                        this.debugCandidates(prefix, origin, candidates);
                        SafeLocationManager.Candidate best = this.pickClosest(origin, candidates);
                        if (best != null) {
                           this.plugin
                              .debugMessage(
                                 prefix + "CHOSEN=" + best.reason + " loc=" + this.fmtLoc(best.loc) + " distSq=" + this.distSq(origin, best.loc) + ".", 1
                              );
                           return SafeLocationManager.GravePlacementResult.of(best.loc, best.reason);
                        } else {
                           this.plugin.debugMessage(prefix + "all candidates null; falling back to original.", 1);
                           return SafeLocationManager.GravePlacementResult.of(origin, SafeLocationManager.GravePlacementReason.FALLBACK_ORIGINAL);
                        }
                     } else {
                        this.plugin.debugMessage(prefix + "origin is void/outside border; computing void candidates only.", 1);
                        this.addVoidCandidates(candidates, origin, grave);
                        this.debugCandidates(prefix, origin, candidates);
                        SafeLocationManager.Candidate best = this.pickClosest(origin, candidates);
                        if (best != null) {
                           this.plugin
                              .debugMessage(
                                 prefix + "CHOSEN=" + best.reason + " loc=" + this.fmtLoc(best.loc) + " distSq=" + this.distSq(origin, best.loc) + ".", 1
                              );
                           return SafeLocationManager.GravePlacementResult.of(best.loc, best.reason);
                        } else {
                           this.plugin.debugMessage(prefix + "no void candidates; falling back to original.", 1);
                           return SafeLocationManager.GravePlacementResult.of(origin, SafeLocationManager.GravePlacementReason.FALLBACK_ORIGINAL);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private void debugCandidates(String prefix, Location origin, List<SafeLocationManager.Candidate> candidates) {
      if (origin != null && origin.getWorld() != null) {
         if (candidates != null && !candidates.isEmpty()) {
            this.plugin.debugMessage(prefix + "candidates:", 1);

            for (SafeLocationManager.Candidate c : candidates) {
               if (c != null && c.loc != null && c.loc.getWorld() != null && Objects.equals(c.loc.getWorld(), origin.getWorld())) {
                  this.plugin.debugMessage(prefix + " - " + c.reason + " loc=" + this.fmtLoc(c.loc) + " distSq=" + this.distSq(origin, c.loc), 1);
               }
            }
         } else {
            this.plugin.debugMessage(prefix + "candidates: (none)", 1);
         }
      }
   }

   private Location centerOnBlock(Location loc) {
      if (loc != null && loc.getWorld() != null) {
         Location rounded = LocationUtil.roundLocation(loc);
         return rounded != null && rounded.getWorld() != null
            ? new Location(rounded.getWorld(), rounded.getBlockX() + 0.5, rounded.getBlockY(), rounded.getBlockZ() + 0.5, loc.getYaw(), loc.getPitch())
            : null;
      } else {
         return null;
      }
   }

   private void addWaterCandidates(
      List<SafeLocationManager.Candidate> out, Location origin, LivingEntity livingEntity, Grave grave, boolean useGround, boolean useRoof
   ) {
      boolean waterSmart = this.plugin.getConfigManager().getConfigSection("placement.water-smart", grave).getBoolean("placement.water-smart");
      boolean waterBottom = this.plugin.getConfigManager().getConfigSection("placement.water-bottom", grave).contains("placement.water-bottom")
         ? this.plugin.getConfigManager().getConfigSection("placement.water-bottom", grave).getBoolean("placement.water-bottom")
         : useGround;
      if (waterSmart) {
         Location smart = this.resolveSmartFromLastSolid(livingEntity, origin, grave, useGround, useRoof);
         if (smart != null) {
            out.add(new SafeLocationManager.Candidate(smart, SafeLocationManager.GravePlacementReason.WATER_SMART));
         }
      }

      if (waterBottom) {
         Location bottom = this.findWaterBottom(origin);
         if (bottom != null && !this.hasGrave(bottom) && this.isLocationSafeGrave(bottom)) {
            out.add(new SafeLocationManager.Candidate(bottom, SafeLocationManager.GravePlacementReason.WATER_BOTTOM));
         }
      }
   }

   private void addLavaCandidates(
      List<SafeLocationManager.Candidate> out, Location origin, LivingEntity livingEntity, Grave grave, boolean useGround, boolean useRoof
   ) {
      boolean lavaSmart = this.plugin.getConfigManager().getConfigSection("placement.lava-smart", grave).getBoolean("placement.lava-smart");
      if (lavaSmart) {
         Location smart = this.resolveSmartFromLastSolid(livingEntity, origin, grave, useGround, useRoof);
         if (smart != null) {
            out.add(new SafeLocationManager.Candidate(smart, SafeLocationManager.GravePlacementReason.LAVA_SMART));
         }
      }

      if (useRoof) {
         Location roof = this.resolveRoofLocation(origin, origin, grave);
         if (roof != null) {
            out.add(new SafeLocationManager.Candidate(roof, SafeLocationManager.GravePlacementReason.ROOF));
         }
      }

      if (useGround) {
         Location ground = this.resolveGroundLocation(origin, origin, grave);
         if (ground != null) {
            out.add(new SafeLocationManager.Candidate(ground, SafeLocationManager.GravePlacementReason.GROUND));
         }
      }
   }

   private void addVoidCandidates(List<SafeLocationManager.Candidate> out, Location origin, Grave grave) {
      if (origin != null && origin.getWorld() != null) {
         if (this.plugin.getConfigManager().getConfigSection("placement.void", grave).getBoolean("placement.void")) {
            World world = origin.getWorld();
            int x = origin.getBlockX();
            int z = origin.getBlockZ();
            int minY = world.getMinHeight();
            int maxY = world.getMaxHeight() - 1;
            SafeLocationManager.Candidate best = null;
            double bestDist = Double.MAX_VALUE;

            for (int y = minY; y <= maxY; y++) {
               Location c = new Location(world, x + 0.5, y, z + 0.5, origin.getYaw(), origin.getPitch());
               if (!this.hasGrave(c) && this.isLocationSafeGrave(c)) {
                  double d = this.distSq(origin, c);
                  if (d < bestDist) {
                     bestDist = d;
                     best = new SafeLocationManager.Candidate(
                        c, y == minY ? SafeLocationManager.GravePlacementReason.VOID_MIN_HEIGHT : SafeLocationManager.GravePlacementReason.VOID_COLUMN
                     );
                  }
               }
            }

            if (best != null) {
               out.add(best);
            }
         }
      }
   }

   private SafeLocationManager.Candidate pickClosest(Location origin, List<SafeLocationManager.Candidate> candidates) {
      if (origin != null && origin.getWorld() != null) {
         if (candidates != null && !candidates.isEmpty()) {
            SafeLocationManager.Candidate best = null;
            double bestDist = Double.MAX_VALUE;

            for (SafeLocationManager.Candidate c : candidates) {
               if (c != null && c.loc != null && c.loc.getWorld() != null && Objects.equals(c.loc.getWorld(), origin.getWorld())) {
                  double d = this.distSq(origin, c.loc);
                  if (d < bestDist) {
                     bestDist = d;
                     best = c;
                  }
               }
            }

            return best;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   private double distSq(Location a, Location b) {
      double dx = a.getX() - b.getX();
      double dy = a.getY() - b.getY();
      double dz = a.getZ() - b.getZ();
      return dx * dx + dy * dy + dz * dz;
   }

   private Location resolvePowderSnowPlacement(LivingEntity livingEntity, Location origin, Grave grave) {
      if (origin == null || origin.getWorld() == null) {
         return null;
      } else if (!this.isPlacementEnabled("placement.powder-snow", grave, true)) {
         return null;
      } else {
         Block block = origin.getBlock();
         Block below = block.getRelative(BlockFace.DOWN);
         if (!this.isPowderSnow(block.getType(), grave) && !this.isPowderSnow(below.getType(), grave)) {
            return null;
         } else {
            Location lastSolid = this.resolveClearedLastSolid(livingEntity, origin, grave);
            if (lastSolid != null) {
               return lastSolid;
            } else {
               Block start = this.isPowderSnow(block.getType(), grave) ? block : below;
               return this.resolvePowderSnowSurfaceTop(start.getLocation(), grave);
            }
         }
      }
   }

   private Location resolvePowderSnowSurfaceTop(Location originInPowderSnow, Grave grave) {
      if (originInPowderSnow != null && originInPowderSnow.getWorld() != null) {
         World world = originInPowderSnow.getWorld();
         int x = originInPowderSnow.getBlockX();
         int z = originInPowderSnow.getBlockZ();
         int minY = world.getMinHeight();
         int maxY = world.getMaxHeight() - 1;
         if (this.isNether(world) && !this.isAboveNetherRoofInternal(originInPowderSnow, grave)) {
            maxY = Math.min(maxY, this.getNetherRoofYInternal(world, grave));
         }

         int y = Math.max(originInPowderSnow.getBlockY(), minY);
         if (!this.isPowderSnow(world.getBlockAt(x, y, z).getType(), grave)) {
            return null;
         } else {
            while (y <= maxY && this.isPowderSnow(world.getBlockAt(x, y, z).getType(), grave)) {
               y++;
            }

            if (y >= minY && y <= maxY) {
               Location surface = new Location(world, x + 0.5, y, z + 0.5, originInPowderSnow.getYaw(), originInPowderSnow.getPitch());
               return this.isClearPlacementAboveSpecialBlock(surface) ? surface : null;
            } else {
               return null;
            }
         }
      } else {
         return null;
      }
   }

   private SafeLocationManager.Candidate resolveNonFullSupportPlacement(Location origin, Grave grave) {
      if (origin != null && origin.getWorld() != null) {
         Block block = origin.getBlock();
         Block below = block.getRelative(BlockFace.DOWN);
         if (this.isPlacementEnabled("placement.replace-avoided", grave, true) && this.isReplacementAvoidedBlock(block.getType(), grave)) {
            Location above = this.centerOnBlock(origin.clone().add(0.0, 1.0, 0.0));
            if (this.isClearPlacementAboveSpecialBlock(above)) {
               return new SafeLocationManager.Candidate(above, SafeLocationManager.GravePlacementReason.REPLACE_AVOIDED);
            }
         }

         if (this.isPlacementEnabled("placement.non-full-support", grave, true) && this.isNonFullSupport(below.getType(), grave)) {
            Location aboveSupport = this.centerOnBlock(origin);
            if (this.isClearPlacementAboveSpecialBlock(aboveSupport)) {
               return new SafeLocationManager.Candidate(aboveSupport, SafeLocationManager.GravePlacementReason.NON_FULL_SUPPORT);
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private boolean isClearPlacementAboveSpecialBlock(Location location) {
      if (location != null && location.getWorld() != null) {
         Location rounded = LocationUtil.roundLocation(location);
         return rounded != null && rounded.getWorld() != null
            ? !this.isVoid(rounded) && this.isInsideBorder(rounded) && !this.hasGrave(rounded) && MaterialUtil.isSafeNotSolid(rounded.getBlock().getType())
            : false;
      } else {
         return false;
      }
   }

   private boolean isFeetBlocked(Location location, Grave grave) {
      if (location != null && location.getWorld() != null) {
         Material type = location.getBlock().getType();
         return type.isSolid() && !MaterialUtil.isWater(type) && !MaterialUtil.isLava(type) && !this.isPowderSnow(type, grave);
      } else {
         return false;
      }
   }

   private boolean isHeadBlocked(Location location, Grave grave) {
      if (location != null && location.getWorld() != null) {
         Block block = location.getBlock();
         Material feet = block.getType();
         Material head = block.getRelative(BlockFace.UP).getType();
         return MaterialUtil.isSafeNotSolid(feet)
            && !MaterialUtil.isWater(feet)
            && !MaterialUtil.isLava(feet)
            && !this.isPowderSnow(feet, grave)
            && head.isSolid()
            && !MaterialUtil.isLava(head);
      } else {
         return false;
      }
   }

   private Location resolveBlockedPlacement(Location origin, Grave grave, SafeLocationManager.GravePlacementReason reason) {
      if (origin != null && origin.getWorld() != null) {
         List<SafeLocationManager.Candidate> candidates = new ArrayList<>();
         Location above = this.searchUpForSafeGrave(origin, origin, grave, origin.getBlockY() + 1);
         if (above != null) {
            candidates.add(new SafeLocationManager.Candidate(above, reason));
         }

         Location below = this.searchDownForSafeGrave(origin, grave, origin.getBlockY() - 1);
         if (below != null) {
            candidates.add(new SafeLocationManager.Candidate(below, reason));
         }

         SafeLocationManager.Candidate best = this.pickClosest(origin, candidates);
         return best != null ? best.loc : null;
      } else {
         return null;
      }
   }

   private boolean isPowderSnow(Material material, Grave grave) {
      return this.materialMatchesConfiguredList(material, "placement.powder-snow-blocks", grave, DEFAULT_POWDER_SNOW_BLOCKS);
   }

   private boolean isReplacementAvoidedBlock(Material material, Grave grave) {
      return this.materialMatchesConfiguredList(material, "placement.replace-avoided-blocks", grave, DEFAULT_REPLACE_AVOIDED_BLOCKS);
   }

   private boolean isNonFullSupport(Material material, Grave grave) {
      return this.materialMatchesConfiguredList(material, "placement.non-full-support-blocks", grave, DEFAULT_NON_FULL_SUPPORT_BLOCKS);
   }

   private boolean isPlacementEnabled(String path, Grave grave, boolean defaultValue) {
      ConfigurationSection section = this.plugin.getConfigManager().getConfigSection(path, grave);
      return section.contains(path) ? section.getBoolean(path) : defaultValue;
   }

   private boolean materialMatchesConfiguredList(Material material, String path, Grave grave, List<String> defaultNames) {
      if (material == null) {
         return false;
      } else {
         ConfigurationSection section = this.plugin.getConfigManager().getConfigSection(path, grave);
         List<String> names = section.contains(path) ? section.getStringList(path) : defaultNames;
         if (names == null || names.isEmpty()) {
            names = defaultNames;
         }

         String materialName = material.name();

         for (String raw : names) {
            if (this.matchesMaterialPattern(materialName, raw)) {
               return true;
            }
         }

         return false;
      }
   }

   private boolean matchesMaterialPattern(String materialName, String rawPattern) {
      if (materialName != null && rawPattern != null) {
         String pattern = rawPattern.trim().toUpperCase();
         if (pattern.isEmpty()) {
            return false;
         } else if ("*".equals(pattern)) {
            return true;
         } else {
            int star = pattern.indexOf(42);
            if (star < 0) {
               return materialName.equals(pattern);
            } else {
               String prefix = pattern.substring(0, star);
               String suffix = pattern.substring(star + 1);
               return materialName.startsWith(prefix) && materialName.endsWith(suffix);
            }
         }
      } else {
         return false;
      }
   }

   private boolean isWallSuffocation(Location location) {
      if (location != null && location.getWorld() != null) {
         Block block = location.getBlock();
         return block.getType().isSolid() && block.getRelative(BlockFace.UP).getType().isSolid();
      } else {
         return false;
      }
   }

   private Location resolveClearedLastSolid(LivingEntity livingEntity, Location deathLocation, Grave grave) {
      if (!(livingEntity instanceof Player player)) {
         return null;
      } else {
         Location lastSolid = this.getLastSolidLocation(player);
         if (lastSolid != null && lastSolid.getWorld() != null) {
            if (deathLocation != null
               && deathLocation.getWorld() != null
               && deathLocation.getWorld().equals(lastSolid.getWorld())
               && this.isNether(lastSolid.getWorld())
               && !this.isAboveNetherRoofInternal(deathLocation, grave)
               && lastSolid.getBlockY() > this.getNetherRoofYInternal(lastSolid.getWorld(), grave)) {
               return null;
            } else {
               Location base = LocationUtil.roundLocation(lastSolid);
               if (base == null || base.getWorld() == null) {
                  return null;
               } else if (!this.isVoid(base) && this.isInsideBorder(base)) {
                  return !this.hasGrave(base) && this.isLocationSafeGrave(base) ? base : null;
               } else {
                  return null;
               }
            }
         } else {
            return null;
         }
      }
   }

   private Location resolveSuffocationLocation(Location origin, Grave grave) {
      if (origin != null && origin.getWorld() != null) {
         List<SafeLocationManager.Candidate> candidates = new ArrayList<>();
         Location above = this.searchUpForSafeGrave(origin, origin, grave, origin.getBlockY() + 2);
         if (above != null) {
            candidates.add(new SafeLocationManager.Candidate(above, SafeLocationManager.GravePlacementReason.SUFFOCATION));
         }

         Location below = this.searchDownForSafeGrave(origin, grave, origin.getBlockY() - 1);
         if (below != null) {
            candidates.add(new SafeLocationManager.Candidate(below, SafeLocationManager.GravePlacementReason.SUFFOCATION));
         }

         SafeLocationManager.Candidate best = this.pickClosest(origin, candidates);
         return best != null ? best.loc : null;
      } else {
         return null;
      }
   }

   private Location resolveSmartFromLastSolid(LivingEntity livingEntity, Location deathLocation, Grave grave, boolean useGround, boolean useRoof) {
      if (livingEntity instanceof Player player) {
         Location lastSolid = this.getLastSolidLocation(player);
         if (lastSolid != null && lastSolid.getWorld() != null) {
            if (deathLocation != null
               && deathLocation.getWorld() != null
               && deathLocation.getWorld().equals(lastSolid.getWorld())
               && this.isNether(lastSolid.getWorld())
               && !this.isAboveNetherRoofInternal(deathLocation, grave)
               && lastSolid.getBlockY() > this.getNetherRoofYInternal(lastSolid.getWorld(), grave)) {
               return null;
            } else {
               Location base = LocationUtil.roundLocation(lastSolid);
               if (base == null || base.getWorld() == null) {
                  return null;
               } else if (this.isVoid(base) || !this.isInsideBorder(base)) {
                  return null;
               } else if (!this.hasGrave(base) && this.isLocationSafeGrave(base)) {
                  return base;
               } else {
                  if (useRoof) {
                     Location roof = this.resolveRoofLocation(base, deathLocation != null ? deathLocation : base, grave);
                     if (roof != null && !this.hasGrave(roof) && this.isLocationSafeGrave(roof)) {
                        return roof;
                     }
                  }

                  if (useGround) {
                     Location ground = this.resolveGroundLocation(base, deathLocation != null ? deathLocation : base, grave);
                     if (ground != null && !this.hasGrave(ground) && this.isLocationSafeGrave(ground)) {
                        return ground;
                     }
                  }

                  return null;
               }
            }
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   private Location resolveGroundLocation(Location base, Location origin, Grave grave) {
      if (base != null && base.getWorld() != null) {
         Location down = this.searchDownForSafeGrave(base, grave, base.getBlockY());
         return down != null ? down : this.searchUpForSafeGrave(base, origin, grave, base.getBlockY() + 1);
      } else {
         return null;
      }
   }

   private Location resolveRoofLocation(Location base, Location origin, Grave grave) {
      if (base != null && base.getWorld() != null) {
         Location up = this.searchUpForSafeGrave(base, origin, grave, base.getBlockY() + 1);
         return up != null ? up : this.searchDownForSafeGrave(base, grave, base.getBlockY());
      } else {
         return null;
      }
   }

   private Location searchDownForSafeGrave(Location base, Grave grave, int startY) {
      if (base != null && base.getWorld() != null) {
         World world = base.getWorld();
         int x = base.getBlockX();
         int z = base.getBlockZ();
         int minY = world.getMinHeight();

         for (int y = startY; y >= minY; y--) {
            Location candidate = new Location(world, x + 0.5, y, z + 0.5, base.getYaw(), base.getPitch());
            Block block = candidate.getBlock();
            Material type = block.getType();
            if (MaterialUtil.isWater(type)) {
               Location surface = this.resolveFluidSurfaceTop(candidate, grave, true);
               if (surface != null) {
                  return surface;
               }
            } else if (MaterialUtil.isLava(type)) {
               Location surface = this.resolveFluidSurfaceTop(candidate, grave, false);
               if (surface != null) {
                  return surface;
               }
            }

            if (!this.hasGrave(candidate) && this.isLocationSafeGrave(candidate)) {
               return candidate;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private Location searchUpForSafeGrave(Location base, Location origin, Grave grave, int startY) {
      if (base != null && base.getWorld() != null) {
         World world = base.getWorld();
         int x = base.getBlockX();
         int z = base.getBlockZ();
         int maxY = world.getMaxHeight() - 1;
         if (this.isNether(world) && origin != null && !this.isAboveNetherRoofInternal(origin, grave)) {
            maxY = Math.min(maxY, this.getNetherRoofYInternal(world, grave));
         }

         int y = startY;

         while (y <= maxY) {
            Location candidate = new Location(world, x + 0.5, y, z + 0.5, base.getYaw(), base.getPitch());
            Block block = candidate.getBlock();
            if (MaterialUtil.isLava(block.getType())) {
               y++;
            } else {
               if (!this.hasGrave(candidate) && this.isLocationSafeGrave(candidate)) {
                  return candidate;
               }

               y++;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private Location resolveFluidSurfaceTop(Location originInFluid, Grave grave, boolean water) {
      if (originInFluid != null && originInFluid.getWorld() != null) {
         World world = originInFluid.getWorld();
         int x = originInFluid.getBlockX();
         int z = originInFluid.getBlockZ();
         int minY = world.getMinHeight();
         int maxY = world.getMaxHeight() - 1;
         if (this.isNether(world) && !this.isAboveNetherRoofInternal(originInFluid, grave)) {
            maxY = Math.min(maxY, this.getNetherRoofYInternal(world, grave));
         }

         int y = Math.max(originInFluid.getBlockY(), minY);
         Material start = world.getBlockAt(x, y, z).getType();
         if (water ? MaterialUtil.isWater(start) : MaterialUtil.isLava(start)) {
            while (y <= maxY) {
               Material t = world.getBlockAt(x, y, z).getType();
               if (water ? !MaterialUtil.isWater(t) : !MaterialUtil.isLava(t)) {
                  break;
               }

               y++;
            }

            if (y >= minY && y <= maxY) {
               Block candidateBlock = world.getBlockAt(x, y, z);
               if (!MaterialUtil.isAir(candidateBlock.getType())) {
                  return null;
               } else {
                  Location place = new Location(world, x + 0.5, y, z + 0.5, originInFluid.getYaw(), originInFluid.getPitch());
                  if (this.isVoid(place) || !this.isInsideBorder(place)) {
                     return null;
                  } else if (this.hasGrave(place)) {
                     return null;
                  } else {
                     return !this.isLocationSafeGraveAboveFluid(place, water) ? null : place;
                  }
               }
            } else {
               return null;
            }
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   private boolean isLocationSafeGraveAboveFluid(Location location, boolean water) {
      if (location == null) {
         return false;
      } else {
         Location rounded = LocationUtil.roundLocation(location);
         if (rounded != null && rounded.getWorld() != null) {
            Block block = rounded.getBlock();
            Block below = block.getRelative(BlockFace.DOWN);
            return this.isInsideBorder(rounded)
               && MaterialUtil.isSafeNotSolid(block.getType())
               && (water ? MaterialUtil.isWater(below.getType()) : MaterialUtil.isLava(below.getType()));
         } else {
            return false;
         }
      }
   }

   private Location findWaterBottom(Location loc) {
      if (loc != null && loc.getWorld() != null) {
         World world = loc.getWorld();
         int x = loc.getBlockX();
         int z = loc.getBlockZ();
         int minY = world.getMinHeight();
         int maxY = world.getMaxHeight() - 1;
         Location top = null;

         for (int y = loc.getBlockY() - 1; y >= minY; y--) {
            Block block = world.getBlockAt(x, y, z);
            Material type = block.getType();
            if (!type.isAir() && MaterialUtil.isWater(type)) {
               top = block.getLocation();
               break;
            }
         }

         if (top == null) {
            for (int yx = loc.getBlockY() + 1; yx <= maxY; yx++) {
               Block block = world.getBlockAt(x, yx, z);
               Material type = block.getType();
               if (!type.isAir() && MaterialUtil.isWater(type)) {
                  top = block.getLocation();
                  break;
               }
            }
         }

         if (top == null) {
            return null;
         } else {
            Location bottom = top;

            for (int yxx = top.getBlockY() - 1; yxx >= minY; yxx--) {
               Block block = world.getBlockAt(x, yxx, z);
               if (!MaterialUtil.isWater(block.getType())) {
                  break;
               }

               bottom = block.getLocation();
            }

            return bottom;
         }
      } else {
         return null;
      }
   }

   public boolean canBuild(LivingEntity livingEntity, Location location, List<String> permissionList) {
      return !(livingEntity instanceof Player player)
         ? true
         : (
               !this.plugin.getConfigManager().getConfigSection("placement.can-build", player, permissionList).getBoolean("placement.can-build")
                  || this.plugin.getCompatibility().canBuild(player, location, this.plugin)
            )
            && (
               !this.plugin.getIntegrationManager().hasProtectionLib()
                  || !this.plugin
                     .getConfigManager()
                     .getConfigSection("placement.can-build-protectionlib", player, permissionList)
                     .getBoolean("placement.can-build-protectionlib")
                  || this.plugin.getIntegrationManager().getProtectionLib().canBuild(location, player)
            );
   }

   public boolean isLocationSafePlayer(Location location) {
      if (location != null && location.getWorld() != null) {
         Block block = location.getBlock();
         if (this.isInsideBorder(location) && !block.getType().isSolid() && !MaterialUtil.isLava(block.getType())) {
            Block blockAbove = block.getRelative(BlockFace.UP);
            Block blockBelow = block.getRelative(BlockFace.DOWN);
            return !block.getType().isSolid()
               && !MaterialUtil.isLava(blockAbove.getType())
               && !MaterialUtil.isAir(blockBelow.getType())
               && !MaterialUtil.isLava(blockBelow.getType());
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public boolean isLocationSafeGrave(Location location) {
      if (location == null) {
         return false;
      } else {
         Location rounded = LocationUtil.roundLocation(location);
         if (rounded == null) {
            return false;
         } else {
            Block block = rounded.getBlock();
            return this.isInsideBorder(rounded)
               && MaterialUtil.isSafeNotSolid(block.getType())
               && MaterialUtil.isSafeSolid(block.getRelative(BlockFace.DOWN).getType());
         }
      }
   }

   public boolean hasGrave(Location location) {
      if (location == null) {
         return false;
      } else {
         Location rounded = LocationUtil.roundLocation(location);
         return rounded == null
            ? false
            : this.plugin.getDataManager().hasChunkData(rounded) && this.plugin.getDataManager().getChunkData(rounded).getBlockDataMap().containsKey(rounded);
      }
   }

   public boolean isInsideBorder(Location location) {
      return location == null
         ? false
         : this.plugin.getVersionManager().is_v1_7()
            || this.plugin.getVersionManager().is_v1_8()
            || this.plugin.getVersionManager().is_v1_9()
            || this.plugin.getVersionManager().is_v1_10()
            || this.plugin.getVersionManager().is_v1_11()
            || location.getWorld() != null && location.getWorld().getWorldBorder().isInside(location);
   }

   public boolean isVoid(Location location) {
      return location != null && location.getWorld() != null
         ? location.getY() < this.getMinHeight(location) || location.getY() > location.getWorld().getMaxHeight()
         : true;
   }

   public int getMinHeight(Location location) {
      return location != null && location.getWorld() != null && this.plugin.getVersionManager().hasMinHeight() ? location.getWorld().getMinHeight() : 0;
   }

   private boolean isNether(World world) {
      return world != null && world.getEnvironment() == Environment.NETHER;
   }

   private boolean isAboveNetherRoofInternal(Location loc, Grave grave) {
      if (loc != null && loc.getWorld() != null) {
         World w = loc.getWorld();
         return this.isNether(w) && loc.getBlockY() > this.getNetherRoofYInternal(w, grave);
      } else {
         return false;
      }
   }

   private int getNetherRoofYInternal(World world, Grave grave) {
      if (world == null) {
         return 0;
      } else {
         int minY = world.getMinHeight();
         int maxY = world.getMaxHeight() - 1;
         List<String> raw = this.plugin.getConfigManager().getConfigSection("placement.nether-roof-limit", grave).getStringList("placement.nether-roof-limit");
         Integer defaultLimit = null;
         Integer worldLimit = null;
         String wName = world.getName();

         for (String s : raw) {
            if (s != null) {
               String v = s.trim();
               if (!v.isEmpty()) {
                  int idx = v.indexOf(58);
                  if (idx > 0 && idx < v.length() - 1) {
                     if (worldLimit == null) {
                        String name = v.substring(0, idx).trim();
                        String num = v.substring(idx + 1).trim();
                        Integer parsed = this.parseIntOrNull(num);
                        if (parsed != null && name.equalsIgnoreCase(wName)) {
                           worldLimit = parsed;
                        }
                     }
                  } else if (defaultLimit == null) {
                     Integer parsed = this.parseIntOrNull(v);
                     if (parsed != null) {
                        defaultLimit = parsed;
                     }
                  }
               }
            }
         }

         int roof = worldLimit != null ? worldLimit : (defaultLimit != null ? defaultLimit : maxY);
         if (roof < minY) {
            roof = minY;
         }

         if (roof > maxY) {
            roof = maxY;
         }

         return roof;
      }
   }

   private Integer parseIntOrNull(String s) {
      if (s == null) {
         return null;
      } else {
         try {
            return Integer.parseInt(s.trim());
         } catch (NumberFormatException var3) {
            return null;
         }
      }
   }

   private String fmtLoc(Location loc) {
      return loc != null && loc.getWorld() != null
         ? loc.getWorld().getName() + " x=" + loc.getBlockX() + " y=" + loc.getBlockY() + " z=" + loc.getBlockZ()
         : "null";
   }

   private static final class Candidate {
      final Location loc;
      final SafeLocationManager.GravePlacementReason reason;

      private Candidate(Location loc, SafeLocationManager.GravePlacementReason reason) {
         this.loc = loc;
         this.reason = reason;
      }
   }

   public static enum GravePlacementReason {
      ORIGINAL_SAFE,
      ROOF,
      GROUND,
      WATER_SMART,
      WATER_ABOVE,
      WATER_TOP,
      WATER_BOTTOM,
      LAVA_SMART,
      LAVA_ABOVE,
      LAVA_TOP,
      SUFFOCATION,
      POWDER_SNOW,
      NON_FULL_SUPPORT,
      FEET_BLOCKED,
      HEAD_BLOCKED,
      REPLACE_AVOIDED,
      ABOVE_BUILD_LIMIT,
      NETHER_ROOF,
      VOID_COLUMN,
      VOID_MIN_HEIGHT,
      FALLBACK_ORIGINAL;
   }

   public static class GravePlacementResult {
      private final Location location;
      private final SafeLocationManager.GravePlacementReason reason;

      private GravePlacementResult(Location location, SafeLocationManager.GravePlacementReason reason) {
         this.location = location;
         this.reason = reason;
      }

      public Location getLocation() {
         return this.location;
      }

      public SafeLocationManager.GravePlacementReason getReason() {
         return this.reason;
      }

      public static SafeLocationManager.GravePlacementResult of(Location location, SafeLocationManager.GravePlacementReason reason) {
         return new SafeLocationManager.GravePlacementResult(location, reason);
      }
   }
}
