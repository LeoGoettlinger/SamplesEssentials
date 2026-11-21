package org.enginehub.piston.inject;

import com.google.auto.value.AutoValue;
import java.lang.annotation.Annotation;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@AutoValue
abstract class TypeAnnotationWrapper implements AnnotationWrapper {
   static TypeAnnotationWrapper from(Class<? extends Annotation> annotationType) {
      return new AutoValue_TypeAnnotationWrapper(annotationType);
   }

   @Nonnull
   @Override
   public abstract Class<? extends Annotation> getAnnotationType();

   @Nullable
   @Override
   public final Annotation getAnnotation() {
      return null;
   }
}
