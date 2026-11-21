package org.skriptlang.skript.bukkit.misc.rotation;

import java.util.function.Function;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Contract;

public class VectorRotator implements Rotator<Vector> {
   private final Function<Vector, Vector> rotator;

   public VectorRotator(Rotator.Axis axis, double angle) {
      this.rotator = switch (axis) {
         case X -> input -> input.rotateAroundX(angle);
         case Y -> input -> input.rotateAroundY(angle);
         case Z -> input -> input.rotateAroundZ(angle);
         case ARBITRARY -> throw new UnsupportedOperationException(
            "Rotation around the " + axis + " axis requires additional data. Use a different constructor."
         );
         case LOCAL_ARBITRARY, LOCAL_X, LOCAL_Y, LOCAL_Z -> input -> input;
      };
   }

   public VectorRotator(Rotator.Axis axis, Vector vector, double angle) {
      this.rotator = switch (axis) {
         case X -> input -> input.rotateAroundX(angle);
         case Y -> input -> input.rotateAroundY(angle);
         case Z -> input -> input.rotateAroundZ(angle);
         case ARBITRARY -> input -> input.rotateAroundNonUnitAxis(vector, angle);
         case LOCAL_ARBITRARY, LOCAL_X, LOCAL_Y, LOCAL_Z -> input -> input;
      };
   }

   @Contract("_ -> param1")
   public Vector rotate(Vector input) {
      return this.rotator.apply(input);
   }
}
