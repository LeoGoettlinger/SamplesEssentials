package com.artillexstudios.axrankmenu.libs.axapi.nms.v1_21_R2.packet;

import com.artillexstudios.axrankmenu.libs.axapi.packet.ClientboundPacketTypes;
import com.artillexstudios.axrankmenu.libs.axapi.packet.FriendlyByteBuf;
import com.artillexstudios.axrankmenu.libs.axapi.packet.PacketSide;
import com.artillexstudios.axrankmenu.libs.axapi.packet.wrapper.PacketWrapper;
import com.artillexstudios.axrankmenu.libs.axapi.reflection.ClassUtils;
import com.artillexstudios.axrankmenu.libs.axapi.reflection.FieldAccessor;
import com.artillexstudios.axrankmenu.libs.axapi.utils.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.IdDispatchCodec;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.EnumProtocolDirection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.GameProtocols;
import net.minecraft.network.protocol.game.PacketListenerPlayIn;
import net.minecraft.network.protocol.game.PacketListenerPlayOut;
import net.minecraft.server.MinecraftServer;

public final class PacketTransformer {
   private static final Function<ByteBuf, RegistryFriendlyByteBuf> decorator = RegistryFriendlyByteBuf.a(MinecraftServer.getServer().ba());
   private static final StreamCodec<ByteBuf, Packet<? super PacketListenerPlayOut>> clientboundCodec = GameProtocols.b.a(decorator).c();
   private static final StreamCodec<ByteBuf, Packet<? super PacketListenerPlayIn>> serverboundCodec = GameProtocols.a.a(decorator).c();
   private static final FieldAccessor toIdAccessor = FieldAccessor.builder().withClass(IdDispatchCodec.class).withField("d").build();
   private static final Object2IntMap<PacketType<?>> clientboundIds = toIdAccessor.getUnchecked(clientboundCodec);
   private static final Object2IntMap<PacketType<?>> serverboundIds = toIdAccessor.getUnchecked(serverboundCodec);

   public static Packet<?> transformClientbound(PacketWrapper wrapper) {
      FriendlyByteBufWrapper buf = new FriendlyByteBufWrapper(decorator.apply(Unpooled.buffer()));
      buf.writeVarInt(ClientboundPacketTypes.forPacketType(wrapper.packetType()));
      wrapper.write(buf);
      return transformClientbound(buf.buf());
   }

   public static Packet<?> transformClientbound(FriendlyByteBuf buf) {
      return transformClientbound(((FriendlyByteBufWrapper)buf).buf());
   }

   public static Packet<? super PacketListenerPlayOut> transformClientbound(PacketDataSerializer buf) {
      Object var2;
      try {
         return (Packet<? super PacketListenerPlayOut>)clientboundCodec.decode(buf);
      } catch (Exception var6) {
         LogUtils.error("Failed to transform packet!", var6);
         var2 = null;
      } finally {
         buf.release();
      }

      return (Packet<? super PacketListenerPlayOut>)var2;
   }

   public static Packet<? super PacketListenerPlayIn> transformServerbound(PacketDataSerializer buf) {
      Object var2;
      try {
         return (Packet<? super PacketListenerPlayIn>)serverboundCodec.decode(buf);
      } catch (Exception var6) {
         LogUtils.error("Failed to transform packet!", var6);
         var2 = null;
      } finally {
         buf.release();
      }

      return (Packet<? super PacketListenerPlayIn>)var2;
   }

   public static Packet<?> transformServerbound(PacketWrapper wrapper) {
      FriendlyByteBufWrapper buf = new FriendlyByteBufWrapper(decorator.apply(Unpooled.buffer()));
      wrapper.write(buf);
      return transformServerbound0(buf.buf());
   }

   public static Packet<?> transformServerbound(FriendlyByteBuf buf) {
      return transformServerbound0(((FriendlyByteBufWrapper)buf).buf());
   }

   private static Packet<?> transformServerbound0(PacketDataSerializer buf) {
      Packet var1;
      try {
         var1 = (Packet)serverboundCodec.decode(buf);
      } finally {
         buf.release();
      }

      return var1;
   }

   public static FriendlyByteBuf transformServerbound(ChannelHandlerContext ctx, Packet<?> packet) {
      return transformServerbound(ctx, packet, buf -> {});
   }

   public static FriendlyByteBuf transformServerbound(Packet<?> packet) {
      return transformServerbound(null, packet, buf -> {});
   }

   public static FriendlyByteBuf transformServerbound(Packet<?> packet, Consumer<FriendlyByteBuf> consumer) {
      return transformServerbound(null, packet, consumer);
   }

   public static FriendlyByteBuf transformServerbound(ChannelHandlerContext ctx, Object packet, Consumer<FriendlyByteBuf> consumer) {
      FriendlyByteBufWrapper wrapper;
      if (packet instanceof Packet) {
         ByteBuf buffer = alloc(ctx);
         serverboundCodec.encode(buffer, (Packet)packet);
         wrapper = new FriendlyByteBufWrapper(decorator.apply(buffer));
      } else {
         if (!(packet instanceof ByteBuf buffer)) {
            LogUtils.error("Unhandled packet class: {}", packet.getClass());
            return null;
         }

         wrapper = new FriendlyByteBufWrapper(decorator.apply(buffer.copy()));
      }

      consumer.accept(wrapper);
      return wrapper;
   }

   public static FriendlyByteBuf transformClientbound(ChannelHandlerContext ctx, Packet<?> packet) {
      return transformClientbound(ctx, packet, buf -> {});
   }

   public static FriendlyByteBuf transformClientbound(Packet<?> packet) {
      return transformClientbound(null, packet, buf -> {});
   }

   public static FriendlyByteBuf transformClientbound(Packet<?> packet, Consumer<FriendlyByteBuf> consumer) {
      return transformClientbound(null, packet, consumer);
   }

   public static FriendlyByteBuf transformClientbound(ChannelHandlerContext ctx, Object packet, Consumer<FriendlyByteBuf> consumer) {
      FriendlyByteBufWrapper wrapper;
      if (packet instanceof Packet) {
         ByteBuf buffer = alloc(ctx);
         clientboundCodec.encode(buffer, (Packet)packet);
         wrapper = new FriendlyByteBufWrapper(decorator.apply(buffer));
      } else {
         if (!(packet instanceof ByteBuf buffer)) {
            LogUtils.error("Unhandled packet class: {}", packet.getClass());
            return null;
         }

         wrapper = new FriendlyByteBufWrapper(decorator.apply(buffer.copy()));
      }

      consumer.accept(wrapper);
      return wrapper;
   }

   public static FriendlyByteBuf newByteBuf() {
      return newByteBuf(null, buf -> {});
   }

   public static FriendlyByteBuf newByteBuf(ChannelHandlerContext ctx) {
      return newByteBuf(ctx, buf -> {});
   }

   public static FriendlyByteBuf newByteBuf(Consumer<FriendlyByteBuf> consumer) {
      return newByteBuf(null, consumer);
   }

   public static FriendlyByteBuf newByteBuf(ChannelHandlerContext ctx, Consumer<FriendlyByteBuf> consumer) {
      FriendlyByteBufWrapper wrapper = new FriendlyByteBufWrapper(decorator.apply(alloc(ctx)));
      consumer.accept(wrapper);
      return wrapper;
   }

   public static ByteBuf alloc() {
      return alloc(null);
   }

   public static ByteBuf alloc(ChannelHandlerContext ctx) {
      return ctx == null ? Unpooled.buffer() : ctx.alloc().buffer();
   }

   public static int packetId(Object input) {
      int packetId;
      if (input instanceof Packet<?> packet) {
         PacketType<?> type = packet.a();
         if (type.a() == EnumProtocolDirection.b) {
            packetId = clientboundIds.getOrDefault(type, -1);
         } else {
            packetId = serverboundIds.getOrDefault(type, -1);
         }
      } else if (input instanceof ByteBuf buffer) {
         int readerIndex = buffer.readerIndex();
         int writerIndex = buffer.writerIndex();
         packetId = VarInt.a(buffer);
         buffer.readerIndex(readerIndex);
         buffer.writerIndex(writerIndex);
      } else {
         LogUtils.warn("Unhandled packet class: {}", ClassUtils.INSTANCE.debugClass(input.getClass()));
         packetId = -1;
      }

      return packetId;
   }

   public static FriendlyByteBuf wrap(ByteBuf buf) {
      return new FriendlyByteBufWrapper(decorator.apply(buf));
   }

   public static FriendlyByteBuf copy(FriendlyByteBufWrapper friendlyByteBufWrapper) {
      return wrap(friendlyByteBufWrapper.buf().copy());
   }

   public static String listPackets(PacketSide side) {
      StringBuilder builder = new StringBuilder();
      if (side == PacketSide.CLIENT_BOUND) {
         GameProtocols.b.a((type, id) -> builder.append(type.b()).append('\n'));
      } else {
         GameProtocols.a.a((type, id) -> builder.append(type.b()).append('\n'));
      }

      return builder.toString();
   }
}
