package ch.njol.skript.lang;

import java.util.Map;
import java.util.WeakHashMap;
import org.bukkit.event.Event;

public abstract class LoopSection extends Section implements SyntaxElement, Debuggable, SectionExitHandler {
   protected final transient Map<Event, Long> currentLoopCounter = new WeakHashMap<>();

   public long getLoopCounter(Event event) {
      return this.currentLoopCounter.getOrDefault(event, 1L);
   }

   @Override
   public abstract TriggerItem getActualNext();

   @Override
   public void exit(Event event) {
      this.currentLoopCounter.remove(event);
   }
}
