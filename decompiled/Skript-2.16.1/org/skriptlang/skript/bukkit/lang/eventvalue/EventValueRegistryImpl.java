package org.skriptlang.skript.bukkit.lang.eventvalue;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.lang.converter.Converter;
import org.skriptlang.skript.util.ClassUtils;

final class EventValueRegistryImpl implements EventValueRegistry {
   private final Skript skript;
   private final Map<EventValue.Time, List<EventValue<?, ?>>> eventValues = new EnumMap<>(EventValue.Time.class);
   private final transient Map<EventValueRegistryImpl.Input<?, ?>, EventValueRegistry.Resolution<?, ?>> eventValuesCache = new ConcurrentHashMap<>();

   public EventValueRegistryImpl(Skript skript) {
      this.skript = skript;

      for (EventValue.Time time : EventValue.Time.values()) {
         this.eventValues.put(time, new ArrayList<>());
      }
   }

   @Override
   public <E extends Event> void register(EventValue<E, ?> eventValue) {
      Preconditions.checkNotNull(eventValue, "eventValue");
      if (eventValue instanceof ConvertedEventValue) {
         throw new SkriptAPIException("Cannot register a converted event value: " + eventValue);
      } else if (this.isRegistered(eventValue)) {
         Skript.warning(eventValue + " is already registered.");
      } else {
         List<EventValue<?, ?>> eventValues = this.eventValues(eventValue.time());
         eventValues.add(eventValue);
         this.eventValuesCache.clear();
      }
   }

   @Override
   public boolean unregister(EventValue<?, ?> eventValue) {
      boolean removed = this.eventValues(eventValue.time()).remove(eventValue);
      if (removed) {
         this.eventValuesCache.clear();
      }

      return removed;
   }

   @Override
   public boolean isRegistered(EventValue<?, ?> eventValue) {
      for (EventValue<?, ?> existing : this.eventValues(eventValue.time())) {
         if (existing.matches(eventValue)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean isRegistered(Class<? extends Event> eventClass, Class<?> valueClass, EventValue.Time time) {
      for (EventValue<?, ?> eventValue : this.eventValues(time)) {
         if (eventValue.matches(eventClass, valueClass)) {
            return true;
         }
      }

      return false;
   }

   private <E extends Event, V> void cache(EventValueRegistryImpl.Input<E, ?> input, EventValueRegistry.Resolution<E, V> resolution, boolean contextDependent) {
      if (!contextDependent) {
         this.eventValuesCache.put(input, resolution);
      }
   }

   @Override
   public <E extends Event, V> EventValueRegistry.Resolution<E, V> resolve(Class<E> eventClass, String identifier) {
      return this.resolve(eventClass, identifier, EventValue.Time.NOW);
   }

   @Override
   public <E extends Event, V> EventValueRegistry.Resolution<E, V> resolve(Class<E> eventClass, String identifier, EventValue.Time time) {
      return this.resolve(eventClass, identifier, time, EventValueRegistry.Flags.DEFAULT);
   }

   @Override
   public <E extends Event, V> EventValueRegistry.Resolution<E, V> resolve(
      Class<E> eventClass, String identifier, EventValue.Time time, EventValueRegistry.Flags flags
   ) {
      Preconditions.checkNotNull(eventClass, "eventClass");
      Preconditions.checkNotNull(identifier, "identifier");
      if (time == EventValue.Time.NOW) {
         flags = flags.without(EventValueRegistry.Flag.FALLBACK_TO_DEFAULT_TIME_STATE);
      }

      EventValueRegistryImpl.Input<E, String> input = EventValueRegistryImpl.Input.of(eventClass, identifier, time, flags);
      EventValueRegistry.Resolution<E, V> resolution = (EventValueRegistry.Resolution<E, V>)this.eventValuesCache.get(input);
      if (resolution != null) {
         return resolution;
      } else {
         Resolver.Output<E, V> output = Resolver.<E, V>builder(eventClass)
            .filter(ev -> ClassUtils.isRelatedTo(ev.eventClass(), eventClass) && ev.matchesInput(identifier))
            .comparator(Resolver.EVENT_DISTANCE_COMPARATOR)
            .mapper(ev -> ev.getConverted(eventClass, (Class<V>)ev.valueClass()))
            .build()
            .resolve(this.eventValues(time));
         resolution = output.resolution();
         if (resolution.successful() || resolution.errored()) {
            this.cache(input, resolution, output.contextDependent());
            return resolution;
         } else if (flags.has(EventValueRegistry.Flag.FALLBACK_TO_DEFAULT_TIME_STATE)) {
            return this.resolve(eventClass, identifier, EventValue.Time.NOW, flags);
         } else {
            resolution = EventValueRegistry.Resolution.empty();
            this.cache(input, resolution, output.contextDependent());
            return resolution;
         }
      }
   }

   @Override
   public <E extends Event, V> EventValueRegistry.Resolution<E, ? extends V> resolve(Class<E> eventClass, Class<V> valueClass) {
      return this.resolve(eventClass, valueClass, EventValue.Time.NOW);
   }

   @Override
   public <E extends Event, V> EventValueRegistry.Resolution<E, ? extends V> resolve(Class<E> eventClass, Class<V> valueClass, EventValue.Time time) {
      return this.resolve(eventClass, valueClass, time, EventValueRegistry.Flags.DEFAULT);
   }

   @Override
   public <E extends Event, V> EventValueRegistry.Resolution<E, ? extends V> resolve(
      Class<E> eventClass, Class<V> valueClass, EventValue.Time time, EventValueRegistry.Flags flags
   ) {
      Preconditions.checkNotNull(eventClass, "eventClass");
      Preconditions.checkNotNull(valueClass, "valueClass");
      if (time == EventValue.Time.NOW) {
         flags = flags.without(EventValueRegistry.Flag.FALLBACK_TO_DEFAULT_TIME_STATE);
      }

      EventValueRegistryImpl.Input<E, Class<?>> input = EventValueRegistryImpl.Input.of(eventClass, valueClass, time, flags);
      EventValueRegistry.Resolution<E, ? extends V> resolution = (EventValueRegistry.Resolution<E, ? extends V>)this.eventValuesCache.get(input);
      if (resolution != null) {
         return resolution;
      } else {
         Resolver.Output<E, V> output = Resolver.exact(eventClass, valueClass).resolve(this.eventValues(time));
         resolution = output.resolution();
         if (!resolution.successful() && !resolution.errored()) {
            output = this.resolveNearest(eventClass, valueClass, time);
            resolution = output.resolution();
            if (!resolution.successful() && !resolution.errored()) {
               if (flags.has(EventValueRegistry.Flag.ALLOW_CONVERSION)) {
                  output = this.resolveWithDowncastConversion(eventClass, valueClass, time);
                  resolution = output.resolution();
                  if (resolution.successful() || resolution.errored()) {
                     this.cache(input, resolution, output.contextDependent());
                     return resolution;
                  }

                  output = this.resolveWithConversion(eventClass, valueClass, time);
                  resolution = output.resolution();
                  if (resolution.successful() || resolution.errored()) {
                     this.cache(input, resolution, output.contextDependent());
                     return resolution;
                  }
               }

               return flags.has(EventValueRegistry.Flag.FALLBACK_TO_DEFAULT_TIME_STATE)
                  ? this.resolve(eventClass, valueClass, EventValue.Time.NOW, flags)
                  : EventValueRegistry.Resolution.empty();
            } else {
               this.cache(input, resolution, output.contextDependent());
               return resolution;
            }
         } else {
            this.cache(input, resolution, output.contextDependent());
            return resolution;
         }
      }
   }

   @Override
   public <E extends Event, V> EventValueRegistry.Resolution<E, V> resolveExact(Class<E> eventClass, Class<V> valueClass, EventValue.Time time) {
      return Resolver.exact(eventClass, valueClass).resolve(this.eventValues(time)).resolution();
   }

   private <E extends Event, V> Resolver.Output<E, V> resolveNearest(Class<E> eventClass, Class<V> valueClass, EventValue.Time time) {
      return Resolver.builder(eventClass, valueClass)
         .filter(ev -> ClassUtils.isRelatedTo(ev.eventClass(), eventClass) && valueClass.isAssignableFrom(ev.valueClass()))
         .comparator(Resolver.EVENT_VALUE_DISTANCE_COMPARATOR)
         .mapper(ev -> ev.getConverted(eventClass, valueClass))
         .filterMatches()
         .build()
         .resolve(this.eventValues(time));
   }

   private <E extends Event, V> Resolver.Output<E, V> resolveWithDowncastConversion(Class<E> eventClass, Class<V> valueClass, EventValue.Time time) {
      Converter<?, V> converter = source -> valueClass.isInstance(source) ? valueClass.cast(source) : null;
      return Resolver.builder(eventClass, valueClass)
         .filter(ev -> ClassUtils.isRelatedTo(ev.eventClass(), eventClass) && ev.valueClass().isAssignableFrom(valueClass))
         .comparator(Resolver.EVENT_VALUE_DISTANCE_COMPARATOR)
         .mapper(ev -> ev.getConverted(eventClass, valueClass, converter))
         .filterMatches()
         .build()
         .resolve(this.eventValues(time));
   }

   private <E extends Event, V> Resolver.Output<E, V> resolveWithConversion(Class<E> eventClass, Class<V> valueClass, EventValue.Time time) {
      return Resolver.builder(eventClass, valueClass)
         .filter(ev -> ClassUtils.isRelatedTo(ev.eventClass(), eventClass))
         .comparator(Resolver.BI_EVENT_DISTANCE_COMPARATOR)
         .mapper(ev -> ev.getConverted(eventClass, valueClass))
         .build()
         .resolve(this.eventValues(time));
   }

   private List<EventValue<?, ?>> eventValues(EventValue.Time time) {
      return this.eventValues.get(time);
   }

   @Unmodifiable
   @Override
   public List<EventValue<?, ?>> elements() {
      return this.eventValues.values().stream().flatMap(Collection::stream).toList();
   }

   @Unmodifiable
   @Override
   public List<EventValue<?, ?>> elements(EventValue.Time time) {
      return List.copyOf(this.eventValues(time));
   }

   @Unmodifiable
   @Override
   public <E extends Event> List<EventValue<? extends E, ?>> elements(Class<E> event) {
      return this.eventValues.values().stream().flatMap(Collection::stream).filter(eventValue -> event.isAssignableFrom(eventValue.eventClass())).toList();
   }

   private record Input<E extends Event, I>(Class<E> eventClass, I input, EventValue.Time time, EventValueRegistry.Flags flags) {
      static <E extends Event> EventValueRegistryImpl.Input<E, String> of(
         Class<E> eventClass, String input, EventValue.Time time, EventValueRegistry.Flags flags
      ) {
         return new EventValueRegistryImpl.Input<>(eventClass, input, time, flags);
      }

      static <E extends Event> EventValueRegistryImpl.Input<E, Class<?>> of(
         Class<E> eventClass, Class<?> input, EventValue.Time time, EventValueRegistry.Flags flags
      ) {
         return new EventValueRegistryImpl.Input<>(eventClass, input, time, flags);
      }
   }

   static class UnmodifiableView implements EventValueRegistry {
      private final EventValueRegistry delegate;

      UnmodifiableView(EventValueRegistry delegate) {
         this.delegate = delegate;
      }

      @Override
      public <E extends Event> void register(EventValue<E, ?> eventValue) {
         throw new UnsupportedOperationException("Cannot register event values with an unmodifiable event value registry.");
      }

      @Override
      public boolean unregister(EventValue<?, ?> eventValue) {
         throw new UnsupportedOperationException("Cannot unregister event values from an unmodifiable event value registry.");
      }

      @Override
      public boolean isRegistered(EventValue<?, ?> eventValue) {
         return this.delegate.isRegistered(eventValue);
      }

      @Override
      public boolean isRegistered(Class<? extends Event> eventClass, Class<?> valueClass, EventValue.Time time) {
         return this.delegate.isRegistered(eventClass, valueClass, time);
      }

      @Override
      public <E extends Event, V> EventValueRegistry.Resolution<E, V> resolve(Class<E> eventClass, String identifier) {
         return this.delegate.resolve(eventClass, identifier);
      }

      @Override
      public <E extends Event, V> EventValueRegistry.Resolution<E, V> resolve(Class<E> eventClass, String identifier, EventValue.Time time) {
         return this.delegate.resolve(eventClass, identifier, time);
      }

      @Override
      public <E extends Event, V> EventValueRegistry.Resolution<E, V> resolve(
         Class<E> eventClass, String identifier, EventValue.Time time, EventValueRegistry.Flags flags
      ) {
         return this.delegate.resolve(eventClass, identifier, time, flags);
      }

      @Override
      public <E extends Event, V> EventValueRegistry.Resolution<E, ? extends V> resolve(Class<E> eventClass, Class<V> valueClass) {
         return this.delegate.resolve(eventClass, valueClass);
      }

      @Override
      public <E extends Event, V> EventValueRegistry.Resolution<E, ? extends V> resolve(Class<E> eventClass, Class<V> valueClass, EventValue.Time time) {
         return this.delegate.resolve(eventClass, valueClass, time);
      }

      @Override
      public <E extends Event, V> EventValueRegistry.Resolution<E, ? extends V> resolve(
         Class<E> eventClass, Class<V> valueClass, EventValue.Time time, EventValueRegistry.Flags flags
      ) {
         return this.delegate.resolve(eventClass, valueClass, time, flags);
      }

      @Override
      public <E extends Event, V> EventValueRegistry.Resolution<E, V> resolveExact(Class<E> eventClass, Class<V> valueClass, EventValue.Time time) {
         return this.delegate.resolveExact(eventClass, valueClass, time);
      }

      @Unmodifiable
      @Override
      public List<EventValue<?, ?>> elements() {
         return this.delegate.elements();
      }

      @Unmodifiable
      @Override
      public List<EventValue<?, ?>> elements(EventValue.Time time) {
         return this.delegate.elements(time);
      }

      @Unmodifiable
      @Override
      public <E extends Event> List<EventValue<? extends E, ?>> elements(Class<E> event) {
         return this.delegate.elements(event);
      }

      @Override
      public EventValueRegistry unmodifiableView() {
         return this;
      }
   }
}
