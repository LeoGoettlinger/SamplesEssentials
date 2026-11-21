package xyz.lychee.lagfixer.libs.kyori.adventure.text.minimessage.translation;

import java.util.Locale;
import org.jetbrains.annotations.NotNull;
import xyz.lychee.lagfixer.libs.kyori.adventure.identity.Identity;
import xyz.lychee.lagfixer.libs.kyori.adventure.pointer.Pointered;
import xyz.lychee.lagfixer.libs.kyori.adventure.pointer.Pointers;

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
