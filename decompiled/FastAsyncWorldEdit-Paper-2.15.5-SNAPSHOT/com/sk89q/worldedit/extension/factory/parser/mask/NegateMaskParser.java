package com.sk89q.worldedit.extension.factory.parser.mask;

import com.fastasyncworldedit.core.configuration.Caption;
import com.fastasyncworldedit.core.extension.factory.parser.AliasedParser;
import com.google.common.collect.ImmutableList;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.Masks;
import com.sk89q.worldedit.internal.registry.InputParser;
import java.util.List;
import java.util.stream.Stream;

public class NegateMaskParser extends InputParser<Mask> implements AliasedParser {
   private final List<String> aliases = ImmutableList.of("!");

   public NegateMaskParser(WorldEdit worldEdit) {
      super(worldEdit);
   }

   @Override
   public Stream<String> getSuggestions(String input, ParserContext context) {
      if (input.isEmpty()) {
         return Stream.of("!");
      } else {
         return input.charAt(0) != '!'
            ? Stream.empty()
            : this.worldEdit.getMaskFactory().getSuggestions(input.substring(1), context).stream().map(s -> "!" + s);
      }
   }

   public Mask parseFromInput(String input, ParserContext context) throws InputParseException {
      if (!input.startsWith("!")) {
         return null;
      } else if (input.length() > 1) {
         return Masks.negate(this.worldEdit.getMaskFactory().parseFromInput(input.substring(1), context));
      } else {
         throw new InputParseException(Caption.of("worldedit.error.parser.negate-nothing"));
      }
   }

   @Override
   public List<String> getMatchedAliases() {
      return this.aliases;
   }
}
