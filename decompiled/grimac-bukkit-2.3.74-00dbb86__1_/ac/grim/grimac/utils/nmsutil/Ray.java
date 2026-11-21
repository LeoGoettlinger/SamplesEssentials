package ac.grim.grimac.utils.nmsutil;

import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.Vector3d;
import ac.grim.grimac.shaded.jetbrains.annotations.Contract;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.utils.data.Pair;
import ac.grim.grimac.utils.math.Vector3dm;

public record Ray(@NotNull Vector3d origin, @NotNull Vector3d direction) implements Cloneable {
   public Ray(@NotNull Vector3dm origin, @NotNull Vector3dm direction) {
      this(new Vector3d(origin.getX(), origin.getY(), origin.getZ()), new Vector3d(direction.getX(), direction.getY(), direction.getZ()));
   }

   public Ray(@NotNull GrimPlayer player, double x, double y, double z, float xRot, float yRot) {
      this(new Vector3d(x, y, z), calculateDirection(player, xRot, yRot));
   }

   @NotNull
   public static Vector3d calculateDirection(@NotNull GrimPlayer player, float xRot, float yRot) {
      float rotX = (float)Math.toRadians(xRot);
      float rotY = (float)Math.toRadians(yRot);
      double xz = player.trigHandler.cos(rotY);
      return new Vector3d(-xz * player.trigHandler.sin(rotX), -player.trigHandler.sin(rotY), xz * player.trigHandler.cos(rotX));
   }

   @Contract(" -> this")
   @NotNull
   public Ray clone() {
      return this;
   }

   @NotNull
   public Vector3d getPointAtDistance(double distance) {
      return this.origin.add(this.direction.multiply(distance));
   }

   @NotNull
   public Pair<Vector3d, Vector3d> closestPointsBetweenLines(@NotNull Ray other) {
      Vector3d n1 = this.direction.crossProduct(other.direction.crossProduct(this.direction));
      Vector3d n2 = other.direction.crossProduct(this.direction.crossProduct(other.direction));
      Vector3d c1 = this.origin.add(this.direction.multiply(other.origin.subtract(this.origin).dot(n2) / this.direction.dot(n2)));
      Vector3d c2 = other.origin.add(other.direction.multiply(this.origin.subtract(other.origin).dot(n1) / other.direction.dot(n1)));
      return new Pair<>(c1, c2);
   }
}
