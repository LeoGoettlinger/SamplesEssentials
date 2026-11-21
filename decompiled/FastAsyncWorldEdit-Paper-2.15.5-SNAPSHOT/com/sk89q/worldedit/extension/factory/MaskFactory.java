package com.sk89q.worldedit.extension.factory;

import com.fastasyncworldedit.core.configuration.Caption;
import com.fastasyncworldedit.core.extension.factory.parser.mask.AdjacentMaskParser;
import com.fastasyncworldedit.core.extension.factory.parser.mask.AngleMaskParser;
import com.fastasyncworldedit.core.extension.factory.parser.mask.BesideMaskParser;
import com.fastasyncworldedit.core.extension.factory.parser.mask.ExtremaMaskParser;
import com.fastasyncworldedit.core.extension.factory.parser.mask.FalseMaskParser;
import com.fastasyncworldedit.core.extension.factory.parser.mask.HotbarMaskParser;
import com.fastasyncworldedit.core.extension.factory.parser.mask.LiquidMaskParser;
import com.fastasyncworldedit.core.extension.factory.parser.mask.ROCAngleMaskParser;
import com.fastasyncworldedit.core.extension.factory.parser.mask.RadiusMaskParser;
import com.fastasyncworldedit.core.extension.factory.parser.mask.RichMaskParser;
import com.fastasyncworldedit.core.extension.factory.parser.mask.RichOffsetMaskParser;
import com.fastasyncworldedit.core.extension.factory.parser.mask.SimplexMaskParser;
import com.fastasyncworldedit.core.extension.factory.parser.mask.SurfaceAngleMaskParser;
import com.fastasyncworldedit.core.extension.factory.parser.mask.SurfaceMaskParser;
import com.fastasyncworldedit.core.extension.factory.parser.mask.TrueMaskParser;
import com.fastasyncworldedit.core.extension.factory.parser.mask.WallMaskParser;
import com.fastasyncworldedit.core.extension.factory.parser.mask.XAxisMaskParser;
import com.fastasyncworldedit.core.extension.factory.parser.mask.YAxisMaskParser;
import com.fastasyncworldedit.core.extension.factory.parser.mask.ZAxisMaskParser;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.factory.parser.mask.AirMaskParser;
import com.sk89q.worldedit.extension.factory.parser.mask.BiomeMaskParser;
import com.sk89q.worldedit.extension.factory.parser.mask.BlockCategoryMaskParser;
import com.sk89q.worldedit.extension.factory.parser.mask.BlockStateMaskParser;
import com.sk89q.worldedit.extension.factory.parser.mask.BlocksMaskParser;
import com.sk89q.worldedit.extension.factory.parser.mask.ExistingMaskParser;
import com.sk89q.worldedit.extension.factory.parser.mask.ExpressionMaskParser;
import com.sk89q.worldedit.extension.factory.parser.mask.LazyRegionMaskParser;
import com.sk89q.worldedit.extension.factory.parser.mask.NegateMaskParser;
import com.sk89q.worldedit.extension.factory.parser.mask.NoiseMaskParser;
import com.sk89q.worldedit.extension.factory.parser.mask.OffsetMaskParser;
import com.sk89q.worldedit.extension.factory.parser.mask.RegionMaskParser;
import com.sk89q.worldedit.extension.factory.parser.mask.SolidMaskParser;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.NoMatchException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.MaskIntersection;
import com.sk89q.worldedit.internal.registry.AbstractFactory;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import java.util.List;
import java.util.stream.Collectors;

public final class MaskFactory extends AbstractFactory<Mask> {
   public MaskFactory(WorldEdit worldEdit) {
      super(worldEdit, new BlocksMaskParser(worldEdit), new RichMaskParser(worldEdit));
      this.register(new ExistingMaskParser(worldEdit));
      this.register(new AirMaskParser(worldEdit));
      this.register(new SolidMaskParser(worldEdit));
      this.register(new LazyRegionMaskParser(worldEdit));
      this.register(new RegionMaskParser(worldEdit));
      this.register(new OffsetMaskParser(worldEdit));
      this.register(new NoiseMaskParser(worldEdit));
      this.register(new BlockStateMaskParser(worldEdit));
      this.register(new NegateMaskParser(worldEdit));
      this.register(new ExpressionMaskParser(worldEdit));
      this.register(new BlockCategoryMaskParser(worldEdit));
      this.register(new BiomeMaskParser(worldEdit));
      this.register(new AdjacentMaskParser(worldEdit));
      this.register(new AngleMaskParser(worldEdit));
      this.register(new BesideMaskParser(worldEdit));
      this.register(new ExtremaMaskParser(worldEdit));
      this.register(new FalseMaskParser(worldEdit));
      this.register(new HotbarMaskParser(worldEdit));
      this.register(new LiquidMaskParser(worldEdit));
      this.register(new RadiusMaskParser(worldEdit));
      this.register(new RichOffsetMaskParser(worldEdit));
      this.register(new ROCAngleMaskParser(worldEdit));
      this.register(new SimplexMaskParser(worldEdit));
      this.register(new SurfaceMaskParser(worldEdit));
      this.register(new TrueMaskParser(worldEdit));
      this.register(new WallMaskParser(worldEdit));
      this.register(new XAxisMaskParser(worldEdit));
      this.register(new YAxisMaskParser(worldEdit));
      this.register(new ZAxisMaskParser(worldEdit));
      this.register(new SurfaceAngleMaskParser(worldEdit));
   }

   @Override
   public List<String> getSuggestions(String input, final ParserContext parserContext) {
      String[] split = input.split(" ");
      if (split.length > 1) {
         String prev = input.substring(0, input.lastIndexOf(" ")) + " ";
         return super.getSuggestions(split[split.length - 1], parserContext).stream().map(s -> prev + s).collect(Collectors.toList());
      } else {
         return super.getSuggestions(input, parserContext);
      }
   }

   public Mask parseFromInput(String input, ParserContext context) throws InputParseException {
      return (Mask)super.parseFromInput(input, context);
   }

   protected Mask getParsed(final String input, final List<Mask> masks) {
      return switch (masks.size()) {
         case 0 -> throw new NoMatchException(Caption.of("worldedit.error.no-match", TextComponent.of(input)));
         case 1 -> masks.get(0).optimize();
         default -> new MaskIntersection(masks).optimize();
      };
   }
}
