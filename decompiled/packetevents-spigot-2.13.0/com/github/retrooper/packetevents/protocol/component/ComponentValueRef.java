package com.github.retrooper.packetevents.protocol.component;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.netty.buffer.ByteBufHelper;
import com.github.retrooper.packetevents.netty.buffer.UnpooledByteBufAllocationHelper;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.util.mappings.GlobalRegistryHolder;
import com.github.retrooper.packetevents.util.mappings.IRegistryHolder;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.Map;
import java.util.WeakHashMap;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface ComponentValueRef<T> {
   @Internal
   static <T> ComponentValueRef<T> ofBytes(ComponentType<T> type, byte[] bytes, ClientVersion version) {
      ServerVersion serverVersion = version.toServerVersion();
      return asCached(registries -> {
         Object byteBuf = UnpooledByteBufAllocationHelper.wrappedBuffer(bytes);

         Object var6;
         try {
            PacketWrapper<?> wrapper = PacketWrapper.createUniversalPacketWrapper(byteBuf, serverVersion);
            wrapper.setRegistryHolder(registries);
            var6 = type.read(wrapper);
         } finally {
            ByteBufHelper.release(byteBuf);
         }

         return (T)var6;
      });
   }

   static <T> ComponentValueRef<T> ofStatic(T value) {
      return registries -> value;
   }

   static <T> ComponentValueRef<T> asCached(ComponentValueRef<T> delegate) {
      return new ComponentValueRef<T>() {
         private final Map<IRegistryHolder, T> values = new WeakHashMap<>();

         @Override
         public synchronized T resolve(IRegistryHolder registries) {
            return this.values.computeIfAbsent(registries, delegate::resolve);
         }
      };
   }

   @Deprecated
   default T resolveUsingGlobals() {
      return this.resolve(GlobalRegistryHolder.INSTANCE);
   }

   T resolve(IRegistryHolder registries);
}
