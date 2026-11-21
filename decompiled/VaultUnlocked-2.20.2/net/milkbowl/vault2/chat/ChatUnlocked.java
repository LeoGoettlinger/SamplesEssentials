package net.milkbowl.vault2.chat;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.milkbowl.vault2.helper.context.Context;
import net.milkbowl.vault2.helper.subject.Subject;
import org.jetbrains.annotations.NotNull;

public interface ChatUnlocked {
   boolean isEnabled();

   @NotNull
   String getName();

   boolean hasGroupSupport();

   Optional<String> getPrefix(@NotNull Context context, @NotNull Subject subject);

   CompletableFuture<Optional<String>> getPrefixAsync(@NotNull Context context, @NotNull Subject subject);

   default String getPrefixOrNull(@NotNull final Context context, @NotNull final Subject subject) {
      return this.getPrefix(context, subject).orElse(null);
   }

   Optional<String> getSuffix(@NotNull Context context, @NotNull Subject subject);

   CompletableFuture<Optional<String>> getSuffixAsync(@NotNull Context context, @NotNull Subject subject);

   default String getSuffixOrNull(@NotNull final Context context, @NotNull final Subject subject) {
      return this.getSuffix(context, subject).orElse(null);
   }

   boolean setPrefix(@NotNull Context context, @NotNull Subject subject, @NotNull String prefix);

   CompletableFuture<Boolean> setPrefixAsync(@NotNull Context context, @NotNull Subject subject, @NotNull String prefix);

   boolean copyPrefix(@NotNull Context context, @NotNull Subject from, @NotNull Subject to);

   CompletableFuture<Boolean> copyPrefixAsync(@NotNull Context context, @NotNull Subject from, @NotNull Subject to);

   boolean setSuffix(@NotNull Context context, @NotNull Subject subject, @NotNull String suffix);

   CompletableFuture<Boolean> setSuffixAsync(@NotNull Context context, @NotNull Subject subject, @NotNull String suffix);

   boolean copySuffix(@NotNull Context context, @NotNull Subject from, @NotNull Subject to);

   CompletableFuture<Boolean> copySuffixAsync(@NotNull Context context, @NotNull Subject from, @NotNull Subject to);

   <T> Optional<T> get(final Context context, final Subject subject, final InfoKey<T> key);

   <T> CompletableFuture<Optional<T>> getAsync(final Context context, final Subject subject, final InfoKey<T> key);

   default <T> T getOrDefault(final Context context, final Subject subject, final InfoKey<T> key, final T defaultValue) {
      Optional<T> value = this.get(context, subject, key);
      return value.orElse(defaultValue);
   }

   <T> boolean set(final Context context, final Subject subject, final InfoKey<T> key, final T value);

   <T> CompletableFuture<Boolean> setAsync(final Context context, final Subject subject, final InfoKey<T> key, final T value);
}
