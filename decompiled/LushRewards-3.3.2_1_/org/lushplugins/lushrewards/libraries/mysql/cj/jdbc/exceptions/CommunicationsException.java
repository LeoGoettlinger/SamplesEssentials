package org.lushplugins.lushrewards.libraries.mysql.cj.jdbc.exceptions;

import java.sql.SQLRecoverableException;
import org.lushplugins.lushrewards.libraries.mysql.cj.Messages;
import org.lushplugins.lushrewards.libraries.mysql.cj.exceptions.ExceptionFactory;
import org.lushplugins.lushrewards.libraries.mysql.cj.exceptions.StreamingNotifiable;
import org.lushplugins.lushrewards.libraries.mysql.cj.jdbc.JdbcConnection;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.PacketReceivedTimeHolder;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.PacketSentTimeHolder;

public class CommunicationsException extends SQLRecoverableException implements StreamingNotifiable {
   private static final long serialVersionUID = 4317904269000988676L;
   private String exceptionMessage;

   public CommunicationsException(
      JdbcConnection conn, PacketSentTimeHolder packetSentTimeHolder, PacketReceivedTimeHolder packetReceivedTimeHolder, Exception underlyingException
   ) {
      this(
         ExceptionFactory.createLinkFailureMessageBasedOnHeuristics(
            conn.getPropertySet(), conn.getSession().getServerSession(), packetSentTimeHolder, packetReceivedTimeHolder, underlyingException
         ),
         underlyingException
      );
   }

   public CommunicationsException(String message, Throwable underlyingException) {
      this.exceptionMessage = message;
      if (underlyingException != null) {
         this.initCause(underlyingException);
      }
   }

   @Override
   public String getMessage() {
      return this.exceptionMessage;
   }

   @Override
   public String getSQLState() {
      return "08S01";
   }

   @Override
   public void setWasStreamingResults() {
      this.exceptionMessage = Messages.getString("CommunicationsException.ClientWasStreaming");
   }
}
