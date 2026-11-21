package de.theredend2000.advancedhunt.lib.kyori.adventure.text.event;

import de.theredend2000.advancedhunt.lib.kyori.adventure.audience.Audience;
import de.theredend2000.advancedhunt.lib.kyori.adventure.permission.PermissionChecker;
import de.theredend2000.advancedhunt.lib.kyori.adventure.util.Services;
import de.theredend2000.advancedhunt.lib.kyori.adventure.util.TriState;
import java.util.ServiceLoader;

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
