package de.theredend2000.advancedhunt.lib.cloud.minecraft.extras.parser;

import de.theredend2000.advancedhunt.lib.cloud.caption.CaptionVariable;
import de.theredend2000.advancedhunt.lib.cloud.caption.StandardCaptionKeys;
import de.theredend2000.advancedhunt.lib.cloud.component.CommandComponent;
import de.theredend2000.advancedhunt.lib.cloud.context.CommandContext;
import de.theredend2000.advancedhunt.lib.cloud.context.CommandInput;
import de.theredend2000.advancedhunt.lib.cloud.exception.parsing.ParserException;
import de.theredend2000.advancedhunt.lib.cloud.parser.ArgumentParseResult;
import de.theredend2000.advancedhunt.lib.cloud.parser.ArgumentParser;
import de.theredend2000.advancedhunt.lib.cloud.parser.ParserContributor;
import de.theredend2000.advancedhunt.lib.cloud.parser.ParserDescriptor;
import de.theredend2000.advancedhunt.lib.cloud.parser.ParserRegistry;
import de.theredend2000.advancedhunt.lib.cloud.suggestion.BlockingSuggestionProvider;
import de.theredend2000.advancedhunt.lib.cloud.type.tuple.Pair;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.format.NamedTextColor;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.format.TextColor;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class TextColorParser<C> implements ArgumentParser<C, TextColor>, BlockingSuggestionProvider.Strings<C> {
   private static final Pattern LEGACY_PREDICATE = Pattern.compile("&[0-9a-fA-F]");
   private static final Pattern HEX_PREDICATE = Pattern.compile("#?([a-fA-F0-9]{1,6})");
   private static final Collection<Pair<Character, NamedTextColor>> COLORS = Arrays.asList(
      Pair.of('0', NamedTextColor.BLACK),
      Pair.of('1', NamedTextColor.DARK_BLUE),
      Pair.of('2', NamedTextColor.DARK_GREEN),
      Pair.of('3', NamedTextColor.DARK_AQUA),
      Pair.of('4', NamedTextColor.DARK_RED),
      Pair.of('5', NamedTextColor.DARK_PURPLE),
      Pair.of('6', NamedTextColor.GOLD),
      Pair.of('7', NamedTextColor.GRAY),
      Pair.of('8', NamedTextColor.DARK_GRAY),
      Pair.of('9', NamedTextColor.BLUE),
      Pair.of('a', NamedTextColor.GREEN),
      Pair.of('b', NamedTextColor.AQUA),
      Pair.of('c', NamedTextColor.RED),
      Pair.of('d', NamedTextColor.LIGHT_PURPLE),
      Pair.of('e', NamedTextColor.YELLOW),
      Pair.of('f', NamedTextColor.WHITE)
   );

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   @NonNull
   public static <C> ParserDescriptor<C, TextColor> textColorParser() {
      return ParserDescriptor.of(new TextColorParser<>(), TextColor.class);
   }

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   @NonNull
   public static <C> CommandComponent.Builder<C, TextColor> textColorComponent() {
      return CommandComponent.<C, TextColor>builder().parser(textColorParser());
   }

   @NonNull
   @Override
   public ArgumentParseResult<TextColor> parse(@NonNull final CommandContext<C> commandContext, @NonNull final CommandInput commandInput) {
      String input = commandInput.peekString();
      if (LEGACY_PREDICATE.matcher(input).matches()) {
         commandInput.moveCursor(1);
         char code = Character.toLowerCase(commandInput.read());

         for (Pair<Character, NamedTextColor> pair : COLORS) {
            if (pair.first() == code) {
               return ArgumentParseResult.success(pair.second());
            }
         }

         commandInput.moveCursor(-2);
      }

      for (Pair<Character, NamedTextColor> pairx : COLORS) {
         if (pairx.second().toString().equalsIgnoreCase(commandInput.peekString())) {
            commandInput.readString();
            return ArgumentParseResult.success(pairx.second());
         }
      }

      if (HEX_PREDICATE.matcher(commandInput.peekString()).matches()) {
         if (commandInput.peek() == '#') {
            commandInput.moveCursor(1);
         }

         return ArgumentParseResult.success(TextColor.color(commandInput.readInteger(16)));
      } else {
         return ArgumentParseResult.failure(new TextColorParser.TextColorParseException(commandContext, input));
      }
   }

   @NonNull
   @Override
   public Iterable<String> stringSuggestions(@NonNull final CommandContext<C> commandContext, @NonNull final CommandInput input) {
      List<String> suggestions = new LinkedList<>();
      String token = input.readString();
      String tokenLower = token.toLowerCase(Locale.ROOT);
      boolean matchedName = false;

      for (String name : NamedTextColor.NAMES.keys()) {
         if (name.contains(tokenLower)) {
            matchedName = true;
         }

         suggestions.add(name);
      }

      if (!matchedName && !token.isEmpty() && (token.equals("#") || HEX_PREDICATE.matcher(token).matches() && token.length() < (token.startsWith("#") ? 7 : 6))
         )
       {
         for (char c = 'a'; c <= 'f'; c++) {
            suggestions.add(String.format("%s%c", token, c));
         }

         for (char c = '0'; c <= '9'; c++) {
            suggestions.add(String.format("%s%c", token, c));
         }
      }

      return suggestions;
   }

   @API(
      status = API.Status.INTERNAL
   )
   public static final class Contributor implements ParserContributor {
      @Override
      public <C> void contribute(final ParserRegistry<C> registry) {
         try {
            registry.registerParser(TextColorParser.textColorParser());
         } catch (LinkageError | Exception var3) {
         }
      }
   }

   private static final class TextColorParseException extends ParserException {
      private TextColorParseException(@NonNull final CommandContext<?> commandContext, @NonNull final String input) {
         super(TextColorParser.class, commandContext, StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_COLOR, CaptionVariable.of("input", input));
      }
   }
}
