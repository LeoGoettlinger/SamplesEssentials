package com.fastasyncworldedit.core.extension.factory.parser.mask;

import com.fastasyncworldedit.core.extension.factory.parser.RichParser;
import com.fastasyncworldedit.core.function.mask.SurfaceAngleMask;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.command.util.SuggestionHelper;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.function.mask.Mask;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

public class SurfaceAngleMaskParser extends RichParser<Mask> {
   public SurfaceAngleMaskParser(WorldEdit worldEdit) {
      super(worldEdit, "#surfaceangle");
   }

   @Override
   protected Stream<String> getSuggestions(String argumentInput, int index, ParserContext context) {
      return index <= 2 ? SuggestionHelper.suggestPositiveDoubles(argumentInput) : Stream.empty();
   }

   public Mask parseFromInput(@Nonnull String[] arguments, ParserContext context) throws InputParseException {
      if (arguments.length > 3) {
         return null;
      } else {
         double min = Double.parseDouble(arguments[0]);
         double max = Double.parseDouble(arguments[1]);
         int size = arguments.length > 2 ? Integer.parseInt(arguments[2]) : 1;
         return new SurfaceAngleMask(context.getExtent(), min, max, size);
      }
   }
}
