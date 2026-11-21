package org.lushplugins.lushrewards.libraries.mysql.cj.protocol;

public interface Message {
   byte[] getByteBuffer();

   int getPosition();
}
