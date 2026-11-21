package org.lushplugins.lushrewards.libraries.mysql.cj.jdbc.exceptions;

import java.sql.SQLException;
import org.lushplugins.lushrewards.libraries.mysql.cj.Messages;

public class PacketTooBigException extends SQLException {
   static final long serialVersionUID = 7248633977685452174L;

   public PacketTooBigException(long packetSize, long maximumPacketSize) {
      super(Messages.getString("PacketTooBigException.0", new Object[]{packetSize, maximumPacketSize}), "S1000");
   }

   public PacketTooBigException(String message) {
      super(message, "S1000");
   }
}
