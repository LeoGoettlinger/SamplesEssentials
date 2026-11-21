package org.enginehub.piston.inject;

import java.lang.annotation.Annotation;
import javax.annotation.Nullable;

interface AnnotationWrapper {
   @Nullable
   Annotation getAnnotation();

   @Nullable
   Class<? extends Annotation> getAnnotationType();
}
