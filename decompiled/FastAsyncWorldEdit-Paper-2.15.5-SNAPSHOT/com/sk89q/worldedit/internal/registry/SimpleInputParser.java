package com.sk89q.worldedit.internal.registry;

import com.fastasyncworldedit.core.extension.factory.parser.AliasedParser;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import java.util.Locale;
import java.util.stream.Stream;

public abstract class SimpleInputParser<E> extends InputParser<E> implements AliasedParser {
   protected SimpleInputParser(WorldEdit worldEdit) {
      super(worldEdit);
   }

   @Override
   public E parseFromInput(String input, ParserContext context) throws InputParseException {
      return !this.getMatchedAliases().contains(input) ? null : this.parseFromSimpleInput(input, context);
   }

   public abstract E parseFromSimpleInput(String input, ParserContext context) throws InputParseException;

   public String getPrimaryMatcher() {
      return this.getMatchedAliases().get(0);
   }

   @Override
   public Stream<String> getSuggestions(String input) {
      if (input.isEmpty()) {
         return Stream.of(this.getPrimaryMatcher());
      } else {
         String prefix = input.toLowerCase(Locale.ROOT);

         for (String alias : this.getMatchedAliases()) {
            if (alias.startsWith(prefix)) {
               return Stream.of(alias);
            }
         }

         return Stream.empty();
      }
   }
}
