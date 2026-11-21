package dev.faststats.core.data;

import com.google.gson.JsonElement;
import java.util.Optional;
import java.util.concurrent.Callable;
import org.jetbrains.annotations.Contract;

public interface Metric<T> {
   @SourceId
   @Contract(
      pure = true
   )
   String getId();

   @Contract(
      pure = true
   )
   Optional<T> compute() throws Exception;

   @Contract(
      pure = true
   )
   Optional<JsonElement> getData() throws Exception;

   @Contract(
      value = "_, _ -> new",
      pure = true
   )
   static Metric<String[]> stringArray(@SourceId String id, Callable<String[]> callable) throws IllegalArgumentException {
      return new ArrayMetric<>(id, callable);
   }

   @Contract(
      value = "_, _ -> new",
      pure = true
   )
   static Metric<Boolean[]> booleanArray(@SourceId String id, Callable<Boolean[]> callable) throws IllegalArgumentException {
      return new ArrayMetric<>(id, callable);
   }

   @Contract(
      value = "_, _ -> new",
      pure = true
   )
   static Metric<Number[]> numberArray(@SourceId String id, Callable<Number[]> callable) throws IllegalArgumentException {
      return new ArrayMetric<>(id, callable);
   }

   @Contract(
      value = "_, _ -> new",
      pure = true
   )
   static Metric<Boolean> bool(@SourceId String id, Callable<Boolean> callable) throws IllegalArgumentException {
      return new SingleValueMetric<>(id, callable);
   }

   @Contract(
      value = "_, _ -> new",
      pure = true
   )
   static Metric<String> string(@SourceId String id, Callable<String> callable) throws IllegalArgumentException {
      return new SingleValueMetric<>(id, callable);
   }

   @Contract(
      value = "_, _ -> new",
      pure = true
   )
   static Metric<Number> number(@SourceId String id, Callable<Number> callable) throws IllegalArgumentException {
      return new SingleValueMetric<>(id, callable);
   }
}
