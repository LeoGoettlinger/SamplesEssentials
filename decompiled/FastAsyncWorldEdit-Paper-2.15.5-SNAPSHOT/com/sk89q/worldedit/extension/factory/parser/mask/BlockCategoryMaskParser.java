package com.sk89q.worldedit.extension.factory.parser.mask;

import com.fastasyncworldedit.core.configuration.Caption;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.command.util.SuggestionHelper;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.function.mask.BlockCategoryMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.internal.registry.InputParser;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.world.block.BlockCategory;
import java.util.Locale;
import java.util.stream.Stream;

public class BlockCategoryMaskParser extends InputParser<Mask> {
   public BlockCategoryMaskParser(WorldEdit worldEdit) {
      super(worldEdit);
   }

   @Override
   public Stream<String> getSuggestions(String input) {
      return SuggestionHelper.getBlockCategorySuggestions(input, false);
   }

   public Mask parseFromInput(String input, ParserContext context) throws InputParseException {
      if (!input.startsWith("##")) {
         return null;
      } else {
         BlockCategory category = BlockCategory.REGISTRY.get(input.substring(2).toLowerCase(Locale.ROOT));
         if (category == null) {
            throw new InputParseException(Caption.of("fawe.error.unrecognised-tag", TextComponent.of(input.substring(2)), TextComponent.of("\\")));
         } else {
            return new BlockCategoryMask(context.requireExtent(), category);
         }
      }
   }
}
