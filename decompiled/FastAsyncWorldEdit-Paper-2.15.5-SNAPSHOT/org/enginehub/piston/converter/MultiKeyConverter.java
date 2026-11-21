package org.enginehub.piston.converter;

import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.sk89q.worldedit.util.formatting.text.Component;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import javax.annotation.Nullable;
import org.enginehub.piston.config.ColorConfig;
import org.enginehub.piston.inject.InjectedValueAccess;
import org.enginehub.piston.util.ComponentHelper;

public class MultiKeyConverter<E> implements ArgumentConverter<E> {
   private final Component choices;
   private final ImmutableSet<String> primaryKeys;
   private final ImmutableMap<String, E> map;
   @Nullable
   private final E unknownValue;
   private final UnaryOperator<String> errorMessage;

   public static <E> MultiKeyConverter.Builder<E> builder(SetMultimap<E, String> items) {
      return new AutoValue_MultiKeyConverter_Arguments.Builder<E>().errorMessage(arg -> "Not a valid argument: " + arg).items(items);
   }

   public static <E> MultiKeyConverter.Builder<E> builder(Collection<E> items, Function<E, Set<String>> lookupKeys) {
      com.google.common.collect.ImmutableSetMultimap.Builder<E, String> map = ImmutableSetMultimap.builder();

      for (E item : items) {
         map.putAll(item, lookupKeys.apply(item));
      }

      return builder(map.build());
   }

   public static <E> MultiKeyConverter<E> from(SetMultimap<E, String> items) {
      return from(items, null);
   }

   public static <E> MultiKeyConverter<E> from(Collection<E> items, Function<E, Set<String>> lookupKeys) {
      return from(items, lookupKeys, null);
   }

   public static <E> MultiKeyConverter<E> from(SetMultimap<E, String> items, @Nullable E unknownValue) {
      return builder(items).unknownValue(unknownValue).build();
   }

   public static <E> MultiKeyConverter<E> from(Collection<E> items, Function<E, Set<String>> lookupKeys, @Nullable E unknownValue) {
      return builder(items, lookupKeys).unknownValue(unknownValue).build();
   }

   private MultiKeyConverter(MultiKeyConverter.Arguments<E> arguments) {
      com.google.common.collect.ImmutableSortedMap.Builder<String, E> map = ImmutableSortedMap.orderedBy(String.CASE_INSENSITIVE_ORDER);
      com.google.common.collect.ImmutableSet.Builder<String> primaryKeysBuilder = ImmutableSet.builder();
      Multimaps.asMap(arguments.items()).forEach((item, keys) -> {
         Preconditions.checkState(keys.size() > 0, "No lookup keys for value %s", item);
         primaryKeysBuilder.add((String)keys.iterator().next());

         for (String key : keys) {
            map.put(key, item);
         }
      });
      this.primaryKeys = primaryKeysBuilder.build();
      this.choices = this.primaryKeys.stream().map(ColorConfig.mainText()::wrap).collect(ComponentHelper.joiningWithBar());
      this.map = map.build();
      this.unknownValue = arguments.unknownValue();
      this.errorMessage = arguments.errorMessage();
   }

   @Override
   public Component describeAcceptableArguments() {
      return this.choices;
   }

   @Override
   public List<String> getSuggestions(String input, InjectedValueAccess context) {
      return SuggestionHelper.limitByPrefix(this.primaryKeys.stream(), input);
   }

   @Override
   public ConversionResult<E> convert(String argument, InjectedValueAccess context) {
      E result = (E)this.map.get(argument);
      if (result == null) {
         return (ConversionResult<E>)(this.unknownValue != null
            ? SuccessfulConversion.fromSingle(this.unknownValue, false)
            : FailedConversion.from(new IllegalArgumentException(this.errorMessage.apply(argument))));
      } else {
         return SuccessfulConversion.fromSingle(result);
      }
   }

   @AutoValue
   abstract static class Arguments<E> {
      abstract ImmutableSetMultimap<E, String> items();

      @Nullable
      abstract E unknownValue();

      abstract UnaryOperator<String> errorMessage();

      @com.google.auto.value.AutoValue.Builder
      interface Builder<E> extends MultiKeyConverter.Builder<E> {
         MultiKeyConverter.Arguments.Builder<E> items(SetMultimap<E, String> var1);

         MultiKeyConverter.Arguments.Builder<E> unknownValue(@Nullable E var1);

         MultiKeyConverter.Arguments.Builder<E> errorMessage(UnaryOperator<String> var1);

         MultiKeyConverter.Arguments<E> autoBuild();

         @Override
         default MultiKeyConverter<E> build() {
            return new MultiKeyConverter<>(this.autoBuild());
         }
      }
   }

   public interface Builder<E> {
      MultiKeyConverter.Builder<E> items(SetMultimap<E, String> var1);

      MultiKeyConverter.Builder<E> unknownValue(@Nullable E var1);

      MultiKeyConverter.Builder<E> errorMessage(UnaryOperator<String> var1);

      MultiKeyConverter<E> build();
   }
}
