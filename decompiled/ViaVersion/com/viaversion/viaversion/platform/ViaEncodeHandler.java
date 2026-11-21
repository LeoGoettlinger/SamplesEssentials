package com.viaversion.viaversion.platform;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.platform.ViaChannelHandler;
import com.viaversion.viaversion.exception.CancelCodecException;
import com.viaversion.viaversion.exception.CancelEncoderException;
import com.viaversion.viaversion.util.ByteBufUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.codec.MessageToMessageEncoder;
import java.util.List;

@Sharable
public class ViaEncodeHandler extends MessageToMessageEncoder<ByteBuf> implements ViaChannelHandler {
   public static final String NAME = "via-encoder";
   protected final UserConnection connection;

   public ViaEncodeHandler(UserConnection connection) {
      this.connection = connection;
   }

   protected void encode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) throws Exception {
      if (!this.connection.checkOutgoingPacket()) {
         throw CancelEncoderException.generate(null);
      } else if (!this.connection.shouldTransformPacket()) {
         out.add(buf.retain());
      } else {
         ByteBuf transformedBuf = ByteBufUtil.copy(ctx.alloc(), buf);

         try {
            this.connection.transformOutgoing(transformedBuf, CancelEncoderException::generate);
            out.add(transformedBuf.retain());
         } finally {
            transformedBuf.release();
         }
      }
   }

   public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
      if (!(cause instanceof CancelCodecException)) {
         super.exceptionCaught(ctx, cause);
      }
   }

   public boolean isSharable() {
      return true;
   }

   @Override
   public UserConnection connection() {
      return this.connection;
   }
}
