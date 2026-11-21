package org.lushplugins.lushrewards.libraries.mysql.cj.jdbc.exceptions;

import org.lushplugins.lushrewards.libraries.mysql.cj.Messages;
import org.lushplugins.lushrewards.libraries.mysql.cj.jdbc.JdbcConnection;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.PacketSentTimeHolder;

public class ConnectionFeatureNotAvailableException extends CommunicationsException {
   private static final long serialVersionUID = 8315412078945570018L;

   public ConnectionFeatureNotAvailableException(JdbcConnection conn, PacketSentTimeHolder packetSentTimeHolder, Exception underlyingException) {
      super(conn, packetSentTimeHolder, null, underlyingException);
   }

   public ConnectionFeatureNotAvailableException(String message, Throwable underlyingException) {
      super(message, underlyingException);
   }

   @Override
   public String getMessage() {
      return Messages.getString("ConnectionFeatureNotAvailableException.0");
   }

   @Override
   public String getSQLState() {
      return "01S00";
   }
}
