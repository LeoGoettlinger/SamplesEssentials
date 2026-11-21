package com.fastasyncworldedit.core.extension.factory.parser.transform;

import com.fastasyncworldedit.core.extension.factory.parser.RichParser;
import com.fastasyncworldedit.core.extent.ResettableExtent;
import com.fastasyncworldedit.core.extent.transform.PatternTransform;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.pattern.Pattern;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

public class PatternTransformParser extends RichParser<ResettableExtent> {
   public PatternTransformParser(WorldEdit worldEdit) {
      super(worldEdit, "#pattern");
   }

   @Override
   protected Stream<String> getSuggestions(String argumentInput, int index, ParserContext context) {
      if (index == 0) {
         return this.worldEdit.getPatternFactory().getSuggestions(argumentInput, context).stream();
      } else {
         return index == 1 ? this.worldEdit.getTransformFactory().getSuggestions(argumentInput, context).stream() : Stream.empty();
      }
   }

   protected ResettableExtent parseFromInput(@Nonnull String[] arguments, ParserContext context) throws InputParseException {
      if (arguments.length > 2) {
         return null;
      } else {
         Pattern pattern = this.worldEdit.getPatternFactory().parseFromInput(arguments[0], context);
         Extent extent = arguments.length == 2 ? this.worldEdit.getTransformFactory().parseFromInput(arguments[1], context) : context.requireExtent();
         return new PatternTransform(extent, pattern);
      }
   }
}
