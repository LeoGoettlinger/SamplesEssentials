package com.fastasyncworldedit.core.extension.factory.parser;

import com.fastasyncworldedit.core.configuration.Caption;
import com.google.common.base.Preconditions;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.internal.registry.InputParser;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

public abstract class RichParser<E> extends InputParser<E> implements AliasedParser {
   private final String[] prefixes;

   protected RichParser(WorldEdit worldEdit, String... aliases) {
      super(worldEdit);
      Preconditions.checkArgument(aliases.length >= 1, "Aliases may not be empty");
      this.prefixes = aliases;
   }

   @Nonnull
   private static Predicate<String> validPrefix(String other) {
      return prefix -> {
         if (prefix.length() > other.length()) {
            return prefix.startsWith(other);
         } else {
            int i = other.indexOf(91);
            return i == -1 ? other.equals(prefix) : other.substring(0, i).equals(prefix);
         }
      };
   }

   @Nonnull
   private Function<String, Stream<? extends String>> extractArguments(String input, ParserContext context) {
      return prefix -> {
         if (input.length() > prefix.length() && input.startsWith(prefix + "[")) {
            String[] strings = this.extractArguments(input.substring(prefix.length()), false);
            StringBuilder builder = new StringBuilder();

            for (int i = 0; i < strings.length - 1; i++) {
               builder.append('[').append(strings[i]).append(']');
            }

            String previous = prefix + builder;
            return this.getSuggestions(strings[strings.length - 1], strings.length - 1, context).map(suggestion -> previous + "[" + suggestion);
         } else {
            return Stream.of(prefix);
         }
      };
   }

   public String getPrefix() {
      return this.prefixes[0];
   }

   @Override
   public List<String> getMatchedAliases() {
      return Arrays.asList(this.prefixes);
   }

   @Override
   public Stream<String> getSuggestions(String input, ParserContext context) {
      return Arrays.stream(this.prefixes).filter(validPrefix(input)).flatMap(this.extractArguments(input, new ParserContext()));
   }

   @Override
   public E parseFromInput(String input, ParserContext context) throws InputParseException {
      int i = input.indexOf(91);
      if (i == -1) {
         return null;
      } else {
         String inputPrefix = input.substring(0, i);

         for (String prefix : this.prefixes) {
            if (inputPrefix.equals(prefix)) {
               String[] arguments = this.extractArguments(input.substring(prefix.length()), true);
               return this.parseFromInput(arguments, context);
            }
         }

         return null;
      }
   }

   @Deprecated
   protected Stream<String> getSuggestions(String argumentInput, int index) {
      return Stream.empty();
   }

   protected Stream<String> getSuggestions(String argumentInput, int index, ParserContext context) {
      return this.getSuggestions(argumentInput, index);
   }

   protected abstract E parseFromInput(@Nonnull String[] arguments, ParserContext context) throws InputParseException;

   protected String[] extractArguments(String input, boolean requireClosing) throws InputParseException {
      int open = 0;
      int openIndex = 0;
      int i = 0;

      List<String> arguments;
      for (arguments = new ArrayList<>(); i < input.length(); i++) {
         if (input.charAt(i) == '[' && open++ == 0) {
            openIndex = i;
         }

         if (input.charAt(i) == ']') {
            if (--open == 0) {
               arguments.add(input.substring(openIndex + 1, i));
            }
         }
      }

      if (!requireClosing) {
         if (open > 0) {
            arguments.add(input.substring(openIndex + 1));
         } else {
            int last = input.lastIndexOf(93);
            if (last != -1) {
               arguments.add(input.substring(last));
            }
         }
      }

      if (requireClosing && open != 0) {
         throw new InputParseException(Caption.of("fawe.error.invalid-bracketing", TextComponent.of("'[' or ']'?")));
      } else {
         return arguments.toArray(new String[0]);
      }
   }
}
