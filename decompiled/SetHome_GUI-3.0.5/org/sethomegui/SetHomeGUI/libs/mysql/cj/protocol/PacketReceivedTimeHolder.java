package org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol;

public interface PacketReceivedTimeHolder {
   default long getLastPacketReceivedTime() {
      return 0L;
   }
}
