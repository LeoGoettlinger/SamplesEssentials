package com.fastasyncworldedit.core.math.transform;

import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.math.transform.Transform;

public record RoundedTransform(Transform transform) implements Transform {
   @Override
   public boolean isIdentity() {
      return this.transform.isIdentity();
   }

   @Override
   public Vector3 apply(Vector3 input) {
      Vector3 val = this.transform.apply(input);
      return val.round();
   }

   public RoundedTransform inverse() {
      return new RoundedTransform(this.transform.inverse());
   }

   public RoundedTransform combine(Transform other) {
      return new RoundedTransform(this.transform.combine(other));
   }

   public Transform getTransform() {
      return this.transform;
   }
}
