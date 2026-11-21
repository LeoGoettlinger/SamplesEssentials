package de.oliver.cloud.parser.flag;

import de.oliver.cloud.caption.Caption;
import de.oliver.cloud.caption.CaptionVariable;
import de.oliver.cloud.caption.StandardCaptionKeys;
import de.oliver.cloud.context.CommandContext;
import de.oliver.cloud.context.CommandInput;
import de.oliver.cloud.exception.parsing.ParserException;
import de.oliver.cloud.key.CloudKey;
import de.oliver.cloud.parser.ArgumentParseResult;
import de.oliver.cloud.parser.ArgumentParser;
import de.oliver.cloud.suggestion.Suggestion;
import de.oliver.cloud.suggestion.SuggestionProvider;
import io.leangen.geantyref.TypeToken;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@API(
   status = Status.INTERNAL,
   consumers = {"de.oliver.cloud.*"}
)
public final class CommandFlagParser<C> implements ArgumentParser.FutureArgumentParser<C, Object>, SuggestionProvider<C> {
   public static final Object FLAG_PARSE_RESULT_OBJECT = new Object();
   public static final CloudKey<String> FLAG_META_KEY = CloudKey.of("__last_flag__", TypeToken.get(String.class));
   public static final CloudKey<Integer> FLAG_CURSOR_KEY = CloudKey.of("__flag_cursor__", TypeToken.get(Integer.class));
   public static final CloudKey<Set<CommandFlag<?>>> PARSED_FLAGS = CloudKey.of("__parsed_flags__", new TypeToken<Set<CommandFlag<?>>>() {});
   private static final Pattern FLAG_PRIMARY_PATTERN = Pattern.compile(" --(?<name>([A-Za-z]+))");
   private static final Pattern FLAG_ALIAS_PATTERN = Pattern.compile(" -(?<name>([A-Za-z]+))");
   private final Collection<CommandFlag<?>> flags;

   public CommandFlagParser(@NonNull final Collection<CommandFlag<?>> flags) {
      this.flags = flags;
   }

   @API(
      status = Status.STABLE
   )
   @NonNull
   public Collection<CommandFlag<?>> flags() {
      return Collections.unmodifiableCollection(this.flags);
   }

   @NonNull
   @Override
   public CompletableFuture<ArgumentParseResult<Object>> parseFuture(@NonNull final CommandContext<C> commandContext, @NonNull final CommandInput commandInput) {
      return new CommandFlagParser.FlagParser().parse(commandContext, commandInput);
   }

   @API(
      status = Status.STABLE
   )
   public CompletableFuture<Optional<String>> parseCurrentFlag(
      final CommandContext<C> commandContext, final CommandInput commandInput, final Executor completionExecutor
   ) {
      if (commandInput.isEmpty()) {
         return CompletableFuture.completedFuture(Optional.empty());
      } else {
         String lastInputValue = commandInput.lastRemainingToken();
         CommandFlagParser<C>.FlagParser parser = new CommandFlagParser.FlagParser();
         CompletableFuture<ArgumentParseResult<Object>> result = parser.parse(commandContext, commandInput);
         return result.thenApplyAsync(parseResult -> {
            if (commandContext.contains(FLAG_CURSOR_KEY)) {
               commandInput.cursor(commandContext.get(FLAG_CURSOR_KEY));
            } else if (parser.lastParsedFlag() == null && commandInput.isEmpty()) {
               int count = lastInputValue.length();
               commandInput.moveCursor(-count);
            }

            return Optional.ofNullable(parser.lastParsedFlag());
         }, completionExecutor);
      }
   }

   @NonNull
   @Override
   public CompletableFuture<Iterable<Suggestion>> suggestionsFuture(@NonNull final CommandContext<C> commandContext, @NonNull final CommandInput input) {
      String lastArg = Objects.requireNonNull(commandContext.getOrDefault(FLAG_META_KEY, ""));
      if (!lastArg.startsWith("-")) {
         String readInput = input.readInput();
         List<CommandFlag<?>> usedFlags = new LinkedList<>();
         Matcher primaryMatcher = FLAG_PRIMARY_PATTERN.matcher(readInput);

         while (primaryMatcher.find()) {
            String name = primaryMatcher.group("name");

            for (CommandFlag<?> flag : this.flags) {
               if (flag.name().equalsIgnoreCase(name)) {
                  usedFlags.add(flag);
                  break;
               }
            }
         }

         Matcher aliasMatcher = FLAG_ALIAS_PATTERN.matcher(readInput);

         while (aliasMatcher.find()) {
            String name = aliasMatcher.group("name");

            for (CommandFlag<?> flagx : this.flags) {
               for (String alias : flagx.aliases()) {
                  if (name.contains(alias)) {
                     usedFlags.add(flagx);
                     break;
                  }
               }
            }
         }

         String nextToken = input.peekString();
         String currentFlag;
         if (nextToken.length() > 1) {
            currentFlag = nextToken.substring(1);
         } else {
            currentFlag = "";
         }

         List<Suggestion> suggestions = new LinkedList<>();

         for (CommandFlag<?> flagx : this.flags) {
            if ((!usedFlags.contains(flagx) || flagx.mode() == CommandFlag.FlagMode.REPEATABLE) && commandContext.hasPermission(flagx.permission())) {
               suggestions.add(Suggestion.suggestion(String.format("--%s", flagx.name())));
            }
         }

         boolean suggestCombined = nextToken.length() > 1 && nextToken.startsWith("-") && !nextToken.startsWith("--");

         for (CommandFlag<?> flagxx : this.flags) {
            if ((!usedFlags.contains(flagxx) || flagxx.mode() == CommandFlag.FlagMode.REPEATABLE) && commandContext.hasPermission(flagxx.permission())) {
               for (String aliasx : flagxx.aliases()) {
                  if (!aliasx.equalsIgnoreCase(currentFlag)) {
                     if (suggestCombined && flagxx.commandComponent() == null) {
                        suggestions.add(Suggestion.suggestion(String.format("%s%s", input.peekString(), aliasx)));
                     } else {
                        suggestions.add(Suggestion.suggestion(String.format("-%s", aliasx)));
                     }
                  }
               }
            }
         }

         if (suggestCombined) {
            suggestions.add(Suggestion.suggestion(input.peekString()));
         }

         return CompletableFuture.completedFuture(suggestions);
      } else {
         CommandFlag<?> currentFlagx = null;
         if (lastArg.startsWith("--")) {
            String flagName = lastArg.substring(2);

            for (CommandFlag<?> flagxxx : this.flags) {
               if (flagName.equalsIgnoreCase(flagxxx.name())) {
                  currentFlagx = flagxxx;
                  break;
               }
            }
         } else {
            String flagName = lastArg.substring(1);

            label166:
            for (CommandFlag<?> flagxxxx : this.flags) {
               for (String aliasxx : flagxxxx.aliases()) {
                  if (aliasxx.equalsIgnoreCase(flagName)) {
                     currentFlagx = flagxxxx;
                     break label166;
                  }
               }
            }
         }

         if (currentFlagx != null && commandContext.hasPermission(currentFlagx.permission()) && currentFlagx.commandComponent() != null) {
            SuggestionProvider suggestionProvider = currentFlagx.commandComponent().suggestionProvider();
            return (CompletableFuture<Iterable<Suggestion>>)suggestionProvider.suggestionsFuture(commandContext, input);
         } else {
            commandContext.store(FLAG_META_KEY, "");
            return this.suggestionsFuture(commandContext, input);
         }
      }
   }

   @API(
      status = Status.STABLE
   )
   public static enum FailureReason {
      UNKNOWN_FLAG(StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_FLAG_UNKNOWN_FLAG),
      DUPLICATE_FLAG(StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_FLAG_DUPLICATE_FLAG),
      NO_FLAG_STARTED(StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_FLAG_NO_FLAG_STARTED),
      MISSING_ARGUMENT(StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_FLAG_MISSING_ARGUMENT),
      NO_PERMISSION(StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_FLAG_NO_PERMISSION);

      private final Caption caption;

      private FailureReason(@NonNull final Caption caption) {
         this.caption = caption;
      }

      @NonNull
      public Caption caption() {
         return this.caption;
      }
   }

   @API(
      status = Status.STABLE
   )
   public static final class FlagParseException extends ParserException {
      private final String input;
      private final CommandFlagParser.FailureReason failureReason;

      public FlagParseException(
         @NonNull final String input, @NonNull final CommandFlagParser.FailureReason failureReason, @NonNull final CommandContext<?> context
      ) {
         super(CommandFlagParser.class, context, failureReason.caption(), CaptionVariable.of("input", input), CaptionVariable.of("flag", input));
         this.input = input;
         this.failureReason = failureReason;
      }

      public String input() {
         return this.input;
      }

      @API(
         status = Status.STABLE
      )
      @NonNull
      public CommandFlagParser.FailureReason failureReason() {
         return this.failureReason;
      }
   }

   private final class FlagParser {
      private String lastParsedFlag;

      private FlagParser() {
      }

      @NonNull
      private CompletableFuture<ArgumentParseResult<Object>> parse(@NonNull final CommandContext<C> commandContext, @NonNull final CommandInput commandInput) {
         CompletableFuture<ArgumentParseResult<Object>> result = CompletableFuture.completedFuture(null);
         Set<CommandFlag<?>> parsedFlags = commandContext.computeIfAbsent(CommandFlagParser.PARSED_FLAGS, k -> new HashSet<>());
         int remainingTokens = commandInput.remainingTokens();

         for (int i = 0; i <= remainingTokens; i++) {
            result = result.thenCompose(
               parseResult -> {
                  commandInput.skipWhitespace();
                  if (parseResult == null && !commandInput.isEmpty()) {
                     String string = commandInput.peekString();
                     if (!string.startsWith("-")) {
                        return CompletableFuture.completedFuture(ArgumentParseResult.success(CommandFlagParser.FLAG_PARSE_RESULT_OBJECT));
                     } else {
                        this.lastParsedFlag = null;
                        if (string.startsWith("--")) {
                           commandInput.moveCursor(2);
                        } else {
                           commandInput.moveCursor(1);
                        }

                        String flagName = commandInput.readStringSkipWhitespace();
                        CommandFlag<?> flag = null;
                        if (string.startsWith("--")) {
                           for (CommandFlag<?> flagCandidate : CommandFlagParser.this.flags) {
                              if (flagName.equalsIgnoreCase(flagCandidate.name())) {
                                 flag = flagCandidate;
                                 break;
                              }
                           }
                        } else {
                           if (flagName.length() != 1) {
                              boolean flagFound = false;

                              for (int j = 0; j < flagName.length(); j++) {
                                 String parsedFlag = Character.toString(flagName.charAt(j)).toLowerCase(Locale.ENGLISH);

                                 for (CommandFlag<?> candidateFlag : CommandFlagParser.this.flags) {
                                    if (candidateFlag.commandComponent() == null && candidateFlag.aliases().contains(parsedFlag)) {
                                       if (parsedFlags.contains(candidateFlag) && candidateFlag.mode() != CommandFlag.FlagMode.REPEATABLE) {
                                          return this.fail(
                                             new CommandFlagParser.FlagParseException(string, CommandFlagParser.FailureReason.DUPLICATE_FLAG, commandContext)
                                          );
                                       }

                                       if (!commandContext.hasPermission(candidateFlag.permission())) {
                                          return this.fail(
                                             new CommandFlagParser.FlagParseException(string, CommandFlagParser.FailureReason.NO_PERMISSION, commandContext)
                                          );
                                       }

                                       commandContext.flags().addPresenceFlag(candidateFlag);
                                       parsedFlags.add(candidateFlag);
                                       flagFound = true;
                                    }
                                 }
                              }

                              if (!flagFound) {
                                 return this.fail(
                                    new CommandFlagParser.FlagParseException(string, CommandFlagParser.FailureReason.NO_FLAG_STARTED, commandContext)
                                 );
                              }

                              return CompletableFuture.completedFuture(null);
                           }

                           label97:
                           for (CommandFlag<?> flagCandidatex : CommandFlagParser.this.flags) {
                              for (String alias : flagCandidatex.aliases()) {
                                 if (alias.equalsIgnoreCase(flagName)) {
                                    flag = flagCandidatex;
                                    break label97;
                                 }
                              }
                           }
                        }

                        if (flag == null) {
                           return this.fail(new CommandFlagParser.FlagParseException(string, CommandFlagParser.FailureReason.UNKNOWN_FLAG, commandContext));
                        } else if (parsedFlags.contains(flag) && flag.mode() != CommandFlag.FlagMode.REPEATABLE) {
                           return this.fail(new CommandFlagParser.FlagParseException(string, CommandFlagParser.FailureReason.DUPLICATE_FLAG, commandContext));
                        } else if (!commandContext.hasPermission(flag.permission())) {
                           return this.fail(new CommandFlagParser.FlagParseException(string, CommandFlagParser.FailureReason.NO_PERMISSION, commandContext));
                        } else if (flag.commandComponent() == null) {
                           commandContext.remove(CommandFlagParser.FLAG_CURSOR_KEY);
                           commandContext.flags().addPresenceFlag(flag);
                           parsedFlags.add(flag);
                           return CompletableFuture.completedFuture(null);
                        } else {
                           if (commandInput.hasRemainingInput() && commandInput.peek() == ' ') {
                              this.lastParsedFlag = string;
                           }

                           if (commandInput.isEmpty(true)) {
                              return this.fail(
                                 new CommandFlagParser.FlagParseException(flag.name(), CommandFlagParser.FailureReason.MISSING_ARGUMENT, commandContext)
                              );
                           } else {
                              this.lastParsedFlag = string;
                              CommandInput commandInputCopy = commandInput.copy();
                              return flag.commandComponent().parser().parseFuture(commandContext, commandInput).thenApply(parsedValue -> {
                                 if (parsedValue.failure().isPresent() || commandInput.isEmpty() || commandInput.peek() != ' ') {
                                    commandContext.store(CommandFlagParser.FLAG_CURSOR_KEY, commandInputCopy.cursor());
                                 }

                                 if (parsedValue.failure().isPresent()) {
                                    return (ArgumentParseResult<Object>)parsedValue;
                                 } else {
                                    commandContext.flags().addValueFlag(flag, parsedValue.parsedValue().get());
                                    parsedFlags.add(flag);
                                    if (!commandInput.isEmpty(false) && commandInput.peek() == ' ') {
                                       this.lastParsedFlag = null;
                                    }

                                    return null;
                                 }
                              });
                           }
                        }
                     }
                  } else {
                     return CompletableFuture.completedFuture((ArgumentParseResult<Object>)parseResult);
                  }
               }
            );
         }

         return result.thenApply(r -> r == null ? ArgumentParseResult.success(CommandFlagParser.FLAG_PARSE_RESULT_OBJECT) : r);
      }

      @Nullable
      private String lastParsedFlag() {
         return this.lastParsedFlag;
      }

      @NonNull
      private CompletableFuture<ArgumentParseResult<Object>> fail(@NonNull final Throwable exception) {
         return ArgumentParseResult.failureFuture(exception);
      }
   }
}
