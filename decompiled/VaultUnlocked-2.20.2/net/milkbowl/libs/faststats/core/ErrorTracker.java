package net.milkbowl.libs.faststats.core;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;
import org.intellij.lang.annotations.RegExp;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

public sealed interface ErrorTracker permits SimpleErrorTracker {
   @Contract(" -> new")
   static ErrorTracker contextAware() {
      SimpleErrorTracker tracker = new SimpleErrorTracker();
      tracker.attachErrorContext(ErrorTracker.class.getClassLoader());
      return tracker;
   }

   @Contract(" -> new")
   static ErrorTracker contextUnaware() {
      return new SimpleErrorTracker();
   }

   @Contract(
      mutates = "this"
   )
   void trackError(String var1);

   @Contract(
      mutates = "this"
   )
   void trackError(Throwable var1);

   @Contract(
      mutates = "this"
   )
   void trackError(String var1, boolean var2);

   @Contract(
      mutates = "this"
   )
   void trackError(Throwable var1, boolean var2);

   @Contract(
      value = "_ -> this",
      mutates = "this"
   )
   ErrorTracker ignoreError(Class<? extends Throwable> var1);

   @Contract(
      value = "_ -> this",
      mutates = "this"
   )
   ErrorTracker ignoreError(Pattern var1);

   @Contract(
      value = "_ -> this",
      mutates = "this"
   )
   default ErrorTracker ignoreError(@RegExp String pattern) {
      return this.ignoreError(Pattern.compile(pattern));
   }

   @Contract(
      value = "_, _ -> this",
      mutates = "this"
   )
   ErrorTracker ignoreError(Class<? extends Throwable> var1, Pattern var2);

   @Contract(
      value = "_, _ -> this",
      mutates = "this"
   )
   default ErrorTracker ignoreError(Class<? extends Throwable> type, @RegExp String pattern) {
      return this.ignoreError(type, Pattern.compile(pattern));
   }

   @Contract(
      value = "_, _ -> this",
      mutates = "this"
   )
   ErrorTracker anonymize(Pattern var1, String var2);

   @Contract(
      value = "_, _ -> this",
      mutates = "this"
   )
   default ErrorTracker anonymize(@RegExp String pattern, String replacement) {
      return this.anonymize(Pattern.compile(pattern), replacement);
   }

   void attachErrorContext(@Nullable ClassLoader var1) throws IllegalStateException;

   void detachErrorContext();

   boolean isContextAttached();

   @Contract(
      mutates = "this"
   )
   void setContextErrorHandler(@Nullable BiConsumer<ClassLoader, Throwable> var1);

   @Contract(
      pure = true
   )
   Optional<BiConsumer<ClassLoader, Throwable>> getContextErrorHandler();

   @Contract(
      pure = true
   )
   static boolean isSameLoader(ClassLoader loader, Throwable error) {
      return ErrorHelper.isSameLoader(loader, error);
   }
}
