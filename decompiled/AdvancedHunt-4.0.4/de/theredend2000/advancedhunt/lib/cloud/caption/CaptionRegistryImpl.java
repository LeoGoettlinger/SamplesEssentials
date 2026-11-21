package de.theredend2000.advancedhunt.lib.cloud.caption;

import java.util.LinkedList;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.common.returnsreceiver.qual.This;

@API(
   status = API.Status.INTERNAL
)
public final class CaptionRegistryImpl<C> implements CaptionRegistry<C> {
   private final LinkedList<CaptionProvider<C>> providers = new LinkedList<>();

   CaptionRegistryImpl() {
   }

   @NonNull
   @Override
   public String caption(@NonNull final Caption caption, @NonNull final C sender) {
      for (CaptionProvider<C> provider : this.providers) {
         String result = provider.provide(caption, sender);
         if (result != null) {
            return result;
         }
      }

      throw new IllegalArgumentException(String.format("There is no caption stored with key '%s'", caption));
   }

   @This
   @NonNull
   @Override
   public CaptionRegistry<C> registerProvider(@NonNull final CaptionProvider<C> provider) {
      this.providers.addFirst(provider);
      return this;
   }
}
