package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.introspect;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Modifier;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JavaType;

public abstract class Annotated {
   protected Annotated() {
   }

   public abstract <A extends Annotation> A getAnnotation(Class<A> var1);

   public abstract boolean hasAnnotation(Class<?> var1);

   public abstract boolean hasOneOf(Class<? extends Annotation>[] var1);

   public abstract AnnotatedElement getAnnotated();

   protected abstract int getModifiers();

   public boolean isPublic() {
      return Modifier.isPublic(this.getModifiers());
   }

   public abstract String getName();

   public abstract JavaType getType();

   public abstract Class<?> getRawType();

   @Deprecated
   public abstract Iterable<Annotation> annotations();

   @Override
   public abstract boolean equals(Object var1);

   @Override
   public abstract int hashCode();

   @Override
   public abstract String toString();
}
