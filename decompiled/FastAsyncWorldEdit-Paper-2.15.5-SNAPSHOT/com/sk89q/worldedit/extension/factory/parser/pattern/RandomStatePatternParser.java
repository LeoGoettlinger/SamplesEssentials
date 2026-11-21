package com.sk89q.worldedit.extension.factory.parser.pattern;

import com.fastasyncworldedit.core.extension.factory.parser.AliasedParser;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.function.pattern.RandomPattern;
import com.sk89q.worldedit.internal.registry.InputParser;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.FuzzyBlockState;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class RandomStatePatternParser extends InputParser<Pattern> implements AliasedParser {
   public RandomStatePatternParser(WorldEdit worldEdit) {
      super(worldEdit);
   }

   @Override
   public Stream<String> getSuggestions(String input, ParserContext context) {
      if (input.isEmpty()) {
         return Stream.of("*");
      } else {
         return !input.startsWith("*")
            ? Stream.empty()
            : this.worldEdit.getBlockFactory().getSuggestions(input.substring(1), context).stream().map(s -> "*" + s);
      }
   }

   public Pattern parseFromInput(String input, ParserContext context) throws InputParseException {
      if (!input.startsWith("*")) {
         return null;
      } else {
         boolean wasFuzzy = context.isPreferringWildcard();
         context.setPreferringWildcard(true);
         BaseBlock block = this.worldEdit.getBlockFactory().parseFromInput(input.substring(1), context);
         context.setPreferringWildcard(wasFuzzy);
         if (block.getStates().size() == block.getBlockType().getPropertyMap().size()) {
            return block;
         } else if (block.toImmutableState() instanceof FuzzyBlockState fbs) {
            RandomPattern randomPattern = new RandomPattern();
            fbs.getBlockType().getAllStates().stream().filter(fbs::equalsFuzzy).map(BlockState::toBaseBlock).forEach(bb -> randomPattern.add(bb, 1.0));
            return randomPattern;
         } else {
            return null;
         }
      }
   }

   @Override
   public List<String> getMatchedAliases() {
      return Collections.singletonList("*");
   }
}
