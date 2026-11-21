package com.viaversion.viaversion.bukkit.handlers;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.platform.ViaInjector;
import com.viaversion.viaversion.bukkit.util.NMSUtil;
import com.viaversion.viaversion.exception.CancelCodecException;
import com.viaversion.viaversion.exception.InformativeException;
import com.viaversion.viaversion.platform.ViaDecodeHandler;
import com.viaversion.viaversion.util.PipelineUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelHandler.Sharable;

@Sharable
public final class BukkitDecodeHandler extends ViaDecodeHandler {
   public BukkitDecodeHandler(UserConnection connection) {
      super(connection);
   }

   @Override
   public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
      if (!PipelineUtil.containsCause(cause, CancelCodecException.class)) {
         super.exceptionCaught(ctx, cause);
         if (!NMSUtil.isDebugPropertySet()) {
            InformativeException exception = PipelineUtil.getCause(cause, InformativeException.class);
            if (exception != null && exception.shouldBePrinted()) {
               cause.printStackTrace();
               exception.setShouldBePrinted(false);
            }
         }
      }
   }

   public void userEventTriggered(ChannelHandlerContext ctx, Object event) throws Exception {
      if (BukkitChannelInitializer.COMPRESSION_ENABLED_EVENT != null && event == BukkitChannelInitializer.COMPRESSION_ENABLED_EVENT) {
         ViaInjector injector = Via.getManager().getInjector();
         ChannelPipeline pipeline = ctx.pipeline();
         pipeline.addAfter("compress", injector.getEncoderName(), pipeline.remove(injector.getEncoderName()));
         pipeline.addAfter("decompress", injector.getDecoderName(), pipeline.remove(injector.getDecoderName()));
      }

      super.userEventTriggered(ctx, event);
   }
}
