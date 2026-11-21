package net.jpountz.lz4;

import java.nio.ByteBuffer;

public final class LZ4JNIHCFastResetCompressor extends AbstractLZ4JNIFastResetCompressor {
   private final int compressionLevel;

   LZ4JNIHCFastResetCompressor() {
      this(9);
   }

   LZ4JNIHCFastResetCompressor(int compressionLevel) {
      super(LZ4JNI.LZ4_createStreamHC(), "Failed to allocate LZ4 HC state");
      if (compressionLevel > 17) {
         compressionLevel = 17;
      } else if (compressionLevel < 1) {
         compressionLevel = 9;
      }

      this.compressionLevel = compressionLevel;
   }

   public int getCompressionLevel() {
      return this.compressionLevel;
   }

   @Override
   protected int compressNative(
      long ptr, byte[] srcArr, ByteBuffer srcBuf, int srcOff, int srcLen, byte[] destArr, ByteBuffer destBuf, int destOff, int maxDestLen
   ) {
      return LZ4JNI.LZ4_compress_HC_extStateHC_fastReset(ptr, srcArr, srcBuf, srcOff, srcLen, destArr, destBuf, destOff, maxDestLen, this.compressionLevel);
   }

   @Override
   protected void freeState(long ptr) {
      LZ4JNI.LZ4_freeStreamHC(ptr);
   }

   @Override
   public String toString() {
      return "LZ4JNIHCFastResetCompressor[compressionLevel=" + this.compressionLevel + ", closed=" + this.isClosed() + "]";
   }
}
