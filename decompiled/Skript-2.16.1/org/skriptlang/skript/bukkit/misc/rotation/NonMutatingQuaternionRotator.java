package org.skriptlang.skript.bukkit.misc.rotation;

import java.util.function.Function;
import org.jetbrains.annotations.Contract;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class NonMutatingQuaternionRotator implements Rotator<Quaternionf> {
   private final Function<Quaternionf, Quaternionf> rotator;

   public NonMutatingQuaternionRotator(Rotator.Axis axis, float angle) {
      this.rotator = switch (axis) {
         case X -> input -> input.rotateLocalX(angle, new Quaternionf());
         case Y -> input -> input.rotateLocalY(angle, new Quaternionf());
         case Z -> input -> input.rotateLocalZ(angle, new Quaternionf());
         case LOCAL_X -> input -> input.rotateX(angle, new Quaternionf());
         case LOCAL_Y -> input -> input.rotateY(angle, new Quaternionf());
         case LOCAL_Z -> input -> input.rotateZ(angle, new Quaternionf());
         case LOCAL_ARBITRARY -> throw new UnsupportedOperationException(
            "Rotation around the " + axis + " axis requires additional data. Use a different constructor."
         );
         case ARBITRARY -> input -> input;
      };
   }

   public NonMutatingQuaternionRotator(Rotator.Axis axis, Vector3f vector, float angle) {
      this.rotator = switch (axis) {
         case X -> input -> input.rotateLocalX(angle, new Quaternionf());
         case Y -> input -> input.rotateLocalY(angle, new Quaternionf());
         case Z -> input -> input.rotateLocalZ(angle, new Quaternionf());
         case LOCAL_X -> input -> input.rotateX(angle, new Quaternionf());
         case LOCAL_Y -> input -> input.rotateY(angle, new Quaternionf());
         case LOCAL_Z -> input -> input.rotateZ(angle, new Quaternionf());
         case LOCAL_ARBITRARY -> input -> input.rotateAxis(angle, vector, new Quaternionf());
         case ARBITRARY -> input -> input;
      };
   }

   @Contract("_ -> new")
   public Quaternionf rotate(Quaternionf input) {
      return this.rotator.apply(input);
   }
}
