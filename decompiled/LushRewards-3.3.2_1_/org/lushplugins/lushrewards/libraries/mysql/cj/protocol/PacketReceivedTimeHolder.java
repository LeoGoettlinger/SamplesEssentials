package org.lushplugins.lushrewards.libraries.mysql.cj.protocol;

public interface PacketReceivedTimeHolder {
   default long getLastPacketReceivedTime() {
      return 0L;
   }
}
