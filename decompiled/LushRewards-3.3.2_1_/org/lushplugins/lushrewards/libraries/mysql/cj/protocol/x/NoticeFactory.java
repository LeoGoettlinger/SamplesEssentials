package org.lushplugins.lushrewards.libraries.mysql.cj.protocol.x;

import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.ProtocolEntityFactory;

public class NoticeFactory implements ProtocolEntityFactory<Notice, XMessage> {
   public Notice createFromMessage(XMessage message) {
      return Notice.getInstance(message);
   }
}
