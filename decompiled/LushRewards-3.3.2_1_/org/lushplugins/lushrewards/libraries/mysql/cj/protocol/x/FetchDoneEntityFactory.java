package org.lushplugins.lushrewards.libraries.mysql.cj.protocol.x;

import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.ProtocolEntityFactory;

public class FetchDoneEntityFactory implements ProtocolEntityFactory<FetchDoneEntity, XMessage> {
   public FetchDoneEntity createFromMessage(XMessage message) {
      return new FetchDoneEntity();
   }
}
