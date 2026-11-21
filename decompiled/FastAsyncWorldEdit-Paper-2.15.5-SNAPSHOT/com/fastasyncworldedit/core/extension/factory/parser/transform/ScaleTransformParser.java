package com.fastasyncworldedit.core.extension.factory.parser.transform;

import com.fastasyncworldedit.core.extension.factory.parser.RichParser;
import com.fastasyncworldedit.core.extent.ResettableExtent;
import com.fastasyncworldedit.core.extent.transform.ScaleTransform;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.command.util.SuggestionHelper;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.extent.Extent;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

public class ScaleTransformParser extends RichParser<ResettableExtent> {
   public ScaleTransformParser(WorldEdit worldEdit) {
      super(worldEdit, "#scale");
   }

   @Override
   protected Stream<String> getSuggestions(String argumentInput, int index, ParserContext context) {
      if (index < 3) {
         return SuggestionHelper.suggestPositiveDoubles(argumentInput);
      } else {
         return index == 3 ? this.worldEdit.getTransformFactory().getSuggestions(argumentInput, context).stream() : Stream.empty();
      }
   }

   protected ResettableExtent parseFromInput(@Nonnull String[] arguments, ParserContext context) throws InputParseException {
      double xScale;
      double yScale;
      double zScale;
      Extent extent;
      if (arguments.length == 1) {
         xScale = yScale = zScale = Double.parseDouble(arguments[0]);
         extent = context.requireExtent();
      } else {
         if (arguments.length != 3 && arguments.length != 4) {
            return null;
         }

         xScale = Double.parseDouble(arguments[0]);
         yScale = Double.parseDouble(arguments[1]);
         zScale = Double.parseDouble(arguments[2]);
         extent = arguments.length == 4 ? this.worldEdit.getTransformFactory().parseFromInput(arguments[3], context) : context.requireExtent();
      }

      return new ScaleTransform(extent, xScale, yScale, zScale);
   }
}
