package com.magmaguy.elitemobs.shaded.reflections.util;

import com.magmaguy.elitemobs.shaded.reflections.Store;
import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface QueryFunction<C, T> extends Function<C, Set<T>>, NameHelper {
   Set<T> apply(C var1);

   static <C, T> QueryFunction<Store, T> empty() {
      return ctx -> Collections.emptySet();
   }

   static <C, T> QueryFunction<Store, T> single(T element) {
      return ctx -> Collections.singleton(element);
   }

   static <C, T> QueryFunction<Store, T> set(Collection<T> elements) {
      return ctx -> new LinkedHashSet<>(elements);
   }

   default QueryFunction<C, T> filter(Predicate<? super T> predicate) {
      return ctx -> this.apply(ctx).stream().filter(predicate).collect(Collectors.toCollection(LinkedHashSet::new));
   }

   default <R> QueryFunction<C, R> map(Function<? super T, ? extends R> function) {
      return ctx -> this.apply(ctx).stream().map(function).collect(Collectors.toCollection(LinkedHashSet::new));
   }

   default <R> QueryFunction<C, R> flatMap(Function<T, ? extends Function<C, Set<R>>> function) {
      return ctx -> this.apply(ctx).stream().flatMap(t -> function.apply((T)t).apply((C)ctx).stream()).collect(Collectors.toCollection(LinkedHashSet::new));
   }

   default QueryFunction<C, T> getAll(Function<T, QueryFunction<C, T>> builder) {
      return this.getAll(builder, t -> t);
   }

   default <R> QueryFunction<C, R> getAll(Function<T, QueryFunction<C, R>> builder, Function<R, T> traverse) {
      return ctx -> {
         List<T> workKeys = new ArrayList<>(this.apply(ctx));
         Set<R> result = new LinkedHashSet<>();

         for (int i = 0; i < workKeys.size(); i++) {
            T key = workKeys.get(i);

            for (R r : builder.apply(key).apply(ctx)) {
               if (result.add(r)) {
                  workKeys.add(traverse.apply(r));
               }
            }
         }

         return result;
      };
   }

   default <R> QueryFunction<C, T> add(QueryFunction<C, T> function) {
      return ctx -> Stream.of(this.apply(ctx), function.apply(ctx)).flatMap(Collection::stream).collect(Collectors.toCollection(LinkedHashSet::new));
   }

   default <R> QueryFunction<C, R> as(Class<? extends R> type, ClassLoader... loaders) {
      return ctx -> {
         Set<T> apply = this.apply(ctx);
         return apply.stream()
            .findFirst()
            .map(
               first -> (Set<R>)(type.isAssignableFrom(first.getClass())
                  ? apply
                  : (
                     first instanceof String
                        ? (Set)this.forNames(apply, type, loaders)
                        : (
                           first instanceof AnnotatedElement
                              ? (Set)this.forNames(this.toNames(apply), type, loaders)
                              : apply.stream().map(t -> (R)t).collect(Collectors.toCollection(LinkedHashSet::new))
                        )
                  ))
            )
            .orElse((Set<R>)apply);
      };
   }

   default <R> QueryFunction<C, Class<?>> asClass(ClassLoader... loaders) {
      return ctx -> (Set<Class<?>>)this.<Class<?>>forNames(this.apply(ctx), Class.class, loaders);
   }

   default QueryFunction<C, String> asString() {
      return ctx -> new LinkedHashSet<>(this.toNames(new AnnotatedElement[]{(AnnotatedElement)this.apply(ctx)}));
   }

   default <R> QueryFunction<C, Class<? extends R>> as() {
      return ctx -> new LinkedHashSet<>(this.apply(ctx));
   }
}
