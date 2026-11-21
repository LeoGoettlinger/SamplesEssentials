package com.magmaguy.elitemobs.shaded.reflections.util;

import com.magmaguy.elitemobs.shaded.reflections.ReflectionUtils;
import com.magmaguy.elitemobs.shaded.reflections.Store;
import java.lang.reflect.AnnotatedElement;
import java.util.LinkedHashSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public interface UtilQueryBuilder<F, E> {
   QueryFunction<Store, E> get(F var1);

   default QueryFunction<Store, E> of(F element) {
      return this.of(ReflectionUtils.extendType().get((F)((AnnotatedElement)element)));
   }

   default QueryFunction<Store, E> of(F element, Predicate<? super E> predicate) {
      return this.of(element).filter(predicate);
   }

   default <T> QueryFunction<Store, E> of(QueryFunction<Store, T> function) {
      return store -> function.apply(store).stream().flatMap(t -> this.get((F)t).apply(store).stream()).collect(Collectors.toCollection(LinkedHashSet::new));
   }
}
