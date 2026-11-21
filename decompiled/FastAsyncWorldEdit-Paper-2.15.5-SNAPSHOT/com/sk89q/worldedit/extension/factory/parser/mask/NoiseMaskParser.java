package com.sk89q.worldedit.extension.factory.parser.mask;

import com.fastasyncworldedit.core.extension.factory.parser.AliasedParser;
import com.google.common.collect.ImmutableList;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.NoiseFilter;
import com.sk89q.worldedit.internal.registry.InputParser;
import com.sk89q.worldedit.math.noise.RandomNoise;
import java.util.List;
import java.util.stream.Stream;

public class NoiseMaskParser extends InputParser<Mask> implements AliasedParser {
   private final List<String> aliases = ImmutableList.of("%");

   public NoiseMaskParser(WorldEdit worldEdit) {
      super(worldEdit);
   }

   @Override
   public Stream<String> getSuggestions(String input) {
      if (input.isEmpty()) {
         return Stream.of("%");
      } else {
         return input.charAt(0) != '%' ? Stream.empty() : Stream.of("%10", "%25", "%50", "%75").filter(s -> s.startsWith(input));
      }
   }

   public Mask parseFromInput(String input, ParserContext context) {
      if (!input.startsWith("%")) {
         return null;
      } else {
         if (input.charAt(1) == '[') {
            int end = input.lastIndexOf(93);
            if (end == -1) {
               return null;
            }

            input = input.substring(2, end);
         } else {
            input = input.substring(1);
         }

         int i = Integer.parseInt(input);
         return new NoiseFilter(new RandomNoise(), i / 100.0);
      }
   }

   @Override
   public List<String> getMatchedAliases() {
      return this.aliases;
   }
}
