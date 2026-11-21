package de.theredend2000.advancedhunt.lib.cloud.minecraft.extras.caption;

import de.theredend2000.advancedhunt.lib.cloud.caption.Caption;
import de.theredend2000.advancedhunt.lib.cloud.caption.CaptionProvider;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class TranslatableCaption implements Caption {
   private final String key;

   public static Caption translatableCaption(final String key) {
      return new TranslatableCaption(key);
   }

   public static <C> CaptionProvider<C> translatableCaptionProvider() {
      return (caption, recipient) -> caption instanceof TranslatableCaption ? caption.key() : null;
   }

   private TranslatableCaption(final String key) {
      this.key = key;
   }

   @NonNull
   @Override
   public String key() {
      return this.key;
   }
}
