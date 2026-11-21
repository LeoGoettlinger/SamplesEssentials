package de.theredend2000.advancedhunt.lib.cloud.minecraft.extras;

import de.theredend2000.advancedhunt.lib.kyori.adventure.audience.Audience;
import org.checkerframework.checker.nullness.qual.NonNull;

final class NativeAudienceProvider<C extends Audience> implements AudienceProvider<C> {
   static final NativeAudienceProvider<?> INSTANCE = new NativeAudienceProvider();

   private NativeAudienceProvider() {
   }

   @NonNull
   public Audience apply(@NonNull final C sender) {
      return sender;
   }
}
