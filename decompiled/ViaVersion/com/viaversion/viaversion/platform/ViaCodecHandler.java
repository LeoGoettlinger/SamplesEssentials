package com.viaversion.viaversion.platform;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.platform.ViaChannelHandler;
import com.viaversion.viaversion.exception.CancelCodecException;
import com.viaversion.viaversion.exception.CancelDecoderException;
import com.viaversion.viaversion.exception.CancelEncoderException;
import com.viaversion.viaversion.util.ByteBufUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.MessageToMessageCodec;
import java.util.List;

public class ViaCodecHandler extends MessageToMessageCodec<ByteBuf, ByteBuf> implements ViaChannelHandler {
   public static final String NAME = "via-codec";
   protected final UserConnection connection;

   public ViaCodecHandler(UserConnection connection) {
      this.connection = connection;
   }

   protected void encode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
      if (!this.connection.checkOutgoingPacket()) {
         throw CancelEncoderException.generate(null);
      } else {
         ByteBuf transformedBuf = ByteBufUtil.copy(ctx.alloc(), in);

         try {
            if (this.connection.shouldTransformPacket()) {
               this.connection.transformOutgoing(transformedBuf, CancelEncoderException::generate);
            }

            out.add(transformedBuf.retain());
         } finally {
            transformedBuf.release();
         }
      }
   }

   protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
      if (!this.connection.checkIncomingPacket(in.readableBytes())) {
         throw CancelDecoderException.generate(null);
      } else {
         ByteBuf transformedBuf = ByteBufUtil.copy(ctx.alloc(), in);

         try {
            if (this.connection.shouldTransformPacket()) {
               this.connection.transformIncoming(transformedBuf, CancelDecoderException::generate);
            }

            out.add(transformedBuf.retain());
         } finally {
            transformedBuf.release();
         }
      }
   }

   public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
      try {
         super.write(ctx, msg, promise);
      } catch (Throwable var5) {
         if (!(var5 instanceof CancelCodecException)) {
            throw var5;
         }

         promise.setSuccess();
      }
   }

   public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
      try {
         super.channelRead(ctx, msg);
      } catch (Throwable var4) {
         if (!(var4 instanceof CancelCodecException)) {
            throw var4;
         }
      }
   }

   @Override
   public UserConnection connection() {
      return this.connection;
   }
}
