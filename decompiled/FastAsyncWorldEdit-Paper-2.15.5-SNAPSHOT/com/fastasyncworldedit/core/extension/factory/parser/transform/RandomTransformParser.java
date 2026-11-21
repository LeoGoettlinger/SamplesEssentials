package com.fastasyncworldedit.core.extension.factory.parser.transform;

import com.fastasyncworldedit.core.extent.ResettableExtent;
import com.fastasyncworldedit.core.extent.transform.RandomTransform;
import com.sk89q.util.StringUtil;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.internal.registry.InputParser;
import java.util.List;
import java.util.stream.Stream;

public class RandomTransformParser extends InputParser<ResettableExtent> {
   public RandomTransformParser(WorldEdit worldEdit) {
      super(worldEdit);
   }

   @Override
   public Stream<String> getSuggestions(String input, ParserContext context) {
      if (input.isEmpty()) {
         return Stream.empty();
      } else {
         List<String> split = StringUtil.split(input, ',', '[', ']');
         if (split.size() == 1) {
            return Stream.empty();
         } else {
            StringBuilder builder = new StringBuilder();

            for (int i = 0; i < split.size() - 1; i++) {
               builder.append(split.get(i)).append(',');
            }

            String previous = builder.toString();
            return this.worldEdit.getTransformFactory().getSuggestions(split.getLast(), context).stream().map(s -> previous + s);
         }
      }
   }

   public ResettableExtent parseFromInput(String input, ParserContext context) throws InputParseException {
      if (input.isEmpty()) {
         return null;
      } else {
         List<String> split = StringUtil.split(input, ',', '[', ']');
         if (split.size() == 1) {
            return null;
         } else {
            RandomTransform randomTransform = new RandomTransform();

            for (String s : split) {
               ResettableExtent transform = this.worldEdit.getTransformFactory().parseFromInput(s, context);
               randomTransform.add(transform, 1.0);
            }

            return randomTransform;
         }
      }
   }
}
