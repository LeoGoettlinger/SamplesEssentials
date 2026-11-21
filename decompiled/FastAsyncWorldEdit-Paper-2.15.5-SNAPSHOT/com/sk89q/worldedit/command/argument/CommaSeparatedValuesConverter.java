package com.sk89q.worldedit.command.argument;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.ImmutableList.Builder;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import java.util.List;
import org.enginehub.piston.converter.ArgumentConverter;
import org.enginehub.piston.converter.ConversionResult;
import org.enginehub.piston.converter.SuccessfulConversion;
import org.enginehub.piston.inject.InjectedValueAccess;

public class CommaSeparatedValuesConverter<T> implements ArgumentConverter<T> {
   private static final Splitter COMMA = Splitter.on(',');
   private final ArgumentConverter<T> delegate;
   private final int maximum;

   public static <T> CommaSeparatedValuesConverter<T> wrap(ArgumentConverter<T> delegate) {
      return wrapAndLimit(delegate, -1);
   }

   public static <T> CommaSeparatedValuesConverter<T> wrapAndLimit(ArgumentConverter<T> delegate, int maximum) {
      return new CommaSeparatedValuesConverter<>(delegate, maximum);
   }

   private CommaSeparatedValuesConverter(ArgumentConverter<T> delegate, int maximum) {
      Preconditions.checkArgument(maximum == -1 || maximum > 1, "Maximum must be bigger than 1, or exactly -1");
      this.delegate = delegate;
      this.maximum = maximum;
   }

   @Override
   public Component describeAcceptableArguments() {
      TextComponent.Builder result = TextComponent.builder("");
      if (this.maximum > -1) {
         result.append(TextComponent.of("up to ")).append(TextComponent.of(this.maximum)).append(TextComponent.space());
      }

      result.append(TextComponent.of("comma separated values of: ")).append(this.delegate.describeAcceptableArguments());
      return result.build();
   }

   @Override
   public List<String> getSuggestions(String input, InjectedValueAccess context) {
      String lastInput = (String)Iterables.getLast(COMMA.split(input), "");

      assert lastInput != null;

      return this.delegate.getSuggestions(lastInput, context);
   }

   @Override
   public ConversionResult<T> convert(String argument, InjectedValueAccess context) {
      Builder<T> result = ImmutableList.builder();

      for (String input : COMMA.split(argument)) {
         ConversionResult<T> temp = this.delegate.convert(input, context);
         if (!temp.isSuccessful()) {
            return temp;
         }

         result.addAll(temp.get());
      }

      return SuccessfulConversion.from(result.build());
   }
}
