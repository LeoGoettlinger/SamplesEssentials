package com.artillexstudios.axtrade.libs.lamp.util;

import com.artillexstudios.axtrade.libs.lamp.core.BaseCommandDispatcher;
import com.artillexstudios.axtrade.libs.lamp.core.reflect.MethodCaller;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

public final class StackTraceSanitizer {
   private static final StackTraceSanitizer DEFAULT_SANITIZER = builder()
      .ignoreClasses(BaseCommandDispatcher.class)
      .ignoreClasses(MethodHandles.class, MethodHandle.class)
      .ignorePackage(MethodCaller.class.getPackage())
      .build();
   private static final StackTraceSanitizer EMPTY = new StackTraceSanitizer(java.util.Collections.emptyList());
   @Unmodifiable
   private final List<Predicate<StackTraceElement>> filters;

   public void sanitize(@NotNull Throwable throwable) {
      if (!this.filters.isEmpty()) {
         if (throwable.getCause() != null) {
            this.sanitize(throwable.getCause());
         }

         List<StackTraceElement> trace = Collections.listOf(throwable.getStackTrace());
         int stripIndex = trace.size();

         for (int i = 0; i < trace.size(); i++) {
            StackTraceElement stackTraceElement = trace.get(i);
            if (this.filters.stream().anyMatch(f -> f.test(stackTraceElement))) {
               stripIndex = i;
               break;
            }
         }

         trace.subList(stripIndex, trace.size()).clear();
         throwable.setStackTrace(trace.toArray(new StackTraceElement[0]));
      }
   }

   private StackTraceSanitizer(@Unmodifiable List<Predicate<StackTraceElement>> filters) {
      this.filters = filters;
   }

   public static StackTraceSanitizer defaultSanitizer() {
      return DEFAULT_SANITIZER;
   }

   public static StackTraceSanitizer empty() {
      return EMPTY;
   }

   public static StackTraceSanitizer.Builder builder() {
      return new StackTraceSanitizer.Builder();
   }

   public static class Builder {
      private final List<Predicate<StackTraceElement>> filters = new ArrayList<>();

      public StackTraceSanitizer.Builder ignoreClasses(@NotNull Class<?>... classes) {
         for (Class<?> clazz : classes) {
            this.filters.add(c -> c.getClassName().equals(clazz.getName()));
         }

         return this;
      }

      public StackTraceSanitizer.Builder ignorePackage(@NotNull String packageName) {
         this.filters.add(c -> c.getClassName().startsWith(packageName));
         return this;
      }

      public StackTraceSanitizer.Builder ignorePackage(@NotNull Package pkg) {
         this.filters.add(c -> c.getClassName().startsWith(pkg.getName()));
         return this;
      }

      public StackTraceSanitizer.Builder ignoreMethod(@NotNull String methodName) {
         this.filters.add(c -> c.getMethodName().equals(methodName));
         return this;
      }

      public StackTraceSanitizer.Builder ignoreNativeMethods() {
         this.filters.add(StackTraceElement::isNativeMethod);
         return this;
      }

      public StackTraceSanitizer build() {
         return new StackTraceSanitizer(java.util.Collections.unmodifiableList(new ArrayList<>(this.filters)));
      }
   }
}
