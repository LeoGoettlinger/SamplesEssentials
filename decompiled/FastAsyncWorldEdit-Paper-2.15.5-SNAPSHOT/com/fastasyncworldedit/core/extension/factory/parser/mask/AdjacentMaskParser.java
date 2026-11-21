package com.fastasyncworldedit.core.extension.factory.parser.mask;

import com.fastasyncworldedit.core.extension.factory.parser.RichParser;
import com.fastasyncworldedit.core.function.mask.AdjacentAnyMask;
import com.fastasyncworldedit.core.function.mask.AdjacentMask;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.command.util.SuggestionHelper;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.function.mask.Mask;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

public class AdjacentMaskParser extends RichParser<Mask> {
   public AdjacentMaskParser(WorldEdit worldEdit) {
      super(worldEdit, "~");
   }

   @Override
   protected Stream<String> getSuggestions(String argumentInput, int index, ParserContext context) {
      if (index == 0) {
         return this.worldEdit.getMaskFactory().getSuggestions(argumentInput, context).stream();
      } else {
         return index != 1 && index != 2 ? Stream.empty() : SuggestionHelper.suggestPositiveDoubles(argumentInput);
      }
   }

   protected Mask parseFromInput(@Nonnull String[] arguments, ParserContext context) throws InputParseException {
      if (arguments.length == 0) {
         return null;
      } else {
         Mask subMask = this.worldEdit.getMaskFactory().parseFromInput(arguments[0], context);
         int min = arguments.length > 1 ? Integer.parseInt(arguments[1]) : -1;
         int max = arguments.length > 2 ? Integer.parseInt(arguments[2]) : -1;
         if (min == -1 && max == -1) {
            min = 1;
            max = 8;
         } else if (max == -1) {
            max = min;
         }

         return (Mask)(max >= 8 && min == 1 ? new AdjacentAnyMask(subMask, context.getMinY(), context.getMaxY()) : new AdjacentMask(subMask, min, max));
      }
   }
}
