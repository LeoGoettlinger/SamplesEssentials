package com.viaversion.viaversion.codec.hash;

@Deprecated
final class FallbackCRC32C implements HashFunction {
   private static final int[] CRC32C_TABLE = new int[256];

   @Override
   public int hashBytes(byte[] data, int length) {
      int crc = -1;

      for (int i = 0; i < length; i++) {
         byte b = data[i];
         int index = (crc ^ b) & 0xFF;
         crc = crc >>> 8 ^ CRC32C_TABLE[index];
      }

      return ~crc;
   }

   static {
      for (int i = 0; i < 256; i++) {
         int crc = i;

         for (int j = 0; j < 8; j++) {
            if ((crc & 1) == 1) {
               crc = crc >>> 1 ^ -2097792136;
            } else {
               crc >>>= 1;
            }
         }

         CRC32C_TABLE[i] = crc;
      }
   }
}
