package org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.x;

import com.google.protobuf.MessageLite;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.Messages;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.CJCommunicationsException;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.CJPacketTooBigException;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.MessageSender;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.PacketSentTimeHolder;

public class SyncMessageSender implements MessageSender<XMessage>, PacketSentTimeHolder {
   static final int HEADER_LEN = 5;
   private OutputStream outputStream;
   private long lastPacketSentTime = 0L;
   private long previousPacketSentTime = 0L;
   private int maxAllowedPacket = -1;
   final Lock syncOperationLock = new ReentrantLock();

   public SyncMessageSender(OutputStream os) {
      this.outputStream = os;
   }

   public void send(XMessage message) {
      this.syncOperationLock.lock();

      try {
         MessageLite msg = message.getMessage();

         try {
            int type = MessageConstants.getTypeForMessageClass((Class<? extends MessageLite>)msg.getClass());
            int size = 1 + msg.getSerializedSize();
            if (this.maxAllowedPacket > 0 && size > this.maxAllowedPacket) {
               throw new CJPacketTooBigException(Messages.getString("PacketTooBigException.1", new Object[]{size, this.maxAllowedPacket}));
            }

            byte[] sizeHeader = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(size).array();
            this.outputStream.write(sizeHeader);
            this.outputStream.write(type);
            msg.writeTo(this.outputStream);
            this.outputStream.flush();
            this.previousPacketSentTime = this.lastPacketSentTime;
            this.lastPacketSentTime = System.currentTimeMillis();
         } catch (IOException var9) {
            throw new CJCommunicationsException("Unable to write message", var9);
         }
      } finally {
         this.syncOperationLock.unlock();
      }
   }

   public CompletableFuture<?> send(XMessage message, CompletableFuture<?> future, Runnable callback) {
      this.syncOperationLock.lock();

      CompletableFuture var13;
      try {
         CompletionHandler<Long, Void> resultHandler = new ErrorToFutureCompletionHandler<>(future, callback);
         MessageLite msg = message.getMessage();

         try {
            this.send(message);
            long result = 5 + msg.getSerializedSize();
            resultHandler.completed(result, null);
         } catch (Throwable var11) {
            resultHandler.failed(var11, null);
         }

         var13 = future;
      } finally {
         this.syncOperationLock.unlock();
      }

      return var13;
   }

   @Override
   public long getLastPacketSentTime() {
      return this.lastPacketSentTime;
   }

   @Override
   public long getPreviousPacketSentTime() {
      return this.previousPacketSentTime;
   }

   @Override
   public void setMaxAllowedPacket(int maxAllowedPacket) {
      this.maxAllowedPacket = maxAllowedPacket;
   }
}
