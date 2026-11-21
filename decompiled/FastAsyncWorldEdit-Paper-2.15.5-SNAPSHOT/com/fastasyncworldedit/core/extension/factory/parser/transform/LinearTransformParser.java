package com.fastasyncworldedit.core.extension.factory.parser.transform;

import com.fastasyncworldedit.core.extension.factory.parser.RichParser;
import com.fastasyncworldedit.core.extent.ResettableExtent;
import com.fastasyncworldedit.core.extent.transform.LinearTransform;
import com.fastasyncworldedit.core.extent.transform.RandomTransform;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

public class LinearTransformParser extends RichParser<ResettableExtent> {
   public LinearTransformParser(WorldEdit worldEdit) {
      super(worldEdit, "#linear", "#l");
   }

   @Override
   protected Stream<String> getSuggestions(String argumentInput, int index, ParserContext context) {
      return index == 0 ? this.worldEdit.getTransformFactory().getSuggestions(argumentInput, context).stream() : Stream.empty();
   }

   protected ResettableExtent parseFromInput(@Nonnull String[] arguments, ParserContext context) throws InputParseException {
      if (arguments.length != 1) {
         return null;
      } else {
         ResettableExtent inner = this.worldEdit.getTransformFactory().parseFromInput(arguments[0], context);
         return (ResettableExtent)(inner instanceof RandomTransform
            ? new LinearTransform(((RandomTransform)inner).getExtents().toArray(new ResettableExtent[0]))
            : inner);
      }
   }
}
