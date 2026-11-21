package com.earth2me.essentials.libs.configurate.objectmapping;

import com.google.auto.value.AutoValue;
import java.lang.annotation.Annotation;

@AutoValue
abstract class Definition<A extends Annotation, T, F> {
   static <A extends Annotation, T, F> Definition<A, T, F> of(final Class<A> annotation, final Class<T> type, final F factory) {
      return new AutoValue_Definition<>(annotation, type, factory);
   }

   abstract Class<A> annotation();

   abstract Class<T> type();

   abstract F factory();
}
