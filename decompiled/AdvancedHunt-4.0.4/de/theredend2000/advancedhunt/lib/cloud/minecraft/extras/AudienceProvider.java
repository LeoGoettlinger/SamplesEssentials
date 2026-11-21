package de.theredend2000.advancedhunt.lib.cloud.minecraft.extras;

import de.theredend2000.advancedhunt.lib.kyori.adventure.audience.Audience;
import java.util.function.Function;
import org.checkerframework.checker.nullness.qual.NonNull;

@FunctionalInterface
public interface AudienceProvider<C> extends Function<C, Audience> {
   @NonNull
   Audience apply(@NonNull C sender);

   static <C extends Audience> AudienceProvider<C> nativeAudience() {
      return (AudienceProvider<C>)NativeAudienceProvider.INSTANCE;
   }
}
