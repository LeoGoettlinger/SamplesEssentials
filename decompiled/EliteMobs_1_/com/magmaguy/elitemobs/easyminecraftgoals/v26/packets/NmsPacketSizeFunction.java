package com.magmaguy.elitemobs.easyminecraftgoals.v26.packets;

import com.magmaguy.elitemobs.easyminecraftgoals.internal.PacketSizeEstimator;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.MinecraftServer;

public final class NmsPacketSizeFunction implements PacketSizeEstimator.PacketSizeFunction {
   private static final StreamCodec<?, ?> NO_CODEC = StreamCodec.of((b, v) -> {}, b -> null);
   private final Map<Class<?>, StreamCodec<?, ?>> codecCache = new ConcurrentHashMap<>();
   private volatile RegistryAccess registryAccess;

   @Override
   public int sizeOf(Object packetObj) {
      if (!(packetObj instanceof Packet<?> packet)) {
         return -1;
      } else {
         StreamCodec codec = this.resolveCodec(packet.getClass());
         if (codec != null && codec != NO_CODEC) {
            RegistryAccess access = this.registryAccess();
            if (access == null) {
               return -1;
            } else {
               ByteBuf raw = Unpooled.buffer();

               byte var7;
               try {
                  RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(raw, access);
                  codec.encode(buf, packet);
                  return buf.readableBytes();
               } catch (Throwable var11) {
                  var7 = -1;
               } finally {
                  raw.release();
               }

               return var7;
            }
         } else {
            return -1;
         }
      }
   }

   private StreamCodec<?, ?> resolveCodec(Class<?> clazz) {
      StreamCodec<?, ?> cached = this.codecCache.get(clazz);
      if (cached != null) {
         return cached;
      } else {
         StreamCodec<?, ?> resolved = NO_CODEC;

         try {
            Field field = clazz.getField("STREAM_CODEC");
            if (field.get(null) instanceof StreamCodec<?, ?> sc) {
               resolved = sc;
            }
         } catch (Throwable var7) {
         }

         this.codecCache.put(clazz, resolved);
         return resolved;
      }
   }

   private RegistryAccess registryAccess() {
      RegistryAccess access = this.registryAccess;
      if (access != null) {
         return access;
      } else {
         try {
            MinecraftServer server = MinecraftServer.getServer();
            if (server != null) {
               access = server.registryAccess();
               this.registryAccess = access;
            }
         } catch (Throwable var3) {
         }

         return access;
      }
   }
}
