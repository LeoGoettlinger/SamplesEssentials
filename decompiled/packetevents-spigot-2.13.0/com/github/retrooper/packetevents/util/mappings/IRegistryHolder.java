package com.github.retrooper.packetevents.util.mappings;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.mapper.MappedEntity;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public interface IRegistryHolder {
   default <T extends MappedEntity> IRegistry<T> getRegistryOr(IRegistry<T> fallbackRegistry) {
      return this.getRegistryOr(fallbackRegistry, PacketEvents.getAPI().getServerManager().getVersion().toClientVersion());
   }

   default <T extends MappedEntity> IRegistry<T> getRegistryOr(IRegistry<T> fallbackRegistry, ClientVersion version) {
      IRegistry<?> replacedRegistry = this.getRegistry(fallbackRegistry.getRegistryKey(), version);
      return (IRegistry<T>)(replacedRegistry != null ? replacedRegistry : fallbackRegistry);
   }

   @Nullable
   default IRegistry<?> getRegistry(ResourceLocation registryKey) {
      return this.getRegistry(registryKey, PacketEvents.getAPI().getServerManager().getVersion().toClientVersion());
   }

   @Nullable
   IRegistry<?> getRegistry(ResourceLocation registryKey, ClientVersion version);
}
