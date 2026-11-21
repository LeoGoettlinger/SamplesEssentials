package com.github.luben.zstd;

import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class RecyclingBufferPool implements BufferPool {
   public static final BufferPool INSTANCE = new RecyclingBufferPool();
   private final Map<Integer, SoftReference<Deque<ByteBuffer>>> pools = new HashMap<>();

   private RecyclingBufferPool() {
   }

   private Deque<ByteBuffer> getDeque(int var1) {
      SoftReference var2 = this.pools.get(var1);
      Object var3;
      if (var2 == null || (var3 = (Deque)var2.get()) == null) {
         var3 = new ArrayDeque();
         var2 = new SoftReference<>(var3);
         this.pools.put(var1, var2);
      }

      return (Deque<ByteBuffer>)var3;
   }

   @Override
   public synchronized ByteBuffer get(int var1) {
      ByteBuffer var2 = this.getDeque(var1).pollFirst();
      if (var2 == null) {
         var2 = ByteBuffer.allocate(var1);
      }

      return var2;
   }

   @Override
   public synchronized void release(ByteBuffer var1) {
      this.getDeque(var1.capacity()).addLast(var1);
   }
}
