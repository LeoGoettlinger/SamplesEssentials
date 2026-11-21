package com.fastasyncworldedit.core.extension.factory.parser.pattern;

import com.fastasyncworldedit.core.command.SuggestInputParseException;
import com.fastasyncworldedit.core.configuration.Caption;
import com.fastasyncworldedit.core.extension.factory.parser.FaweParser;
import com.fastasyncworldedit.core.math.random.TrueRandom;
import com.fastasyncworldedit.core.util.StringMan;
import com.sk89q.minecraft.util.commands.CommandLocals;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.NoMatchException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.function.pattern.RandomPattern;
import com.sk89q.worldedit.internal.command.CommandArgParser;
import com.sk89q.worldedit.internal.expression.Expression;
import com.sk89q.worldedit.internal.util.Substring;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.event.ClickEvent;
import com.sk89q.worldedit.world.block.BlockTypes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.enginehub.piston.inject.MemoizingValueAccess;
import org.enginehub.piston.suggestion.Suggestion;

public class RichPatternParser extends FaweParser<Pattern> {
   private static final java.util.regex.Pattern percentPatternRegex = java.util.regex.Pattern.compile("[0-9]+(\\.[0-9]*)?%.*");

   public RichPatternParser(WorldEdit worldEdit) {
      super(worldEdit);
   }

   public Pattern parseFromInput(String input, ParserContext context) throws InputParseException {
      if (input.isEmpty()) {
         throw new SuggestInputParseException(
            Caption.of("fawe.error.no-input-provided"),
            () -> Stream.concat(Stream.of("#", ",", "&"), BlockTypes.getNameSpaces().stream().map(n -> n + ":")).collect(Collectors.toList())
         );
      } else {
         List<Double> chances = new ArrayList<>();
         List<Pattern> patterns = new ArrayList<>();
         CommandLocals locals = new CommandLocals();
         Actor actor = context != null ? context.getActor() : null;
         if (actor != null) {
            locals.put(Actor.class, actor);
         }

         try {
            for (Entry<FaweParser.ParseEntry, List<String>> entry : parse(input)) {
               FaweParser.ParseEntry pe = entry.getKey();
               String command = pe.getInput();
               String full = pe.getFull();
               Pattern pattern = null;
               double chance = 1.0;
               if (command.isEmpty()) {
                  pattern = this.parseFromInput(StringMan.join(entry.getValue(), ','), context);
               } else if (!this.worldEdit.getPatternFactory().containsAlias(command)) {
                  char char0 = command.charAt(0);
                  boolean charPattern = input.length() > 1 && input.charAt(1) != '[';
                  if (charPattern && input.charAt(0) == '=') {
                     pattern = this.parseFromInput(char0 + "[" + input.substring(1) + "]", context);
                  }

                  if (char0 == '#' && command.length() > 1 && command.charAt(1) != '#') {
                     throw new SuggestInputParseException(
                        new NoMatchException(
                           Caption.of(
                              "fawe.error.parse.unknown-pattern",
                              full,
                              TextComponent.of("https://intellectualsites.github.io/fastasyncworldedit-documentation/patterns/patterns")
                                 .clickEvent(ClickEvent.openUrl("https://intellectualsites.github.io/fastasyncworldedit-documentation/patterns/patterns"))
                           )
                        ),
                        () -> full.length() == 1
                           ? new ArrayList<>(this.worldEdit.getPatternFactory().getSuggestions(""))
                           : new ArrayList<>(this.worldEdit.getPatternFactory().getSuggestions(command.toLowerCase(Locale.ROOT)))
                     );
                  }

                  if (charPattern && (char0 == '$' || char0 == '^' || char0 == '*' || char0 == '#' && input.charAt(1) == '#')) {
                     pattern = this.worldEdit.getPatternFactory().parseWithoutRich(full, context);
                  }

                  if (pattern == null) {
                     if (command.startsWith("[")) {
                        int end = command.lastIndexOf(93);
                        pattern = this.parseFromInput(command.substring(1, end == -1 ? command.length() : end), context);
                     } else {
                        int percentIndex = command.indexOf(37);
                        if (percentIndex != -1 && percentPatternRegex.matcher(command).matches()) {
                           chance = Expression.compile(command.substring(0, percentIndex)).evaluate();
                           String value = command.substring(percentIndex + 1);
                           if (!entry.getValue().isEmpty()) {
                              boolean addBrackets = !value.isEmpty();
                              if (addBrackets) {
                                 value = value + "[";
                              }

                              value = value + StringMan.join(entry.getValue(), "][");
                              if (addBrackets) {
                                 value = value + "]";
                              }
                           }

                           pattern = this.parseFromInput(value, context);
                        } else {
                           try {
                              pattern = this.worldEdit.getBlockFactory().parseFromInput(pe.getFull(), context);
                           } catch (NoMatchException var22) {
                              throw new NoMatchException(
                                 Caption.of(
                                    "fawe.error.parse.unknown-pattern",
                                    full,
                                    TextComponent.of("https://intellectualsites.github.io/fastasyncworldedit-documentation/patterns/patterns")
                                       .clickEvent(ClickEvent.openUrl("https://intellectualsites.github.io/fastasyncworldedit-documentation/patterns/patterns"))
                                 )
                              );
                           }
                        }
                     }
                  }
               } else {
                  List<String> args = entry.getValue();

                  try {
                     pattern = this.worldEdit.getPatternFactory().parseWithoutRich(full, context);
                  } catch (InputParseException var20) {
                     throw var20;
                  } catch (Throwable var21) {
                     throw SuggestInputParseException.of(
                        var21,
                        full,
                        () -> {
                           try {
                              String cmdArgs = args.isEmpty() ? "" : " " + StringMan.join(args, " ");
                              List<Substring> split = CommandArgParser.forArgString(cmdArgs).parseArgs().toList();
                              List<String> argStrings = split.stream().map(Substring::getSubstring).collect(Collectors.toList());
                              MemoizingValueAccess access = this.getPlatform().initializeInjectedValues(() -> cmdArgs, actor, null, true);
                              List<String> suggestions = this.getPlatform()
                                 .getCommandManager()
                                 .getSuggestions(access, argStrings)
                                 .stream()
                                 .map(Suggestion::getSuggestion)
                                 .collect(Collectors.toUnmodifiableList());
                              List<String> result = new ArrayList<>();
                              if (suggestions.size() <= 2) {
                                 for (int ix = 0; ix < suggestions.size(); ix++) {
                                    String suggestion = suggestions.get(ix);
                                    if (suggestion.indexOf(32) != 0) {
                                       String[] splitSuggestion = suggestion.split(" ");
                                       suggestion = "[" + StringMan.join((Object[])splitSuggestion, "][") + "]";
                                       result.set(ix, suggestion);
                                    }
                                 }
                              }

                              return result;
                           } catch (Throwable var12x) {
                              var12x.printStackTrace();
                              throw new InputParseException(TextComponent.of(var12x.getMessage()));
                           }
                        }
                     );
                  }
               }

               if (pattern != null) {
                  patterns.add(pattern);
                  chances.add(chance);
               }
            }
         } catch (InputParseException var23) {
            throw var23;
         } catch (Throwable var24) {
            var24.printStackTrace();
            throw new InputParseException(TextComponent.of(var24.getMessage()), var24);
         }

         if (patterns.isEmpty()) {
            return null;
         } else if (patterns.size() == 1) {
            return patterns.get(0);
         } else {
            RandomPattern random = new RandomPattern(new TrueRandom());

            for (int i = 0; i < patterns.size(); i++) {
               random.add(patterns.get(i), chances.get(i));
            }

            return random;
         }
      }
   }

   @Override
   public List<String> getMatchedAliases() {
      return Collections.emptyList();
   }
}
