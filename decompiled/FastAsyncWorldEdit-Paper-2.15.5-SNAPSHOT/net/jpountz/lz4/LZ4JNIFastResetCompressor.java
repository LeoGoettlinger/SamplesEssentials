package net.jpountz.lz4;

import java.nio.ByteBuffer;

public final class LZ4JNIFastResetCompressor extends AbstractLZ4JNIFastResetCompressor {
   private final int acceleration;

   LZ4JNIFastResetCompressor() {
      this(1);
   }

   LZ4JNIFastResetCompressor(int acceleration) {
      super(LZ4JNI.LZ4_createStream(), "Failed to allocate LZ4 state");
      if (acceleration < 1) {
         acceleration = 1;
      } else if (acceleration > 65537) {
         acceleration = 65537;
      }

      this.acceleration = acceleration;
   }

   public int getAcceleration() {
      return this.acceleration;
   }

   @Override
   protected int compressNative(
      long ptr, byte[] srcArr, ByteBuffer srcBuf, int srcOff, int srcLen, byte[] destArr, ByteBuffer destBuf, int destOff, int maxDestLen
   ) {
      return LZ4JNI.LZ4_compress_fast_extState_fastReset(ptr, srcArr, srcBuf, srcOff, srcLen, destArr, destBuf, destOff, maxDestLen, this.acceleration);
   }

   @Override
   protected void freeState(long ptr) {
      LZ4JNI.LZ4_freeStream(ptr);
   }

   @Override
   public String toString() {
      return "LZ4JNIFastResetCompressor[acceleration=" + this.acceleration + ", closed=" + this.isClosed() + "]";
   }
}
