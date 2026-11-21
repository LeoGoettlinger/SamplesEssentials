package org.lushplugins.lushrewards.libraries.mysql.cj.protocol.x;

import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.ProtocolEntityFactory;

public class StatementExecuteOkFactory implements ProtocolEntityFactory<StatementExecuteOk, XMessage> {
   public StatementExecuteOk createFromMessage(XMessage message) {
      return new StatementExecuteOk();
   }
}
