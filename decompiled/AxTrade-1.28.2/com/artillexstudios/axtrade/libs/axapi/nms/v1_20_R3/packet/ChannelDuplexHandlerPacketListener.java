package com.artillexstudios.axtrade.libs.axapi.nms.v1_20_R3.packet;

import com.artillexstudios.axtrade.libs.axapi.packet.ClientboundPacketTypes;
import com.artillexstudios.axtrade.libs.axapi.packet.FriendlyByteBuf;
import com.artillexstudios.axtrade.libs.axapi.packet.PacketEvent;
import com.artillexstudios.axtrade.libs.axapi.packet.PacketEvents;
import com.artillexstudios.axtrade.libs.axapi.packet.PacketSide;
import com.artillexstudios.axtrade.libs.axapi.packet.PacketType;
import com.artillexstudios.axtrade.libs.axapi.packet.ServerboundPacketTypes;
import com.artillexstudios.axtrade.libs.axapi.utils.featureflags.FeatureFlags;
import com.artillexstudios.axtrade.libs.axapi.utils.logging.LogUtils;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.PacketListenerPlayIn;
import net.minecraft.network.protocol.game.PacketListenerPlayOut;
import org.bukkit.entity.Player;

public final class ChannelDuplexHandlerPacketListener extends ChannelDuplexHandler {
   private final Player player;

   public ChannelDuplexHandlerPacketListener(Player player) {
      this.player = player;
   }

   public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
      if (!PacketEvents.INSTANCE.listening()) {
         super.write(ctx, msg, promise);
      } else if (!(msg instanceof Packet)) {
         super.write(ctx, msg, promise);
      } else if (msg instanceof ClientboundBundlePacket bundlePacket) {
         if (FeatureFlags.DEBUG_OUTGOING_PACKETS.get()) {
            LogUtils.info("Bundle packet");
         }

         List<Packet<PacketListenerPlayOut>> packets = new ArrayList<>();

         for (Packet<PacketListenerPlayOut> subPacket : bundlePacket.a()) {
            int packetId = PacketTransformer.packetId(PacketSide.CLIENT_BOUND, subPacket);
            if (packetId == -1) {
               super.write(ctx, msg, promise);
               return;
            }

            PacketType type = ClientboundPacketTypes.forPacketId(packetId);
            if (FeatureFlags.DEBUG_OUTGOING_PACKETS.get()) {
               LogUtils.info("(bundle) Packet id: {}, class: {}, type: {}", packetId, subPacket.getClass(), type);
            }

            PacketEvent event = new PacketEvent(this.player, PacketSide.CLIENT_BOUND, type, () -> {
               try {
                  FriendlyByteBuf buf = PacketTransformer.transformClientbound(ctx, subPacket, fbb -> {});
                  if (buf == null) {
                     super.write(ctx, msg, promise);
                     return null;
                  } else {
                     return buf;
                  }
               } catch (Exception var8x) {
                  try {
                     super.write(ctx, msg, promise);
                     return null;
                  } catch (Exception var7x) {
                     throw new RuntimeException(var7x);
                  }
               }
            });
            PacketEvents.INSTANCE.callEvent(event);
            if (event.cancelled()) {
               FriendlyByteBufWrapper out = (FriendlyByteBufWrapper)event.directOut();
               if (out != null) {
                  out.buf().release();
               }

               FriendlyByteBufWrapper in = (FriendlyByteBufWrapper)event.directIn();
               if (in != null) {
                  in.buf().release();
               }
            } else {
               FriendlyByteBufWrapper outx = (FriendlyByteBufWrapper)event.directOut();
               FriendlyByteBufWrapper in = (FriendlyByteBufWrapper)event.directIn();
               if (in != null) {
                  in.buf().release();
               }

               if (outx == null) {
                  packets.add(subPacket);
               } else {
                  packets.add(PacketTransformer.transformClientbound(outx.buf()));
               }
            }
         }

         super.write(ctx, new ClientboundBundlePacket(packets), promise);
      } else {
         int packetIdx = PacketTransformer.packetId(PacketSide.CLIENT_BOUND, msg);
         if (packetIdx == -1) {
            super.write(ctx, msg, promise);
         } else {
            PacketType typex = ClientboundPacketTypes.forPacketId(packetIdx);
            if (FeatureFlags.DEBUG_OUTGOING_PACKETS.get()) {
               LogUtils.info("Packet id: {}, class: {}, type: {}", packetIdx, msg.getClass(), typex);
            }

            PacketEvent event = new PacketEvent(this.player, PacketSide.CLIENT_BOUND, typex, () -> {
               try {
                  FriendlyByteBuf buf = PacketTransformer.transformClientbound(ctx, msg, fbb -> {});
                  if (buf == null) {
                     super.write(ctx, msg, promise);
                     return null;
                  } else {
                     return buf;
                  }
               } catch (Exception var7x) {
                  try {
                     super.write(ctx, msg, promise);
                     return null;
                  } catch (Exception var6x) {
                     throw new RuntimeException(var6x);
                  }
               }
            });

            try {
               PacketEvents.INSTANCE.callEvent(event);
            } catch (RuntimeException var13) {
               LogUtils.info("Packet id: {}, class: {}, type: {}", packetIdx, msg.getClass(), typex);
            }

            if (event.cancelled()) {
               if (FeatureFlags.DEBUG_OUTGOING_PACKETS.get()) {
                  LogUtils.info("Cancelled event!");
               }

               FriendlyByteBufWrapper outxx = (FriendlyByteBufWrapper)event.directOut();
               if (outxx != null) {
                  outxx.buf().release();
               }

               FriendlyByteBufWrapper inx = (FriendlyByteBufWrapper)event.directIn();
               if (inx != null) {
                  inx.buf().release();
               }
            } else {
               FriendlyByteBufWrapper inx = (FriendlyByteBufWrapper)event.directIn();
               if (inx != null) {
                  inx.buf().release();
               }

               FriendlyByteBufWrapper outxxx = (FriendlyByteBufWrapper)event.directOut();
               if (outxxx == null) {
                  if (FeatureFlags.DEBUG_OUTGOING_PACKETS.get()) {
                     LogUtils.info("Unchanged!");
                  }

                  super.write(ctx, msg, promise);
               } else {
                  if (FeatureFlags.DEBUG_OUTGOING_PACKETS.get()) {
                     LogUtils.info("Changed!");
                  }

                  Packet<? super PacketListenerPlayOut> transformed = PacketTransformer.transformClientbound(outxxx.buf());
                  if (transformed == null) {
                     super.write(ctx, msg, promise);
                  } else {
                     super.write(ctx, transformed, promise);
                  }
               }
            }
         }
      }
   }

   public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
      if (!PacketEvents.INSTANCE.listening()) {
         super.channelRead(ctx, msg);
      } else if (!(msg instanceof Packet)) {
         super.channelRead(ctx, msg);
      } else if (msg instanceof ServerboundCustomPayloadPacket var11) {
         ServerboundCustomPayloadPacket var10000 = var11;

         try {
            var10000.a();
         } catch (Throwable var10) {
            throw new MatchException(var10.toString(), var10);
         }

         super.channelRead(ctx, msg);
      } else {
         int packetId = PacketTransformer.packetId(PacketSide.SERVER_BOUND, msg);
         if (packetId == -1) {
            super.channelRead(ctx, msg);
         } else {
            PacketType type = ServerboundPacketTypes.forPacketId(packetId);
            if (FeatureFlags.DEBUG_INCOMING_PACKETS.get()) {
               LogUtils.info("Incoming packet id: {}, class: {}, type: {}", packetId, msg.getClass(), type);
            }

            PacketEvent event = new PacketEvent(this.player, PacketSide.SERVER_BOUND, type, () -> {
               try {
                  FriendlyByteBuf buf = PacketTransformer.transformServerbound(ctx, msg, fbb -> {});
                  if (buf == null) {
                     super.channelRead(ctx, msg);
                     return null;
                  } else {
                     return buf;
                  }
               } catch (Exception var6x) {
                  try {
                     super.channelRead(ctx, msg);
                     return null;
                  } catch (Exception var5x) {
                     throw new RuntimeException(var5x);
                  }
               }
            });
            PacketEvents.INSTANCE.callEvent(event);
            if (event.cancelled()) {
               if (FeatureFlags.DEBUG_INCOMING_PACKETS.get()) {
                  LogUtils.info("Incoming cancelled event!");
               }

               FriendlyByteBufWrapper in = (FriendlyByteBufWrapper)event.directIn();
               if (in != null) {
                  in.buf().release();
               }

               FriendlyByteBufWrapper out = (FriendlyByteBufWrapper)event.directOut();
               if (out != null) {
                  out.buf().release();
               }
            } else {
               FriendlyByteBufWrapper inx = (FriendlyByteBufWrapper)event.directIn();
               if (inx != null) {
                  inx.buf().release();
               }

               FriendlyByteBufWrapper out = (FriendlyByteBufWrapper)event.directOut();
               if (out == null) {
                  if (FeatureFlags.DEBUG_INCOMING_PACKETS.get()) {
                     LogUtils.info("Incoming unchanged!");
                  }

                  super.channelRead(ctx, msg);
               } else {
                  if (FeatureFlags.DEBUG_INCOMING_PACKETS.get()) {
                     LogUtils.info("Incoming changed!");
                  }

                  Packet<? super PacketListenerPlayIn> transformed = PacketTransformer.transformServerbound(out.buf());
                  if (transformed == null) {
                     super.channelRead(ctx, msg);
                  } else {
                     super.channelRead(ctx, transformed);
                  }
               }
            }
         }
      }
   }
}
