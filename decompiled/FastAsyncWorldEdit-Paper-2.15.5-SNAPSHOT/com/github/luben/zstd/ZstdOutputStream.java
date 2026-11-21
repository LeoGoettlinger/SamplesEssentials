package com.github.luben.zstd;

import com.github.luben.zstd.util.Native;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class ZstdOutputStream extends FilterOutputStream {
   private final long stream;
   private long srcPos = 0L;
   private long dstPos = 0L;
   private final BufferPool bufferPool;
   private final ByteBuffer dstByteBuffer;
   private final byte[] dst;
   private boolean isClosed = false;
   private boolean finalize = true;
   private static final int dstSize = (int)recommendedCOutSize();
   private boolean closeFrameOnFlush;
   private boolean frameClosed = true;

   private static native long recommendedCOutSize();

   private static native long createCStream();

   private static native int freeCStream(long var0);

   private native int resetCStream(long var1);

   private native int compressStream(long var1, byte[] var3, int var4, byte[] var5, int var6);

   private native int flushStream(long var1, byte[] var3, int var4);

   private native int endStream(long var1, byte[] var3, int var4);

   /** @deprecated */
   public ZstdOutputStream(OutputStream var1, int var2, boolean var3, boolean var4) throws IOException {
      this(var1);
      this.closeFrameOnFlush = var3;
      Zstd.setCompressionLevel(this.stream, var2);
      Zstd.setCompressionChecksums(this.stream, var4);
   }

   /** @deprecated */
   public ZstdOutputStream(OutputStream var1, int var2, boolean var3) throws IOException {
      this(var1);
      this.closeFrameOnFlush = var3;
      Zstd.setCompressionLevel(this.stream, var2);
   }

   public ZstdOutputStream(OutputStream var1, int var2) throws IOException {
      this(var1);
      this.closeFrameOnFlush = false;
      Zstd.setCompressionLevel(this.stream, var2);
   }

   public ZstdOutputStream(OutputStream var1) throws IOException {
      this(var1, NoPool.INSTANCE);
   }

   public ZstdOutputStream(OutputStream var1, BufferPool var2) throws IOException {
      super(var1);
      this.stream = createCStream();
      this.closeFrameOnFlush = false;
      this.bufferPool = var2;
      this.dstByteBuffer = var2.get(dstSize);
      if (this.dstByteBuffer == null) {
         throw new IOException("Cannot get ByteBuffer of size " + dstSize + " from the BufferPool");
      } else {
         this.dst = Zstd.extractArray(this.dstByteBuffer);
      }
   }

   public synchronized ZstdOutputStream setChecksum(boolean var1) throws IOException {
      if (!this.frameClosed) {
         throw new IOException("Change of parameter on initialized stream");
      } else {
         int var2 = Zstd.setCompressionChecksums(this.stream, var1);
         if (Zstd.isError(var2)) {
            throw new IOException("Compression param: " + Zstd.getErrorName(var2));
         } else {
            return this;
         }
      }
   }

   public synchronized ZstdOutputStream setLevel(int var1) throws IOException {
      if (!this.frameClosed) {
         throw new IOException("Change of parameter on initialized stream");
      } else {
         int var2 = Zstd.setCompressionLevel(this.stream, var1);
         if (Zstd.isError(var2)) {
            throw new IOException("Compression param: " + Zstd.getErrorName(var2));
         } else {
            return this;
         }
      }
   }

   public synchronized ZstdOutputStream setWorkers(int var1) throws IOException {
      if (!this.frameClosed) {
         throw new IOException("Change of parameter on initialized stream");
      } else {
         int var2 = Zstd.setCompressionWorkers(this.stream, var1);
         if (Zstd.isError(var2)) {
            throw new IOException("Compression param: " + Zstd.getErrorName(var2));
         } else {
            return this;
         }
      }
   }

   public synchronized ZstdOutputStream setCloseFrameOnFlush(boolean var1) throws IOException {
      if (!this.frameClosed) {
         throw new IOException("Change of parameter on initialized stream");
      } else {
         this.closeFrameOnFlush = var1;
         return this;
      }
   }

   public synchronized ZstdOutputStream setDict(byte[] var1) throws IOException {
      if (!this.frameClosed) {
         throw new IOException("Change of parameter on initialized stream");
      } else {
         int var2 = Zstd.loadDictCompress(this.stream, var1, var1.length);
         if (Zstd.isError(var2)) {
            throw new IOException("Compression param: " + Zstd.getErrorName(var2));
         } else {
            return this;
         }
      }
   }

   public synchronized ZstdOutputStream setDict(ZstdDictCompress var1) throws IOException {
      if (!this.frameClosed) {
         throw new IOException("Change of parameter on initialized stream");
      } else {
         int var2 = Zstd.loadFastDictCompress(this.stream, var1);
         if (Zstd.isError(var2)) {
            throw new IOException("Compression param: " + Zstd.getErrorName(var2));
         } else {
            return this;
         }
      }
   }

   public void setFinalize(boolean var1) {
      this.finalize = var1;
   }

   @Override
   public synchronized void write(byte[] var1, int var2, int var3) throws IOException {
      if (this.isClosed) {
         throw new IOException("Stream closed");
      } else {
         if (this.frameClosed) {
            int var4 = this.resetCStream(this.stream);
            if (Zstd.isError(var4)) {
               throw new IOException("Compression error: cannot create header: " + Zstd.getErrorName(var4));
            }

            this.frameClosed = false;
         }

         int var6 = var2 + var3;
         this.srcPos = var2;

         while (this.srcPos < var6) {
            int var5 = this.compressStream(this.stream, this.dst, this.dst.length, var1, var6);
            if (Zstd.isError(var5)) {
               throw new IOException("Compression error: " + Zstd.getErrorName(var5));
            }

            if (this.dstPos > 0L) {
               this.out.write(this.dst, 0, (int)this.dstPos);
            }
         }
      }
   }

   @Override
   public void write(int var1) throws IOException {
      byte[] var2 = new byte[]{(byte)var1};
      this.write(var2, 0, 1);
   }

   @Override
   public synchronized void flush() throws IOException {
      if (this.isClosed) {
         throw new IOException("Stream closed");
      } else {
         if (!this.frameClosed) {
            int var1;
            if (this.closeFrameOnFlush) {
               while (true) {
                  var1 = this.endStream(this.stream, this.dst, this.dst.length);
                  if (Zstd.isError(var1)) {
                     throw new IOException("Compression error: " + Zstd.getErrorName(var1));
                  }

                  this.out.write(this.dst, 0, (int)this.dstPos);
                  if (var1 <= 0) {
                     this.frameClosed = true;
                     break;
                  }
               }
            } else {
               do {
                  var1 = this.flushStream(this.stream, this.dst, this.dst.length);
                  if (Zstd.isError(var1)) {
                     throw new IOException("Compression error: " + Zstd.getErrorName(var1));
                  }

                  this.out.write(this.dst, 0, (int)this.dstPos);
               } while (var1 > 0);
            }

            this.out.flush();
         }
      }
   }

   @Override
   public synchronized void close() throws IOException {
      if (!this.isClosed) {
         try {
            int var1;
            if (!this.frameClosed) {
               do {
                  var1 = this.endStream(this.stream, this.dst, this.dst.length);
                  if (Zstd.isError(var1)) {
                     throw new IOException("Compression error: " + Zstd.getErrorName(var1));
                  }

                  this.out.write(this.dst, 0, (int)this.dstPos);
               } while (var1 > 0);
            }

            this.out.close();
         } finally {
            this.isClosed = true;
            this.bufferPool.release(this.dstByteBuffer);
            freeCStream(this.stream);
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
