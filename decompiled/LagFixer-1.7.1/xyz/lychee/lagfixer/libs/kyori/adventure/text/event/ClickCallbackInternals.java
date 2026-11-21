package xyz.lychee.lagfixer.libs.kyori.adventure.text.event;

import org.jetbrains.annotations.NotNull;
import xyz.lychee.lagfixer.libs.kyori.adventure.audience.Audience;
import xyz.lychee.lagfixer.libs.kyori.adventure.permission.PermissionChecker;
import xyz.lychee.lagfixer.libs.kyori.adventure.util.Services;
import xyz.lychee.lagfixer.libs.kyori.adventure.util.TriState;

final class ClickCallbackInternals {
   static final PermissionChecker ALWAYS_FALSE = PermissionChecker.always(TriState.FALSE);
   static final ClickCallback.Provider PROVIDER = Services.service(ClickCallback.Provider.class).orElseGet(ClickCallbackInternals.Fallback::new);

   private ClickCallbackInternals() {
   }

   static final class Fallback implements ClickCallback.Provider {
      @NotNull
      @Override
      public ClickEvent create(@NotNull final ClickCallback<Audience> callback, @NotNull final ClickCallback.Options options) {
         return ClickEvent.suggestCommand("Callbacks are not supported on this platform!");
      }
   }
}
