package dev.faststats.core;

import dev.faststats.core.data.Metric;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Async.Schedule;

public interface Metrics {
   @Token
   @Contract(
      pure = true
   )
   String getToken();

   @Contract(
      pure = true
   )
   Optional<ErrorTracker> getErrorTracker();

   @Contract(
      pure = true
   )
   Metrics.Config getConfig();

   default void ready() {
   }

   @Contract(
      mutates = "this"
   )
   void shutdown();

   public sealed interface Config permits SimpleMetrics.Config {
      @Contract(
         pure = true
      )
      UUID serverId();

      @Contract(
         pure = true
      )
      boolean enabled();

      @Contract(
         pure = true
      )
      boolean errorTracking();

      @Contract(
         pure = true
      )
      boolean additionalMetrics();

      @Contract(
         pure = true
      )
      boolean debug();
   }

   public interface Factory<T, F extends Metrics.Factory<T, F>> {
      @Contract(
         mutates = "this"
      )
      F addMetric(Metric<?> var1) throws IllegalArgumentException;

      @Contract(
         mutates = "this"
      )
      F onFlush(Runnable var1);

      @Contract(
         mutates = "this"
      )
      F errorTracker(ErrorTracker var1);

      @Contract(
         mutates = "this"
      )
      F debug(boolean var1);

      @Contract(
         mutates = "this"
      )
      F token(@Token String var1) throws IllegalArgumentException;

      @Contract(
         mutates = "this"
      )
      F url(URI var1);

      @Schedule
      @Contract(
         value = "_ -> new",
         mutates = "io"
      )
      Metrics create(T var1) throws IllegalStateException;
   }
}
