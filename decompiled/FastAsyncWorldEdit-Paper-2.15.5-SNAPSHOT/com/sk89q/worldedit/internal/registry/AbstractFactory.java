package com.sk89q.worldedit.internal.registry;

import com.fastasyncworldedit.core.configuration.Caption;
import com.fastasyncworldedit.core.extension.factory.parser.AliasedParser;
import com.fastasyncworldedit.core.extension.factory.parser.FaweParser;
import com.google.common.base.Preconditions;
import com.sk89q.util.StringUtil;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.NoMatchException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractFactory<E> {
   protected final WorldEdit worldEdit;
   protected final List<InputParser<E>> parsers = new ArrayList<>();
   private final FaweParser<E> richParser;

   protected AbstractFactory(WorldEdit worldEdit, InputParser<E> defaultParser) {
      this(worldEdit, defaultParser, null);
   }

   protected AbstractFactory(WorldEdit worldEdit, InputParser<E> defaultParser, FaweParser<E> richParser) {
      Preconditions.checkNotNull(worldEdit);
      Preconditions.checkNotNull(defaultParser);
      this.worldEdit = worldEdit;
      this.parsers.add(defaultParser);
      this.richParser = richParser;
   }

   public List<InputParser<E>> getParsers() {
      return Collections.unmodifiableList(this.parsers);
   }

   public E parseFromInput(String input, ParserContext context) throws InputParseException {
      List<E> parsed = new ArrayList<>();

      for (String component : StringUtil.split(input, ' ', '[', ']')) {
         if (!component.isEmpty()) {
            if (this.richParser != null) {
               E match = this.richParser.parseFromInput(component, context);
               if (match != null) {
                  parsed.add(match);
                  continue;
               }
            }

            this.parseFromParsers(context, parsed, component);
         }
      }

      return this.getParsed(input, parsed);
   }

   @Deprecated
   public List<String> getSuggestions(String input) {
      return this.getSuggestions(input, new ParserContext());
   }

   public List<String> getSuggestions(String input, ParserContext context) {
      return this.parsers.stream().flatMap(p -> p.getSuggestions(input, context)).collect(Collectors.toList());
   }

   public void register(InputParser<E> inputParser) {
      Preconditions.checkNotNull(inputParser);
      this.parsers.add(this.parsers.size() - 1, inputParser);
   }

   public boolean containsAlias(String alias) {
      return this.parsers.stream().anyMatch(p -> !(p instanceof AliasedParser) ? false : ((AliasedParser)p).getMatchedAliases().contains(alias));
   }

   protected void parseFromParsers(final ParserContext context, final List<E> parsed, final String component) {
      E match = null;

      for (InputParser<E> parser : this.getParsers()) {
         match = parser.parseFromInput(component, context);
         if (match != null) {
            break;
         }
      }

      if (match == null) {
         throw new NoMatchException(Caption.of("worldedit.error.no-match", TextComponent.of(component)));
      } else {
         parsed.add(match);
      }
   }

   public E parseWithoutRich(String input, ParserContext context) throws InputParseException {
      List<E> parsed = new ArrayList<>();

      for (String component : StringUtil.split(input, ' ', '[', ']')) {
         if (!component.isEmpty()) {
            this.parseFromParsers(context, parsed, component);
         }
      }

      return this.getParsed(input, parsed);
   }

   protected E getParsed(final String input, final List<E> parsed) {
      return parsed.isEmpty() ? null : parsed.get(0);
   }
}
