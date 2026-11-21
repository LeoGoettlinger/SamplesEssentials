package com.sk89q.worldedit.extension.platform;

import com.sk89q.worldedit.internal.annotation.Radii;
import java.io.Serializable;
import org.jspecify.annotations.Nullable;

final class AutoAnnotation_Annotations_radii implements Radii, Serializable {
   private static final long serialVersionUID = -5526853588017907499L;
   private final int value;

   AutoAnnotation_Annotations_radii(int value) {
      this.value = value;
   }

   @Override
   public Class<? extends Radii> annotationType() {
      return Radii.class;
   }

   @Override
   public int value() {
      return this.value;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder("@com.sk89q.worldedit.internal.annotation.Radii(");
      sb.append(this.value);
      return sb.append(')').toString();
   }

   @Override
   public boolean equals(@Nullable Object o) {
      if (o == this) {
         return true;
      } else {
         return o instanceof Radii that ? this.value == that.value() : false;
      }
   }

   @Override
   public int hashCode() {
      return 1335633679 ^ this.value;
   }
}
