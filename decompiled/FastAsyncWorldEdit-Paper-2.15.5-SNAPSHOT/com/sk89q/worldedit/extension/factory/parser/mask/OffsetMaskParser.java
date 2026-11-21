package com.sk89q.worldedit.extension.factory.parser.mask;

import com.fastasyncworldedit.core.extension.factory.parser.AliasedParser;
import com.google.common.collect.ImmutableList;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.function.mask.ExistingBlockMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.OffsetMask;
import com.sk89q.worldedit.internal.registry.InputParser;
import com.sk89q.worldedit.math.BlockVector3;
import java.util.List;
import java.util.stream.Stream;

public class OffsetMaskParser extends InputParser<Mask> implements AliasedParser {
   private final List<String> aliases = ImmutableList.of(">", "<");

   public OffsetMaskParser(WorldEdit worldEdit) {
      super(worldEdit);
   }

   @Override
   public Stream<String> getSuggestions(String input, ParserContext context) {
      if (input.isEmpty()) {
         return Stream.of(">", "<");
      } else {
         char firstChar = input.charAt(0);
         return firstChar != '>' && firstChar != '<'
            ? Stream.empty()
            : this.worldEdit.getMaskFactory().getSuggestions(input.substring(1), context).stream().map(s -> firstChar + s);
      }
   }

   public Mask parseFromInput(String input, ParserContext context) throws InputParseException {
      char firstChar = input.charAt(0);
      if (firstChar != '>' && firstChar != '<') {
         return null;
      } else {
         Mask submask;
         if (input.length() > 1) {
            submask = this.worldEdit.getMaskFactory().parseFromInput(input.substring(1), context);
         } else {
            submask = new ExistingBlockMask(context.requireExtent());
         }

         return new OffsetMask(submask, BlockVector3.at(0, firstChar == '>' ? -1 : 1, 0), context.getMinY(), context.getMaxY());
      }
   }

   @Override
   public List<String> getMatchedAliases() {
      return this.aliases;
   }
}
