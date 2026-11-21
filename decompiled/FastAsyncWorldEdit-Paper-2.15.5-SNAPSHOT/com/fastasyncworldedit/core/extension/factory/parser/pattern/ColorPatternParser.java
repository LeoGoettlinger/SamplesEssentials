package com.fastasyncworldedit.core.extension.factory.parser.pattern;

import com.fastasyncworldedit.core.configuration.Caption;
import com.fastasyncworldedit.core.extension.factory.parser.RichParser;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.command.util.SuggestionHelper;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import java.awt.Color;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

public class ColorPatternParser extends RichParser<Pattern> {
   public ColorPatternParser(WorldEdit worldEdit) {
      super(worldEdit, "#color", "#colour");
   }

   @Override
   public Stream<String> getSuggestions(String argumentInput, int index, ParserContext context) {
      return index > 4 ? Stream.empty() : SuggestionHelper.suggestPositiveIntegers(argumentInput);
   }

   public Pattern parseFromInput(@Nonnull String[] input, ParserContext context) throws InputParseException {
      if (input.length != 4) {
         throw new InputParseException(
            Caption.of("fawe.error.command.syntax", TextComponent.of(this.getPrefix() + "[r][g][b][a] (e.g. " + this.getPrefix() + "[156][100][0][120])"))
         );
      } else {
         Color color = new Color(
            Math.clamp((long)Integer.parseInt(input[0]), 0, 255),
            Math.clamp((long)Integer.parseInt(input[1]), 0, 255),
            Math.clamp((long)Integer.parseInt(input[2]), 0, 255),
            Math.clamp((long)Integer.parseInt(input[3]), 0, 255)
         );
         return context.requireSession().getTextureUtil().getNearestBlock(color.getRGB());
      }
   }
}
