package ch.njol.skript.registrations;

import ch.njol.skript.classes.Changer;
import ch.njol.util.Kleenean;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import java.util.Collection;
import java.util.List;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.lang.converter.Converter;

@Deprecated(
   since = "2.15",
   forRemoval = true
)
public class EventValues {
   /** @deprecated */
   public static final int TIME_PAST = EventValue.Time.PAST.value();
   /** @deprecated */
   public static final int TIME_NOW = EventValue.Time.NOW.value();
   /** @deprecated */
   public static final int TIME_FUTURE = EventValue.Time.FUTURE.value();
   private static EventValueRegistry registry;

   private EventValues() {
   }

   @Internal
   public static void setEventValueRegistry(EventValueRegistry registry) {
      if (EventValues.registry != null) {
         throw new IllegalStateException("EventValueRegistry is already set and cannot be changed.");
      } else {
         EventValues.registry = registry;
      }
   }

   /** @deprecated */
   @Unmodifiable
   public static List<EventValues.EventValueInfo<?, ?>> getEventValuesListForTime(int time) {
      return registry.elements(EventValue.Time.of(time)).stream().map(EventValues.EventValueInfo::fromModern).toList();
   }

   /** @deprecated */
   public static <T, E extends Event> void registerEventValue(Class<E> eventClass, Class<T> valueClass, Converter<E, T> converter) {
      registerEventValue(eventClass, valueClass, converter, TIME_NOW);
   }

   /** @deprecated */
   public static <T, E extends Event> void registerEventValue(Class<E> eventClass, Class<T> valueClass, Converter<E, T> converter, int time) {
      registerEventValue(eventClass, valueClass, converter, time, null, (Class<? extends E>[])null);
   }

   /** @deprecated */
   @SafeVarargs
   public static <T, E extends Event> void registerEventValue(
      Class<E> eventClass,
      Class<T> valueClass,
      Converter<E, T> converter,
      int time,
      @Nullable String excludeErrorMessage,
      @Nullable Class<? extends E>... excludes
   ) {
      EventValue.Builder<E, T> builder = EventValue.builder(eventClass, valueClass)
         .getter(converter)
         .time(EventValue.Time.of(time))
         .excludedErrorMessage(excludeErrorMessage)
         .excludes(excludes);
      if (converter instanceof EventConverter<E, T> eventConverter) {
         builder.registerChanger(Changer.ChangeMode.SET, eventConverter::set);
      }

      registry.register(builder.build());
   }

   /** @deprecated */
   @Nullable
   public static <T, E extends Event> T getEventValue(E event, Class<T> valueClass, int time) {
      return registry.resolve(event.getClass(), valueClass, EventValue.Time.of(time))
         .uniqueOptional()
         .map(eventValue -> ((EventValue<E, ? extends T>)eventValue).get(event))
         .orElse(null);
   }

   /** @deprecated */
   @Nullable
   public static <E extends Event, T> Converter<? super E, ? extends T> getExactEventValueConverter(Class<E> eventClass, Class<T> valueClass, int time) {
      return registry.resolveExact(eventClass, valueClass, EventValue.Time.of(time)).anyOptional().map(EventValue::converter).orElse(null);
   }

   public static <T, E extends Event> Kleenean hasMultipleConverters(Class<E> eventClass, Class<T> valueClass, int time) {
      List<Converter<? super E, ? extends T>> getters = getEventValueConverters(eventClass, valueClass, time, true, false);
      return getters == null ? Kleenean.UNKNOWN : Kleenean.get(getters.size() > 1);
   }

   /** @deprecated */
   @Nullable
   public static <T, E extends Event> Converter<? super E, ? extends T> getEventValueConverter(Class<E> eventClass, Class<T> valueClass, int time) {
      return getEventValueConverter(eventClass, valueClass, time, true);
   }

   @Nullable
   private static <T, E extends Event> Converter<? super E, ? extends T> getEventValueConverter(
      Class<E> eventClass, Class<T> valueClass, int time, boolean allowDefault
   ) {
      List<Converter<? super E, ? extends T>> list = getEventValueConverters(eventClass, valueClass, time, allowDefault);
      return list != null && !list.isEmpty() ? list.get(0) : null;
   }

   @Nullable
   private static <T, E extends Event> List<Converter<? super E, ? extends T>> getEventValueConverters(
      Class<E> eventClass, Class<T> valueClass, int time, boolean allowDefault
   ) {
      return getEventValueConverters(eventClass, valueClass, time, allowDefault, true);
   }

   @Nullable
   private static <T, E extends Event> List<Converter<? super E, ? extends T>> getEventValueConverters(
      Class<E> eventClass, Class<T> valueClass, int time, boolean allowDefault, boolean allowConverting
   ) {
      EventValueRegistry.Flags flags = EventValueRegistry.Flags.NONE;
      if (allowDefault) {
         flags = flags.with(EventValueRegistry.Flag.FALLBACK_TO_DEFAULT_TIME_STATE);
      }

      if (allowConverting) {
         flags = flags.with(EventValueRegistry.Flag.ALLOW_CONVERSION);
      }

      EventValueRegistry.Resolution<E, ? extends T> resolution = registry.resolve(eventClass, valueClass, EventValue.Time.of(time), flags);
      return !resolution.successful() ? null : resolution.all().stream().map(EventValue::converter).toList();
   }

   public static boolean doesExactEventValueHaveTimeStates(Class<? extends Event> eventClass, Class<?> valueClass) {
      return getExactEventValueConverter(eventClass, valueClass, TIME_PAST) != null || getExactEventValueConverter(eventClass, valueClass, TIME_FUTURE) != null;
   }

   public static boolean doesEventValueHaveTimeStates(Class<? extends Event> eventClass, Class<?> valueClass) {
      return getEventValueConverter(eventClass, valueClass, TIME_PAST, false) != null
         || getEventValueConverter(eventClass, valueClass, TIME_FUTURE, false) != null;
   }

   public static int[] getTimeStates() {
      return new int[]{TIME_PAST, TIME_NOW, TIME_FUTURE};
   }

   public static Multimap<Class<? extends Event>, EventValues.EventValueInfo<?, ?>> getPerEventEventValues() {
      Multimap<Class<? extends Event>, EventValues.EventValueInfo<?, ?>> eventValues = MultimapBuilder.hashKeys().hashSetValues().build();

      for (int time : getTimeStates()) {
         for (EventValues.EventValueInfo<?, ?> eventValueInfo : getEventValuesListForTime(time)) {
            Collection<EventValues.EventValueInfo<?, ?>> existing = eventValues.get(eventValueInfo.eventClass);
            existing.add(eventValueInfo);
            eventValues.putAll(eventValueInfo.eventClass, existing);
         }
      }

      return eventValues;
   }

   public record EventValueInfo<E extends Event, T>(
      Class<E> eventClass,
      Class<T> valueClass,
      Converter<E, T> converter,
      @Nullable String excludeErrorMessage,
      @Nullable Class<? extends E>[] excludes,
      int time
   ) {
      public EventValueInfo(
         Class<E> eventClass,
         Class<T> valueClass,
         Converter<E, T> converter,
         @Nullable String excludeErrorMessage,
         @Nullable Class<? extends E>[] excludes,
         int time
      ) {
         assert eventClass != null;

         assert valueClass != null;

         assert converter != null;

         this.eventClass = eventClass;
         this.valueClass = valueClass;
         this.converter = converter;
         this.excludeErrorMessage = excludeErrorMessage;
         this.excludes = excludes;
         this.time = time;
      }

      public static <E extends Event, T> EventValues.EventValueInfo<E, T> fromModern(EventValue<E, T> eventValue) {
         return new EventValues.EventValueInfo<>(
            eventValue.eventClass(), eventValue.valueClass(), eventValue.changer(Changer.ChangeMode.SET).map(changer -> new EventConverter<E, T>() {
               @Nullable
               public T convert(E from) {
                  return eventValue.get(from);
               }

               @Override
               public void set(E event, @Nullable T value) {
                  changer.change(event, value);
               }
            }).orElse(eventValue.converter()), eventValue.excludedErrorMessage(), eventValue.excludedEvents().toArray(new Class[0]), eventValue.time().value()
         );
      }
   }
}
