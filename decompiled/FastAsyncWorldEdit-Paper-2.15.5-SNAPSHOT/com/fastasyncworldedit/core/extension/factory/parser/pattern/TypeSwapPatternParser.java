package com.fastasyncworldedit.core.extension.factory.parser.pattern;

import com.fastasyncworldedit.core.configuration.Caption;
import com.fastasyncworldedit.core.extension.factory.parser.RichParser;
import com.fastasyncworldedit.core.function.pattern.TypeSwapPattern;
import com.fastasyncworldedit.core.util.Permission;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import java.util.List;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

public class TypeSwapPatternParser extends RichParser<Pattern> {
   private static final List<String> SUGGESTIONS = List.of("oak", "spruce", "stone", "sandstone");

   public TypeSwapPatternParser(WorldEdit worldEdit) {
      super(worldEdit, "#typeswap", "#ts", "#swaptype");
   }

   @Override
   public Stream<String> getSuggestions(String argumentInput, int index, ParserContext context) {
      return index > 2 ? Stream.empty() : SUGGESTIONS.stream();
   }

   public Pattern parseFromInput(@Nonnull String[] input, ParserContext context) throws InputParseException {
      if (input.length != 2) {
         throw new InputParseException(
            Caption.of("fawe.error.command.syntax", TextComponent.of(this.getPrefix() + "[input][output] (e.g. " + this.getPrefix() + "[spruce][oak])"))
         );
      } else {
         return new TypeSwapPattern(
            context.requireExtent(), input[0], input[1], Permission.hasPermission(context.requireActor(), "fawe.pattern.typeswap.regex")
         );
      }
   }
}
