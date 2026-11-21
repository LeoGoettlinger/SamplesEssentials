package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.annotation;

import java.lang.annotation.Annotation;

public interface JacksonAnnotationValue<A extends Annotation> {
   Class<A> valueFor();
}
