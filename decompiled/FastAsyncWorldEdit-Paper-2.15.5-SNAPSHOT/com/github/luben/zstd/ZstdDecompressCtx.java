package com.github.luben.zstd;

import com.github.luben.zstd.util.Native;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class ZstdDecompressCtx extends AutoCloseBase {
   private long nativePtr = 0L;
   private ZstdDictDecompress decompression_dict = null;

   private native void init();

   private native void free();

   public ZstdDecompressCtx() {
      this.init();
      if (0L == this.nativePtr) {
         throw new IllegalStateException("ZSTD_createDeCompressCtx failed");
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

   public ZstdDecompressCtx loadDict(ZstdDictDecompress var1) {
      if (this.nativePtr == 0L) {
         throw new IllegalStateException("Decompression context is closed");
      } else {
         this.acquireSharedLock();
         var1.acquireSharedLock();

         try {
            long var2 = this.loadDDictFast0(var1);
            if (Zstd.isError(var2)) {
               throw new ZstdException(var2);
            }

            this.decompression_dict = var1;
         } finally {
            var1.releaseSharedLock();
            this.releaseSharedLock();
         }

         return this;
      }
   }

   private native long loadDDictFast0(ZstdDictDecompress var1);

   public ZstdDecompressCtx loadDict(byte[] var1) {
      if (this.nativePtr == 0L) {
         throw new IllegalStateException("Compression context is closed");
      } else {
         this.acquireSharedLock();

         try {
            long var2 = this.loadDDict0(var1);
            if (Zstd.isError(var2)) {
               throw new ZstdException(var2);
            }

            this.decompression_dict = null;
         } finally {
            this.releaseSharedLock();
         }

         return this;
      }
   }

   private native long loadDDict0(byte[] var1);

   public int decompressDirectByteBuffer(ByteBuffer var1, int var2, int var3, ByteBuffer var4, int var5, int var6) {
      if (this.nativePtr == 0L) {
         throw new IllegalStateException("Decompression context is closed");
      } else if (!var4.isDirect()) {
         throw new IllegalArgumentException("srcBuff must be a direct buffer");
      } else if (!var1.isDirect()) {
         throw new IllegalArgumentException("dstBuff must be a direct buffer");
      } else {
         this.acquireSharedLock();

         int var9;
         try {
            long var7 = this.decompressDirectByteBuffer0(var1, var2, var3, var4, var5, var6);
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

   private native long decompressDirectByteBuffer0(ByteBuffer var1, int var2, int var3, ByteBuffer var4, int var5, int var6);

   public int decompressByteArray(byte[] var1, int var2, int var3, byte[] var4, int var5, int var6) {
      if (this.nativePtr == 0L) {
         throw new IllegalStateException("Decompression context is closed");
      } else {
         this.acquireSharedLock();

         int var9;
         try {
            long var7 = this.decompressByteArray0(var1, var2, var3, var4, var5, var6);
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

   private native long decompressByteArray0(byte[] var1, int var2, int var3, byte[] var4, int var5, int var6);

   public int decompress(ByteBuffer var1, ByteBuffer var2) throws ZstdException {
      int var3 = this.decompressDirectByteBuffer(var1, var1.position(), var1.limit() - var1.position(), var2, var2.position(), var2.limit() - var2.position());
      ((Buffer)var2).position(var2.limit());
      ((Buffer)var1).position(var1.position() + var3);
      return var3;
   }

   public ByteBuffer decompress(ByteBuffer var1, int var2) throws ZstdException {
      ByteBuffer var3 = ByteBuffer.allocateDirect(var2);
      int var4 = this.decompressDirectByteBuffer(var3, 0, var2, var1, var1.position(), var1.limit());
      ((Buffer)var1).position(var1.limit());
      return var3;
   }

   public int decompress(byte[] var1, byte[] var2) {
      return this.decompressByteArray(var1, 0, var1.length, var2, 0, var2.length);
   }

   public byte[] decompress(byte[] var1, int var2) throws ZstdException {
      byte[] var3 = new byte[var2];
      int var4 = this.decompress(var3, var1);
      return var4 != var2 ? Arrays.copyOfRange(var3, 0, var4) : var3;
   }

   static {
      Native.load();
   }
}
