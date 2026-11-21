package com.fastasyncworldedit.core.extension.factory.parser.pattern;

import com.fastasyncworldedit.core.configuration.Caption;
import com.fastasyncworldedit.core.extension.factory.parser.RichParser;
import com.fastasyncworldedit.core.function.pattern.BufferedPattern;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

public class BufferedPatternParser extends RichParser<Pattern> {
   public BufferedPatternParser(WorldEdit worldEdit) {
      super(worldEdit, "#buffer");
   }

   @Override
   protected Stream<String> getSuggestions(String argumentInput, int index, ParserContext context) {
      return index == 0 ? this.worldEdit.getPatternFactory().getSuggestions(argumentInput, context).stream() : Stream.empty();
   }

   protected Pattern parseFromInput(@Nonnull String[] arguments, ParserContext context) throws InputParseException {
      if (arguments.length != 1) {
         throw new InputParseException(
            Caption.of("fawe.error.command.syntax", TextComponent.of(this.getPrefix() + "[pattern] (e.g. " + this.getPrefix() + "[stone,dirt])"))
         );
      } else {
         Pattern inner = this.worldEdit.getPatternFactory().parseFromInput(arguments[0], context);
         return new BufferedPattern(context.requireActor(), inner, context.getSelection());
      }
   }
}
