package com.sk89q.worldedit.extension.factory.parser.mask;

import com.google.common.collect.ImmutableList;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.RegionMask;
import com.sk89q.worldedit.internal.registry.SimpleInputParser;
import com.sk89q.worldedit.session.request.RequestSelection;
import java.util.List;

public class LazyRegionMaskParser extends SimpleInputParser<Mask> {
   private final List<String> aliases = ImmutableList.of("#dregion", "#dselection", "#dsel");

   public LazyRegionMaskParser(WorldEdit worldEdit) {
      super(worldEdit);
   }

   @Override
   public List<String> getMatchedAliases() {
      return this.aliases;
   }

   public Mask parseFromSimpleInput(String input, ParserContext context) {
      return new RegionMask(new RequestSelection());
   }
}
