package org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.x;

import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.ProtocolEntityFactory;

public class FetchDoneMoreResultsFactory implements ProtocolEntityFactory<FetchDoneMoreResults, XMessage> {
   public FetchDoneMoreResults createFromMessage(XMessage message) {
      return new FetchDoneMoreResults();
   }
}
