package org.enginehub.piston.suggestion;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.UnmodifiableIterator;
import com.google.common.collect.ImmutableList.Builder;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.enginehub.piston.ArgBinding;
import org.enginehub.piston.Command;
import org.enginehub.piston.CommandParseResult;
import org.enginehub.piston.converter.ArgumentConverter;
import org.enginehub.piston.converter.ArgumentConverterAccess;
import org.enginehub.piston.converter.SuggestionHelper;
import org.enginehub.piston.part.ArgAcceptingCommandFlag;
import org.enginehub.piston.part.ArgAcceptingCommandPart;
import org.enginehub.piston.part.CommandFlag;
import org.enginehub.piston.part.CommandPart;
import org.enginehub.piston.part.SubCommandPart;
import org.enginehub.piston.util.StreamHelper;

public class DefaultSuggestionProvider implements SuggestionProvider {
   private static final SuggestionProvider INSTANCE = new DefaultSuggestionProvider();

   public static SuggestionProvider getInstance() {
      return INSTANCE;
   }

   private DefaultSuggestionProvider() {
   }

   @Override
   public ImmutableSet<Suggestion> provideSuggestions(List<String> args, CommandParseResult parseResult) {
      return ImmutableSet.copyOf(this.getSuggestionStream(args, parseResult).iterator());
   }

   private Stream<Suggestion> getSuggestionStream(List<String> args, CommandParseResult parseResult) {
      if (args.size() - parseResult.getBoundArguments().size() > 1) {
         return Stream.of();
      } else {
         String last = (String)Iterables.getLast(args, "");
         if (last.startsWith("-")) {
            Optional<Stream<String>> argSuggestions = this.maybeSuggestArgFlag(last, "", parseResult);
            if (argSuggestions.isPresent()) {
               return argSuggestions.get().map(this.asSuggestion(args.size()));
            }

            Set<CommandFlag> flags = this.unmatchedFlags(parseResult);
            if (!flags.isEmpty()) {
               return this.suggestFlags(last, flags).map(this.asSuggestion(args.size() - 1));
            }
         }

         if (args.size() == parseResult.getBoundArguments().size() && this.isLastExactMatch(parseResult.getBoundArguments())) {
            return this.suggestUnmatchedArguments("", parseResult).map(this.asSuggestion(args.size()));
         } else {
            if (args.size() > 1) {
               String secondToLast = args.get(args.size() - 2);
               if (secondToLast.startsWith("-")) {
                  Optional<Stream<String>> argSuggestionsx = this.maybeSuggestArgFlag(secondToLast, last, parseResult);
                  if (argSuggestionsx.isPresent()) {
                     return argSuggestionsx.get().map(this.asSuggestion(args.size() - 1));
                  }
               }
            }

            return this.suggestUnmatchedArguments(last, parseResult).map(this.asSuggestion(args.size() - 1));
         }
      }
   }

   private boolean isLastExactMatch(ImmutableList<ArgBinding> argBindings) {
      if (argBindings.isEmpty()) {
         return true;
      } else {
         ArgBinding last = (ArgBinding)Iterables.getLast(argBindings);
         UnmodifiableIterator var3 = last.getParts().iterator();

         while (var3.hasNext()) {
            CommandPart commandPart = (CommandPart)var3.next();
            if (!last.isExactMatch(commandPart)) {
               return false;
            }
         }

         return true;
      }
   }

   private Optional<Stream<String>> maybeSuggestArgFlag(String flags, String input, CommandParseResult parseResult) {
      if (flags.length() > 1) {
         char lastFlag = flags.charAt(flags.length() - 1);
         return StreamHelper.cast(parseResult.getPrimaryCommand().getParts().stream(), ArgAcceptingCommandFlag.class)
            .filter(f -> f.getName() == lastFlag)
            .findAny()
            .map(matchingArgLast -> this.suggestFromParts(input, ImmutableSet.of(matchingArgLast), parseResult));
      } else {
         return Optional.empty();
      }
   }

   private Function<String, Suggestion> asSuggestion(int replacing) {
      return suggestion -> Suggestion.builder().suggestion(suggestion).replacedArgument(replacing).build();
   }

   private Stream<String> suggestFlags(String input, Set<CommandFlag> flags) {
      return flags.stream().map(flag -> input + flag.getName());
   }

   private Stream<String> suggestUnmatchedArguments(String input, CommandParseResult parseResult) {
      Builder<CommandPart> parts = ImmutableList.builder();
      ImmutableSet<CommandPart> usedExactParts = ImmutableSet.copyOf(
         parseResult.getBoundArguments().stream().flatMap(a -> a.getParts().stream().filter(a::isExactMatch)).iterator()
      );
      UnmodifiableIterator var5 = parseResult.getPrimaryCommand().getParts().iterator();

      while (var5.hasNext()) {
         CommandPart part = (CommandPart)var5.next();
         if (!(part instanceof CommandFlag)) {
            if (usedExactParts.contains(part)) {
               parts = ImmutableList.builder();
            } else {
               parts.add(part);
               if (part.isRequired()) {
                  break;
               }
            }
         }
      }

      return this.suggestFromParts(input, parts.build(), parseResult);
   }

   private Set<CommandFlag> unmatchedFlags(CommandParseResult result) {
      Set<CommandPart> usedParts = result.getBoundArguments().stream().flatMap(a -> a.getParts().stream()).collect(Collectors.toSet());
      return StreamHelper.cast(result.getPrimaryCommand().getParts().stream(), CommandFlag.class)
         .filter(flag -> !usedParts.contains(flag))
         .collect(Collectors.toSet());
   }

   private Stream<String> suggestFromParts(String input, Collection<CommandPart> parts, CommandParseResult parseResult) {
      ArgumentConverterAccess converters = parseResult.getParameters().getConverters();
      Predicate<String> nameFilter = SuggestionHelper.byPrefix(input);
      return Stream.concat(
         StreamHelper.cast(parts.stream(), ArgAcceptingCommandPart.class)
            .filter(part -> part.getTypes().size() > 0)
            .flatMap(part -> part.getTypes().stream())
            .map(key -> (ArgumentConverter)converters.getConverter(key).orElseThrow(() -> new IllegalStateException("No converter for type " + key)))
            .flatMap(converter -> converter.getSuggestions(input, parseResult.getParameters()).stream()),
         StreamHelper.cast(parts.stream(), SubCommandPart.class)
            .flatMap(part -> part.getCommands().stream())
            .filter(c -> nameFilter.test(c.getName()) && c.getCondition().satisfied(parseResult.getParameters()))
            .map(Command::getName)
      );
   }
}
