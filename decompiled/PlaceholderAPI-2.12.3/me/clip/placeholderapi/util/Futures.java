package me.clip.placeholderapi.util;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public final class Futures {
   private Futures() {
   }

   public static <T> void onMainThread(@NotNull PlaceholderAPIPlugin plugin, @NotNull CompletableFuture<T> future, @NotNull BiConsumer<T, Throwable> consumer) {
      future.whenComplete((value, exception) -> {
         if (Bukkit.isPrimaryThread()) {
            consumer.accept((T)value, exception);
         } else {
            plugin.getScheduler().runTask(() -> consumer.accept((T)value, exception));
         }
      });
   }

   @NotNull
   public static <T> Collector<CompletableFuture<T>, ?, CompletableFuture<List<T>>> collector() {
      return Collectors.collectingAndThen(Collectors.toList(), Futures::of);
   }

   @NotNull
   public static <T> CompletableFuture<List<T>> of(@NotNull Stream<CompletableFuture<T>> futures) {
      return of(futures.collect(Collectors.toList()));
   }

   @NotNull
   public static <T> CompletableFuture<List<T>> of(@NotNull Collection<CompletableFuture<T>> futures) {
      return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenApplyAsync($ -> awaitCompletion(futures));
   }

   @NotNull
   private static <T> List<T> awaitCompletion(@NotNull Collection<CompletableFuture<T>> futures) {
      return futures.stream().map(CompletableFuture::join).collect(Collectors.toList());
   }
}
