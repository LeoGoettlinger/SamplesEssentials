package com.viaversion.viaversion.velocity.handlers;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.platform.ViaInjector;
import com.viaversion.viaversion.platform.ViaChannelInitializer;
import com.viaversion.viaversion.platform.ViaEncodeHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;

public class VelocityChannelInitializer extends ViaChannelInitializer {
   public static final String MINECRAFT_ENCODER = "minecraft-encoder";
   public static final String MINECRAFT_DECODER = "minecraft-decoder";
   public static final Object COMPRESSION_ENABLED_EVENT;

   public VelocityChannelInitializer(ChannelInitializer<Channel> original, boolean clientSide) {
      super(original, clientSide);
   }

   @Override
   protected void injectPipeline(ChannelPipeline pipeline, UserConnection connection) {
      ViaInjector injector = Via.getManager().getInjector();
      pipeline.addBefore("minecraft-encoder", injector.getEncoderName(), new ViaEncodeHandler(connection));
      pipeline.addBefore("minecraft-decoder", injector.getDecoderName(), new VelocityDecodeHandler(connection));
   }

   static {
      try {
         Class<?> eventClass = Class.forName("com.velocitypowered.proxy.protocol.VelocityConnectionEvent");
         COMPRESSION_ENABLED_EVENT = eventClass.getDeclaredField("COMPRESSION_ENABLED").get(null);
      } catch (ReflectiveOperationException var1) {
         throw new RuntimeException(var1);
      }
   }
}
