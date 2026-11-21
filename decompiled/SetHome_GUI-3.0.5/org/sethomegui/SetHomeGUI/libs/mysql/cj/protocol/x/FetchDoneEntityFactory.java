package org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.x;

import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.ProtocolEntityFactory;

public class FetchDoneEntityFactory implements ProtocolEntityFactory<FetchDoneEntity, XMessage> {
   public FetchDoneEntity createFromMessage(XMessage message) {
      return new FetchDoneEntity();
   }
}
