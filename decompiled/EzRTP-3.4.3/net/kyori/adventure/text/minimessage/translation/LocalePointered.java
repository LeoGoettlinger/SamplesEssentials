package net.kyori.adventure.text.minimessage.translation;

import java.util.Locale;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.pointer.Pointered;
import net.kyori.adventure.pointer.Pointers;
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
