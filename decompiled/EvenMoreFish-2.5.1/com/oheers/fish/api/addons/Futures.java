package com.oheers.fish.api.addons;

import com.oheers.fish.api.utils.Scheduling;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;

public final class Futures {
   private Futures() {
   }

   public static <T> void onMainThread(@NonNull Plugin plugin, @NonNull CompletableFuture<T> future, @NonNull BiConsumer<T, Throwable> consumer) {
      future.whenComplete((value, exception) -> {
         if (Bukkit.isPrimaryThread()) {
            consumer.accept((T)value, exception);
         } else {
            Scheduling.getInstance().runTask(() -> consumer.accept((T)value, exception));
         }
      });
   }

   @NonNull
   public static <T> Collector<CompletableFuture<T>, ?, CompletableFuture<List<T>>> collector() {
      return Collectors.collectingAndThen(Collectors.toList(), Futures::of);
   }

   @NonNull
   public static <T> CompletableFuture<List<T>> of(@NonNull Stream<CompletableFuture<T>> futures) {
      return of(futures.collect(Collectors.toList()));
   }

   @NonNull
   public static <T> CompletableFuture<List<T>> of(@NonNull Collection<CompletableFuture<T>> futures) {
      return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenApplyAsync($ -> awaitCompletion(futures));
   }

   @NonNull
   private static <T> List<T> awaitCompletion(@NonNull Collection<CompletableFuture<T>> futures) {
      return futures.stream().map(CompletableFuture::join).collect(Collectors.toList());
   }
}
