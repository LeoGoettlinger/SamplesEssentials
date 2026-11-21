package org.lushplugins.lushrewards.libraries.mysql.cj.protocol.x;

import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.ProtocolEntityFactory;
import org.lushplugins.lushrewards.libraries.mysql.cj.x.protobuf.MysqlxResultset;

public class XProtocolRowFactory implements ProtocolEntityFactory<XProtocolRow, XMessage> {
   public XProtocolRow createFromMessage(XMessage message) {
      return new XProtocolRow(MysqlxResultset.Row.class.cast(message.getMessage()));
   }
}
