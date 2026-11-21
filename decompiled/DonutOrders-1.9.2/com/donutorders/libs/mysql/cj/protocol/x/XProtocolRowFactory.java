package com.donutorders.libs.mysql.cj.protocol.x;

import com.donutorders.libs.mysql.cj.protocol.ProtocolEntityFactory;
import com.donutorders.libs.mysql.cj.x.protobuf.MysqlxResultset;

public class XProtocolRowFactory implements ProtocolEntityFactory<XProtocolRow, XMessage> {
   public XProtocolRow createFromMessage(XMessage message) {
      return new XProtocolRow(MysqlxResultset.Row.class.cast(message.getMessage()));
   }
}
