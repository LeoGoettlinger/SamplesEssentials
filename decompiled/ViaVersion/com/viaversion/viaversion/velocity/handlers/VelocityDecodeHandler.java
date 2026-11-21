package com.viaversion.viaversion.velocity.handlers;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.platform.ViaInjector;
import com.viaversion.viaversion.platform.ViaDecodeHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelHandler.Sharable;

@Sharable
public class VelocityDecodeHandler extends ViaDecodeHandler {
   public VelocityDecodeHandler(UserConnection connection) {
      super(connection);
   }

   public void userEventTriggered(ChannelHandlerContext ctx, Object event) throws Exception {
      if (event == VelocityChannelInitializer.COMPRESSION_ENABLED_EVENT) {
         ViaInjector injector = Via.getManager().getInjector();
         ChannelPipeline pipeline = ctx.pipeline();
         ChannelHandler encoder = pipeline.remove(injector.getEncoderName());
         pipeline.addBefore("minecraft-encoder", injector.getEncoderName(), encoder);
         ChannelHandler decoder = pipeline.remove(injector.getDecoderName());
         pipeline.addBefore("minecraft-decoder", injector.getDecoderName(), decoder);
      }

      super.userEventTriggered(ctx, event);
   }
}
