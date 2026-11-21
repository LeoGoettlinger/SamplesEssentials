package com.fastasyncworldedit.core.extension.factory.parser.pattern;

import com.fastasyncworldedit.core.configuration.Caption;
import com.fastasyncworldedit.core.extension.factory.parser.RichParser;
import com.fastasyncworldedit.core.math.random.NoiseRandom;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.command.util.SuggestionHelper;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.function.pattern.RandomPattern;
import com.sk89q.worldedit.math.noise.NoiseGenerator;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

public abstract class NoisePatternParser extends RichParser<Pattern> {
   private final String name;
   private final Supplier<NoiseGenerator> generatorSupplier;

   protected NoisePatternParser(WorldEdit worldEdit, String name, Supplier<NoiseGenerator> generatorSupplier) {
      super(worldEdit, "#" + name);
      this.name = name;
      this.generatorSupplier = generatorSupplier;
   }

   @Override
   protected Stream<String> getSuggestions(String argumentInput, int index, ParserContext context) {
      if (index == 0) {
         return SuggestionHelper.suggestPositiveDoubles(argumentInput);
      } else {
         return index == 1 ? this.worldEdit.getPatternFactory().getSuggestions(argumentInput, context).stream() : Stream.empty();
      }
   }

   protected Pattern parseFromInput(@Nonnull String[] arguments, ParserContext context) {
      if (arguments.length != 2) {
         throw new InputParseException(
            Caption.of("fawe.error.command.syntax", TextComponent.of(this.getPrefix() + "[scale][pattern] (e.g. " + this.getPrefix() + "[5][dirt,stone])"))
         );
      } else {
         double scale = this.parseScale(arguments[0]);
         Pattern inner = this.worldEdit.getPatternFactory().parseFromInput(arguments[1], context);
         if (inner instanceof RandomPattern) {
            return new RandomPattern(new NoiseRandom(this.generatorSupplier.get(), scale), (RandomPattern)inner);
         } else if (inner instanceof BlockStateHolder) {
            return inner;
         } else {
            throw new InputParseException(TextComponent.of("Pattern " + inner.getClass().getSimpleName() + " cannot be used with #" + this.name));
         }
      }
   }

   protected double parseScale(String argument) {
      double scale = Double.parseDouble(argument);
      return 1.0 / Math.max(1.0, scale);
   }
}
