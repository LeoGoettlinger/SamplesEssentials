package com.sk89q.worldedit.extension.factory.parser.pattern;

import com.fastasyncworldedit.core.configuration.Caption;
import com.sk89q.util.StringUtil;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.function.pattern.RandomPattern;
import com.sk89q.worldedit.internal.registry.InputParser;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import java.util.List;
import java.util.stream.Stream;

public class RandomPatternParser extends InputParser<Pattern> {
   private static final java.util.regex.Pattern regex = java.util.regex.Pattern.compile("[0-9]+(\\.[0-9]*)?%.*");

   public RandomPatternParser(WorldEdit worldEdit) {
      super(worldEdit);
   }

   @Override
   public Stream<String> getSuggestions(String input, ParserContext context) {
      List<String> patterns = StringUtil.split(input, ',', '[', ']');
      if (patterns.isEmpty()) {
         return Stream.empty();
      } else {
         String percent = null;
         String token = patterns.get(patterns.size() - 1);
         if (regex.matcher(token).matches()) {
            String[] p = token.split("%", 2);
            percent = p[0];
            token = p[1];
         } else if (patterns.size() == 1) {
            return Stream.empty();
         }

         String previous = patterns.size() == 1 ? "" : String.join(",", patterns.subList(0, patterns.size() - 1)) + ",";
         String prefix = previous + (percent == null ? "" : percent + "%");
         List<String> innerSuggestions = this.worldEdit.getPatternFactory().getSuggestions(token, context);
         return innerSuggestions.stream().map(s -> prefix + s);
      }
   }

   public Pattern parseFromInput(String input, ParserContext context) throws InputParseException {
      RandomPattern randomPattern = new RandomPattern();
      List<String> patterns = StringUtil.split(input, ',', '[', ']');
      if (patterns.size() == 1) {
         return null;
      } else {
         for (String token : patterns) {
            double chance;
            Pattern innerPattern;
            if (token.matches("[0-9]+(\\.[0-9]*)?%.*")) {
               String[] p = token.split("%", 2);
               if (p.length < 2) {
                  throw new InputParseException(Caption.of("worldedit.error.parser.missing-random-type", TextComponent.of(input)));
               }

               chance = Double.parseDouble(p[0]);
               innerPattern = this.worldEdit.getPatternFactory().parseFromInput(p[1], context);
            } else {
               chance = 1.0;
               innerPattern = this.worldEdit.getPatternFactory().parseFromInput(token, context);
            }

            randomPattern.add(innerPattern, chance);
         }

         return randomPattern;
      }
   }
}
