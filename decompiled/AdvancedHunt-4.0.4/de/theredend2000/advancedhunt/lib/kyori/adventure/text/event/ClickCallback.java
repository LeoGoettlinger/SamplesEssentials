package de.theredend2000.advancedhunt.lib.kyori.adventure.text.event;

import de.theredend2000.advancedhunt.lib.kyori.adventure.audience.Audience;
import de.theredend2000.advancedhunt.lib.kyori.adventure.builder.AbstractBuilder;
import de.theredend2000.advancedhunt.lib.kyori.adventure.permission.PermissionChecker;
import de.theredend2000.advancedhunt.lib.kyori.adventure.util.PlatformAPI;
import java.time.Duration;
import java.time.temporal.TemporalAmount;
import java.util.function.Consumer;
import java.util.function.Predicate;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jspecify.annotations.Nullable;

@FunctionalInterface
public interface ClickCallback<T extends Audience> {
   Duration DEFAULT_LIFETIME = Duration.ofHours(12L);
   int UNLIMITED_USES = -1;

   @CheckReturnValue
   @Contract(
      pure = true
   )
   static <W extends Audience, N extends W> ClickCallback<W> widen(
      final ClickCallback<N> original, final Class<N> type, @Nullable final Consumer<? super Audience> otherwise
   ) {
      return audience -> {
         if (type.isInstance(audience)) {
            original.accept((N)((Audience)type.cast(audience)));
         } else if (otherwise != null) {
            otherwise.accept(audience);
         }
      };
   }

   @CheckReturnValue
   @Contract(
      pure = true
   )
   static <W extends Audience, N extends W> ClickCallback<W> widen(final ClickCallback<N> original, final Class<N> type) {
      return widen(original, type, null);
   }

   void accept(final T audience);

   @CheckReturnValue
   @Contract(
      pure = true
   )
   default ClickCallback<T> filter(final Predicate<T> filter) {
      return this.filter(filter, null);
   }

   @CheckReturnValue
   @Contract(
      pure = true
   )
   default ClickCallback<T> filter(final Predicate<T> filter, @Nullable final Consumer<? super Audience> otherwise) {
      return audience -> {
         if (filter.test(audience)) {
            this.accept(audience);
         } else if (otherwise != null) {
            otherwise.accept(audience);
         }
      };
   }

   @CheckReturnValue
   @Contract(
      pure = true
   )
   default ClickCallback<T> requiringPermission(final String permission) {
      return this.requiringPermission(permission, null);
   }

   @CheckReturnValue
   @Contract(
      pure = true
   )
   default ClickCallback<T> requiringPermission(final String permission, @Nullable final Consumer<? super Audience> otherwise) {
      return this.filter(audience -> audience.getOrDefault(PermissionChecker.POINTER, ClickCallbackInternals.ALWAYS_FALSE).test(permission), otherwise);
   }

   public sealed interface Options permits ClickCallbackOptionsImpl {
      static ClickCallback.Options.Builder builder() {
         return new ClickCallbackOptionsImpl.BuilderImpl();
      }

      static ClickCallback.Options.Builder builder(final ClickCallback.Options existing) {
         return new ClickCallbackOptionsImpl.BuilderImpl(existing);
      }

      int uses();

      Duration lifetime();

      public sealed interface Builder extends AbstractBuilder<ClickCallback.Options> permits ClickCallbackOptionsImpl.BuilderImpl {
         ClickCallback.Options.Builder uses(int useCount);

         ClickCallback.Options.Builder lifetime(final TemporalAmount duration);
      }
   }

   @PlatformAPI
   @Internal
   public interface Provider {
      ClickEvent<?> create(final ClickCallback<Audience> callback, final ClickCallback.Options options);
   }
}
