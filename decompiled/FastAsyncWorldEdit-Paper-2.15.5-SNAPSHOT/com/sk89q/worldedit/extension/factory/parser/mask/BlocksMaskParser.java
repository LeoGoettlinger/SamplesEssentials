package com.sk89q.worldedit.extension.factory.parser.mask;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.function.mask.BlockMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.internal.registry.InputParser;
import com.sk89q.worldedit.world.block.BaseBlock;
import java.util.Set;
import java.util.stream.Stream;

public class BlocksMaskParser extends InputParser<Mask> {
   public BlocksMaskParser(WorldEdit worldEdit) {
      super(worldEdit);
   }

   @Override
   public Stream<String> getSuggestions(String input, ParserContext context) {
      return this.worldEdit.getBlockFactory().getSuggestions(input, context).stream();
   }

   public Mask parseFromInput(String component, ParserContext context) throws InputParseException {
      ParserContext tempContext = new ParserContext(context);
      tempContext.setRestricted(false);
      tempContext.setPreferringWildcard(true);
      tempContext.setTryLegacy(context.isTryingLegacy());

      try {
         Set<BaseBlock> holders = this.worldEdit.getBlockFactory().parseFromListInput(component, tempContext);
         return holders.isEmpty() ? null : new BlockMask(context.requireExtent(), holders);
      } catch (InputParseException var5) {
         return null;
      }
   }
}
