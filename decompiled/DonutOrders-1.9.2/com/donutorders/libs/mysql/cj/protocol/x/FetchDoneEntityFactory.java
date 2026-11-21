package com.donutorders.libs.mysql.cj.protocol.x;

import com.donutorders.libs.mysql.cj.protocol.ProtocolEntityFactory;

public class FetchDoneEntityFactory implements ProtocolEntityFactory<FetchDoneEntity, XMessage> {
   public FetchDoneEntity createFromMessage(XMessage message) {
      return new FetchDoneEntity();
   }
}
