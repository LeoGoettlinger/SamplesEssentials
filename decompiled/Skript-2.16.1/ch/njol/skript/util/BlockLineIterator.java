package ch.njol.skript.util;

import java.util.Iterator;
import java.util.NoSuchElementException;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;

public class BlockLineIterator implements Iterator<Block> {
   private final Vector3d current;
   private final Vector3d end;
   private final Vector3d centeredEnd;
   final Vector3d step;
   private final World world;
   private boolean finished;

   public BlockLineIterator(@NotNull Location start, @NotNull Location end) {
      this.current = start.toVector().toVector3d();
      this.world = start.getWorld();
      this.end = end.toVector().toVector3d();
      this.centeredEnd = centered(this.end);
      this.step = this.end.sub(this.current, new Vector3d()).normalize();
   }

   public BlockLineIterator(@NotNull Block start, @NotNull Block end) {
      this(start.getLocation().toCenterLocation(), end.getLocation().toCenterLocation());
   }

   public BlockLineIterator(Location start, @NotNull Vector direction, double distance) {
      this(start, start.clone().add(direction.clone().normalize().multiply(distance)));
   }

   public BlockLineIterator(@NotNull Block start, Vector direction, double distance) {
      this(start.getLocation().toCenterLocation(), direction, distance);
   }

   @Override
   public boolean hasNext() {
      return !this.finished;
   }

   public Block next() {
      if (!this.hasNext()) {
         throw new NoSuchElementException("Reached the final block destination");
      } else if (this.end.sub(this.current, new Vector3d()).dot(this.step) < -1.0) {
         throw new NoSuchElementException("Overshot the final block!");
      } else {
         Vector3d center = centered(this.current);
         Block block = getBlock(center, this.world);
         if (center.equals(this.centeredEnd)) {
            this.finished = true;
         }

         double t = stepsToNextFace(this.current, this.step, center) + Math.ulp(1.0F);
         this.current.fma(t, this.step);
         return block;
      }
   }

   static double stepsToNextFace(Vector3d start, @NotNull Vector3d step, Vector3d center) {
      Vector3d neededSteps = new Vector3d(Math.signum(step.x), Math.signum(step.y), Math.signum(step.z))
         .mulAdd(0.5, center)
         .sub(start)
         .div(step, new Vector3d());
      if (Double.isNaN(neededSteps.x)) {
         neededSteps.x = Double.POSITIVE_INFINITY;
      }

      if (Double.isNaN(neededSteps.y)) {
         neededSteps.y = Double.POSITIVE_INFINITY;
      }

      if (Double.isNaN(neededSteps.z)) {
         neededSteps.z = Double.POSITIVE_INFINITY;
      }

      return neededSteps.get(neededSteps.minComponent());
   }

   @org.jetbrains.annotations.Contract("_ -> new")
   private static Vector3d centered(@NotNull Vector3d vector) {
      return vector.floor(new Vector3d()).add(0.5, 0.5, 0.5);
   }

   @NotNull
   private static Block getBlock(@NotNull Vector3d vector, @NotNull World world) {
      return Vector.fromJOML(vector).toLocation(world).getBlock();
   }
}
