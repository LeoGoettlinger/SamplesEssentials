package com.viaversion.viabackwards.protocol.v1_12_2to1_12_1.storage;

import com.viaversion.viaversion.api.connection.StorableObject;
import java.util.HashMap;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;

public class KeepAliveTracker implements StorableObject {
   private final Map<Integer, Long> pending = new HashMap<>();
   private int nextId;

   public int track(long original) {
      int id = this.nextId++;
      this.pending.put(id, original);
      return id;
   }

   @Nullable
   public Long consume(int id) {
      return this.pending.remove(id);
   }
}
