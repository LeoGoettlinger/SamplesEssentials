package com.fastasyncworldedit.core.extension.factory.parser.pattern;

import com.fastasyncworldedit.core.configuration.Caption;
import com.fastasyncworldedit.core.extension.factory.parser.RichParser;
import com.fastasyncworldedit.core.function.pattern.SolidRandomOffsetPattern;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.command.util.SuggestionHelper;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

public class SolidRandomOffsetPatternParser extends RichParser<Pattern> {
   public SolidRandomOffsetPatternParser(WorldEdit worldEdit) {
      super(worldEdit, "#solidspread");
   }

   @Override
   protected Stream<String> getSuggestions(String argumentInput, int index, ParserContext context) {
      return switch (index) {
         case 0 -> this.worldEdit.getPatternFactory().getSuggestions(argumentInput, context).stream();
         case 1, 2, 3 -> SuggestionHelper.suggestPositiveIntegers(argumentInput);
         default -> Stream.empty();
      };
   }

   protected Pattern parseFromInput(@Nonnull String[] arguments, ParserContext context) throws InputParseException {
      if (arguments.length != 2 && arguments.length != 4) {
         throw new InputParseException(
            Caption.of("fawe.error.command.syntax", TextComponent.of(this.getPrefix() + "[pattern] (e.g. " + this.getPrefix() + "[stone][2][0][4])"))
         );
      } else {
         Pattern inner = this.worldEdit.getPatternFactory().parseFromInput(arguments[0], context);
         int x;
         int y;
         int z;
         if (arguments.length == 4) {
            x = Integer.parseInt(arguments[1]);
            y = Integer.parseInt(arguments[2]);
            z = Integer.parseInt(arguments[3]);
         } else {
            x = y = z = Integer.parseInt(arguments[1]);
         }

         return new SolidRandomOffsetPattern(inner, x, y, z, context.getMinY(), context.getMaxY());
      }
   }
}
