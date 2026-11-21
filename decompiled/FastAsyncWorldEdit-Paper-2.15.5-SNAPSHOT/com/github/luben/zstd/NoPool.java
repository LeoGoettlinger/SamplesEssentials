package com.github.luben.zstd;

import java.nio.ByteBuffer;

public class NoPool implements BufferPool {
   public static final BufferPool INSTANCE = new NoPool();

   private NoPool() {
   }

   @Override
   public synchronized ByteBuffer get(int var1) {
      return ByteBuffer.allocate(var1);
   }

   @Override
   public synchronized void release(ByteBuffer var1) {
   }
}
