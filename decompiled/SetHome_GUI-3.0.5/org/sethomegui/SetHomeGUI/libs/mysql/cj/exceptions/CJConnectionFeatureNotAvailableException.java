package org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions;

import org.sethomegui.SetHomeGUI.libs.mysql.cj.Messages;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.conf.PropertySet;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.PacketSentTimeHolder;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.ServerSession;

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
