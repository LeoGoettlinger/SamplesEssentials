package com.sk89q.worldedit.extension.factory.parser.mask;

import com.fastasyncworldedit.core.configuration.Caption;
import com.fastasyncworldedit.core.extension.factory.parser.AliasedParser;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.command.util.SuggestionHelper;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.NoMatchException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.function.mask.BiomeMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.internal.registry.InputParser;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.world.biome.BiomeType;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BiomeMaskParser extends InputParser<Mask> implements AliasedParser {
   private final List<String> aliases = ImmutableList.of("$");

   public BiomeMaskParser(WorldEdit worldEdit) {
      super(worldEdit);
   }

   @Override
   public Stream<String> getSuggestions(String input) {
      if (input.isEmpty()) {
         return Stream.of("$");
      } else if (input.charAt(0) == '$') {
         input = input.substring(1);
         int lastTermIdx = input.lastIndexOf(44);
         if (lastTermIdx <= 0) {
            return SuggestionHelper.getNamespacedRegistrySuggestions(BiomeType.REGISTRY, input).map(s -> "$" + s);
         } else {
            String prev = input.substring(0, lastTermIdx) + ",";
            Set<String> prevBiomes = Arrays.stream(prev.split(",", 0)).collect(Collectors.toSet());
            String search = input.substring(lastTermIdx + 1);
            return SuggestionHelper.getNamespacedRegistrySuggestions(BiomeType.REGISTRY, search).filter(s -> !prevBiomes.contains(s)).map(s -> "$" + prev + s);
         }
      } else {
         return Stream.empty();
      }
   }

   public Mask parseFromInput(String input, ParserContext context) throws InputParseException {
      if (!input.startsWith("$")) {
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

         Set<BiomeType> biomes = new HashSet<>();

         for (String biomeName : Splitter.on(",").split(input)) {
            BiomeType biome = BiomeType.REGISTRY.get(biomeName);
            if (biome == null) {
               throw new NoMatchException(Caption.of("worldedit.error.unknown-biome", TextComponent.of(biomeName)));
            }

            biomes.add(biome);
         }

         return new BiomeMask(context.requireExtent(), biomes);
      }
   }

   @Override
   public List<String> getMatchedAliases() {
      return this.aliases;
   }
}
