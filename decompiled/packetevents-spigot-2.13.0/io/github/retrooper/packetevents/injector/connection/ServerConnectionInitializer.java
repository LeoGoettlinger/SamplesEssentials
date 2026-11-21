package io.github.retrooper.packetevents.injector.connection;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.UserConnectEvent;
import com.github.retrooper.packetevents.netty.channel.ChannelHelper;
import com.github.retrooper.packetevents.protocol.ConnectionState;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.util.FakeChannelUtil;
import com.github.retrooper.packetevents.util.PacketEventsImplHelper;
import io.github.retrooper.packetevents.injector.handlers.PacketEventsDecoder;
import io.github.retrooper.packetevents.injector.handlers.PacketEventsEncoder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import java.util.NoSuchElementException;

public class ServerConnectionInitializer {
   public static void initChannel(Object ch, ConnectionState connectionState) {
      Channel channel = (Channel)ch;
      if (!FakeChannelUtil.isFakeChannel(channel)) {
         User user = new User(channel, connectionState, null, new UserProfile(null, null));
         if (connectionState == ConnectionState.PLAY) {
            user.setClientVersion(PacketEvents.getAPI().getServerManager().getVersion().toClientVersion());
            PacketEvents.getAPI().getLogManager().warn("Late injection detected, we missed packets so some functionality may break!");
         }

         synchronized (channel) {
            if (channel.pipeline().get("splitter") == null) {
               channel.close();
            } else {
               UserConnectEvent connectEvent = new UserConnectEvent(user);
               PacketEvents.getAPI().getEventManager().callEvent(connectEvent);
               if (connectEvent.isCancelled()) {
                  channel.unsafe().closeForcibly();
               } else {
                  relocateHandlers(channel, null, user);
                  channel.closeFuture()
                     .addListener((ChannelFutureListener)future -> PacketEventsImplHelper.handleDisconnection(user.getChannel(), user.getUUID()));
                  PacketEvents.getAPI().getProtocolManager().setUser(channel, user);
               }
            }
         }
      }
   }

   public static void destroyHandlers(Object ch) {
      Channel channel = (Channel)ch;
      if (channel.pipeline().get(PacketEvents.DECODER_NAME) != null) {
         channel.pipeline().remove(PacketEvents.DECODER_NAME);
      } else {
         PacketEvents.getAPI().getLogManager().warn("Could not find decoder handler in channel pipeline!");
      }

      if (channel.pipeline().get(PacketEvents.ENCODER_NAME) != null) {
         channel.pipeline().remove(PacketEvents.ENCODER_NAME);
      } else {
         PacketEvents.getAPI().getLogManager().warn("Could not find encoder handler in channel pipeline!");
      }
   }

   public static void relocateHandlers(Channel ctx, PacketEventsDecoder decoder, User user) {
      try {
         ChannelHandler encoder;
         if (decoder != null) {
            if (decoder.hasBeenRelocated) {
               return;
            }

            decoder.hasBeenRelocated = true;
            decoder = (PacketEventsDecoder)ctx.pipeline().remove(PacketEvents.DECODER_NAME);
            encoder = ctx.pipeline().remove(PacketEvents.ENCODER_NAME);
            decoder = new PacketEventsDecoder(decoder);
            encoder = new PacketEventsEncoder(encoder);
         } else {
            encoder = new PacketEventsEncoder(user);
            decoder = new PacketEventsDecoder(user);
         }

         String decoderName = ctx.pipeline().names().contains("inbound_config") ? "inbound_config" : "decoder";
         ctx.pipeline().addBefore(decoderName, PacketEvents.DECODER_NAME, decoder);
         String encoderName = ctx.pipeline().names().contains("outbound_config") ? "outbound_config" : "encoder";
         ctx.pipeline().addBefore(encoderName, PacketEvents.ENCODER_NAME, encoder);
      } catch (NoSuchElementException var6) {
         String handlers = ChannelHelper.pipelineHandlerNamesAsString(ctx);
         throw new IllegalStateException("PacketEvents failed to add a decoder to the netty pipeline. Pipeline handlers: " + handlers, var6);
      }
   }
}
