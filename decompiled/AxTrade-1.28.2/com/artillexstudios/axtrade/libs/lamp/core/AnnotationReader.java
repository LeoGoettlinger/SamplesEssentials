package com.artillexstudios.axtrade.libs.lamp.core;

import com.artillexstudios.axtrade.libs.lamp.annotation.Command;
import com.artillexstudios.axtrade.libs.lamp.annotation.DefaultFor;
import com.artillexstudios.axtrade.libs.lamp.annotation.DistributeOnMethods;
import com.artillexstudios.axtrade.libs.lamp.annotation.Subcommand;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

final class AnnotationReader {
   private static final List<Class<? extends Annotation>> COMMAND_ANNOTATIONS = Stream.of(Command.class, Subcommand.class, DefaultFor.class)
      .collect(Collectors.toList());
   private final AnnotatedElement element;
   private Map<Class<? extends Annotation>, Annotation> annotations;

   public static AnnotationReader create(@NotNull BaseCommandHandler handler, @NotNull AnnotatedElement method) {
      return createReader(handler, method);
   }

   public <T extends Annotation> T get(@NotNull Class<T> annotationType) {
      return (T)this.annotations.get(annotationType);
   }

   public void add(@NotNull Annotation annotation) {
      this.annotations.putIfAbsent(annotation.annotationType(), annotation);
   }

   void replaceAnnotations(BaseCommandHandler handler) {
      if (!handler.annotationReplacers.isEmpty()) {
         Map<Class<? extends Annotation>, Annotation> newAnnotations = new HashMap<>(this.annotations);

         for (Annotation annotation : this.annotations.values()) {
            List<Annotation> replaced = handler.replaceAnnotation(this.element, annotation);
            if (replaced != null) {
               replaced.forEach(a -> {
                  Annotation var10000 = newAnnotations.put(a.annotationType(), a);
               });
            }
         }

         this.annotations = newAnnotations;
      }
   }

   public boolean shouldDismiss() {
      if (!(this.element instanceof Method)) {
         return false;
      } else {
         return this.annotations.isEmpty() ? true : COMMAND_ANNOTATIONS.stream().noneMatch(annotation -> this.annotations.containsKey(annotation));
      }
   }

   @NotNull
   private static AnnotationReader createReader(@NotNull BaseCommandHandler handler, @NotNull AnnotatedElement method) {
      Map<Class<? extends Annotation>, Annotation> annotations = toMap(method.getAnnotations());
      AnnotationReader reader = new AnnotationReader(method, annotations);
      reader.replaceAnnotations(handler);
      return reader;
   }

   public void distributeAnnotations() {
      if (this.element instanceof Method) {
         for (Class<?> top = ((Method)this.element).getDeclaringClass(); top != null; top = top.getDeclaringClass()) {
            toMap(top.getAnnotations()).forEach((type, annotation) -> {
               if (type.isAnnotationPresent(DistributeOnMethods.class)) {
                  this.annotations.putIfAbsent((Class<? extends Annotation>)type, annotation);
               }
            });
         }
      }
   }

   public <R, T extends Annotation> R get(@NotNull Class<T> type, Function<T, R> f) {
      return this.get(type, f, () -> null);
   }

   public <R, T extends Annotation> R get(@NotNull Class<T> type, Function<T, R> f, Supplier<R> def) {
      T ann = (T)this.annotations.get(type);
      return ann != null ? f.apply(ann) : def.get();
   }

   @NotNull
   public <T extends Annotation> T get(@NotNull Class<T> type, String err) {
      return this.get(type);
   }

   public boolean contains(Class<? extends Annotation> annotation) {
      return this.annotations.containsKey(annotation);
   }

   private static Map<Class<? extends Annotation>, Annotation> toMap(Annotation[] annotations) {
      Map<Class<? extends Annotation>, Annotation> map = new HashMap<>();

      for (Annotation annotation : annotations) {
         map.put(annotation.annotationType(), annotation);
      }

      return map;
   }

   public boolean isEmpty() {
      return this.annotations.isEmpty();
   }

   @Override
   public String toString() {
      return "AnnotationReader(element=" + this.element + ", annotations=" + this.annotations + ")";
   }

   public AnnotationReader(AnnotatedElement element, Map<Class<? extends Annotation>, Annotation> annotations) {
      this.element = element;
      this.annotations = annotations;
   }
}
