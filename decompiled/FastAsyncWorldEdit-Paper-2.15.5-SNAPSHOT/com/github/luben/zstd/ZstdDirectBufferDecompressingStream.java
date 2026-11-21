package com.github.luben.zstd;

import com.github.luben.zstd.util.Native;
import java.io.Closeable;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;

public class ZstdDirectBufferDecompressingStream implements Closeable {
   private ByteBuffer source;
   private final long stream;
   private boolean finishedFrame = false;
   private boolean closed = false;
   private boolean streamEnd = false;
   private boolean finalize = true;
   private int consumed;
   private int produced;

   protected ByteBuffer refill(ByteBuffer var1) {
      return var1;
   }

   private static native int recommendedDOutSize();

   private static native long createDStream();

   private static native int freeDStream(long var0);

   private native int initDStream(long var1);

   private native long decompressStream(long var1, ByteBuffer var3, int var4, int var5, ByteBuffer var6, int var7, int var8);

   public ZstdDirectBufferDecompressingStream(ByteBuffer var1) {
      if (!var1.isDirect()) {
         throw new IllegalArgumentException("Source buffer should be a direct buffer");
      } else {
         synchronized (this) {
            this.source = var1;
            this.stream = createDStream();
            this.initDStream(this.stream);
         }
      }
   }

   public void setFinalize(boolean var1) {
      this.finalize = var1;
   }

   public synchronized boolean hasRemaining() {
      return !this.streamEnd && (this.source.hasRemaining() || !this.finishedFrame);
   }

   public static int recommendedTargetBufferSize() {
      return recommendedDOutSize();
   }

   public synchronized ZstdDirectBufferDecompressingStream setDict(byte[] var1) throws IOException {
      int var2 = Zstd.loadDictDecompress(this.stream, var1, var1.length);
      if (Zstd.isError(var2)) {
         throw new IOException("Decompression error: " + Zstd.getErrorName(var2));
      } else {
         return this;
      }
   }

   public synchronized ZstdDirectBufferDecompressingStream setDict(ZstdDictDecompress var1) throws IOException {
      var1.acquireSharedLock();

      try {
         int var2 = Zstd.loadFastDictDecompress(this.stream, var1);
         if (Zstd.isError(var2)) {
            throw new IOException("Decompression error: " + Zstd.getErrorName(var2));
         }
      } finally {
         var1.releaseSharedLock();
      }

      return this;
   }

   public synchronized int read(ByteBuffer var1) throws IOException {
      if (!var1.isDirect()) {
         throw new IllegalArgumentException("Target buffer should be a direct buffer");
      } else if (this.closed) {
         throw new IOException("Stream closed");
      } else if (this.streamEnd) {
         return 0;
      } else {
         long var2 = this.decompressStream(this.stream, var1, var1.position(), var1.remaining(), this.source, this.source.position(), this.source.remaining());
         if (Zstd.isError(var2)) {
            throw new IOException(Zstd.getErrorName(var2));
         } else {
            ((Buffer)this.source).position(this.source.position() + this.consumed);
            ((Buffer)var1).position(var1.position() + this.produced);
            if (!this.source.hasRemaining()) {
               this.source = this.refill(this.source);
               if (!this.source.isDirect()) {
                  throw new IllegalArgumentException("Source buffer should be a direct buffer");
               }
            }

            this.finishedFrame = var2 == 0L;
            if (this.finishedFrame) {
               this.streamEnd = !this.source.hasRemaining();
            }

            return this.produced;
         }
      }
   }

   @Override
   public synchronized void close() throws IOException {
      if (!this.closed) {
         try {
            freeDStream(this.stream);
         } finally {
            this.closed = true;
            this.source = null;
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
