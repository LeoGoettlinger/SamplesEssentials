package com.magmaguy.elitemobs.magmacore.ai;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class MindMemoryStore {
   private final MindSchema schema;
   private final Map<MindMemoryKey<?>, MindMemoryStore.Entry> values = new LinkedHashMap<>();
   private long currentTick;
   private MindPersistentState pendingRestore;
   private final MindMemoryView sharedView = new MindMemoryStore.View();
   private long expiredAtTick;
   private boolean expiryCurrent;

   public MindMemoryStore(MindSchema schema) {
      this.schema = schema;
   }

   public MindMemoryView view(long gameTick) {
      this.currentTick = gameTick;
      this.materializePendingRestore(gameTick);
      this.expire(gameTick);
      return this.sharedView;
   }

   public MindMemoryStore transferTo(MindSchema nextSchema, StateTransfer transfer) {
      this.materializePendingRestore(this.currentTick);
      MindMemoryStore next = new MindMemoryStore(nextSchema);
      if (transfer == StateTransfer.NONE) {
         return next;
      } else {
         for (Map.Entry<MindMemoryKey<?>, MindMemoryStore.Entry> entry : this.values.entrySet()) {
            MindMemoryKey<?> oldKey = entry.getKey();
            MindMemoryKey<?> nextKey = nextSchema.memory(oldKey.identifier());
            if (nextKey != null
               && nextKey.type().equals(oldKey.type())
               && (transfer != StateTransfer.PERSISTENT_ONLY || oldKey.persistent() && nextKey.persistent())) {
               next.values.put(nextKey, entry.getValue());
            }
         }

         next.currentTick = this.currentTick;
         return next;
      }
   }

   public void shiftClock(long elapsedTicks) {
      if (elapsedTicks < 0L) {
         throw new IllegalArgumentException("elapsedTicks cannot be negative");
      } else if (elapsedTicks != 0L) {
         if (this.pendingRestore == null) {
            this.values
               .replaceAll(
                  (ignored, entry) -> new MindMemoryStore.Entry(
                     entry.value, entry.expiresAtTick == Long.MAX_VALUE ? Long.MAX_VALUE : saturatedAdd(entry.expiresAtTick, elapsedTicks)
                  )
               );
         }

         this.currentTick = saturatedAdd(this.currentTick, elapsedTicks);
         this.expiryCurrent = false;
      }
   }

   public MindPersistentState persistentState() {
      if (this.pendingRestore != null) {
         return this.pendingRestore;
      } else {
         this.expire(this.currentTick);
         Map<String, Object> encoded = new LinkedHashMap<>();
         Map<String, Long> remainingTtl = new LinkedHashMap<>();

         for (Map.Entry<MindMemoryKey<?>, MindMemoryStore.Entry> entry : this.values.entrySet()) {
            MindMemoryKey<?> key = entry.getKey();
            if (key.persistent()) {
               encoded.put(key.identifier(), encode(key, entry.getValue().value));
               if (entry.getValue().expiresAtTick != Long.MAX_VALUE) {
                  remainingTtl.put(key.identifier(), Math.max(1L, entry.getValue().expiresAtTick - this.currentTick));
               }
            }
         }

         return new MindPersistentState(1, encoded, remainingTtl);
      }
   }

   public void restore(MindPersistentState state) {
      if (state.formatVersion() != 1) {
         throw new IllegalArgumentException("Unsupported mind state format " + state.formatVersion());
      } else {
         this.values.clear();
         this.pendingRestore = state;
         this.expiryCurrent = false;
      }
   }

   private void materializePendingRestore(long gameTick) {
      if (this.pendingRestore != null) {
         MindPersistentState state = this.pendingRestore;
         this.pendingRestore = null;

         for (Map.Entry<String, Object> encoded : state.memories().entrySet()) {
            MindMemoryKey<?> key = this.schema.memory(encoded.getKey());
            if (key != null && key.persistent()) {
               Long remaining = state.remainingTtlTicks().get(encoded.getKey());
               long expiresAt = remaining == null ? Long.MAX_VALUE : (gameTick > Long.MAX_VALUE - remaining ? Long.MAX_VALUE : gameTick + remaining);
               this.values.put(key, new MindMemoryStore.Entry(decode(key, encoded.getValue()), expiresAt));
            }
         }
      }
   }

   private void expire(long gameTick) {
      if (!this.expiryCurrent || this.expiredAtTick != gameTick) {
         Iterator<MindMemoryStore.Entry> iterator = this.values.values().iterator();

         while (iterator.hasNext()) {
            MindMemoryStore.Entry entry = iterator.next();
            if (entry.expiresAtTick <= gameTick) {
               iterator.remove();
            }
         }

         this.expiredAtTick = gameTick;
         this.expiryCurrent = true;
      }
   }

   private static long saturatedAdd(long value, long increment) {
      return value > Long.MAX_VALUE - increment ? Long.MAX_VALUE : value + increment;
   }

   private static <T> Object encode(MindMemoryKey<T> key, Object value) {
      return key.codec().encode(key.requireType(value));
   }

   private static <T> Object decode(MindMemoryKey<T> key, Object value) {
      return key.requireType(key.codec().decode(value));
   }

   private record Entry(Object value, long expiresAtTick) {
   }

   private final class View implements MindMemoryView {
      @Override
      public <T> Optional<T> get(MindMemoryKey<T> key) {
         this.requireDeclared(key);
         MindMemoryStore.Entry entry = MindMemoryStore.this.values.get(key);
         return entry == null ? Optional.empty() : Optional.of(key.requireType(entry.value));
      }

      @Override
      public <T> void set(MindMemoryKey<T> key, T value) {
         this.put(key, (long)value, Long.MAX_VALUE);
      }

      @Override
      public <T> void set(MindMemoryKey<T> key, T value, long ttlTicks) {
         if (ttlTicks <= 0L) {
            throw new IllegalArgumentException("ttlTicks must be positive");
         } else {
            long expiresAt = MindMemoryStore.this.currentTick > Long.MAX_VALUE - ttlTicks ? Long.MAX_VALUE : MindMemoryStore.this.currentTick + ttlTicks;
            this.put(key, (long)value, expiresAt);
         }
      }

      @Override
      public void forget(MindMemoryKey<?> key) {
         this.requireDeclared(key);
         MindMemoryStore.this.values.remove(key);
      }

      @Override
      public boolean contains(MindMemoryKey<?> key) {
         this.requireDeclared(key);
         return MindMemoryStore.this.values.containsKey(key);
      }

      private <T> void put(MindMemoryKey<T> key, T value, long expiresAtTick) {
         this.requireDeclared(key);
         MindMemoryStore.this.values.put(key, new MindMemoryStore.Entry(key.requireType(value), expiresAtTick));
         if (expiresAtTick <= MindMemoryStore.this.currentTick) {
            MindMemoryStore.this.expiryCurrent = false;
         }
      }

      private void requireDeclared(MindMemoryKey<?> key) {
         if (!MindMemoryStore.this.schema.accepts(key)) {
            throw new IllegalArgumentException("Memory is not declared by this mind: " + key.identifier());
         }
      }
   }
}
