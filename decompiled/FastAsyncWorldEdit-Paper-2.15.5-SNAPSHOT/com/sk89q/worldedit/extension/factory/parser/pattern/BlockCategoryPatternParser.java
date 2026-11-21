package com.sk89q.worldedit.extension.factory.parser.pattern;

import com.fastasyncworldedit.core.configuration.Caption;
import com.fastasyncworldedit.core.extension.factory.parser.AliasedParser;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.command.util.SuggestionHelper;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.function.pattern.RandomPattern;
import com.sk89q.worldedit.internal.registry.InputParser;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.world.block.BlockCategory;
import com.sk89q.worldedit.world.block.BlockType;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

public class BlockCategoryPatternParser extends InputParser<Pattern> implements AliasedParser {
   public BlockCategoryPatternParser(WorldEdit worldEdit) {
      super(worldEdit);
   }

   @Override
   public Stream<String> getSuggestions(String input) {
      return SuggestionHelper.getBlockCategorySuggestions(input, true);
   }

   public Pattern parseFromInput(String input, ParserContext context) throws InputParseException {
      if (!input.startsWith("##")) {
         return null;
      } else {
         String tag = input.substring(2).toLowerCase(Locale.ROOT);
         boolean anyState = false;
         if (tag.startsWith("*")) {
            tag = tag.substring(1);
            anyState = true;
         }

         BlockCategory category = BlockCategory.REGISTRY.get(tag);
         if (category == null) {
            throw new InputParseException(Caption.of("worldedit.error.unknown-tag", TextComponent.of(tag)));
         } else {
            RandomPattern randomPattern = new RandomPattern();
            Set<BlockType> blocks = category.getAll();
            if (blocks.isEmpty()) {
               throw new InputParseException(Caption.of("worldedit.error.empty-tag", TextComponent.of(category.id())));
            } else {
               if (anyState) {
                  blocks.stream().flatMap(blockTypex -> blockTypex.getAllStates().stream()).forEach(state -> randomPattern.add(state, 1.0));
               } else {
                  for (BlockType blockType : blocks) {
                     randomPattern.add(blockType.getDefaultState(), 1.0);
                  }
               }

               return randomPattern;
            }
         }
      }
   }

   @Override
   public List<String> getMatchedAliases() {
      return Collections.singletonList("##");
   }
}
