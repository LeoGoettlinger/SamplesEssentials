package com.fastasyncworldedit.core.extension.factory.parser.pattern;

import com.fastasyncworldedit.core.configuration.Caption;
import com.fastasyncworldedit.core.extension.factory.parser.RichParser;
import com.fastasyncworldedit.core.function.pattern.MaskedPattern;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

public class MaskedPatternParser extends RichParser<Pattern> {
   public MaskedPatternParser(WorldEdit worldEdit) {
      super(worldEdit, "#mask");
   }

   @Override
   protected Stream<String> getSuggestions(String argumentInput, int index, ParserContext context) {
      return switch (index) {
         case 0 -> this.worldEdit.getMaskFactory().getSuggestions(argumentInput, context).stream();
         case 1, 2 -> this.worldEdit.getPatternFactory().getSuggestions(argumentInput, context).stream();
         default -> Stream.empty();
      };
   }

   protected Pattern parseFromInput(@Nonnull String[] arguments, ParserContext context) throws InputParseException {
      if (arguments.length != 3) {
         throw new InputParseException(
            Caption.of(
               "fawe.error.command.syntax",
               TextComponent.of(this.getPrefix() + "[mask][pattern][pattern] (e.g. " + this.getPrefix() + "[oak_planks][dirt][stone])")
            )
         );
      } else {
         Mask mask = this.worldEdit.getMaskFactory().parseFromInput(arguments[0], context);
         Pattern inner1 = this.worldEdit.getPatternFactory().parseFromInput(arguments[1], context);
         Pattern inner2 = this.worldEdit.getPatternFactory().parseFromInput(arguments[2], context);
         return new MaskedPattern(mask, inner1, inner2);
      }
   }
}
