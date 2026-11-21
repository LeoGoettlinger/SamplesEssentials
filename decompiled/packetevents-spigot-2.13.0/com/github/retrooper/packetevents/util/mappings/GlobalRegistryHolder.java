package com.github.retrooper.packetevents.util.mappings;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public final class GlobalRegistryHolder implements IRegistryHolder {
   public static final IRegistryHolder INSTANCE = new GlobalRegistryHolder();

   private GlobalRegistryHolder() {
   }

   public static Object getGlobalRegistryCacheKey(@Nullable User user, ClientVersion version) {
      return version;
   }

   @Nullable
   @Override
   public IRegistry<?> getRegistry(ResourceLocation registryKey, ClientVersion version) {
      SynchronizedRegistriesHandler.RegistryEntry<?> registryEntry = SynchronizedRegistriesHandler.getRegistryEntry(registryKey);
      if (registryEntry == null) {
         return null;
      } else {
         Object cacheKey = getGlobalRegistryCacheKey(null, version);
         return registryEntry.getSyncedRegistry(cacheKey);
      }
   }
}
