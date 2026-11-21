package com.github.luben.zstd;

import com.github.luben.zstd.util.Native;

public class ZstdDictDecompress extends SharedDictBase {
   private long nativePtr = 0L;

   private native void init(byte[] var1, int var2, int var3);

   private native void free();

   public ZstdDictDecompress(byte[] var1) {
      this(var1, 0, var1.length);
   }

   public ZstdDictDecompress(byte[] var1, int var2, int var3) {
      this.init(var1, var2, var3);
      if (this.nativePtr == 0L) {
         throw new IllegalStateException("ZSTD_createDDict failed");
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

   static {
      Native.load();
   }
}
