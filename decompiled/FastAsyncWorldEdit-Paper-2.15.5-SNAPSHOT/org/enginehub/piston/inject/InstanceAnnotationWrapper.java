package org.enginehub.piston.inject;

import com.google.auto.value.AutoValue;
import java.lang.annotation.Annotation;
import javax.annotation.Nonnull;

@AutoValue
abstract class InstanceAnnotationWrapper implements AnnotationWrapper {
   static InstanceAnnotationWrapper from(Annotation annotationInstance) {
      return new AutoValue_InstanceAnnotationWrapper(annotationInstance);
   }

   @Nonnull
   @Override
   public abstract Annotation getAnnotation();

   @Override
   public final Class<? extends Annotation> getAnnotationType() {
      return this.getAnnotation().annotationType();
   }
}
