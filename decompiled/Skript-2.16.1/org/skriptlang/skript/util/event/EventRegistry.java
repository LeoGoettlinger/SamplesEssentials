package org.skriptlang.skript.util.event;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.Unmodifiable;

public class EventRegistry<E extends Event> {
   private final Set<E> events = ConcurrentHashMap.newKeySet();

   public void register(E event) {
      this.events.add(event);
   }

   public <T extends E> void register(Class<T> eventType, T event) {
      this.events.add((E)event);
   }

   public void unregister(E event) {
      this.events.remove(event);
   }

   @Unmodifiable
   public Set<E> events() {
      return ImmutableSet.copyOf(this.events);
   }

   @Unmodifiable
   public <T extends E> Set<T> events(Class<T> type) {
      Builder<T> builder = ImmutableSet.builder();
      this.events.stream().filter(event -> type.isAssignableFrom(event.getClass())).forEach(e -> builder.add(e));
      return builder.build();
   }
}
