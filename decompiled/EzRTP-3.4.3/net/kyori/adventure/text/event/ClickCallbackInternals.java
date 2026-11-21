package net.kyori.adventure.text.event;

import java.util.ServiceLoader;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.permission.PermissionChecker;
import net.kyori.adventure.util.Services;
import net.kyori.adventure.util.TriState;

final class ClickCallbackInternals {
   static final PermissionChecker ALWAYS_FALSE = PermissionChecker.always(TriState.FALSE);
   static final ClickCallback.Provider PROVIDER = Services.service(
         ServiceLoader.load(ClickCallback.Provider.class, ClickCallback.Provider.class.getClassLoader()), ClickCallback.Provider.class
      )
      .orElseGet(ClickCallbackInternals.Fallback::new);

   private ClickCallbackInternals() {
   }

   static final class Fallback implements ClickCallback.Provider {
      @Override
      public ClickEvent<?> create(final ClickCallback<Audience> callback, final ClickCallback.Options options) {
         return ClickEvent.suggestCommand("Callbacks are not supported on this platform!");
      }
   }
}
