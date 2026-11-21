package ch.njol.skript.lang;

import java.util.Iterator;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public interface Loopable<T> {
   @Nullable
   Iterator<? extends T> iterator(Event var1);

   boolean isLoopOf(String var1);

   default boolean supportsLoopPeeking() {
      return false;
   }
}
