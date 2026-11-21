package com.fastasyncworldedit.core.extension.factory.parser.pattern;

import com.fastasyncworldedit.core.configuration.Caption;
import com.fastasyncworldedit.core.extension.factory.parser.RichParser;
import com.fastasyncworldedit.core.function.pattern.DesaturatePattern;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.command.util.SuggestionHelper;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

public class DesaturatePatternParser extends RichParser<Pattern> {
   public DesaturatePatternParser(WorldEdit worldEdit) {
      super(worldEdit, "#desaturate");
   }

   @Override
   public Stream<String> getSuggestions(String argumentInput, int index, ParserContext context) {
      return index == 0 ? SuggestionHelper.suggestPositiveDoubles(argumentInput) : Stream.empty();
   }

   public Pattern parseFromInput(@Nonnull String[] arguments, ParserContext context) throws InputParseException {
      if (arguments.length != 1) {
         throw new InputParseException(
            Caption.of("fawe.error.command.syntax", TextComponent.of(this.getPrefix() + "[percent] (e.g. " + this.getPrefix() + "[90])"))
         );
      } else {
         return new DesaturatePattern(context.requireExtent(), context.requireSession(), Double.parseDouble(arguments[0]) / 100.0);
      }
   }
}
