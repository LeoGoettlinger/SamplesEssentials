package net.jpountz.lz4;

import java.nio.ByteBuffer;
import net.jpountz.util.Native;

enum LZ4JNI {
   static native void init();

   static native int LZ4_compress_limitedOutput(byte[] var0, ByteBuffer var1, int var2, int var3, byte[] var4, ByteBuffer var5, int var6, int var7);

   static native int LZ4_compressHC(byte[] var0, ByteBuffer var1, int var2, int var3, byte[] var4, ByteBuffer var5, int var6, int var7, int var8);

   static native int LZ4_decompress_fast(byte[] var0, ByteBuffer var1, int var2, byte[] var3, ByteBuffer var4, int var5, int var6);

   static native int LZ4_decompress_safe(byte[] var0, ByteBuffer var1, int var2, int var3, byte[] var4, ByteBuffer var5, int var6, int var7);

   static native int LZ4_compressBound(int var0);

   static native long LZ4_createStream();

   static native int LZ4_freeStream(long var0);

   static native int LZ4_compress_fast_extState_fastReset(
      long var0, byte[] var2, ByteBuffer var3, int var4, int var5, byte[] var6, ByteBuffer var7, int var8, int var9, int var10
   );

   static native long LZ4_createStreamHC();

   static native int LZ4_freeStreamHC(long var0);

   static native int LZ4_compress_HC_extStateHC_fastReset(
      long var0, byte[] var2, ByteBuffer var3, int var4, int var5, byte[] var6, ByteBuffer var7, int var8, int var9, int var10
   );

   static {
      Native.load();
      init();
   }
}
