package com.viaversion.viaversion.codec.hash;

import java.util.zip.CRC32C;

final class JavaCRC32C implements HashFunction {
   private final CRC32C crc = new CRC32C();

   @Override
   public int hashBytes(byte[] data, int length) {
      this.crc.reset();
      this.crc.update(data, 0, length);
      return (int)this.crc.getValue();
   }
}
