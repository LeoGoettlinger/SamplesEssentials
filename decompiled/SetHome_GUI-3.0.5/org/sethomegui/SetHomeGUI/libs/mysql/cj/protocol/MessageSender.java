package org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.CJOperationNotSupportedException;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.ExceptionFactory;

public interface MessageSender<M extends Message> {
   default void send(byte[] message, int messageLen, byte messageSequence) throws IOException {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   default void send(M message) {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   default CompletableFuture<?> send(M message, CompletableFuture<?> future, Runnable callback) {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   default void setMaxAllowedPacket(int maxAllowedPacket) {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   default MessageSender<M> undecorateAll() {
      return this;
   }

   default MessageSender<M> undecorate() {
      return this;
   }
}
