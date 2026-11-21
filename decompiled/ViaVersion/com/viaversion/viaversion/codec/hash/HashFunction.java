package com.viaversion.viaversion.codec.hash;

@FunctionalInterface
public interface HashFunction {
   static HashFunction crc32c() {
      return (HashFunction)(Runtime.version().feature() < 9 ? new FallbackCRC32C() : new JavaCRC32C());
   }

   int hashBytes(byte[] var1, int var2);

   default int hashBytes(byte[] data) {
      return this.hashBytes(data, data.length);
   }
}
