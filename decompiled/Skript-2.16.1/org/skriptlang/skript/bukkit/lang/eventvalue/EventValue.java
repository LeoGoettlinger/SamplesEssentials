package org.skriptlang.skript.bukkit.lang.eventvalue;

import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.coll.CollectionUtils;
import java.util.Collection;
import java.util.Optional;
import java.util.SequencedCollection;
import java.util.function.BiPredicate;
import java.util.function.Function;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.lang.converter.Converter;

public sealed interface EventValue<E extends Event, V> permits EventValueImpl, ConvertedEventValue {
   static <E extends Event, V> EventValue.Builder<E, V> builder(Class<E> eventClass, Class<V> valueClass) {
      return new EventValueImpl.BuilderImpl<>(eventClass, valueClass);
   }

   static <E extends Event, V> EventValue<E, V> simple(Class<E> eventClass, Class<V> valueClass, Converter<E, V> converter) {
      return builder(eventClass, valueClass).getter(converter).build();
   }

   @Contract(
      pure = true
   )
   Class<E> eventClass();

   @Contract(
      pure = true
   )
   Class<V> valueClass();

   @Contract(
      pure = true
   )
   @Unmodifiable
   SequencedCollection<String> patterns();

   EventValue.Validation validate(Class<?> var1);

   @Contract(
      pure = true
   )
   boolean matchesInput(String var1);

   @Contract(
      pure = true
   )
   V get(E var1);

   @Contract(
      pure = true
   )
   Converter<E, V> converter();

   @Contract(
      pure = true
   )
   boolean hasChanger(ch.njol.skript.classes.Changer.ChangeMode var1);

   @Contract(
      pure = true
   )
   Optional<EventValue.Changer<E, V>> changer(ch.njol.skript.classes.Changer.ChangeMode var1);

   @Contract(
      pure = true
   )
   EventValue.Time time();

   @Contract(
      pure = true
   )
   @Unmodifiable
   Collection<Class<? extends E>> excludedEvents();

   @Contract(
      pure = true
   )
   @Nullable
   String excludedErrorMessage();

   @Contract(
      pure = true
   )
   boolean contextDependent();

   @Contract(
      pure = true
   )
   boolean matches(EventValue<?, ?> var1);

   @Contract(
      pure = true
   )
   default boolean matches(Class<? extends Event> eventClass, Class<?> valueClass, SequencedCollection<String> patterns) {
      return this.matches(eventClass, valueClass) && this.patterns().equals(patterns);
   }

   @Contract(
      pure = true
   )
   default boolean matches(Class<? extends Event> eventClass, Class<?> valueClass) {
      return this.eventClass().equals(eventClass) && this.valueClass().equals(valueClass);
   }

   @Nullable
   <ConvertedEvent extends Event, ConvertedValue> EventValue<ConvertedEvent, ConvertedValue> getConverted(
      Class<ConvertedEvent> var1, Class<ConvertedValue> var2
   );

   @Nullable
   default <NewEvent extends Event, NewValue> EventValue<NewEvent, NewValue> getConverted(
      Class<NewEvent> newEventClass, Class<NewValue> newValueClass, Converter<V, NewValue> converter
   ) {
      return this.getConverted(newEventClass, newValueClass, converter, null);
   }

   <NewEvent extends Event, NewValue> EventValue<NewEvent, NewValue> getConverted(
      Class<NewEvent> var1, Class<NewValue> var2, Converter<V, NewValue> var3, @Nullable Converter<NewValue, V> var4
   );

   public interface Builder<E extends Event, V> {
      @Contract(
         value = "_ -> this",
         mutates = "this"
      )
      EventValue.Builder<E, V> patterns(String... var1);

      @Contract(
         value = "_ -> this",
         mutates = "this"
      )
      EventValue.Builder<E, V> inputValidator(BiPredicate<String, SkriptParser.ParseResult> var1);

      @Contract(
         value = "_ -> this",
         mutates = "this"
      )
      EventValue.Builder<E, V> eventValidator(Function<Class<?>, EventValue.Validation> var1);

      @Contract(
         value = "_ -> this",
         mutates = "this"
      )
      EventValue.Builder<E, V> getter(Converter<E, V> var1);

      @Contract(
         value = "_, _ -> this",
         mutates = "this"
      )
      EventValue.Builder<E, V> registerChanger(ch.njol.skript.classes.Changer.ChangeMode var1, EventValue.Changer<E, V> var2);

      @Contract(
         value = "_ -> this",
         mutates = "this"
      )
      EventValue.Builder<E, V> time(EventValue.Time var1);

      @Contract(
         value = "_ -> this",
         mutates = "this"
      )
      default EventValue.Builder<E, V> excludes(Class<? extends E> event) {
         this.excludes(CollectionUtils.array(event));
         return this;
      }

      @Contract(
         value = "_, _ -> this",
         mutates = "this"
      )
      default EventValue.Builder<E, V> excludes(Class<? extends E> event1, Class<? extends E> event2) {
         this.excludes(CollectionUtils.array(event1, event2));
         return this;
      }

      @Contract(
         value = "_, _, _ -> this",
         mutates = "this"
      )
      default EventValue.Builder<E, V> excludes(Class<? extends E> event1, Class<? extends E> event2, Class<? extends E> event3) {
         this.excludes(CollectionUtils.array(event1, event2, event3));
         return this;
      }

      @Contract(
         value = "_ -> this",
         mutates = "this"
      )
      EventValue.Builder<E, V> excludes(Class<? extends E>[] var1);

      @Contract(
         value = "_ -> this",
         mutates = "this"
      )
      EventValue.Builder<E, V> excludedErrorMessage(String var1);

      @Contract(
         value = " -> this",
         mutates = "this"
      )
      EventValue.Builder<E, V> contextDependent();

      EventValue<E, V> build();
   }

   @FunctionalInterface
   public interface Changer<E extends Event, V> {
      void change(E var1, V var2);
   }

   public static enum Time {
      PAST(-1),
      NOW(0),
      FUTURE(1);

      private final int value;

      private Time(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static EventValue.Time of(int value) {
         return switch (value) {
            case -1 -> PAST;
            case 0 -> NOW;
            case 1 -> FUTURE;
            default -> throw new IllegalArgumentException("Invalid time value: " + value);
         };
      }
   }

   public static enum Validation {
      VALID,
      INVALID,
      ABORT;
   }
}
