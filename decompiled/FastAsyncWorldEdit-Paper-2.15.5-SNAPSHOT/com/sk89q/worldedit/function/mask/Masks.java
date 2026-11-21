package com.sk89q.worldedit.function.mask;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import javax.annotation.Nullable;

public final class Masks {
   static final Masks.AlwaysTrue ALWAYS_TRUE = new Masks.AlwaysTrue();
   static final Masks.AlwaysFalse ALWAYS_FALSE = new Masks.AlwaysFalse();

   private Masks() {
   }

   public static Mask alwaysTrue() {
      return ALWAYS_TRUE;
   }

   public static Mask alwaysFalse() {
      return ALWAYS_FALSE;
   }

   public static Mask2D alwaysTrue2D() {
      return ALWAYS_TRUE;
   }

   public static Mask negate(final Mask mask) {
      return mask.inverse();
   }

   public static Mask2D negate(final Mask2D mask) {
      if (mask instanceof Masks.AlwaysTrue) {
         return ALWAYS_FALSE;
      } else if (mask instanceof Masks.AlwaysFalse) {
         return ALWAYS_TRUE;
      } else if (mask instanceof Masks.NegatedMask2D) {
         return ((Masks.NegatedMask2D)mask).mask;
      } else {
         Preconditions.checkNotNull(mask);
         return new Masks.NegatedMask2D(mask);
      }
   }

   public static Mask asMask(final Mask2D mask) {
      return new AbstractMask() {
         @Override
         public boolean test(BlockVector3 vector) {
            return mask.test(vector.toBlockVector2());
         }

         @Nullable
         @Override
         public Mask2D toMask2D() {
            return mask;
         }

         @Override
         public Mask copy() {
            return Masks.asMask(mask.copy2D());
         }
      };
   }

   protected static class AlwaysFalse implements Mask, Mask2D {
      @Override
      public boolean test(BlockVector3 vector) {
         return false;
      }

      @Override
      public boolean test(BlockVector2 vector) {
         return false;
      }

      @Nullable
      @Override
      public Mask2D toMask2D() {
         return this;
      }

      @Override
      public Mask tryCombine(Mask other) {
         return this;
      }

      @Override
      public Mask tryOr(Mask other) {
         return other;
      }

      @Override
      public Mask copy() {
         return new Masks.AlwaysFalse();
      }

      @Override
      public Mask2D copy2D() {
         return new Masks.AlwaysFalse();
      }
   }

   protected static class AlwaysTrue implements Mask, Mask2D {
      @Override
      public boolean test(BlockVector3 vector) {
         return true;
      }

      @Override
      public boolean test(BlockVector2 vector) {
         return true;
      }

      @Nullable
      @Override
      public Mask2D toMask2D() {
         return this;
      }

      @Override
      public Mask tryCombine(Mask other) {
         return other;
      }

      @Override
      public Mask tryOr(Mask other) {
         return this;
      }

      @Override
      public Mask copy() {
         return new Masks.AlwaysTrue();
      }

      @Override
      public Mask2D copy2D() {
         return new Masks.AlwaysTrue();
      }
   }

   protected record NegatedMask(Mask mask) implements Mask {
      @Override
      public boolean test(BlockVector3 vector) {
         return !this.mask.test(vector);
      }

      @Nullable
      @Override
      public Mask2D toMask2D() {
         Mask2D mask2D = this.mask.toMask2D();
         return mask2D == null ? null : Masks.negate(mask2D);
      }

      @Override
      public Mask copy() {
         return Masks.negate(this.mask.copy());
      }
   }

   private record NegatedMask2D(Mask2D mask) implements Mask2D {
      @Override
      public boolean test(BlockVector2 vector) {
         return !this.mask.test(vector);
      }

      @Override
      public Mask2D copy2D() {
         return Masks.negate(this.mask.copy2D());
      }
   }
}
