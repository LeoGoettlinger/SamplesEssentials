package com.github.luben.zstd;

import com.github.luben.zstd.util.Native;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class ZstdCompressCtx extends AutoCloseBase {
   private long nativePtr = 0L;
   private ZstdDictCompress compression_dict = null;

   private native void init();

   private native void free();

   public ZstdCompressCtx() {
      this.init();
      if (0L == this.nativePtr) {
         throw new IllegalStateException("ZSTD_createCompressCtx failed");
      } else {
         this.storeFence();
      }
   }

   @Override
   void doClose() {
      if (this.nativePtr != 0L) {
         this.free();
         this.nativePtr = 0L;
      }
   }

   public ZstdCompressCtx setLevel(int var1) {
      if (this.nativePtr == 0L) {
         throw new IllegalStateException("Compression context is closed");
      } else {
         this.acquireSharedLock();
         this.setLevel0(var1);
         this.releaseSharedLock();
         return this;
      }
   }

   private native void setLevel0(int var1);

   public ZstdCompressCtx setChecksum(boolean var1) {
      if (this.nativePtr == 0L) {
         throw new IllegalStateException("Compression context is closed");
      } else {
         this.acquireSharedLock();
         this.setChecksum0(var1);
         this.releaseSharedLock();
         return this;
      }
   }

   private native void setChecksum0(boolean var1);

   public ZstdCompressCtx setContentSize(boolean var1) {
      if (this.nativePtr == 0L) {
         throw new IllegalStateException("Compression context is closed");
      } else {
         this.acquireSharedLock();
         this.setContentSize0(var1);
         this.releaseSharedLock();
         return this;
      }
   }

   private native void setContentSize0(boolean var1);

   public ZstdCompressCtx setDictID(boolean var1) {
      if (this.nativePtr == 0L) {
         throw new IllegalStateException("Compression context is closed");
      } else {
         this.acquireSharedLock();
         this.setDictID0(var1);
         this.releaseSharedLock();
         return this;
      }
   }

   private native void setDictID0(boolean var1);

   public ZstdCompressCtx loadDict(ZstdDictCompress var1) {
      if (this.nativePtr == 0L) {
         throw new IllegalStateException("Compression context is closed");
      } else {
         this.acquireSharedLock();
         var1.acquireSharedLock();

         try {
            long var2 = this.loadCDictFast0(var1);
            if (Zstd.isError(var2)) {
               throw new ZstdException(var2);
            }

            this.compression_dict = var1;
         } finally {
            var1.releaseSharedLock();
            this.releaseSharedLock();
         }

         return this;
      }
   }

   private native long loadCDictFast0(ZstdDictCompress var1);

   public ZstdCompressCtx loadDict(byte[] var1) {
      if (this.nativePtr == 0L) {
         throw new IllegalStateException("Compression context is closed");
      } else {
         this.acquireSharedLock();

         try {
            long var2 = this.loadCDict0(var1);
            if (Zstd.isError(var2)) {
               throw new ZstdException(var2);
            }

            this.compression_dict = null;
         } finally {
            this.releaseSharedLock();
         }

         return this;
      }
   }

   private native long loadCDict0(byte[] var1);

   public int compressDirectByteBuffer(ByteBuffer var1, int var2, int var3, ByteBuffer var4, int var5, int var6) {
      if (this.nativePtr == 0L) {
         throw new IllegalStateException("Compression context is closed");
      } else if (!var4.isDirect()) {
         throw new IllegalArgumentException("srcBuff must be a direct buffer");
      } else if (!var1.isDirect()) {
         throw new IllegalArgumentException("dstBuff must be a direct buffer");
      } else {
         this.acquireSharedLock();

         int var9;
         try {
            long var7 = this.compressDirectByteBuffer0(var1, var2, var3, var4, var5, var6);
            if (Zstd.isError(var7)) {
               throw new ZstdException(var7);
            }

            if (var7 > 2147483647L) {
               throw new ZstdException(Zstd.errGeneric(), "Output size is greater than MAX_INT");
            }

            var9 = (int)var7;
         } finally {
            this.releaseSharedLock();
         }

         return var9;
      }
   }

   private native long compressDirectByteBuffer0(ByteBuffer var1, int var2, int var3, ByteBuffer var4, int var5, int var6);

   public int compressByteArray(byte[] var1, int var2, int var3, byte[] var4, int var5, int var6) {
      if (this.nativePtr == 0L) {
         throw new IllegalStateException("Compression context is closed");
      } else {
         this.acquireSharedLock();

         int var9;
         try {
            long var7 = this.compressByteArray0(var1, var2, var3, var4, var5, var6);
            if (Zstd.isError(var7)) {
               throw new ZstdException(var7);
            }

            if (var7 > 2147483647L) {
               throw new ZstdException(Zstd.errGeneric(), "Output size is greater than MAX_INT");
            }

            var9 = (int)var7;
         } finally {
            this.releaseSharedLock();
         }

         return var9;
      }
   }

   private native long compressByteArray0(byte[] var1, int var2, int var3, byte[] var4, int var5, int var6);

   public int compress(ByteBuffer var1, ByteBuffer var2) {
      int var3 = this.compressDirectByteBuffer(var1, var1.position(), var1.limit() - var1.position(), var2, var2.position(), var2.limit() - var2.position());
      ((Buffer)var2).position(var2.limit());
      ((Buffer)var1).position(var1.position() + var3);
      return var3;
   }

   public ByteBuffer compress(ByteBuffer var1) throws ZstdException {
      long var2 = Zstd.compressBound(var1.limit() - var1.position());
      if (var2 > 2147483647L) {
         throw new ZstdException(Zstd.errGeneric(), "Max output size is greater than MAX_INT");
      } else {
         ByteBuffer var4 = ByteBuffer.allocateDirect((int)var2);
         int var5 = this.compressDirectByteBuffer(var4, 0, (int)var2, var1, var1.position(), var1.limit() - var1.position());
         ((Buffer)var1).position(var1.limit());
         ((Buffer)var4).limit(var5);
         return var4;
      }
   }

   public int compress(byte[] var1, byte[] var2) {
      return this.compressByteArray(var1, 0, var1.length, var2, 0, var2.length);
   }

   public byte[] compress(byte[] var1) {
      long var2 = Zstd.compressBound(var1.length);
      if (var2 > 2147483647L) {
         throw new ZstdException(Zstd.errGeneric(), "Max output size is greater than MAX_INT");
      } else {
         byte[] var4 = new byte[(int)var2];
         int var5 = this.compressByteArray(var4, 0, var4.length, var1, 0, var1.length);
         return Arrays.copyOfRange(var4, 0, var5);
      }
   }

   static {
      Native.load();
   }
}
