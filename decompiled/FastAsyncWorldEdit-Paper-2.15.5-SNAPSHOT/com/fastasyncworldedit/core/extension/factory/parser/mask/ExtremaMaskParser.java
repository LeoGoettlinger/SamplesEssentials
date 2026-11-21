package com.fastasyncworldedit.core.extension.factory.parser.mask;

import com.fastasyncworldedit.core.configuration.Caption;
import com.fastasyncworldedit.core.extension.factory.parser.RichParser;
import com.fastasyncworldedit.core.function.mask.ExtremaMask;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.command.util.SuggestionHelper;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

public class ExtremaMaskParser extends RichParser<Mask> {
   private final String[] flags = new String[]{"-o"};

   public ExtremaMaskParser(WorldEdit worldEdit) {
      super(worldEdit, "#extrema");
   }

   @Override
   protected Stream<String> getSuggestions(String argumentInput, int index, ParserContext context) {
      if (index == 0 || index == 1) {
         return SuggestionHelper.suggestPositiveDoubles(argumentInput).flatMap(s -> Stream.of(s, s + "d"));
      } else {
         return index > 1 && index <= 1 + this.flags.length ? Stream.of(this.flags) : Stream.empty();
      }
   }

   protected Mask parseFromInput(@Nonnull String[] arguments, ParserContext context) throws InputParseException {
      if (arguments.length >= 2 && arguments.length <= 2 + this.flags.length) {
         String minArg = arguments[0];
         String maxArg = arguments[1];
         boolean degree = minArg.endsWith("d");
         if (degree ^ maxArg.endsWith("d")) {
            throw new InputParseException(Caption.of("fawe.error.mask.angle"));
         } else {
            boolean overlay = false;
            if (arguments.length > 2) {
               for (int index = 2; index < 2 + this.flags.length; index++) {
                  String flag = arguments[index];
                  if (!flag.equals("-o")) {
                     throw new InputParseException(Caption.of("fawe.error.invalid-flag", TextComponent.of(flag)));
                  }

                  overlay = true;
               }
            }

            double min;
            double max;
            if (degree) {
               double minDeg = Double.parseDouble(minArg.substring(0, minArg.length() - 1));
               double maxDeg = Double.parseDouble(maxArg.substring(0, maxArg.length() - 1));
               min = Math.tan(minDeg * (Math.PI / 180.0));
               max = Math.tan(maxDeg * (Math.PI / 180.0));
            } else {
               min = Double.parseDouble(minArg);
               max = Double.parseDouble(maxArg);
            }

            return new ExtremaMask(context.getExtent(), min, max, overlay, 4);
         }
      } else {
         return null;
      }
   }
}
