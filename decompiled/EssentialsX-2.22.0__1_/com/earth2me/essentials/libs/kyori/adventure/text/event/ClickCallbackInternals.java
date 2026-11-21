package com.earth2me.essentials.libs.kyori.adventure.text.event;

import com.earth2me.essentials.libs.kyori.adventure.audience.Audience;
import com.earth2me.essentials.libs.kyori.adventure.permission.PermissionChecker;
import com.earth2me.essentials.libs.kyori.adventure.util.Services;
import com.earth2me.essentials.libs.kyori.adventure.util.TriState;
import org.jetbrains.annotations.NotNull;

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
