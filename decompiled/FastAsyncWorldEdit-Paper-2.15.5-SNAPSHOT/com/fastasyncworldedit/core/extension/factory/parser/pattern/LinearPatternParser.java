package com.fastasyncworldedit.core.extension.factory.parser.pattern;

import com.fastasyncworldedit.core.configuration.Caption;
import com.fastasyncworldedit.core.extension.factory.parser.RichParser;
import com.fastasyncworldedit.core.function.pattern.LinearBlockPattern;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.command.util.SuggestionHelper;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.function.pattern.RandomPattern;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

public class LinearPatternParser extends RichParser<Pattern> {
   public LinearPatternParser(WorldEdit worldEdit) {
      super(worldEdit, "#linear", "#l");
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
      if (arguments.length != 1) {
         throw new InputParseException(
            Caption.of("fawe.error.command.syntax", TextComponent.of(this.getPrefix() + "[pattern] (e.g. " + this.getPrefix() + "[stone,dirt])"))
         );
      } else {
         Pattern inner = this.worldEdit.getPatternFactory().parseFromInput(arguments[0], context);
         if (inner instanceof BlockStateHolder) {
            return inner;
         } else if (inner instanceof RandomPattern) {
            Set<Pattern> patterns = ((RandomPattern)inner).getPatterns();
            return new LinearBlockPattern(patterns.toArray(new Pattern[0]));
         } else {
            throw new InputParseException(TextComponent.of("Pattern " + inner.getClass().getSimpleName() + " cannot be used with " + this.getPrefix()));
         }
      }
   }
}
