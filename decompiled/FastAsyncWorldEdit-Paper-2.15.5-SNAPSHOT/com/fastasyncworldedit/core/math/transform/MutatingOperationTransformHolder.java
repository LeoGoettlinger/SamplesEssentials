package com.fastasyncworldedit.core.math.transform;

import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.math.transform.CombinedTransform;
import com.sk89q.worldedit.math.transform.Transform;
import java.util.function.Function;

public class MutatingOperationTransformHolder<T extends Transform> implements Transform {
   private final Function<? super T, ? extends T> operation;
   private T transform;

   public MutatingOperationTransformHolder(T transform, Function<? super T, ? extends T> operation) {
      this.transform = transform;
      this.operation = operation;
   }

   public static Transform transform(Transform transform) {
      return transform(transform, false);
   }

   public static Transform transform(Transform transform, boolean parallel) {
      return (Transform)(switch (transform) {
         case MutatingOperationTransformHolder<?> mutating -> parallel ? mutating.copy().mutate() : mutating.mutate();
         case CombinedTransform combined -> {
            if (!parallel) {
               combined.getTransforms().forEach(t -> transform(t, false));
               yield combined;
            } else {
               yield new CombinedTransform(combined.getTransforms().stream().map(t -> transform(t, true)).toList());
            }
         }
         default -> transform;
      });
   }

   public MutatingOperationTransformHolder<T> mutate() {
      if (this.operation != null) {
         this.transform = (T)this.operation.apply(this.transform);
      }

      return this;
   }

   @Override
   public boolean isIdentity() {
      return this.transform.isIdentity();
   }

   @Override
   public Vector3 apply(final Vector3 input) {
      return this.transform.apply(input);
   }

   @Override
   public Transform inverse() {
      return this.transform.inverse();
   }

   @Override
   public Transform combine(final Transform other) {
      return this.transform.combine(other);
   }

   public MutatingOperationTransformHolder<T> copy() {
      T transform = (T)(this.transform instanceof MutatingOperationTransformHolder<?> mutating ? mutating.copy() : this.transform);
      return new MutatingOperationTransformHolder<>(transform, this.operation);
   }
}
