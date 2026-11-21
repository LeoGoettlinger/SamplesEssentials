package com.github.luben.zstd;

import com.github.luben.zstd.util.Native;
import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;

public class ZstdDirectBufferCompressingStream implements Closeable, Flushable {
   private ByteBuffer target;
   private final long stream;
   private int consumed = 0;
   private int produced = 0;
   private boolean closed = false;
   private boolean initialized = false;
   private boolean finalize = true;
   private int level = 3;
   private byte[] dict = null;
   private ZstdDictCompress fastDict = null;

   protected ByteBuffer flushBuffer(ByteBuffer var1) throws IOException {
      return var1;
   }

   public ZstdDirectBufferCompressingStream(ByteBuffer var1, int var2) throws IOException {
      if (!var1.isDirect()) {
         throw new IllegalArgumentException("Target buffer should be a direct buffer");
      } else {
         synchronized (this) {
            this.target = var1;
            this.level = var2;
            this.stream = createCStream();
         }
      }
   }

   public static int recommendedOutputBufferSize() {
      return (int)recommendedCOutSize();
   }

   private static native long recommendedCOutSize();

   private static native long createCStream();

   private static native int freeCStream(long var0);

   private native int initCStream(long var1, int var3);

   private native int initCStreamWithDict(long var1, byte[] var3, int var4, int var5);

   private native int initCStreamWithFastDict(long var1, ZstdDictCompress var3);

   private native int compressDirectByteBuffer(long var1, ByteBuffer var3, int var4, int var5, ByteBuffer var6, int var7, int var8);

   private native int flushStream(long var1, ByteBuffer var3, int var4, int var5);

   private native int endStream(long var1, ByteBuffer var3, int var4, int var5);

   public synchronized ZstdDirectBufferCompressingStream setDict(byte[] var1) throws IOException {
      if (this.initialized) {
         throw new IOException("Change of parameter on initialized stream");
      } else {
         this.dict = var1;
         this.fastDict = null;
         return this;
      }
   }

   public synchronized ZstdDirectBufferCompressingStream setDict(ZstdDictCompress var1) throws IOException {
      if (this.initialized) {
         throw new IOException("Change of parameter on initialized stream");
      } else {
         this.dict = null;
         this.fastDict = var1;
         return this;
      }
   }

   public void setFinalize(boolean var1) {
      this.finalize = var1;
   }

   public synchronized void compress(ByteBuffer var1) throws IOException {
      if (!var1.isDirect()) {
         throw new IllegalArgumentException("Source buffer should be a direct buffer");
      } else if (this.closed) {
         throw new IOException("Stream closed");
      } else {
         if (!this.initialized) {
            int var2 = 0;
            ZstdDictCompress var3 = this.fastDict;
            if (var3 != null) {
               var3.acquireSharedLock();

               try {
                  var2 = this.initCStreamWithFastDict(this.stream, var3);
               } finally {
                  var3.releaseSharedLock();
               }
            } else if (this.dict != null) {
               var2 = this.initCStreamWithDict(this.stream, this.dict, this.dict.length, this.level);
            } else {
               var2 = this.initCStream(this.stream, this.level);
            }

            if (Zstd.isError(var2)) {
               throw new IOException("Compression error: cannot create header: " + Zstd.getErrorName(var2));
            }

            this.initialized = true;
         }

         while (var1.hasRemaining()) {
            if (!this.target.hasRemaining()) {
               this.target = this.flushBuffer(this.target);
               if (!this.target.isDirect()) {
                  throw new IllegalArgumentException("Target buffer should be a direct buffer");
               }

               if (!this.target.hasRemaining()) {
                  throw new IOException("The target buffer has no more space, even after flushing, and there are still bytes to compress");
               }
            }

            int var8 = this.compressDirectByteBuffer(
               this.stream, this.target, this.target.position(), this.target.remaining(), var1, var1.position(), var1.remaining()
            );
            if (Zstd.isError(var8)) {
               throw new IOException("Compression error: " + Zstd.getErrorName(var8));
            }

            ((Buffer)this.target).position(this.target.position() + this.produced);
            ((Buffer)var1).position(var1.position() + this.consumed);
         }
      }
   }

   @Override
   public synchronized void flush() throws IOException {
      if (this.closed) {
         throw new IOException("Already closed");
      } else {
         int var1;
         if (this.initialized) {
            do {
               var1 = this.flushStream(this.stream, this.target, this.target.position(), this.target.remaining());
               if (Zstd.isError(var1)) {
                  throw new IOException("Compression error: " + Zstd.getErrorName(var1));
               }

               ((Buffer)this.target).position(this.target.position() + this.produced);
               this.target = this.flushBuffer(this.target);
               if (!this.target.isDirect()) {
                  throw new IllegalArgumentException("Target buffer should be a direct buffer");
               }

               if (var1 > 0 && !this.target.hasRemaining()) {
                  throw new IOException("The target buffer has no more space, even after flushing, and there are still bytes to compress");
               }
            } while (var1 > 0);
         }
      }
   }

   @Override
   public synchronized void close() throws IOException {
      if (!this.closed) {
         try {
            int var1;
            if (this.initialized) {
               do {
                  var1 = this.endStream(this.stream, this.target, this.target.position(), this.target.remaining());
                  if (Zstd.isError(var1)) {
                     throw new IOException("Compression error: " + Zstd.getErrorName(var1));
                  }

                  ((Buffer)this.target).position(this.target.position() + this.produced);
                  this.target = this.flushBuffer(this.target);
                  if (!this.target.isDirect()) {
                     throw new IllegalArgumentException("Target buffer should be a direct buffer");
                  }

                  if (var1 > 0 && !this.target.hasRemaining()) {
                     throw new IOException("The target buffer has no more space, even after flushing, and there are still bytes to compress");
                  }
               } while (var1 > 0);
            }
         } finally {
            freeCStream(this.stream);
            this.closed = true;
            this.initialized = false;
            this.target = null;
         }
      }
   }

   @Override
   protected void finalize() throws Throwable {
      if (this.finalize) {
         this.close();
      }
   }

   static {
      Native.load();
   }
}
