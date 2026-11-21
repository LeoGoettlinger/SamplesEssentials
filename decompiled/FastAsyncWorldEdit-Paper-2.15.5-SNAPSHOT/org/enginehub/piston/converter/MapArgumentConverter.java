package org.enginehub.piston.converter;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.sk89q.worldedit.util.formatting.text.Component;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.enginehub.piston.config.ColorConfig;
import org.enginehub.piston.inject.InjectedValueAccess;
import org.enginehub.piston.util.ComponentHelper;

public final class MapArgumentConverter<T> implements ArgumentConverter<T> {
   private final ImmutableMap<String, T> map;

   public static MapArgumentConverter<String> forChoices(Set<String> choices) {
      Builder<String, String> map = ImmutableMap.builder();
      choices.forEach(c -> map.put(c, c));
      return from(map.build());
   }

   public static <T> MapArgumentConverter<T> from(Map<String, T> map) {
      return new MapArgumentConverter<>(map);
   }

   private MapArgumentConverter(Map<String, T> map) {
      this.map = ImmutableMap.copyOf(map);
   }

   @Override
   public ConversionResult<T> convert(String argument, InjectedValueAccess context) {
      T result = (T)this.map.get(argument);
      return (ConversionResult<T>)(result == null
         ? FailedConversion.from(new IllegalArgumentException("Invalid value: " + argument))
         : SuccessfulConversion.fromSingle(result));
   }

   @Override
   public Component describeAcceptableArguments() {
      return this.map.keySet().stream().map(ColorConfig.mainText()::wrap).collect(ComponentHelper.joiningWithBar());
   }

   @Override
   public List<String> getSuggestions(String input, InjectedValueAccess context) {
      return SuggestionHelper.limitByPrefix(this.map.keySet().stream(), input);
   }
}
