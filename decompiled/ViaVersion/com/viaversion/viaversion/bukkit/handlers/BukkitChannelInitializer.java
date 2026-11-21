package com.viaversion.viaversion.bukkit.handlers;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.platform.ViaInjector;
import com.viaversion.viaversion.platform.ViaChannelInitializer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class BukkitChannelInitializer extends ViaChannelInitializer {
   public static final String MINECRAFT_ENCODER = "encoder";
   public static final String MINECRAFT_DECODER = "decoder";
   public static final String MINECRAFT_OUTBOUND_CONFIG = "outbound_config";
   public static final String MINECRAFT_COMPRESSOR = "compress";
   public static final String MINECRAFT_DECOMPRESSOR = "decompress";
   public static final Object COMPRESSION_ENABLED_EVENT = paperCompressionEnabledEvent();

   @Nullable
   private static Object paperCompressionEnabledEvent() {
      try {
         Class<?> eventClass = Class.forName("io.papermc.paper.network.ConnectionEvent");
         return eventClass.getDeclaredField("COMPRESSION_THRESHOLD_SET").get(null);
      } catch (ReflectiveOperationException var1) {
         return null;
      }
   }

   public BukkitChannelInitializer(ChannelInitializer<Channel> original) {
      super(original, false);
   }

   @Override
   protected void injectPipeline(ChannelPipeline pipeline, UserConnection connection) {
      injectMinecraftPipeline(pipeline, connection);
   }

   private static void injectMinecraftPipeline(ChannelPipeline pipeline, UserConnection connection) {
      ViaInjector injector = Via.getManager().getInjector();
      String encoderName = pipeline.get("outbound_config") != null ? "outbound_config" : "encoder";
      pipeline.addBefore(encoderName, injector.getEncoderName(), new BukkitEncodeHandler(connection));
      pipeline.addBefore("decoder", injector.getDecoderName(), new BukkitDecodeHandler(connection));
   }

   public static void afterChannelInitialize(Channel channel) {
      injectMinecraftPipeline(channel.pipeline(), createUserConnection(channel, false));
   }
}
