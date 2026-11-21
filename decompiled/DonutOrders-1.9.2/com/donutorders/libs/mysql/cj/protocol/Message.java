package com.donutorders.libs.mysql.cj.protocol;

public interface Message {
   byte[] getByteBuffer();

   int getPosition();
}
