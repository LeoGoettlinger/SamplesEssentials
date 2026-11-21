package su.nightexpress.nightcore.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.Engine;

public class LocationUtil {
   @Deprecated
   @Nullable
   public static String serialize(@NonNull Location location) {
      World world = location.getWorld();
      return world == null
         ? null
         : location.getX() + "," + location.getY() + "," + location.getZ() + "," + location.getPitch() + "," + location.getYaw() + "," + world.getName();
   }

   @Deprecated
   @NonNull
   public static List<String> serialize(@NonNull Collection<Location> list) {
      return new ArrayList<>(list.stream().map(LocationUtil::serialize).filter(Objects::nonNull).toList());
   }

   @Deprecated
   @Nullable
   public static Location deserialize(@NonNull String raw) {
      String[] split = raw.split(",");
      if (split.length != 6) {
         return null;
      } else {
         World world = Bukkit.getWorld(split[5]);
         if (world == null) {
            Engine.core().error("Invalid/Unloaded world for: '" + raw + "' location!");
            return null;
         } else {
            double x = NumberUtil.getAnyDouble(split[0], 0.0);
            double y = NumberUtil.getAnyDouble(split[1], 0.0);
            double z = NumberUtil.getAnyDouble(split[2], 0.0);
            float pitch = (float)NumberUtil.getAnyDouble(split[3], 0.0);
            float yaw = (float)NumberUtil.getAnyDouble(split[4], 0.0);
            return new Location(world, x, y, z, yaw, pitch);
         }
      }
   }

   @Deprecated
   @NonNull
   public static List<Location> deserialize(@NonNull Collection<String> list) {
      return new ArrayList<>(list.stream().map(LocationUtil::deserialize).filter(Objects::nonNull).toList());
   }

   @NonNull
   public static String getWorldName(@NonNull Location location) {
      World world = location.getWorld();
      return world == null ? "null" : world.getName();
   }

   @Deprecated
   @NonNull
   public static Location getCenter(@NonNull Location location) {
      return setCenter3D(location);
   }

   @Deprecated
   @NonNull
   public static Location getCenter(@NonNull Location location, boolean doVertical) {
      return setCenter(location, doVertical);
   }

   @NonNull
   public static Location setCenter2D(@NonNull Location location) {
      return setCenter(location, false);
   }

   @NonNull
   public static Location setCenter3D(@NonNull Location location) {
      return setCenter(location, true);
   }

   @NonNull
   public static Location setCenter(@NonNull Location location, boolean is3D) {
      location.setX(location.getBlockX() + 0.5);
      location.setY(location.getBlockY() + (is3D ? 0.5 : 0.0));
      location.setZ(location.getBlockZ() + 0.5);
      return location;
   }

   @NonNull
   public static Vector getDirection(@NonNull Location from, @NonNull Location to) {
      Location origin = from.clone();
      origin.setDirection(to.toVector().subtract(origin.toVector()));
      return origin.getDirection();
   }
}
