package com.github.luben.zstd;

import com.github.luben.zstd.util.Native;

public class ZstdDictCompress extends SharedDictBase {
   private long nativePtr = 0L;
   private int level = 3;

   private native void init(byte[] var1, int var2, int var3, int var4);

   private native void free();

   public ZstdDictCompress(byte[] var1, int var2) {
      this(var1, 0, var1.length, var2);
   }

   public ZstdDictCompress(byte[] var1, int var2, int var3, int var4) {
      this.level = var4;
      if (var1.length - var2 < 0) {
         throw new IllegalArgumentException("Dictionary buffer is to short");
      } else {
         this.init(var1, var2, var3, var4);
         if (0L == this.nativePtr) {
            throw new IllegalStateException("ZSTD_createCDict failed");
         } else {
            this.storeFence();
         }
      }
   }

   int level() {
      return this.level;
   }

   @Override
   void doClose() {
      if (this.nativePtr != 0L) {
         this.free();
         this.nativePtr = 0L;
      }
   }

   static {
      Native.load();
   }
}
