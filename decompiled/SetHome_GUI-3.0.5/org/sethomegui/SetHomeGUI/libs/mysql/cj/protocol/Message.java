package org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol;

public interface Message {
   byte[] getByteBuffer();

   int getPosition();
}
