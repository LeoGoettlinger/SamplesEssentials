package ac.grim.grimac.shaded.kyori.adventure.util;

import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public abstract class Listenable<L> {
   private final List<L> listeners = new CopyOnWriteArrayList<>();

   protected final void forEachListener(@NotNull final Consumer<L> consumer) {
      for (L listener : this.listeners) {
         consumer.accept(listener);
      }
   }

   protected final void addListener0(@NotNull final L listener) {
      this.listeners.add(listener);
   }

   protected final void removeListener0(@NotNull final L listener) {
      this.listeners.remove(listener);
   }
}
