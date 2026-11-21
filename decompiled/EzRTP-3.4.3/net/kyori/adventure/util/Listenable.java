package net.kyori.adventure.util;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public abstract class Listenable<L> {
   private final List<L> listeners = new CopyOnWriteArrayList<>();

   protected final void forEachListener(final Consumer<L> consumer) {
      for (L listener : this.listeners) {
         consumer.accept(listener);
      }
   }

   protected final void addListener0(final L listener) {
      this.listeners.add(listener);
   }

   protected final void removeListener0(final L listener) {
      this.listeners.remove(listener);
   }
}
