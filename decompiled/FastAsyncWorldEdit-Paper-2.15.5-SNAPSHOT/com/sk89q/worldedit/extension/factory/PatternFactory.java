package com.sk89q.worldedit.extension.factory;

import com.fastasyncworldedit.core.configuration.Caption;
import com.fastasyncworldedit.core.extension.factory.parser.pattern.AngleColorPatternParser;
import com.fastasyncworldedit.core.extension.factory.parser.pattern.AverageColorPatternParser;
import com.fastasyncworldedit.core.extension.factory.parser.pattern.BiomePatternParser;
import com.fastasyncworldedit.core.extension.factory.parser.pattern.BufferedPattern2DParser;
import com.fastasyncworldedit.core.extension.factory.parser.pattern.BufferedPatternParser;
import com.fastasyncworldedit.core.extension.factory.parser.pattern.ColorPatternParser;
import com.fastasyncworldedit.core.extension.factory.parser.pattern.DarkenPatternParser;
import com.fastasyncworldedit.core.extension.factory.parser.pattern.DesaturatePatternParser;
import com.fastasyncworldedit.core.extension.factory.parser.pattern.ExistingPatternParser;
import com.fastasyncworldedit.core.extension.factory.parser.pattern.ExpressionPatternParser;
import com.fastasyncworldedit.core.extension.factory.parser.pattern.HotbarPatternParser;
import com.fastasyncworldedit.core.extension.factory.parser.pattern.LightenPatternParser;
import com.fastasyncworldedit.core.extension.factory.parser.pattern.Linear2DPatternParser;
import com.fastasyncworldedit.core.extension.factory.parser.pattern.Linear3DPatternParser;
import com.fastasyncworldedit.core.extension.factory.parser.pattern.LinearPatternParser;
import com.fastasyncworldedit.core.extension.factory.parser.pattern.MaskedPatternParser;
import com.fastasyncworldedit.core.extension.factory.parser.pattern.NoXPatternParser;
import com.fastasyncworldedit.core.extension.factory.parser.pattern.NoYPatternParser;
import com.fastasyncworldedit.core.extension.factory.parser.pattern.NoZPatternParser;
import com.fastasyncworldedit.core.extension.factory.parser.pattern.OffsetPatternParser;
import com.fastasyncworldedit.core.extension.factory.parser.pattern.PerlinPatternParser;
import com.fastasyncworldedit.core.extension.factory.parser.pattern.RandomFullClipboardPatternParser;
import com.fastasyncworldedit.core.extension.factory.parser.pattern.RandomOffsetPatternParser;
import com.fastasyncworldedit.core.extension.factory.parser.pattern.RelativePatternParser;
import com.fastasyncworldedit.core.extension.factory.parser.pattern.RichPatternParser;
import com.fastasyncworldedit.core.extension.factory.parser.pattern.RidgedMultiFractalPatternParser;
import com.fastasyncworldedit.core.extension.factory.parser.pattern.SaturatePatternParser;
import com.fastasyncworldedit.core.extension.factory.parser.pattern.SimplexPatternParser;
import com.fastasyncworldedit.core.extension.factory.parser.pattern.SolidRandomOffsetPatternParser;
import com.fastasyncworldedit.core.extension.factory.parser.pattern.SurfaceRandomOffsetPatternParser;
import com.fastasyncworldedit.core.extension.factory.parser.pattern.TypeSwapPatternParser;
import com.fastasyncworldedit.core.extension.factory.parser.pattern.VoronoiPatternParser;
import com.fastasyncworldedit.core.math.random.TrueRandom;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.factory.parser.pattern.BlockCategoryPatternParser;
import com.sk89q.worldedit.extension.factory.parser.pattern.ClipboardPatternParser;
import com.sk89q.worldedit.extension.factory.parser.pattern.RandomPatternParser;
import com.sk89q.worldedit.extension.factory.parser.pattern.RandomStatePatternParser;
import com.sk89q.worldedit.extension.factory.parser.pattern.SingleBlockPatternParser;
import com.sk89q.worldedit.extension.factory.parser.pattern.TypeOrStateApplyingPatternParser;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.NoMatchException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.function.pattern.RandomPattern;
import com.sk89q.worldedit.internal.registry.AbstractFactory;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import java.util.List;

public final class PatternFactory extends AbstractFactory<Pattern> {
   public PatternFactory(WorldEdit worldEdit) {
      super(worldEdit, new SingleBlockPatternParser(worldEdit), new RichPatternParser(worldEdit));
      this.register(new RandomPatternParser(worldEdit));
      this.register(new ClipboardPatternParser(worldEdit));
      this.register(new TypeOrStateApplyingPatternParser(worldEdit));
      this.register(new RandomStatePatternParser(worldEdit));
      this.register(new BlockCategoryPatternParser(worldEdit));
      this.register(new AngleColorPatternParser(worldEdit));
      this.register(new AverageColorPatternParser(worldEdit));
      this.register(new BiomePatternParser(worldEdit));
      this.register(new BufferedPatternParser(worldEdit));
      this.register(new BufferedPattern2DParser(worldEdit));
      this.register(new ColorPatternParser(worldEdit));
      this.register(new DarkenPatternParser(worldEdit));
      this.register(new DesaturatePatternParser(worldEdit));
      this.register(new ExistingPatternParser(worldEdit));
      this.register(new ExpressionPatternParser(worldEdit));
      this.register(new HotbarPatternParser(worldEdit));
      this.register(new LightenPatternParser(worldEdit));
      this.register(new Linear2DPatternParser(worldEdit));
      this.register(new Linear3DPatternParser(worldEdit));
      this.register(new LinearPatternParser(worldEdit));
      this.register(new MaskedPatternParser(worldEdit));
      this.register(new NoXPatternParser(worldEdit));
      this.register(new NoYPatternParser(worldEdit));
      this.register(new NoZPatternParser(worldEdit));
      this.register(new OffsetPatternParser(worldEdit));
      this.register(new PerlinPatternParser(worldEdit));
      this.register(new RandomFullClipboardPatternParser(worldEdit));
      this.register(new RandomOffsetPatternParser(worldEdit));
      this.register(new RelativePatternParser(worldEdit));
      this.register(new RidgedMultiFractalPatternParser(worldEdit));
      this.register(new SaturatePatternParser(worldEdit));
      this.register(new SimplexPatternParser(worldEdit));
      this.register(new SolidRandomOffsetPatternParser(worldEdit));
      this.register(new SurfaceRandomOffsetPatternParser(worldEdit));
      this.register(new TypeSwapPatternParser(worldEdit));
      this.register(new VoronoiPatternParser(worldEdit));
   }

   public Pattern parseFromInput(String input, ParserContext context) throws InputParseException {
      return (Pattern)super.parseFromInput(input, context);
   }

   protected Pattern getParsed(final String input, final List<Pattern> patterns) {
      switch (patterns.size()) {
         case 0:
            throw new NoMatchException(Caption.of("worldedit.error.no-match", TextComponent.of(input)));
         case 1:
            return patterns.get(0);
         default:
            RandomPattern randomPattern = new RandomPattern(new TrueRandom());

            for (Pattern pattern : patterns) {
               randomPattern.add(pattern, 1.0);
            }

            return randomPattern;
      }
   }
}
