package com.donutorders.libs.mysql.cj.exceptions;

import com.donutorders.libs.mysql.cj.conf.PropertySet;
import com.donutorders.libs.mysql.cj.protocol.PacketReceivedTimeHolder;
import com.donutorders.libs.mysql.cj.protocol.PacketSentTimeHolder;
import com.donutorders.libs.mysql.cj.protocol.ServerSession;

public class CJCommunicationsException extends CJException {
   private static final long serialVersionUID = 344035358493554245L;

   public CJCommunicationsException() {
   }

   public CJCommunicationsException(String message) {
      super(message);
   }

   public CJCommunicationsException(String message, Throwable cause) {
      super(message, cause);
   }

   public CJCommunicationsException(Throwable cause) {
      super(cause);
   }

   protected CJCommunicationsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
      super(message, cause, enableSuppression, writableStackTrace);
   }

   public void init(
      PropertySet propertySet, ServerSession serverSession, PacketSentTimeHolder packetSentTimeHolder, PacketReceivedTimeHolder packetReceivedTimeHolder
   ) {
      this.exceptionMessage = ExceptionFactory.createLinkFailureMessageBasedOnHeuristics(
         propertySet, serverSession, packetSentTimeHolder, packetReceivedTimeHolder, this.getCause()
      );
   }
}
