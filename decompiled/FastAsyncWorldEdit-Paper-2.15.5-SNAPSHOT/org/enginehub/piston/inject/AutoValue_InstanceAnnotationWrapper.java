package org.enginehub.piston.inject;

import java.lang.annotation.Annotation;
import javax.annotation.Nonnull;

final class AutoValue_InstanceAnnotationWrapper extends InstanceAnnotationWrapper {
   private final Annotation annotation;

   AutoValue_InstanceAnnotationWrapper(Annotation annotation) {
      if (annotation == null) {
         throw new NullPointerException("Null annotation");
      } else {
         this.annotation = annotation;
      }
   }

   @Nonnull
   @Override
   public Annotation getAnnotation() {
      return this.annotation;
   }

   @Override
   public String toString() {
      return "InstanceAnnotationWrapper{annotation=" + this.annotation + "}";
   }

   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else {
         return o instanceof InstanceAnnotationWrapper that ? this.annotation.equals(that.getAnnotation()) : false;
      }
   }

   @Override
   public int hashCode() {
      int h$ = 1;
      h$ *= 1000003;
      return h$ ^ this.annotation.hashCode();
   }
}
