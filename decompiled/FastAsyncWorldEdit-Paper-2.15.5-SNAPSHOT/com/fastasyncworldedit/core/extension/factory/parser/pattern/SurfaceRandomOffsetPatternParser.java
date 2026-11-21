package com.fastasyncworldedit.core.extension.factory.parser.pattern;

import com.fastasyncworldedit.core.configuration.Caption;
import com.fastasyncworldedit.core.extension.factory.parser.RichParser;
import com.fastasyncworldedit.core.function.pattern.SurfaceRandomOffsetPattern;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.command.util.SuggestionHelper;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

public class SurfaceRandomOffsetPatternParser extends RichParser<Pattern> {
   public SurfaceRandomOffsetPatternParser(WorldEdit worldEdit) {
      super(worldEdit, "#surfacespread");
   }

   @Override
   protected Stream<String> getSuggestions(String argumentInput, int index, ParserContext context) {
      return switch (index) {
         case 0 -> this.worldEdit.getPatternFactory().getSuggestions(argumentInput, context).stream();
         case 1 -> SuggestionHelper.suggestPositiveIntegers(argumentInput);
         default -> Stream.empty();
      };
   }

   protected Pattern parseFromInput(@Nonnull String[] arguments, ParserContext context) throws InputParseException {
      if (arguments.length != 2) {
         throw new InputParseException(
            Caption.of("fawe.error.command.syntax", TextComponent.of(this.getPrefix() + "[pattern] (e.g. " + this.getPrefix() + "[stone][2])"))
         );
      } else {
         Pattern inner = this.worldEdit.getPatternFactory().parseFromInput(arguments[0], context);
         int distance = Integer.parseInt(arguments[1]);
         return new SurfaceRandomOffsetPattern(inner, distance, context.getMinY(), context.getMaxY());
      }
   }
}
