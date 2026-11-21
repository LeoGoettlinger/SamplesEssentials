package com.donutorders.libs.mysql.cj.exceptions;

import com.donutorders.libs.mysql.cj.Messages;
import com.donutorders.libs.mysql.cj.conf.PropertySet;
import com.donutorders.libs.mysql.cj.protocol.PacketSentTimeHolder;
import com.donutorders.libs.mysql.cj.protocol.ServerSession;

public class CJConnectionFeatureNotAvailableException extends CJCommunicationsException {
   private static final long serialVersionUID = -4129847384681995107L;

   public CJConnectionFeatureNotAvailableException() {
   }

   public CJConnectionFeatureNotAvailableException(
      PropertySet propertySet, ServerSession serverSession, PacketSentTimeHolder packetSentTimeHolder, Exception underlyingException
   ) {
      super(underlyingException);
      this.init(propertySet, serverSession, packetSentTimeHolder, null);
   }

   @Override
   public String getMessage() {
      return Messages.getString("ConnectionFeatureNotAvailableException.0");
   }
}
