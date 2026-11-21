package org.enginehub.piston.inject;

import java.lang.annotation.Annotation;
import javax.annotation.Nullable;

final class NullAnnotationWrapper implements AnnotationWrapper {
   private static final NullAnnotationWrapper INSTANCE = new NullAnnotationWrapper();

   static NullAnnotationWrapper getInstance() {
      return INSTANCE;
   }

   private NullAnnotationWrapper() {
   }

   @Nullable
   @Override
   public Annotation getAnnotation() {
      return null;
   }

   @Nullable
   @Override
   public Class<? extends Annotation> getAnnotationType() {
      return null;
   }

   @Override
   public String toString() {
      return this.getClass().getSimpleName();
   }
}
