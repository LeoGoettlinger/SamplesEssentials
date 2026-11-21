package org.skriptlang.skript.bukkit.lang.eventvalue;

import ch.njol.skript.Skript;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.util.Registry;
import org.skriptlang.skript.util.ViewProvider;

public interface EventValueRegistry extends Registry<EventValue<?, ?>>, ViewProvider<EventValueRegistry> {
   static EventValueRegistry empty(Skript skript) {
      return new EventValueRegistryImpl(skript);
   }

   <E extends Event> void register(EventValue<E, ?> var1);

   boolean unregister(EventValue<?, ?> var1);

   boolean isRegistered(EventValue<?, ?> var1);

   boolean isRegistered(Class<? extends Event> var1, Class<?> var2, EventValue.Time var3);

   <E extends Event, V> EventValueRegistry.Resolution<E, V> resolve(Class<E> var1, String var2);

   <E extends Event, V> EventValueRegistry.Resolution<E, V> resolve(Class<E> var1, String var2, EventValue.Time var3);

   <E extends Event, V> EventValueRegistry.Resolution<E, V> resolve(Class<E> var1, String var2, EventValue.Time var3, EventValueRegistry.Flags var4);

   <E extends Event, V> EventValueRegistry.Resolution<E, ? extends V> resolve(Class<E> var1, Class<V> var2);

   <E extends Event, V> EventValueRegistry.Resolution<E, ? extends V> resolve(Class<E> var1, Class<V> var2, EventValue.Time var3);

   <E extends Event, V> EventValueRegistry.Resolution<E, ? extends V> resolve(Class<E> var1, Class<V> var2, EventValue.Time var3, EventValueRegistry.Flags var4);

   <E extends Event, V> EventValueRegistry.Resolution<E, V> resolveExact(Class<E> var1, Class<V> var2, EventValue.Time var3);

   @Unmodifiable
   List<EventValue<?, ?>> elements();

   @Unmodifiable
   List<EventValue<?, ?>> elements(EventValue.Time var1);

   @Unmodifiable
   <E extends Event> List<EventValue<? extends E, ?>> elements(Class<E> var1);

   default EventValueRegistry unmodifiableView() {
      return new EventValueRegistryImpl.UnmodifiableView(this);
   }

   public static enum Flag {
      FALLBACK_TO_DEFAULT_TIME_STATE,
      ALLOW_CONVERSION;
   }

   public record Flags(Set<EventValueRegistry.Flag> set) {
      public static final EventValueRegistry.Flags DEFAULT = new EventValueRegistry.Flags(
         Collections.unmodifiableSet(EnumSet.allOf(EventValueRegistry.Flag.class))
      );
      public static final EventValueRegistry.Flags NONE = new EventValueRegistry.Flags(
         Collections.unmodifiableSet(EnumSet.noneOf(EventValueRegistry.Flag.class))
      );

      public static EventValueRegistry.Flags of(Collection<EventValueRegistry.Flag> flags) {
         return new EventValueRegistry.Flags(EnumSet.copyOf(flags));
      }

      public static EventValueRegistry.Flags of(EventValueRegistry.Flag... flags) {
         return new EventValueRegistry.Flags(EnumSet.noneOf(EventValueRegistry.Flag.class)).with(flags);
      }

      public boolean has(EventValueRegistry.Flag flag) {
         return this.set.contains(flag);
      }

      public EventValueRegistry.Flags with(EventValueRegistry.Flag... flags) {
         Set<EventValueRegistry.Flag> newSet = EnumSet.copyOf(this.set);
         newSet.addAll(Arrays.asList(flags));
         return new EventValueRegistry.Flags(newSet);
      }

      public EventValueRegistry.Flags without(EventValueRegistry.Flag... flags) {
         Set<EventValueRegistry.Flag> newSet = EnumSet.copyOf(this.set);
         Arrays.asList(flags).forEach(newSet::remove);
         return new EventValueRegistry.Flags(newSet);
      }
   }

   public record Resolution<E extends Event, V>(List<EventValue<E, V>> all, boolean errored) {
      public static <E extends Event, V> EventValueRegistry.Resolution<E, V> of(List<EventValue<E, V>> eventValues) {
         return new EventValueRegistry.Resolution<>(eventValues, false);
      }

      public static <E extends Event, V> EventValueRegistry.Resolution<E, V> empty() {
         return new EventValueRegistry.Resolution<>(Collections.emptyList(), false);
      }

      public static <E extends Event, V> EventValueRegistry.Resolution<E, V> error() {
         return new EventValueRegistry.Resolution<>(Collections.emptyList(), true);
      }

      public boolean successful() {
         return !this.all.isEmpty();
      }

      public boolean multiple() {
         return this.all.size() > 1;
      }

      public EventValue<E, V> unique() {
         if (this.all.size() != 1) {
            throw new IllegalStateException("Resolution is not unique (size: " + this.all.size() + ")");
         } else {
            return this.all.getFirst();
         }
      }

      public EventValue<E, V> uniqueOrNull() {
         return this.all.size() != 1 ? null : this.all.getFirst();
      }

      public Optional<EventValue<E, V>> uniqueOptional() {
         return this.all.size() != 1 ? Optional.empty() : Optional.of(this.all.getFirst());
      }

      public EventValue<E, V> any() {
         if (this.all.isEmpty()) {
            throw new IllegalStateException("Resolution is empty");
         } else {
            return this.all.getFirst();
         }
      }

      public EventValue<E, V> anyOrNull() {
         return this.all.isEmpty() ? null : this.all.getFirst();
      }

      public Optional<EventValue<E, V>> anyOptional() {
         return this.all.isEmpty() ? Optional.empty() : Optional.of(this.all.getFirst());
      }

      public int size() {
         return this.all.size();
      }
   }
}
