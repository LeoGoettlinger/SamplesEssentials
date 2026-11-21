package com.donutorders.libs.mysql.cj.protocol.x;

import com.donutorders.libs.mysql.cj.protocol.ProtocolEntityFactory;

public class StatementExecuteOkFactory implements ProtocolEntityFactory<StatementExecuteOk, XMessage> {
   public StatementExecuteOk createFromMessage(XMessage message) {
      return new StatementExecuteOk();
   }
}
