package com.donutorders.libs.mysql.cj.protocol.x;

import com.donutorders.libs.mysql.cj.protocol.ProtocolEntityFactory;

public class NoticeFactory implements ProtocolEntityFactory<Notice, XMessage> {
   public Notice createFromMessage(XMessage message) {
      return Notice.getInstance(message);
   }
}
