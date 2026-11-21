package io.github.retrooper.packetevents.injector.handlers;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.exception.PacketProcessException;
import com.github.retrooper.packetevents.netty.buffer.ByteBufHelper;
import com.github.retrooper.packetevents.protocol.ConnectionState;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.util.ExceptionUtil;
import com.github.retrooper.packetevents.util.PacketEventsImplHelper;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDisconnect;
import io.github.retrooper.packetevents.injector.connection.ServerConnectionInitializer;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import io.github.retrooper.packetevents.util.folia.FoliaScheduler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class PacketEventsDecoder extends MessageToMessageDecoder<ByteBuf> {
   public User user;
   public Player player;
   public boolean hasBeenRelocated;

   public PacketEventsDecoder(User user) {
      this.user = user;
   }

   public PacketEventsDecoder(PacketEventsDecoder decoder) {
      this.user = decoder.user;
      this.player = decoder.player;
      this.hasBeenRelocated = decoder.hasBeenRelocated;
   }

   public void read(ChannelHandlerContext ctx, ByteBuf input, List<Object> out) throws Exception {
      try {
         PacketEventsImplHelper.handleServerBoundPacket(ctx.channel(), this.user, this.player, input, true);
         out.add(ByteBufHelper.retain(input));
      } catch (Throwable var5) {
         if (ExceptionUtil.isException(var5, PacketProcessException.class)) {
            throw var5;
         } else {
            throw new PacketProcessException(var5);
         }
      }
   }

   public void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) throws Exception {
      if (buffer.isReadable()) {
         this.read(ctx, buffer, out);
      }
   }

   public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
      if (!ExceptionUtil.isException(cause, PacketProcessException.class)) {
         super.exceptionCaught(ctx, cause);
      } else {
         boolean debug = PacketEvents.getAPI().getSettings().isDebugEnabled() || SpigotReflectionUtil.isMinecraftServerInstanceDebugging();
         if (debug || this.user != null && this.user.getDecoderState() != ConnectionState.HANDSHAKING) {
            if (PacketEvents.getAPI().getSettings().isFullStackTraceEnabled()) {
               String state = this.user != null ? this.user.getDecoderState().name() : "null";
               String clientVersion = this.user != null ? this.user.getClientVersion().getReleaseName() : "null";
               PacketEvents.getAPI()
                  .getLogManager()
                  .warn(
                     "An error occurred while processing a packet from "
                        + this.user.getProfile().getName()
                        + " (state: "
                        + state
                        + ", clientVersion: "
                        + clientVersion
                        + ", serverVersion: "
                        + PacketEvents.getAPI().getServerManager().getVersion().getReleaseName()
                        + ")",
                     cause
                  );
            } else {
               PacketEvents.getAPI().getLogManager().warn(cause.getMessage());
            }
         }

         if (PacketEvents.getAPI().getSettings().isKickOnPacketExceptionEnabled()) {
            try {
               if (this.user != null) {
                  this.user.sendPacket((PacketWrapper<?>)(new WrapperPlayServerDisconnect(Component.text("Invalid packet"))));
               }
            } catch (Exception var6) {
            }

            ctx.channel().close();
            if (this.player != null) {
               FoliaScheduler.getEntityScheduler()
                  .runDelayed(this.player, (Plugin)PacketEvents.getAPI().getPlugin(), o -> this.player.kickPlayer("Invalid packet"), null, 1L);
            }

            if (this.user != null && this.user.getProfile().getName() != null) {
               PacketEvents.getAPI().getLogManager().warn("Disconnected " + this.user.getProfile().getName() + " due to an invalid packet!");
            }
         }
      }
   }

   public void userEventTriggered(final ChannelHandlerContext ctx, final Object event) throws Exception {
      if (PacketEventsEncoder.COMPRESSION_ENABLED_EVENT != null && event == PacketEventsEncoder.COMPRESSION_ENABLED_EVENT) {
         ServerConnectionInitializer.relocateHandlers(ctx.channel(), this, this.user);
         super.userEventTriggered(ctx, event);
      } else {
         super.userEventTriggered(ctx, event);
      }
   }
}
