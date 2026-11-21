package com.sk89q.worldedit.extension.factory.parser.mask;

import com.fastasyncworldedit.core.configuration.Caption;
import com.fastasyncworldedit.core.extension.factory.parser.AliasedParser;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.function.mask.BlockStateMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.internal.registry.InputParser;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import java.util.List;
import java.util.stream.Stream;

public class BlockStateMaskParser extends InputParser<Mask> implements AliasedParser {
   private final List<String> aliases = ImmutableList.of("^", "^=");

   public BlockStateMaskParser(WorldEdit worldEdit) {
      super(worldEdit);
   }

   @Override
   public Stream<String> getSuggestions(String input) {
      return input.isEmpty() ? Stream.of("^[", "^=[") : Stream.of("^[", "^=[").filter(s -> s.startsWith(input));
   }

   public Mask parseFromInput(String input, ParserContext context) throws InputParseException {
      if ((input.startsWith("^[") || input.startsWith("^=[")) && input.endsWith("]")) {
         boolean strict = input.charAt(1) == '=';
         String states = input.substring(2 + (strict ? 1 : 0), input.length() - 1);

         try {
            return new BlockStateMask(
               context.requireExtent(), Splitter.on(',').omitEmptyStrings().trimResults().withKeyValueSeparator('=').split(states), strict
            );
         } catch (Exception var6) {
            throw new InputParseException(Caption.of("worldedit.error.parser.bad-state-format", TextComponent.of(String.valueOf(var6))));
         }
      } else {
         return null;
      }
   }

   @Override
   public List<String> getMatchedAliases() {
      return this.aliases;
   }
}
