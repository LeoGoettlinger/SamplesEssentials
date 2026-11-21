package com.earth2me.essentials.libs.kyori.adventure.text.minimessage.translation;

import com.earth2me.essentials.libs.kyori.adventure.identity.Identity;
import com.earth2me.essentials.libs.kyori.adventure.pointer.Pointered;
import com.earth2me.essentials.libs.kyori.adventure.pointer.Pointers;
import java.util.Locale;
import org.jetbrains.annotations.NotNull;

final class LocalePointered implements Pointered {
   private final Pointers pointers;

   LocalePointered(@NotNull final Locale locale) {
      this.pointers = Pointers.builder().withStatic(Identity.LOCALE, locale).build();
   }

   @NotNull
   @Override
   public Pointers pointers() {
      return this.pointers;
   }
}
