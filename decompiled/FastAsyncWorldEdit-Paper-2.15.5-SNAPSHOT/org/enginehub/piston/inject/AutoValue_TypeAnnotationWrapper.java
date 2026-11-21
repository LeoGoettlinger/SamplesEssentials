package org.enginehub.piston.inject;

import java.lang.annotation.Annotation;
import javax.annotation.Nonnull;

final class AutoValue_TypeAnnotationWrapper extends TypeAnnotationWrapper {
   private final Class<? extends Annotation> annotationType;

   AutoValue_TypeAnnotationWrapper(Class<? extends Annotation> annotationType) {
      if (annotationType == null) {
         throw new NullPointerException("Null annotationType");
      } else {
         this.annotationType = annotationType;
      }
   }

   @Nonnull
   @Override
   public Class<? extends Annotation> getAnnotationType() {
      return this.annotationType;
   }

   @Override
   public String toString() {
      return "TypeAnnotationWrapper{annotationType=" + this.annotationType + "}";
   }

   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else {
         return o instanceof TypeAnnotationWrapper that ? this.annotationType.equals(that.getAnnotationType()) : false;
      }
   }

   @Override
   public int hashCode() {
      int h$ = 1;
      h$ *= 1000003;
      return h$ ^ this.annotationType.hashCode();
   }
}
