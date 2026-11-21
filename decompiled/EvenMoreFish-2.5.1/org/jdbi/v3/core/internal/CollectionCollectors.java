package org.jdbi.v3.core.internal;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Collections;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import org.jdbi.v3.core.internal.exceptions.Unchecked;

public final class CollectionCollectors {
   private static final Supplier<Collector> SET_COLLECTOR = setFactory();

   private CollectionCollectors() {
   }

   private static Supplier<Collector> setFactory() {
      try {
         MethodHandle mh = MethodHandles.publicLookup().findStatic(Collectors.class, "toUnmodifiableSet", MethodType.methodType(Collector.class));
         return Unchecked.supplier(() -> (Collector)mh.invokeExact());
      } catch (ReflectiveOperationException var1) {
         return () -> Collectors.collectingAndThen(Collectors.toSet(), Collections::unmodifiableSet);
      }
   }

   public static <T> Collector<T, ?, Set<T>> toUnmodifiableSet() {
      return SET_COLLECTOR.get();
   }
}
