package dev.vatten.baserad;

import dev.vatten.baserad.events.Event;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class EventHandler {
   private final HashMap<Class<? extends Event>, List<Consumer<Event>>> eventHandlers = new HashMap<>();

   public EventHandler(VattenPlugin plugin) {
   }

   public <T extends Event> void registerEventHandler(Class<T> type, Consumer<T> consumer) {
      if (!this.eventHandlers.containsKey(type)) {
         this.eventHandlers.put(type, new ArrayList<>());
      }

      this.eventHandlers.get(type).add(consumer);
   }

   public <T extends Event> void dispatchEvent(T event) {
      if (this.eventHandlers.containsKey(event.getClass())) {
         for (Consumer<Event> consumer : this.eventHandlers.get(event.getClass())) {
            consumer.accept(event);
         }
      }
   }
}
