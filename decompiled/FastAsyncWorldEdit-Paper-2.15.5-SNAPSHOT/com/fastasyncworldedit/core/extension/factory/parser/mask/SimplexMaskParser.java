package com.fastasyncworldedit.core.extension.factory.parser.mask;

import com.fastasyncworldedit.core.extension.factory.parser.RichParser;
import com.fastasyncworldedit.core.function.mask.SimplexMask;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.command.util.SuggestionHelper;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.function.mask.Mask;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

public class SimplexMaskParser extends RichParser<Mask> {
   private static final String SIMPLEX_PREFIX = "#simplex";

   public SimplexMaskParser(WorldEdit worldEdit) {
      super(worldEdit, "#simplex");
   }

   @Override
   protected Stream<String> getSuggestions(String argumentInput, int index, ParserContext context) {
      return index < 3 ? SuggestionHelper.suggestPositiveDoubles(argumentInput) : Stream.empty();
   }

   protected Mask parseFromInput(@Nonnull String[] arguments, ParserContext context) throws InputParseException {
      if (arguments.length != 3) {
         return null;
      } else {
         double scale = Double.parseDouble(arguments[0]);
         double min = Double.parseDouble(arguments[1]);
         double max = Double.parseDouble(arguments[2]);
         scale = 1.0 / Math.max(1.0, scale);
         min = (min - 50.0) / 50.0;
         max = (max - 50.0) / 50.0;
         return new SimplexMask(scale, min, max);
      }
   }
}
