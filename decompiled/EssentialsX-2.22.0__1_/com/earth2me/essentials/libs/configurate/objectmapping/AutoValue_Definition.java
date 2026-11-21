package com.earth2me.essentials.libs.configurate.objectmapping;

import java.lang.annotation.Annotation;

final class AutoValue_Definition<A extends Annotation, T, F> extends Definition<A, T, F> {
   private final Class<A> annotation;
   private final Class<T> type;
   private final F factory;

   AutoValue_Definition(Class<A> annotation, Class<T> type, F factory) {
      if (annotation == null) {
         throw new NullPointerException("Null annotation");
      } else {
         this.annotation = annotation;
         if (type == null) {
            throw new NullPointerException("Null type");
         } else {
            this.type = type;
            if (factory == null) {
               throw new NullPointerException("Null factory");
            } else {
               this.factory = factory;
            }
         }
      }
   }

   @Override
   Class<A> annotation() {
      return this.annotation;
   }

   @Override
   Class<T> type() {
      return this.type;
   }

   @Override
   F factory() {
      return this.factory;
   }

   @Override
   public String toString() {
      return "Definition{annotation=" + this.annotation + ", type=" + this.type + ", factory=" + this.factory + "}";
   }

   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof Definition)) {
         return false;
      } else {
         Definition<?, ?, ?> that = (Definition<?, ?, ?>)o;
         return this.annotation.equals(that.annotation()) && this.type.equals(that.type()) && this.factory.equals(that.factory());
      }
   }

   @Override
   public int hashCode() {
      int h$ = 1;
      h$ *= 1000003;
      h$ ^= this.annotation.hashCode();
      h$ *= 1000003;
      h$ ^= this.type.hashCode();
      h$ *= 1000003;
      return h$ ^ this.factory.hashCode();
   }
}
