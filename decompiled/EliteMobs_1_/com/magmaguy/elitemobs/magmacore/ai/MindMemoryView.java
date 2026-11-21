package com.magmaguy.elitemobs.magmacore.ai;

import java.util.Optional;

public interface MindMemoryView {
   <T> Optional<T> get(MindMemoryKey<T> var1);

   <T> void set(MindMemoryKey<T> var1, T var2);

   <T> void set(MindMemoryKey<T> var1, T var2, long var3);

   void forget(MindMemoryKey<?> var1);

   boolean contains(MindMemoryKey<?> var1);
}
