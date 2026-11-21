package com.sk89q.worldedit.extension.factory.parser.mask;

import com.fastasyncworldedit.core.configuration.Caption;
import com.google.common.collect.ImmutableList;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.RegionMask;
import com.sk89q.worldedit.internal.registry.SimpleInputParser;
import java.util.List;

public class RegionMaskParser extends SimpleInputParser<Mask> {
   private final List<String> aliases = ImmutableList.of("#region", "#selection", "#sel");

   public RegionMaskParser(WorldEdit worldEdit) {
      super(worldEdit);
   }

   @Override
   public List<String> getMatchedAliases() {
      return this.aliases;
   }

   public Mask parseFromSimpleInput(String input, ParserContext context) throws InputParseException {
      try {
         return new RegionMask(context.requireSession().getSelection(context.requireWorld()).clone());
      } catch (IncompleteRegionException var4) {
         throw new InputParseException(Caption.of("worldedit.error.incomplete-region"));
      }
   }
}
