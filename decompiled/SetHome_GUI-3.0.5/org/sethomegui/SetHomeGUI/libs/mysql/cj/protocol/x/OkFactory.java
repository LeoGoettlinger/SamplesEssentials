package org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.x;

import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.ProtocolEntityFactory;

public class OkFactory implements ProtocolEntityFactory<Ok, XMessage> {
   public Ok createFromMessage(XMessage message) {
      return new Ok();
   }
}
