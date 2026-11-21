package com.ranull.graves.manager;

import com.ranull.graves.Graves;
import com.ranull.graves.type.Grave;
import com.ranull.graves.util.LocationUtil;
import dev.cwhead.GravesX.manager.SafeLocationManager;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public final class LocationManager {
   private final Graves plugin;

   public LocationManager(Graves plugin) {
      this.plugin = plugin;
   }

   private SafeLocationManager safe() {
      return this.plugin.getSafeLocationManager();
   }

   public void setLastSolidLocation(Entity entity, Location location) {
      this.safe().setLastSolidLocation(entity, location);
   }

   public Location getLastSolidLocation(Entity entity) {
      return this.safe().getLastSolidLocation(entity);
   }

   public void removeLastSolidLocation(Entity entity) {
      this.safe().removeLastSolidLocation(entity);
   }

   public Location getSafeTeleportLocation(Entity entity, Location location, Grave grave, Graves plugin) {
      return this.safe().getSafeTeleportLocation(entity, location, grave, plugin);
   }

   public Location getSafeGraveLocation(LivingEntity livingEntity, Location location, Grave grave) {
      return this.safe().getSafeGraveLocation(livingEntity, location, grave);
   }

   public boolean isLocationSafePlayer(Location location) {
      return this.safe().isLocationSafePlayer(location);
   }

   public boolean isLocationSafeGrave(Location location) {
      return this.safe().isLocationSafeGrave(location);
   }

   public boolean hasGrave(Location location) {
      return this.safe().hasGrave(location);
   }

   public boolean isInsideBorder(Location location) {
      return this.safe().isInsideBorder(location);
   }

   public boolean isVoid(Location location) {
      return this.safe().isVoid(location);
   }

   public int getMinHeight(Location location) {
      return this.safe().getMinHeight(location);
   }

   public Location getNewLocationIfCachedGraveExists(LivingEntity livingEntity, Location location, Grave grave) {
      String prefix = "[LocationManager] getNewLocationIfCachedGraveExists: ";
      if (location == null) {
         this.plugin.debugMessage(prefix + "input location is null.", 1);
         return null;
      } else {
         Location rounded = LocationUtil.roundLocation(location);
         if (rounded != null && rounded.getWorld() != null) {
            if (!this.hasCachedGraveAt(rounded)) {
               this.plugin.debugMessage(prefix + "no cached grave at " + this.fmtLoc(rounded) + ", using original.", 1);
               return rounded;
            } else {
               this.plugin.debugMessage(prefix + "cached grave exists at " + this.fmtLoc(rounded) + ", searching nearby.", 1);
               World world = rounded.getWorld();
               int bx = rounded.getBlockX();
               int by = rounded.getBlockY();
               int bz = rounded.getBlockZ();
               int[] dyOrder = new int[]{0, 1, -1};

               for (int dy : dyOrder) {
                  int y = by + dy;
                  Location north = new Location(world, bx, y, bz - 1, rounded.getYaw(), rounded.getPitch());
                  if (this.isLocationSafeGraveAndPlayer(north) && !this.hasCachedGraveAt(north)) {
                     return LocationUtil.roundLocation(north);
                  }

                  Location south = new Location(world, bx, y, bz + 1, rounded.getYaw(), rounded.getPitch());
                  if (this.isLocationSafeGraveAndPlayer(south) && !this.hasCachedGraveAt(south)) {
                     return LocationUtil.roundLocation(south);
                  }

                  Location west = new Location(world, bx - 1, y, bz, rounded.getYaw(), rounded.getPitch());
                  if (this.isLocationSafeGraveAndPlayer(west) && !this.hasCachedGraveAt(west)) {
                     return LocationUtil.roundLocation(west);
                  }

                  Location east = new Location(world, bx + 1, y, bz, rounded.getYaw(), rounded.getPitch());
                  if (this.isLocationSafeGraveAndPlayer(east) && !this.hasCachedGraveAt(east)) {
                     return LocationUtil.roundLocation(east);
                  }

                  Location nw = new Location(world, bx - 1, y, bz - 1, rounded.getYaw(), rounded.getPitch());
                  if (this.isLocationSafeGraveAndPlayer(nw) && !this.hasCachedGraveAt(nw)) {
                     return LocationUtil.roundLocation(nw);
                  }

                  Location ne = new Location(world, bx + 1, y, bz - 1, rounded.getYaw(), rounded.getPitch());
                  if (this.isLocationSafeGraveAndPlayer(ne) && !this.hasCachedGraveAt(ne)) {
                     return LocationUtil.roundLocation(ne);
                  }

                  Location sw = new Location(world, bx - 1, y, bz + 1, rounded.getYaw(), rounded.getPitch());
                  if (this.isLocationSafeGraveAndPlayer(sw) && !this.hasCachedGraveAt(sw)) {
                     return LocationUtil.roundLocation(sw);
                  }

                  Location se = new Location(world, bx + 1, y, bz + 1, rounded.getYaw(), rounded.getPitch());
                  if (this.isLocationSafeGraveAndPlayer(se) && !this.hasCachedGraveAt(se)) {
                     return LocationUtil.roundLocation(se);
                  }
               }

               this.plugin.debugMessage(prefix + "no suitable nearby location found; returning null.", 1);
               return null;
            }
         } else {
            this.plugin.debugMessage(prefix + "rounded or world is null.", 1);
            return null;
         }
      }
   }

   public boolean hasCachedGraveAt(Location location) {
      if (location == null) {
         return false;
      } else {
         Location rounded = LocationUtil.roundLocation(location);
         if (rounded != null && rounded.getWorld() != null) {
            int x = rounded.getBlockX();
            int y = rounded.getBlockY();
            int z = rounded.getBlockZ();
            Map<UUID, Grave> graveMap = this.plugin.getCacheManager().getGraveMap();
            if (graveMap != null && !graveMap.isEmpty()) {
               for (Grave cached : graveMap.values()) {
                  if (cached != null) {
                     Location gl = cached.getLocationDeath();
                     if (gl != null) {
                        Location gr = LocationUtil.roundLocation(gl);
                        if (gr != null
                           && gr.getWorld() != null
                           && gr.getWorld().equals(rounded.getWorld())
                           && gr.getBlockX() == x
                           && gr.getBlockY() == y
                           && gr.getBlockZ() == z) {
                           return true;
                        }
                     }
                  }
               }

               return false;
            } else {
               return false;
            }
         } else {
            return false;
         }
      }
   }

   private boolean isLocationSafeGraveAndPlayer(Location location) {
      if (location == null) {
         return false;
      } else {
         Location rounded = LocationUtil.roundLocation(location);
         if (rounded == null || rounded.getWorld() == null) {
            return false;
         } else {
            return !this.isVoid(rounded) && this.isInsideBorder(rounded)
               ? !this.hasGrave(rounded) && this.isLocationSafeGrave(rounded) && this.isLocationSafePlayer(rounded)
               : false;
         }
      }
   }

   private String fmtLoc(Location loc) {
      return loc != null && loc.getWorld() != null
         ? loc.getWorld().getName() + " x=" + loc.getBlockX() + " y=" + loc.getBlockY() + " z=" + loc.getBlockZ()
         : "null";
   }
}
