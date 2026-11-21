package io.leangen.geantyref;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.AnnotatedTypeVariable;
import java.lang.reflect.TypeVariable;

class AnnotatedTypeVariableImpl extends AnnotatedTypeImpl implements AnnotatedTypeVariable {
   private AnnotatedType[] annotatedBounds;

   AnnotatedTypeVariableImpl(TypeVariable<?> type) {
      this(type, type.getAnnotations());
   }

   AnnotatedTypeVariableImpl(TypeVariable<?> type, Annotation[] annotations) {
      super(type, annotations);
      AnnotatedType[] annotatedBounds = type.getAnnotatedBounds();
      if (annotatedBounds == null || annotatedBounds.length == 0) {
         annotatedBounds = new AnnotatedType[0];
      }

      this.annotatedBounds = annotatedBounds;
   }

   AnnotatedTypeVariableImpl init(AnnotatedType[] annotatedBounds) {
      this.type = new TypeVariableImpl((TypeVariable)this.type, annotatedBounds);
      this.annotatedBounds = annotatedBounds;
      return this;
   }

   AnnotatedTypeVariableImpl setAnnotations(Annotation[] annotations) {
      this.annotations = this.toMap(annotations);
      return this;
   }

   @Override
   public AnnotatedType[] getAnnotatedBounds() {
      return (AnnotatedType[])this.annotatedBounds.clone();
   }

   @Override
   public boolean equals(Object other) {
      return other instanceof AnnotatedTypeVariable && super.equals(other);
   }

   @Override
   public String toString() {
      return this.annotationsString() + ((TypeVariable)this.type).getName();
   }
}
