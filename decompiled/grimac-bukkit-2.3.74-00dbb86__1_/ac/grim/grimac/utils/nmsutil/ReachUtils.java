package ac.grim.grimac.utils.nmsutil;

import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.ClientVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.BlockFace;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.Vector3d;
import ac.grim.grimac.shaded.jetbrains.annotations.Contract;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import ac.grim.grimac.utils.collisions.datatypes.SimpleCollisionBox;
import ac.grim.grimac.utils.data.Pair;
import ac.grim.grimac.utils.math.GrimMath;
import ac.grim.grimac.utils.math.Vector3dm;
import ac.grim.grimac.utils.math.VectorUtils;
import lombok.Generated;

public final class ReachUtils {
   @Contract("_, _, _ -> new")
   @NotNull
   public static Pair<Vector3d, BlockFace> calculateIntercept(@NotNull SimpleCollisionBox self, @NotNull Vector3d origin, @NotNull Vector3d end) {
      Vector3d minX = getIntermediateWithXValue(origin, end, self.minX);
      Vector3d maxX = getIntermediateWithXValue(origin, end, self.maxX);
      Vector3d minY = getIntermediateWithYValue(origin, end, self.minY);
      Vector3d maxY = getIntermediateWithYValue(origin, end, self.maxY);
      Vector3d minZ = getIntermediateWithZValue(origin, end, self.minZ);
      Vector3d maxZ = getIntermediateWithZValue(origin, end, self.maxZ);
      if (!isVecInYZ(self, minX)) {
         minX = null;
      }

      if (!isVecInYZ(self, maxX)) {
         maxX = null;
      }

      if (!isVecInXZ(self, minY)) {
         minY = null;
      }

      if (!isVecInXZ(self, maxY)) {
         maxY = null;
      }

      if (!isVecInXY(self, minZ)) {
         minZ = null;
      }

      if (!isVecInXY(self, maxZ)) {
         maxZ = null;
      }

      Vector3d best = null;
      BlockFace bestFace = null;
      if (minX != null) {
         best = minX;
         bestFace = BlockFace.WEST;
      }

      if (maxX != null && (best == null || origin.distanceSquared(maxX) < origin.distanceSquared(best))) {
         best = maxX;
         bestFace = BlockFace.EAST;
      }

      if (minY != null && (best == null || origin.distanceSquared(minY) < origin.distanceSquared(best))) {
         best = minY;
         bestFace = BlockFace.DOWN;
      }

      if (maxY != null && (best == null || origin.distanceSquared(maxY) < origin.distanceSquared(best))) {
         best = maxY;
         bestFace = BlockFace.UP;
      }

      if (minZ != null && (best == null || origin.distanceSquared(minZ) < origin.distanceSquared(best))) {
         best = minZ;
         bestFace = BlockFace.NORTH;
      }

      if (maxZ != null && (best == null || origin.distanceSquared(maxZ) < origin.distanceSquared(best))) {
         best = maxZ;
         bestFace = BlockFace.SOUTH;
      }

      return new Pair<>(best, bestFace);
   }

   @Nullable
   public static Vector3d getIntermediateWithXValue(@NotNull Vector3d self, @NotNull Vector3d other, double x) {
      double deltaX = other.getX() - self.getX();
      double deltaY = other.getY() - self.getY();
      double deltaZ = other.getZ() - self.getZ();
      if (deltaX * deltaX < 1.0E-7F) {
         return null;
      } else {
         double d3 = (x - self.getX()) / deltaX;
         return d3 >= 0.0 && d3 <= 1.0 ? self.add(deltaX * d3, deltaY * d3, deltaZ * d3) : null;
      }
   }

   @Nullable
   public static Vector3d getIntermediateWithYValue(@NotNull Vector3d self, @NotNull Vector3d other, double y) {
      double deltaX = other.getX() - self.getX();
      double deltaY = other.getY() - self.getY();
      double deltaZ = other.getZ() - self.getZ();
      if (deltaY * deltaY < 1.0E-7F) {
         return null;
      } else {
         double d3 = (y - self.getY()) / deltaY;
         return d3 >= 0.0 && d3 <= 1.0 ? self.add(deltaX * d3, deltaY * d3, deltaZ * d3) : null;
      }
   }

   @Nullable
   public static Vector3d getIntermediateWithZValue(@NotNull Vector3d self, @NotNull Vector3d other, double z) {
      double deltaX = other.getX() - self.getX();
      double deltaY = other.getY() - self.getY();
      double deltaZ = other.getZ() - self.getZ();
      if (deltaZ * deltaZ < 1.0E-7F) {
         return null;
      } else {
         double d3 = (z - self.getZ()) / deltaZ;
         return d3 >= 0.0 && d3 <= 1.0 ? self.add(deltaX * d3, deltaY * d3, deltaZ * d3) : null;
      }
   }

   @Contract("_, null -> false")
   private static boolean isVecInYZ(@NotNull SimpleCollisionBox self, @Nullable Vector3d vec) {
      return vec != null && vec.getY() >= self.minY && vec.getY() <= self.maxY && vec.getZ() >= self.minZ && vec.getZ() <= self.maxZ;
   }

   @Contract("_, null -> false")
   private static boolean isVecInXZ(@NotNull SimpleCollisionBox self, @Nullable Vector3d vec) {
      return vec != null && vec.getX() >= self.minX && vec.getX() <= self.maxX && vec.getZ() >= self.minZ && vec.getZ() <= self.maxZ;
   }

   @Contract("_, null -> false")
   private static boolean isVecInXY(@NotNull SimpleCollisionBox self, @Nullable Vector3d vec) {
      return vec != null && vec.getX() >= self.minX && vec.getX() <= self.maxX && vec.getY() >= self.minY && vec.getY() <= self.maxY;
   }

   @Contract("_, _, _ -> new")
   @NotNull
   public static Vector3dm getLook(@NotNull GrimPlayer player, float yaw, float pitch) {
      if (player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_12_2)) {
         float yawRadians = GrimMath.radians(-yaw) - (float) Math.PI;
         float pitchRadians = GrimMath.radians(-pitch);
         float pitchCos = -player.trigHandler.cos(pitchRadians);
         float x = player.trigHandler.sin(yawRadians);
         float y = player.trigHandler.sin(pitchRadians);
         float z = player.trigHandler.cos(yawRadians);
         return new Vector3dm(x * pitchCos, y, z * pitchCos);
      } else {
         float pitchRadians = GrimMath.radians(pitch);
         float yawRadians = GrimMath.radians(-yaw);
         float pitchCos = player.trigHandler.cos(pitchRadians);
         float x = player.trigHandler.sin(yawRadians);
         float y = player.trigHandler.sin(pitchRadians);
         float z = player.trigHandler.cos(yawRadians);
         return new Vector3dm(x * pitchCos, -y, z * pitchCos);
      }
   }

   public static boolean isVecInside(@NotNull SimpleCollisionBox self, @NotNull Vector3d vec) {
      return vec.getX() > self.minX
         && vec.getX() < self.maxX
         && vec.getY() > self.minY
         && vec.getY() < self.maxY
         && vec.getZ() > self.minZ
         && vec.getZ() < self.maxZ;
   }

   public static double getMinReachToBox(@NotNull GrimPlayer player, @NotNull SimpleCollisionBox targetBox) {
      double lowest = Double.MAX_VALUE;
      double[] possibleEyeHeights = player.getPossibleEyeHeights();

      for (double eyes : possibleEyeHeights) {
         Vector3dm closestPoint = VectorUtils.cutBoxToVector(player.x, player.y + eyes, player.z, targetBox);
         lowest = Math.min(lowest, closestPoint.distance(player.x, player.y + eyes, player.z));
      }

      return lowest;
   }

   @Generated
   private ReachUtils() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
