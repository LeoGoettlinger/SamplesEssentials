package org.skriptlang.skript.bukkit.lang.eventvalue;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.registrations.Classes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.util.ClassUtils;

class Resolver<E extends Event, V> {
   static final Resolver.EventComparatorFactory EVENT_DISTANCE_COMPARATOR = eventClass -> Comparator.comparingInt(
      ev -> ClassUtils.hierarchyDistance(ev.eventClass(), eventClass)
   );
   static final Resolver.EventComparatorFactory BI_EVENT_DISTANCE_COMPARATOR = eventClass -> EVENT_DISTANCE_COMPARATOR.create(eventClass)
      .thenComparingInt(ev -> ClassUtils.hierarchyDistance(eventClass, ev.valueClass()));
   static final Resolver.EventValueComparatorFactory EVENT_VALUE_DISTANCE_COMPARATOR = (eventClass, valueClass) -> BI_EVENT_DISTANCE_COMPARATOR.create(
         eventClass
      )
      .thenComparingInt(ev -> ClassUtils.hierarchyDistance(valueClass, ev.valueClass()));
   private final Class<E> eventClass;
   @Nullable
   private final Class<V> valueClass;
   private final Predicate<EventValue<?, ?>> filter;
   private final Comparator<EventValue<?, ?>> comparator;
   private final Function<EventValue<?, ?>, EventValue<E, V>> mapper;
   private final boolean filterMatches;

   private Resolver(
      Class<E> eventClass,
      @Nullable Class<V> valueClass,
      Predicate<EventValue<?, ?>> filter,
      Comparator<EventValue<?, ?>> comparator,
      Function<EventValue<?, ?>, EventValue<E, V>> mapper,
      boolean filterMatches
   ) {
      this.eventClass = eventClass;
      this.valueClass = valueClass;
      this.filter = filter;
      this.comparator = comparator;
      this.mapper = mapper;
      this.filterMatches = filterMatches;
   }

   public Resolver.Output<E, V> resolve(List<EventValue<?, ?>> eventValues) {
      List<EventValue<E, V>> best = new ArrayList<>();
      EventValue<?, ?> bestMatch = null;
      boolean contextDependent = false;

      for (EventValue<?, ?> eventValue : eventValues) {
         if (this.filter.test(eventValue)) {
            contextDependent |= eventValue.contextDependent();
            switch (eventValue.validate(this.eventClass)) {
               case INVALID:
                  break;
               case ABORT:
                  return new Resolver.Output<>(EventValueRegistry.Resolution.error(), contextDependent);
               default:
                  int comparison = bestMatch != null ? this.comparator.compare(eventValue, bestMatch) : -1;
                  if (comparison < 0) {
                     EventValue<E, V> converted = this.mapper != null ? this.mapper.apply(eventValue) : eventValue;
                     if (converted != null) {
                        best.clear();
                        best.add(converted);
                        bestMatch = eventValue;
                     }
                  } else if (comparison == 0) {
                     EventValue<E, V> converted = this.mapper != null ? this.mapper.apply(eventValue) : eventValue;
                     if (converted != null) {
                        best.add(converted);
                     }
                  }
            }
         }
      }

      return this.valueClass != null && this.filterMatches
         ? new Resolver.Output<>(EventValueRegistry.Resolution.of(filterEventValues(this.valueClass, best)), contextDependent)
         : new Resolver.Output<>(EventValueRegistry.Resolution.of(best), contextDependent);
   }

   private static <E extends Event, V> List<EventValue<E, V>> filterEventValues(Class<V> valueClass, List<EventValue<E, V>> eventValues) {
      if (eventValues.size() <= 1) {
         return eventValues;
      } else {
         List<EventValue<E, V>> filtered = new ArrayList<>();
         ClassInfo<V> requestedValueClassInfo = Classes.getExactClassInfo(valueClass);

         for (EventValue<E, V> eventValue : eventValues) {
            ClassInfo<V> eventValueClassInfo = Classes.getExactClassInfo(eventValue.valueClass());
            if (eventValueClassInfo == null || eventValueClassInfo.equals(requestedValueClassInfo)) {
               filtered.add(eventValue);
            }
         }

         return filtered.isEmpty() ? eventValues : filtered;
      }
   }

   static <E extends Event, V> Resolver.Builder<E, V> builder(Class<E> eventClass) {
      return new Resolver.Builder<>(eventClass, null);
   }

   static <E extends Event, V> Resolver.Builder<E, V> builder(Class<E> eventClass, Class<V> valueClass) {
      return new Resolver.Builder<>(eventClass, valueClass);
   }

   static <E extends Event, V> Resolver<E, V> exact(Class<E> eventClass, Class<V> valueClass) {
      return builder(eventClass, valueClass)
         .filter(ev -> ev.eventClass().isAssignableFrom(eventClass) && ev.valueClass().equals(valueClass))
         .comparator(EVENT_DISTANCE_COMPARATOR)
         .filterMatches()
         .build();
   }

   static class Builder<E extends Event, V> {
      private final Class<E> eventClass;
      @Nullable
      private final Class<V> valueClass;
      private Predicate<EventValue<?, ?>> filter = ev -> true;
      private Comparator<EventValue<?, ?>> comparator = (a, b) -> 0;
      private Function<EventValue<?, ?>, EventValue<E, V>> mapper;
      private boolean filterMatches = false;

      Builder(Class<E> eventClass, @Nullable Class<V> valueClass) {
         this.eventClass = eventClass;
         this.valueClass = valueClass;
      }

      public Resolver.Builder<E, V> filter(Predicate<EventValue<?, ?>> filter) {
         this.filter = filter;
         return this;
      }

      public Resolver.Builder<E, V> comparator(Comparator<EventValue<?, ?>> comparator) {
         this.comparator = comparator;
         return this;
      }

      public Resolver.Builder<E, V> comparator(Resolver.EventComparatorFactory factory) {
         this.comparator = factory.create(this.eventClass);
         return this;
      }

      public Resolver.Builder<E, V> comparator(Resolver.EventValueComparatorFactory factory) {
         this.comparator = factory.create(this.eventClass, this.valueClass);
         return this;
      }

      public Resolver.Builder<E, V> mapper(Function<EventValue<?, ?>, EventValue<E, V>> mapper) {
         this.mapper = mapper;
         return this;
      }

      public Resolver.Builder<E, V> filterMatches() {
         this.filterMatches = true;
         return this;
      }

      public Resolver<E, V> build() {
         return new Resolver<>(this.eventClass, this.valueClass, this.filter, this.comparator, this.mapper, this.filterMatches);
      }
   }

   @FunctionalInterface
   interface EventComparatorFactory {
      Comparator<EventValue<?, ?>> create(Class<? extends Event> var1);
   }

   @FunctionalInterface
   interface EventValueComparatorFactory {
      Comparator<EventValue<?, ?>> create(Class<? extends Event> var1, Class<?> var2);
   }

   record Output<E extends Event, V>(EventValueRegistry.Resolution<E, V> resolution, boolean contextDependent) {
   }
}
