package com.viaversion.viaversion.platform;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.platform.ViaInjector;
import com.viaversion.viaversion.connection.UserConnectionImpl;
import com.viaversion.viaversion.protocol.ProtocolPipelineImpl;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import java.lang.reflect.Method;

public abstract class ViaChannelInitializer extends ChannelInitializer<Channel> implements WrappedChannelInitializer {
   private static final Method INIT_CHANNEL_METHOD;
   private final ChannelInitializer<Channel> original;
   private final boolean clientSide;

   protected ViaChannelInitializer(ChannelInitializer<Channel> original, boolean clientSide) {
      this.original = original;
      this.clientSide = clientSide;
   }

   protected void initChannel(Channel channel) throws Exception {
      INIT_CHANNEL_METHOD.invoke(this.original, channel);
      UserConnection connection = createUserConnection(channel, this.clientSide);
      this.injectPipeline(channel.pipeline(), connection);
   }

   public static UserConnection createUserConnection(Channel channel, boolean clientSide) {
      UserConnection connection = new UserConnectionImpl(channel, clientSide);
      new ProtocolPipelineImpl(connection);
      return connection;
   }

   public static void reorderPipeline(ChannelPipeline pipeline, String compress, String decompress) {
      ViaInjector injector = Via.getManager().getInjector();
      int decompressIndex = pipeline.names().indexOf(decompress);
      if (decompressIndex != -1) {
         if (decompressIndex > pipeline.names().indexOf(injector.getDecoderName())) {
            ChannelHandler encoderHandler = pipeline.remove(injector.getEncoderName());
            ChannelHandler decoderHandler = pipeline.remove(injector.getDecoderName());
            pipeline.addAfter(compress, injector.getEncoderName(), encoderHandler);
            pipeline.addAfter(decompress, injector.getDecoderName(), decoderHandler);
         }
      }
   }

   protected abstract void injectPipeline(ChannelPipeline var1, UserConnection var2);

   @Override
   public ChannelInitializer<Channel> original() {
      return this.original;
   }

   public boolean clientSide() {
      return this.clientSide;
   }

   static {
      try {
         INIT_CHANNEL_METHOD = ChannelInitializer.class.getDeclaredMethod("initChannel", Channel.class);
         INIT_CHANNEL_METHOD.setAccessible(true);
      } catch (ReflectiveOperationException var1) {
         throw new RuntimeException(var1);
      }
   }
}
