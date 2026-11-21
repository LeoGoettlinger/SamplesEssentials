package com.donutorders.libs.mysql.cj.protocol;

public interface PacketReceivedTimeHolder {
   default long getLastPacketReceivedTime() {
      return 0L;
   }
}
