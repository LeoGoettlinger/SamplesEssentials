package de.exlll.configlib;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.AnnotatedType;

public sealed interface ConfigurationElement<T extends AnnotatedElement>
   permits ConfigurationElements.FieldElement,
   ConfigurationElements.RecordComponentElement {
   T element();

   String name();

   Class<?> type();

   AnnotatedType annotatedType();

   Object value(Object var1);

   Class<?> declaringType();

   default <A extends Annotation> A annotation(Class<A> annotationType) {
      return this.element().getAnnotation(annotationType);
   }
}
