package com.skyblockexp.ezrtp.util.compat;

import java.lang.reflect.Method;
import java.util.Optional;
import org.bukkit.Location;
import org.bukkit.World;

public final class WorldBorderCompat {
   private WorldBorderCompat() {
   }

   public static Optional<Double> getBorderRadius(World world) {
      if (world == null) {
         return Optional.empty();
      } else {
         try {
            Method getWorldBorder = World.class.getMethod("getWorldBorder");
            Object border = getWorldBorder.invoke(world);
            if (border == null) {
               return Optional.empty();
            }

            try {
               Method getSize = border.getClass().getMethod("getSize");
               Object sizeObj = getSize.invoke(border);
               if (sizeObj instanceof Number) {
                  double size = ((Number)sizeObj).doubleValue();
                  return Optional.of(size * 0.5);
               }
            } catch (NoSuchMethodException var9) {
            }

            try {
               Method getDiameter = border.getClass().getMethod("getDiameter");
               Object diamObj = getDiameter.invoke(border);
               if (diamObj instanceof Number) {
                  double diam = ((Number)diamObj).doubleValue();
                  return Optional.of(diam * 0.5);
               }
            } catch (NoSuchMethodException var8) {
            }

            try {
               Method getCenter = border.getClass().getMethod("getCenter");
               Object centerObj = getCenter.invoke(border);
               if (centerObj instanceof Location) {
                  return Optional.empty();
               }
            } catch (NoSuchMethodException var7) {
            }
         } catch (Exception var10) {
         }

         return Optional.empty();
      }
   }

   public static double getBorderRadiusOrDefault(World world, double defaultRadius) {
      return getBorderRadius(world).orElse(defaultRadius);
   }

   public static Optional<Location> getCenter(World world) {
      if (world == null) {
         return Optional.empty();
      } else {
         try {
            Method getWorldBorder = World.class.getMethod("getWorldBorder");
            Object border = getWorldBorder.invoke(world);
            if (border == null) {
               return Optional.empty();
            }

            try {
               Method getCenter = border.getClass().getMethod("getCenter");
               Object center = getCenter.invoke(border);
               if (center instanceof Location) {
                  return Optional.of((Location)center);
               }
            } catch (NoSuchMethodException var13) {
            }

            try {
               Method getCenterX = border.getClass().getMethod("getCenterX");
               Method getCenterZ = border.getClass().getMethod("getCenterZ");
               Object xObj = getCenterX.invoke(border);
               Object zObj = getCenterZ.invoke(border);
               if (xObj instanceof Number && zObj instanceof Number) {
                  double x = ((Number)xObj).doubleValue();
                  double z = ((Number)zObj).doubleValue();
                  Location loc = new Location(world, x, world.getSpawnLocation() != null ? world.getSpawnLocation().getY() : 64.0, z);
                  return Optional.of(loc);
               }
            } catch (NoSuchMethodException var12) {
            }
         } catch (Exception var14) {
         }

         return Optional.empty();
      }
   }

   public static boolean isInside(Location location) {
      if (location == null) {
         return false;
      } else {
         World world = location.getWorld();
         if (world == null) {
            return false;
         } else {
            try {
               Method getWorldBorder = World.class.getMethod("getWorldBorder");
               Object border = getWorldBorder.invoke(world);
               if (border == null) {
                  return true;
               } else {
                  try {
                     Method isInsideLoc = border.getClass().getMethod("isInside", Location.class);
                     Object res = isInsideLoc.invoke(border, location);
                     if (res instanceof Boolean) {
                        return (Boolean)res;
                     }
                  } catch (NoSuchMethodException var11) {
                  }

                  try {
                     Method isInsideD = border.getClass().getMethod("isInside", double.class, double.class);
                     Object res = isInsideD.invoke(border, location.getX(), location.getZ());
                     if (res instanceof Boolean) {
                        return (Boolean)res;
                     }
                  } catch (NoSuchMethodException var10) {
                  }

                  Optional<Location> center = getCenter(world);
                  Optional<Double> radius = getBorderRadius(world);
                  if (center.isPresent() && radius.isPresent()) {
                     double dx = location.getX() - center.get().getX();
                     double dz = location.getZ() - center.get().getZ();
                     return dx * dx + dz * dz <= radius.get() * radius.get();
                  } else {
                     return true;
                  }
               }
            } catch (Exception var12) {
               return true;
            }
         }
      }
   }
}
