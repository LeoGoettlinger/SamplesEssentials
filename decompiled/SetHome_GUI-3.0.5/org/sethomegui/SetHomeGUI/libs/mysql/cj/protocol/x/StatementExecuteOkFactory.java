package org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.x;

import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.ProtocolEntityFactory;

public class StatementExecuteOkFactory implements ProtocolEntityFactory<StatementExecuteOk, XMessage> {
   public StatementExecuteOk createFromMessage(XMessage message) {
      return new StatementExecuteOk();
   }
}
