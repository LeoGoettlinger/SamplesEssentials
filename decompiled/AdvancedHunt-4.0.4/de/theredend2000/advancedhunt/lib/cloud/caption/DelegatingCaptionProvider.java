package de.theredend2000.advancedhunt.lib.cloud.caption;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class DelegatingCaptionProvider<C> implements CaptionProvider<C> {
   @NonNull
   public abstract CaptionProvider<C> delegate();

   @Nullable
   @Override
   public final String provide(@NonNull final Caption caption, @NonNull final C recipient) {
      return this.delegate().provide(caption, recipient);
   }
}
