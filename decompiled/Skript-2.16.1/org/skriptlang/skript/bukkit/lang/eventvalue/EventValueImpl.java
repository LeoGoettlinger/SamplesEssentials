package org.skriptlang.skript.bukkit.lang.eventvalue;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.patterns.MatchResult;
import ch.njol.skript.patterns.PatternCompiler;
import ch.njol.skript.patterns.RegexPatternElement;
import ch.njol.skript.patterns.SkriptPattern;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Utils;
import com.google.common.base.MoreObjects;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.SequencedCollection;
import java.util.function.BiPredicate;
import java.util.function.Function;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.lang.converter.Converter;

final class EventValueImpl<E extends Event, V> implements EventValue<E, V> {
   private final Class<E> eventClass;
   private final Class<V> valueClass;
   private final SequencedCollection<String> patterns;
   private final boolean hasCustomInputValidator;
   @Nullable
   private final BiPredicate<String, SkriptParser.ParseResult> inputValidator;
   @Nullable
   private final Function<Class<?>, EventValue.Validation> eventValidator;
   private final Converter<E, V> converter;
   private final Map<ch.njol.skript.classes.Changer.ChangeMode, EventValue.Changer<E, V>> changers;
   private final EventValue.Time time;
   private final Collection<Class<? extends E>> excludedEvents;
   @Nullable
   private final String excludedErrorMessage;
   private final boolean contextDepenent;
   private SkriptPattern[] compiledPatterns;

   private EventValueImpl(EventValueImpl.BuilderImpl<E, V> builder) {
      this.eventClass = builder.eventClass;
      this.valueClass = builder.valueClass;
      this.patterns = builder.patterns;
      this.hasCustomInputValidator = builder.hasCustomInputValidator;
      this.inputValidator = builder.inputValidator;
      this.eventValidator = builder.eventValidator;
      this.converter = builder.converter;
      this.changers = builder.changers;
      this.time = builder.time;
      this.excludedEvents = builder.excludedEvents;
      this.excludedErrorMessage = builder.excludedErrorMessage;
      this.contextDepenent = builder.contextDependent;
   }

   @Override
   public Class<E> eventClass() {
      return this.eventClass;
   }

   @Override
   public Class<V> valueClass() {
      return this.valueClass;
   }

   @Unmodifiable
   @Override
   public SequencedCollection<String> patterns() {
      return this.patterns;
   }

   @Override
   public EventValue.Validation validate(Class<?> event) {
      for (Class<? extends E> excludedEvent : this.excludedEvents) {
         if (excludedEvent.isAssignableFrom(event)) {
            if (this.excludedErrorMessage != null) {
               Skript.error(this.excludedErrorMessage);
            }

            return EventValue.Validation.ABORT;
         }
      }

      return this.eventValidator == null ? EventValue.Validation.VALID : this.eventValidator.apply(event);
   }

   @Override
   public boolean matchesInput(String input) {
      for (SkriptPattern pattern : this.compilePatterns()) {
         MatchResult match = pattern.match(input);
         if (match != null && (this.inputValidator == null || this.inputValidator.test(input, match.toParseResult()))) {
            return true;
         }
      }

      return false;
   }

   private SkriptPattern[] compilePatterns() {
      if (this.compiledPatterns != null) {
         return this.compiledPatterns;
      } else {
         this.compiledPatterns = this.patterns.isEmpty()
            ? this.patternsFromType(this.valueClass)
            : this.patterns.stream().map(PatternCompiler::compile).toArray(SkriptPattern[]::new);
         return this.compiledPatterns;
      }
   }

   private SkriptPattern[] patternsFromType(Class<?> type) {
      boolean plural = type.isArray();
      if (plural) {
         type = type.componentType();
      }

      ClassInfo<?> info = Classes.getExactClassInfo(type);
      if (info != null && info.getUserInputPatterns() != null) {
         return Arrays.stream(info.getUserInputPatterns())
            .map(RegexPatternElement::new)
            .map(pattern -> new SkriptPattern(pattern, 0))
            .toArray(SkriptPattern[]::new);
      } else {
         String name = type.getSimpleName().toLowerCase(Locale.ENGLISH);
         return new SkriptPattern[]{PatternCompiler.compile(Utils.toEnglishPlural(name, plural))};
      }
   }

   @Override
   public V get(E event) {
      return this.converter.convert(event);
   }

   @Override
   public Converter<E, V> converter() {
      return this.converter;
   }

   @Override
   public boolean hasChanger(ch.njol.skript.classes.Changer.ChangeMode mode) {
      return this.changers.containsKey(mode);
   }

   @Override
   public Optional<EventValue.Changer<E, V>> changer(ch.njol.skript.classes.Changer.ChangeMode mode) {
      return Optional.ofNullable(this.changers.get(mode));
   }

   @Override
   public EventValue.Time time() {
      return this.time;
   }

   @Unmodifiable
   @Override
   public Collection<Class<? extends E>> excludedEvents() {
      return this.excludedEvents;
   }

   @Nullable
   @Override
   public String excludedErrorMessage() {
      return this.excludedErrorMessage;
   }

   @Override
   public boolean contextDependent() {
      return this.contextDepenent;
   }

   @Override
   public boolean matches(EventValue<?, ?> eventValue) {
      return this.matches((Class<? extends Event>)eventValue.eventClass(), eventValue.valueClass(), eventValue.patterns())
         && eventValue instanceof EventValueImpl<?, ?> other
         && this.hasCustomInputValidator == other.hasCustomInputValidator
         && (!this.hasCustomInputValidator || this.inputValidator == other.inputValidator)
         && this.eventValidator == other.eventValidator
         && this.excludedEvents.equals(other.excludedEvents);
   }

   @Nullable
   @Override
   public <ConvertedEvent extends Event, ConvertedValue> EventValue<ConvertedEvent, ConvertedValue> getConverted(
      Class<ConvertedEvent> newEventClass, Class<ConvertedValue> newValueClass
   ) {
      return ConvertedEventValue.newInstance(newEventClass, newValueClass, this);
   }

   @Override
   public <ConvertedEvent extends Event, ConvertedValue> EventValue<ConvertedEvent, ConvertedValue> getConverted(
      Class<ConvertedEvent> newEventClass,
      Class<ConvertedValue> newValueClass,
      Converter<V, ConvertedValue> converter,
      @Nullable Converter<ConvertedValue, V> reverseConverter
   ) {
      return new ConvertedEventValue<>(newEventClass, newValueClass, this, converter, reverseConverter);
   }

   @Override
   public String toString() {
      return MoreObjects.toStringHelper(this)
         .add("eventClass", this.eventClass)
         .add("valueClass", this.valueClass)
         .add("patterns", this.patterns)
         .add("time", this.time)
         .toString();
   }

   static class BuilderImpl<E extends Event, V> implements EventValue.Builder<E, V> {
      private final Class<E> eventClass;
      private final Class<V> valueClass;
      private final Map<ch.njol.skript.classes.Changer.ChangeMode, EventValue.Changer<E, V>> changers = new EnumMap<>(
         ch.njol.skript.classes.Changer.ChangeMode.class
      );
      private SequencedCollection<String> patterns = Collections.emptyList();
      private boolean hasCustomInputValidator;
      @Nullable
      private BiPredicate<String, SkriptParser.ParseResult> inputValidator;
      @Nullable
      private Function<Class<?>, EventValue.Validation> eventValidator;
      private Converter<E, V> converter;
      private EventValue.Time time = EventValue.Time.NOW;
      private Collection<Class<? extends E>> excludedEvents = Collections.emptyList();
      @Nullable
      private String excludedErrorMessage;
      private boolean contextDependent = false;

      BuilderImpl(Class<E> eventClass, Class<V> valueClass) {
         this.eventClass = eventClass;
         this.valueClass = valueClass;
      }

      @Override
      public EventValue.Builder<E, V> patterns(String... patterns) {
         this.patterns = patterns != null ? List.of(patterns) : Collections.emptyList();
         return this;
      }

      @Override
      public EventValue.Builder<E, V> inputValidator(BiPredicate<String, SkriptParser.ParseResult> inputValidator) {
         this.inputValidator = inputValidator;
         this.hasCustomInputValidator = inputValidator != null;
         return this;
      }

      @Override
      public EventValue.Builder<E, V> eventValidator(Function<Class<?>, EventValue.Validation> eventValidator) {
         this.eventValidator = eventValidator;
         return this;
      }

      @Override
      public EventValue.Builder<E, V> getter(Converter<E, V> converter) {
         this.converter = converter;
         return this;
      }

      @Override
      public EventValue.Builder<E, V> registerChanger(ch.njol.skript.classes.Changer.ChangeMode mode, EventValue.Changer<E, V> changer) {
         this.changers.put(mode, changer);
         return this;
      }

      @Override
      public EventValue.Builder<E, V> time(EventValue.Time time) {
         this.time = time;
         return this;
      }

      @SafeVarargs
      @Override
      public final EventValue.Builder<E, V> excludes(Class<? extends E>... events) {
         this.excludedEvents = events != null ? List.of(events) : Collections.emptyList();
         return this;
      }

      @Override
      public EventValue.Builder<E, V> excludedErrorMessage(String excludedErrorMessage) {
         this.excludedErrorMessage = excludedErrorMessage;
         return this;
      }

      @Override
      public EventValue.Builder<E, V> contextDependent() {
         this.contextDependent = true;
         return this;
      }

      @Override
      public EventValue<E, V> build() {
         if (this.patterns == null) {
            boolean plural = this.valueClass.isArray();
            ClassInfo<?> type = Classes.getExactClassInfo(plural ? this.valueClass.getComponentType() : this.valueClass);
            if (type != null && type.getUserInputPatterns() != null) {
               this.inputValidator = combinePredicates((input, parseResult) -> plural == Utils.getEnglishPlural(input).getSecond(), this.inputValidator);
            }
         }

         return new EventValueImpl<>(this);
      }

      private static <T, U> BiPredicate<T, U> combinePredicates(@Nullable BiPredicate<T, U> first, @Nullable BiPredicate<T, U> second) {
         if (first == null) {
            return second;
         } else {
            return second == null ? first : first.and(second);
         }
      }
   }
}
