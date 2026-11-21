package ch.njol.skript.lang.util;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class ContextlessEvent extends Event {
   private ContextlessEvent() {
   }

   public static ContextlessEvent get() {
      return new ContextlessEvent();
   }

   @NotNull
   public HandlerList getHandlers() {
      throw new IllegalStateException();
   }
}
