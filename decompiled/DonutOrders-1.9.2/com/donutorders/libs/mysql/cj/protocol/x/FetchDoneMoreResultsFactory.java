package com.donutorders.libs.mysql.cj.protocol.x;

import com.donutorders.libs.mysql.cj.protocol.ProtocolEntityFactory;

public class FetchDoneMoreResultsFactory implements ProtocolEntityFactory<FetchDoneMoreResults, XMessage> {
   public FetchDoneMoreResults createFromMessage(XMessage message) {
      return new FetchDoneMoreResults();
   }
}
