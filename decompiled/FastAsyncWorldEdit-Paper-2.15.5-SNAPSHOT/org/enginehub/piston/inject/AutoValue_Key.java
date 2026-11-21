package org.enginehub.piston.inject;

import com.google.common.reflect.TypeToken;

final class AutoValue_Key<T> extends Key<T> {
   private final TypeToken<T> typeToken;
   private final AnnotationWrapper annotationWrapper;

   AutoValue_Key(TypeToken<T> typeToken, AnnotationWrapper annotationWrapper) {
      if (typeToken == null) {
         throw new NullPointerException("Null typeToken");
      } else {
         this.typeToken = typeToken;
         if (annotationWrapper == null) {
            throw new NullPointerException("Null annotationWrapper");
         } else {
            this.annotationWrapper = annotationWrapper;
         }
      }
   }

   @Override
   public TypeToken<T> getTypeToken() {
      return this.typeToken;
   }

   @Override
   AnnotationWrapper getAnnotationWrapper() {
      return this.annotationWrapper;
   }

   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else {
         return !(o instanceof Key<?> that) ? false : this.typeToken.equals(that.getTypeToken()) && this.annotationWrapper.equals(that.getAnnotationWrapper());
      }
   }

   @Override
   public int hashCode() {
      int h$ = 1;
      h$ *= 1000003;
      h$ ^= this.typeToken.hashCode();
      h$ *= 1000003;
      return h$ ^ this.annotationWrapper.hashCode();
   }
}
