package com.fastasyncworldedit.core.extension.factory.parser.pattern;

import com.fastasyncworldedit.core.configuration.Caption;
import com.fastasyncworldedit.core.extension.factory.parser.RichParser;
import com.fastasyncworldedit.core.function.pattern.BiomeApplyingPattern;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.NoMatchException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.biome.BiomeTypes;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

public class BiomePatternParser extends RichParser<Pattern> {
   private static final String BIOME_PREFIX = "$";

   public BiomePatternParser(WorldEdit worldEdit) {
      super(worldEdit, "#biome", "$");
   }

   public Pattern parseFromInput(String input, ParserContext context) throws InputParseException {
      if (input.startsWith("$")) {
         String biomeId = input.substring(1);
         BiomeType biomeType = BiomeTypes.get(biomeId);
         if (biomeType == null) {
            throw new NoMatchException(Caption.of("worldedit.error.unknown-biome", TextComponent.of(biomeId)));
         } else {
            return new BiomeApplyingPattern(context.requireExtent(), biomeType);
         }
      } else {
         return (Pattern)super.parseFromInput(input, context);
      }
   }

   @Override
   public Stream<String> getSuggestions(String input, ParserContext context) {
      return input.startsWith("$") ? BiomeType.REGISTRY.getSuggestions(input.substring(1)).map(biome -> "$" + biome) : super.getSuggestions(input, context);
   }

   @Override
   protected Stream<String> getSuggestions(String argumentInput, int index, ParserContext context) {
      return index == 0 ? BiomeType.REGISTRY.getSuggestions(argumentInput) : Stream.empty();
   }

   protected Pattern parseFromInput(@Nonnull String[] arguments, ParserContext context) throws InputParseException {
      if (arguments.length != 1) {
         throw new InputParseException(Caption.of("fawe.error.invalid-arguments", TextComponent.of("#biome[<biome>]")));
      } else {
         BiomeType biomeType = BiomeTypes.get(arguments[0]);
         if (biomeType == null) {
            throw new NoMatchException(Caption.of("worldedit.error.unknown-biome", TextComponent.of(arguments[0])));
         } else {
            return new BiomeApplyingPattern(context.requireExtent(), biomeType);
         }
      }
   }
}
