package org.skriptlang.skript.bukkit.lang.eventvalue;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Optional;
import java.util.SequencedCollection;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.lang.converter.Converter;
import org.skriptlang.skript.lang.converter.Converters;

record ConvertedEventValue<SourceEvent extends Event, ConvertedEvent extends Event, SourceValue, ConvertedValue>(
   Class<ConvertedEvent> eventClass,
   Class<ConvertedValue> valueClass,
   EventValue<SourceEvent, SourceValue> source,
   Converter<SourceValue, ConvertedValue> valueConverter,
   @Nullable Converter<ConvertedValue, SourceValue> reverseConverter
) implements EventValue<ConvertedEvent, ConvertedValue> {
   public ConvertedEventValue(
      Class<ConvertedEvent> eventClass,
      Class<ConvertedValue> valueClass,
      EventValue<SourceEvent, SourceValue> source,
      Converter<SourceValue, ConvertedValue> valueConverter,
      @Nullable Converter<ConvertedValue, SourceValue> reverseConverter
   ) {
      this.eventClass = eventClass;
      this.valueClass = valueClass;
      this.source = source;
      this.valueConverter = valueConverter;
      this.reverseConverter = reverseConverter == null ? getConverter(valueClass, source.valueClass()) : reverseConverter;
   }

   public static <SourceEvent extends Event, ConvertedEvent extends Event, SourceValue, ConvertedValue> EventValue<ConvertedEvent, ConvertedValue> newInstance(
      Class<ConvertedEvent> eventClass, Class<ConvertedValue> valueClass, EventValue<SourceEvent, SourceValue> source
   ) {
      if (source.eventClass().isAssignableFrom(eventClass) && valueClass.isAssignableFrom(source.valueClass())) {
         return (EventValue<ConvertedEvent, ConvertedValue>)source;
      } else {
         Converter<SourceValue, ConvertedValue> converter = getConverter(source.valueClass(), valueClass);
         return converter == null ? null : new ConvertedEventValue<>(eventClass, valueClass, source, converter, getConverter(valueClass, source.valueClass()));
      }
   }

   @Nullable
   private static <F, T> Converter<F, T> getConverter(Class<F> from, Class<T> to) {
      if (from.isArray() && to.isArray()) {
         Converter componentConverter = getConverter(from.componentType(), to.componentType());
         return componentConverter == null ? null : obj -> {
            T converted = to.cast(Array.newInstance(to.componentType(), Array.getLength(obj)));
            int i = 0;

            for (int length = Array.getLength(converted); i < length; i++) {
               Object convertedObj = componentConverter.convert(Array.get(obj, i));
               if (convertedObj == null) {
                  return null;
               }

               Array.set(converted, i, convertedObj);
            }

            return converted;
         };
      } else {
         return to.isAssignableFrom(from) ? value -> (T)value : Converters.getConverter(from, to);
      }
   }

   @Unmodifiable
   @Override
   public SequencedCollection<String> patterns() {
      return this.source.patterns();
   }

   @Override
   public EventValue.Validation validate(Class<?> event) {
      return this.source.validate(event);
   }

   @Override
   public boolean matchesInput(String input) {
      return this.source.matchesInput(input);
   }

   @Override
   public ConvertedValue get(ConvertedEvent event) {
      return this.converter().convert(event);
   }

   @Override
   public Converter<ConvertedEvent, ConvertedValue> converter() {
      return event -> {
         if (!this.source.eventClass().isAssignableFrom(event.getClass())) {
            return null;
         } else {
            SourceValue sourceValue = this.source.get(this.source.eventClass().cast(event));
            return this.valueConverter.convert(sourceValue);
         }
      };
   }

   @Override
   public boolean hasChanger(ch.njol.skript.classes.Changer.ChangeMode mode) {
      return this.source.hasChanger(mode);
   }

   @Override
   public Optional<EventValue.Changer<ConvertedEvent, ConvertedValue>> changer(ch.njol.skript.classes.Changer.ChangeMode mode) {
      return this.source.changer(mode).map(changer -> (event, value) -> {
         if (this.source.eventClass().isAssignableFrom(event.getClass())) {
            if (this.reverseConverter != null) {
               SourceValue sourceValue = this.reverseConverter.convert(value);
               if (sourceValue != null) {
                  changer.change(this.source.eventClass().cast(event), sourceValue);
               }
            }
         }
      });
   }

   @Override
   public EventValue.Time time() {
      return this.source.time();
   }

   @Unmodifiable
   @Override
   public Collection<Class<? extends ConvertedEvent>> excludedEvents() {
      return this.source.excludedEvents().stream().filter(this.eventClass::isAssignableFrom).toList();
   }

   @Nullable
   @Override
   public String excludedErrorMessage() {
      return this.source.excludedErrorMessage();
   }

   @Override
   public boolean contextDependent() {
      return this.source.contextDependent();
   }

   @Override
   public boolean matches(EventValue<?, ?> eventValue) {
      return this.matches((Class<? extends Event>)eventValue.eventClass(), eventValue.valueClass(), eventValue.patterns());
   }

   @Nullable
   @Override
   public <NewEvent extends Event, NewValue> EventValue<NewEvent, NewValue> getConverted(Class<NewEvent> newEventClass, Class<NewValue> newValueClass) {
      return newInstance(newEventClass, newValueClass, this.source);
   }

   @Override
   public <NewEvent extends Event, NewValue> EventValue<NewEvent, NewValue> getConverted(
      Class<NewEvent> newEventClass,
      Class<NewValue> newValueClass,
      Converter<ConvertedValue, NewValue> converter,
      @Nullable Converter<NewValue, ConvertedValue> reverseConverter
   ) {
      return new ConvertedEventValue<>(newEventClass, newValueClass, this, converter, reverseConverter);
   }
}
