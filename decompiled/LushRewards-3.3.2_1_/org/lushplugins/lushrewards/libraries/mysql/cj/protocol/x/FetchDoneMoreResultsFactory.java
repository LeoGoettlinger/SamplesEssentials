package org.lushplugins.lushrewards.libraries.mysql.cj.protocol.x;

import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.ProtocolEntityFactory;

public class FetchDoneMoreResultsFactory implements ProtocolEntityFactory<FetchDoneMoreResults, XMessage> {
   public FetchDoneMoreResults createFromMessage(XMessage message) {
      return new FetchDoneMoreResults();
   }
}
