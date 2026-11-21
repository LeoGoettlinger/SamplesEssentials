package net.jpountz.lz4;

import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantLock;
import net.jpountz.util.ByteBufferUtils;
import net.jpountz.util.SafeUtils;

abstract class AbstractLZ4JNIFastResetCompressor extends LZ4Compressor implements AutoCloseable {
   private static final String IN_USE_ERROR = "This compressor is not thread-safe and is already in use";
   private static final String CLOSED_ERROR = "Compressor has been closed";
   private static final String UNSUPPORTED_BUFFER_ERROR = "ByteBuffer must be direct or array-backed";
   private final ReentrantLock lock = new ReentrantLock();
   private long statePtr;

   AbstractLZ4JNIFastResetCompressor(long statePtr, String allocationFailureMessage) {
      if (statePtr == 0L) {
         throw new LZ4Exception(allocationFailureMessage);
      } else {
         this.statePtr = statePtr;
      }
   }

   @Override
   public final int compress(byte[] src, int srcOff, int srcLen, byte[] dest, int destOff, int maxDestLen) {
      if (!this.lock.tryLock()) {
         throw new IllegalStateException("This compressor is not thread-safe and is already in use");
      } else {
         int var10;
         try {
            long ptr = this.checkOpen();
            SafeUtils.checkRange(src, srcOff, srcLen);
            SafeUtils.checkRange(dest, destOff, maxDestLen);
            int result = this.compressNative(ptr, src, null, srcOff, srcLen, dest, null, destOff, maxDestLen);
            var10 = checkResult(result);
         } finally {
            this.lock.unlock();
         }

         return var10;
      }
   }

   @Override
   public final int compress(ByteBuffer src, int srcOff, int srcLen, ByteBuffer dest, int destOff, int maxDestLen) {
      if (!this.lock.tryLock()) {
         throw new IllegalStateException("This compressor is not thread-safe and is already in use");
      } else {
         int var16;
         try {
            long ptr = this.checkOpen();
            checkByteBuffer(src);
            checkByteBuffer(dest);
            ByteBufferUtils.checkNotReadOnly(dest);
            ByteBufferUtils.checkRange(src, srcOff, srcLen);
            ByteBufferUtils.checkRange(dest, destOff, maxDestLen);
            byte[] srcArr = src.hasArray() ? src.array() : null;
            byte[] destArr = dest.hasArray() ? dest.array() : null;
            ByteBuffer srcBuf = srcArr == null ? src : null;
            ByteBuffer destBuf = destArr == null ? dest : null;
            int srcBufferOff = srcOff + (srcArr != null ? src.arrayOffset() : 0);
            int destBufferOff = destOff + (destArr != null ? dest.arrayOffset() : 0);
            int result = this.compressNative(ptr, srcArr, srcBuf, srcBufferOff, srcLen, destArr, destBuf, destBufferOff, maxDestLen);
            var16 = checkResult(result);
         } finally {
            this.lock.unlock();
         }

         return var16;
      }
   }

   public final boolean isClosed() {
      this.lock.lock();

      boolean var1;
      try {
         var1 = this.statePtr == 0L;
      } finally {
         this.lock.unlock();
      }

      return var1;
   }

   @Override
   public final void close() {
      if (!this.lock.tryLock()) {
         throw new IllegalStateException("This compressor is not thread-safe and is already in use");
      } else {
         try {
            long ptr = this.statePtr;
            this.statePtr = 0L;
            if (ptr != 0L) {
               this.freeState(ptr);
            }
         } finally {
            this.lock.unlock();
         }
      }
   }

   private long checkOpen() {
      if (this.statePtr == 0L) {
         throw new IllegalStateException("Compressor has been closed");
      } else {
         return this.statePtr;
      }
   }

   private static int checkResult(int result) {
      if (result <= 0) {
         throw new LZ4Exception("maxDestLen is too small");
      } else {
         return result;
      }
   }

   private static void checkByteBuffer(ByteBuffer buffer) {
      if (!buffer.hasArray() && !buffer.isDirect()) {
         throw new IllegalArgumentException("ByteBuffer must be direct or array-backed");
      }
   }

   protected abstract int compressNative(long var1, byte[] var3, ByteBuffer var4, int var5, int var6, byte[] var7, ByteBuffer var8, int var9, int var10);

   protected abstract void freeState(long var1);
}
