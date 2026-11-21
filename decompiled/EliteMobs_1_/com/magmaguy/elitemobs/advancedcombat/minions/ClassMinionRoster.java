package com.magmaguy.elitemobs.advancedcombat.minions;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

public final class ClassMinionRoster<T> {
   private final int cap;
   private final Deque<T> entries = new ArrayDeque<>();

   public ClassMinionRoster(int cap) {
      if (cap < 1) {
         throw new IllegalArgumentException("cap must be positive");
      } else {
         this.cap = cap;
      }
   }

   public List<T> admit(T entry) {
      return this.admitAll(List.of(Objects.requireNonNull(entry, "entry")));
   }

   public List<T> admitAll(Collection<? extends T> additions) {
      Objects.requireNonNull(additions, "additions");
      if (additions.size() > this.cap) {
         throw new IllegalArgumentException("A batch cannot exceed the roster cap");
      } else {
         List<T> evicted = new ArrayList<>();

         for (T addition : additions) {
            Objects.requireNonNull(addition, "addition");

            while (this.entries.size() >= this.cap) {
               evicted.add(this.entries.removeFirst());
            }

            this.entries.addLast(addition);
         }

         return List.copyOf(evicted);
      }
   }

   public boolean remove(T entry) {
      return this.entries.remove(entry);
   }

   public List<T> entries() {
      return List.copyOf(this.entries);
   }

   public List<T> drain() {
      List<T> drained = this.entries();
      this.entries.clear();
      return drained;
   }
}
