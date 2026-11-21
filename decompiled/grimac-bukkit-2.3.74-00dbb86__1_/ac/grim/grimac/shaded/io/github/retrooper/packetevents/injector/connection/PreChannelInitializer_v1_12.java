package ac.grim.grimac.shaded.io.github.retrooper.packetevents.injector.connection;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.ConnectionState;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

public class PreChannelInitializer_v1_12 extends ChannelInboundHandlerAdapter {
   private static final InternalLogger logger = InternalLoggerFactory.getInstance(ChannelInitializer.class);

   public void channelRegistered(ChannelHandlerContext ctx) {
      try {
         ServerConnectionInitializer.initChannel(ctx.channel(), ConnectionState.HANDSHAKING);
      } catch (Throwable var7) {
         this.exceptionCaught(ctx, var7);
      } finally {
         ChannelPipeline pipeline = ctx.pipeline();
         if (pipeline.context(this) != null) {
            pipeline.remove(this);
         }
      }

      ctx.pipeline().fireChannelRegistered();
   }

   public void exceptionCaught(ChannelHandlerContext ctx, Throwable t) {
      logger.warn("Failed to initialize a channel. Closing: " + ctx.channel(), t);
      ctx.close();
   }
}
