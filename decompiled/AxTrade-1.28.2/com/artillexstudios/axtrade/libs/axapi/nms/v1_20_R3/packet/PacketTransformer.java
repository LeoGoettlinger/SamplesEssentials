package com.artillexstudios.axtrade.libs.axapi.nms.v1_20_R3.packet;

import com.artillexstudios.axtrade.libs.axapi.packet.ClientboundPacketTypes;
import com.artillexstudios.axtrade.libs.axapi.packet.FriendlyByteBuf;
import com.artillexstudios.axtrade.libs.axapi.packet.PacketSide;
import com.artillexstudios.axtrade.libs.axapi.packet.wrapper.PacketWrapper;
import com.artillexstudios.axtrade.libs.axapi.reflection.ClassUtils;
import com.artillexstudios.axtrade.libs.axapi.reflection.FieldAccessor;
import com.artillexstudios.axtrade.libs.axapi.utils.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.function.Consumer;
import net.minecraft.network.EnumProtocol;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.VarInt;
import net.minecraft.network.EnumProtocol.a;
import net.minecraft.network.protocol.EnumProtocolDirection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketListenerPlayIn;
import net.minecraft.network.protocol.game.PacketListenerPlayOut;

public final class PacketTransformer {
   private static final FieldAccessor packetSetAccessor = FieldAccessor.builder().withClass(a.class).withField("c").build();
   private static final FieldAccessor classToIdAccessor = FieldAccessor.builder().withClass("net.minecraft.network.EnumProtocol$b").withField("b").build();
   private static final a<?> clientboundCodec = EnumProtocol.b.b(EnumProtocolDirection.b);
   private static final a<?> serverboundCodec = EnumProtocol.b.b(EnumProtocolDirection.a);

   public static Packet<?> transformClientbound(PacketWrapper wrapper) {
      FriendlyByteBufWrapper buf = new FriendlyByteBufWrapper(new PacketDataSerializer(Unpooled.buffer()));
      buf.writeVarInt(ClientboundPacketTypes.forPacketType(wrapper.packetType()));
      wrapper.write(buf);
      return transformClientbound(buf.buf());
   }

   public static Packet<?> transformClientbound(FriendlyByteBuf buf) {
      return transformClientbound(((FriendlyByteBufWrapper)buf).buf());
   }

   public static Packet<PacketListenerPlayOut> transformClientbound(PacketDataSerializer buf) {
      Object var2;
      try {
         return clientboundCodec.a(buf.n(), buf);
      } catch (Exception var6) {
         LogUtils.error("Failed to transform packet!", var6);
         var2 = null;
      } finally {
         buf.release();
      }

      return (Packet<PacketListenerPlayOut>)var2;
   }

   public static Packet<PacketListenerPlayIn> transformServerbound(PacketDataSerializer buf) {
      Object var2;
      try {
         return serverboundCodec.a(buf.n(), buf);
      } catch (Exception var6) {
         LogUtils.error("Failed to transform packet!", var6);
         var2 = null;
      } finally {
         buf.release();
      }

      return (Packet<PacketListenerPlayIn>)var2;
   }

   public static Packet<?> transformServerbound(PacketWrapper wrapper) {
      FriendlyByteBufWrapper buf = new FriendlyByteBufWrapper(new PacketDataSerializer(Unpooled.buffer()));
      wrapper.write(buf);
      return transformServerbound0(buf.buf());
   }

   public static Packet<?> transformServerbound(FriendlyByteBuf buf) {
      return transformServerbound0(((FriendlyByteBufWrapper)buf).buf());
   }

   private static Packet<?> transformServerbound0(PacketDataSerializer buf) {
      Packet var1;
      try {
         var1 = serverboundCodec.a(buf.n(), buf);
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

   public static FriendlyByteBufWrapper transformServerbound(ChannelHandlerContext ctx, Object packet, Consumer<FriendlyByteBuf> consumer) {
      FriendlyByteBufWrapper wrapper;
      if (packet instanceof Packet<?> packetObject) {
         PacketDataSerializer buffer = new PacketDataSerializer(alloc(ctx));
         packetObject.a(buffer);
         wrapper = new FriendlyByteBufWrapper(buffer);
      } else {
         if (!(packet instanceof ByteBuf buffer)) {
            LogUtils.error("Unhandled packet class: {} Pipeline: {}", packet.getClass(), ctx.channel().pipeline().names());
            return null;
         }

         wrapper = new FriendlyByteBufWrapper(new PacketDataSerializer(buffer.copy()));
         wrapper.readVarInt();
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

   public static FriendlyByteBufWrapper transformClientbound(ChannelHandlerContext ctx, Object packet, Consumer<FriendlyByteBuf> consumer) {
      FriendlyByteBufWrapper wrapper;
      if (packet instanceof Packet<?> packetObject) {
         PacketDataSerializer buffer = new PacketDataSerializer(alloc(ctx));
         packetObject.a(buffer);
         wrapper = new FriendlyByteBufWrapper(buffer);
      } else {
         if (!(packet instanceof ByteBuf buffer)) {
            LogUtils.error("Unhandled packet class: {} Pipeline: {}", packet.getClass(), ctx.channel().pipeline().names());
            return null;
         }

         wrapper = new FriendlyByteBufWrapper(new PacketDataSerializer(buffer.copy()));
         wrapper.readVarInt();
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
      FriendlyByteBufWrapper wrapper = new FriendlyByteBufWrapper(new PacketDataSerializer(alloc(ctx)));
      consumer.accept(wrapper);
      return wrapper;
   }

   public static ByteBuf alloc() {
      return alloc(null);
   }

   public static ByteBuf alloc(ChannelHandlerContext ctx) {
      return ctx == null ? Unpooled.buffer() : ctx.alloc().buffer();
   }

   public static int packetId(PacketSide side, Object input) {
      if (input instanceof Packet<?> packet) {
         int packetId;
         if (side == PacketSide.CLIENT_BOUND) {
            packetId = clientboundCodec.a(packet);
         } else {
            packetId = serverboundCodec.a(packet);
         }

         return packetId;
      } else {
         int packetId;
         if (input instanceof ByteBuf buffer) {
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
   }

   public static FriendlyByteBuf wrap(ByteBuf buf) {
      return new FriendlyByteBufWrapper(new PacketDataSerializer(buf));
   }

   public static FriendlyByteBuf copy(FriendlyByteBufWrapper friendlyByteBufWrapper) {
      return wrap(friendlyByteBufWrapper.buf().copy());
   }

   public static String listPackets(PacketSide side) {
      StringBuilder builder = new StringBuilder();
      ObjectIterator var2 = EnumProtocol.b
         .a(side == PacketSide.CLIENT_BOUND ? EnumProtocolDirection.b : EnumProtocolDirection.a)
         .int2ObjectEntrySet()
         .iterator();

      while (var2.hasNext()) {
         Entry<Class<? extends Packet<?>>> key = (Entry<Class<? extends Packet<?>>>)var2.next();
         builder.append(key.getValue()).append('\n');
      }

      return builder.toString();
   }
}
