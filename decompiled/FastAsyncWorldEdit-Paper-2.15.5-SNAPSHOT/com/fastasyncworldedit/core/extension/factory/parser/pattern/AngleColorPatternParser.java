package com.fastasyncworldedit.core.extension.factory.parser.pattern;

import com.fastasyncworldedit.core.configuration.Caption;
import com.fastasyncworldedit.core.extension.factory.parser.RichParser;
import com.fastasyncworldedit.core.function.pattern.AngleColorPattern;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.command.util.SuggestionHelper;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

public class AngleColorPatternParser extends RichParser<Pattern> {
   public AngleColorPatternParser(WorldEdit worldEdit) {
      super(worldEdit, "#anglecolor", "#anglecolour");
   }

   @Override
   public Stream<String> getSuggestions(String argumentInput, int index, ParserContext context) {
      return index != 0 ? Stream.empty() : SuggestionHelper.suggestPositiveIntegers(argumentInput);
   }

   public Pattern parseFromInput(@Nonnull String[] input, ParserContext context) throws InputParseException {
      if (input.length != 1) {
         throw new InputParseException(
            Caption.of("fawe.error.command.syntax", TextComponent.of(this.getPrefix() + "[distance] (e.g. " + this.getPrefix() + "[10])"))
         );
      } else {
         int distance = Integer.parseInt(input[0]);
         return new AngleColorPattern(context.requireExtent(), context.requireSession(), distance);
      }
   }
}
