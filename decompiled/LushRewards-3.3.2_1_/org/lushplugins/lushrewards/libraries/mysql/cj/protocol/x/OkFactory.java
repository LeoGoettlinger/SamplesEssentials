package org.lushplugins.lushrewards.libraries.mysql.cj.protocol.x;

import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.ProtocolEntityFactory;

public class OkFactory implements ProtocolEntityFactory<Ok, XMessage> {
   public Ok createFromMessage(XMessage message) {
      return new Ok();
   }
}
