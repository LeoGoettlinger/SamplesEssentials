package org.enginehub.piston.impl;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import com.google.common.collect.UnmodifiableIterator;
import com.google.common.collect.ImmutableList.Builder;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.apache.logging.log4j.Logger;
import org.enginehub.piston.Command;
import org.enginehub.piston.CommandMetadata;
import org.enginehub.piston.CommandParseResult;
import org.enginehub.piston.config.ColorConfig;
import org.enginehub.piston.converter.ArgumentConverter;
import org.enginehub.piston.converter.ArgumentConverterAccess;
import org.enginehub.piston.converter.ConversionResult;
import org.enginehub.piston.converter.FailedConversion;
import org.enginehub.piston.converter.SuccessfulConversion;
import org.enginehub.piston.exception.ConditionFailedException;
import org.enginehub.piston.exception.ConversionFailedException;
import org.enginehub.piston.exception.NoSuchFlagException;
import org.enginehub.piston.exception.UsageException;
import org.enginehub.piston.inject.InjectedValueAccess;
import org.enginehub.piston.inject.Key;
import org.enginehub.piston.part.ArgAcceptingCommandFlag;
import org.enginehub.piston.part.ArgAcceptingCommandPart;
import org.enginehub.piston.part.ArgConsumingCommandPart;
import org.enginehub.piston.part.CommandArgument;
import org.enginehub.piston.part.CommandFlag;
import org.enginehub.piston.part.CommandPart;
import org.enginehub.piston.part.NoArgCommandFlag;
import org.enginehub.piston.part.SubCommandPart;
import org.enginehub.piston.util.ComponentHelper;
import org.enginehub.piston.util.StreamHelper;

class CommandParser {
   private static final Logger LOGGER = LogManagerCompat.getLogger();
   private static final ThreadLocal<String> PARSE_ID = new ThreadLocal<>();
   private final ArgumentConverterAccess converters;
   private final CommandMetadata metadata;
   private final CommandParseResultImpl.Builder parseResult = CommandParseResultImpl.builder();
   private final CommandParametersImpl.Builder parameters = CommandParametersImpl.builder();
   private final CommandInfoCache commandInfoCache;
   private final ImmutableList<String> arguments;
   private final ListIterator<String> argIter;
   private final InjectedValueAccess context;
   private final Set<CommandFlag> seenFlags = new HashSet<>();
   private com.google.common.collect.ImmutableMap.Builder<CommandPart, Boolean> argBindings = ImmutableMap.builder();
   @Nullable
   private CommandParser.PerCommandDetails perCommandDetails;
   @Nullable
   private CommandArgument lastFailedOptional;
   @Nullable
   private CommandParseResult result;
   private boolean justUnconsumed;

   private static void log(String message, Object... args) {
      if (LOGGER.isDebugEnabled()) {
         LOGGER.debug("[" + PARSE_ID.get() + "]: " + message, args);
      }
   }

   private static String newId() {
      long rng = ThreadLocalRandom.current().nextLong(0L, Long.MAX_VALUE);
      return Strings.padStart(Long.toString(rng, 36), 13, '0');
   }

   private static void logParseStart() {
      PARSE_ID.set(newId());
      log("started parsing");
   }

   CommandParser(ArgumentConverterAccess converters, CommandInfoCache commandInfoCache, Command initial, CommandMetadata metadata, InjectedValueAccess context) {
      this.commandInfoCache = commandInfoCache;
      this.converters = converters;
      this.metadata = metadata;
      this.arguments = metadata.getArguments();
      this.argIter = this.arguments.listIterator();
      this.context = context;
      this.switchToCommand(initial);
   }

   private void buildParseResult() {
      Preconditions.checkState(this.result == null, "Multiple calls to build final result");
      if (this.argBindings.build().size() > 0 && this.argIter.hasPrevious()) {
         this.bindArgument();
      }

      this.fillInDefaults();
      this.result = this.parseResult
         .parameters(this.parameters.metadata(this.metadata).injectedValues(this.context).converters(this.converters).build())
         .build();
   }

   private CommandParseResult getResult() {
      return (CommandParseResult)Preconditions.checkNotNull(this.result, "Not finished parsing");
   }

   private UsageException usageException(Component message) {
      this.buildParseResult();
      return new UsageException(message, this.getResult());
   }

   private UsageException notEnoughArgumentsException() {
      return this.usageException(TextComponent.of("Not enough arguments."));
   }

   private UsageException tooManyArgumentsException() {
      return this.usageException(TextComponent.of("Too many arguments."));
   }

   private ConversionFailedException conversionFailedException(ArgAcceptingCommandPart nextArg, String token) {
      ArgumentConverter<?> converter = nextArg.getTypes()
         .stream()
         .map(k -> (ArgumentConverter<?>)this.converters.getConverter(k).orElse(null))
         .filter(Objects::nonNull)
         .findFirst()
         .orElseThrow(IllegalStateException::new);
      this.buildParseResult();
      return new ConversionFailedException(
         this.getResult(), nextArg.getTextRepresentation(), converter, (FailedConversion<?>)converter.convert(token, this.context)
      );
   }

   private ConditionFailedException conditionFailed() {
      this.buildParseResult();
      return new ConditionFailedException(this.getResult().getExecutionPath());
   }

   private boolean testCondition(Command.Condition condition) {
      return condition.satisfied(this.context);
   }

   private CommandParser.PerCommandDetails perCommandDetails() {
      return Objects.requireNonNull(this.perCommandDetails);
   }

   private String currentArgument() {
      int argumentIndex = this.argIter.previousIndex();
      if (argumentIndex < 0) {
         throw new IllegalStateException("No argument has been asked for yet");
      } else {
         return (String)this.arguments.get(argumentIndex);
      }
   }

   private boolean hasNextArgument() {
      return this.argIter.hasNext();
   }

   private String nextArgument() {
      Preconditions.checkState(this.hasNextArgument(), "No next argument present, this call should be guarded with hasNextArgument");
      if (this.argIter.hasPrevious() && !this.justUnconsumed) {
         this.bindArgument();
      }

      this.justUnconsumed = false;
      String next = this.argIter.next();
      this.argBindings = ImmutableMap.builder();
      return next;
   }

   private void bindArgument() {
      ImmutableMap<CommandPart, Boolean> binding = this.argBindings.build();
      Preconditions.checkState(!binding.isEmpty() || this.currentArgument().equals("--"), "Argument never bound: %s", this.currentArgument());
      this.parseResult.addArgument(ArgBindingImpl.builder().input(this.currentArgument()).partsMap(binding).build());
   }

   private void unconsumeArgument() {
      Preconditions.checkState(this.argBindings.build().isEmpty(), "Argument already bound: %s", this.currentArgument());
      Preconditions.checkState(this.argIter.hasPrevious(), "Trying to unconsume nothing");
      this.argIter.previous();
      this.justUnconsumed = true;
   }

   private int remainingNonFlagArguments() {
      return (int)this.arguments.stream().skip(this.argIter.previousIndex()).filter(s -> !this.isFlag(s)).count();
   }

   private boolean hasNextPart() {
      return this.perCommandDetails().partIter.hasNext();
   }

   private ArgConsumingCommandPart nextPart() {
      Preconditions.checkState(this.hasNextPart(), "No next part, this call should be guarded with hasNextPart()");
      return this.perCommandDetails().partIter.next();
   }

   private void unconsumePart() {
      ListIterator<ArgConsumingCommandPart> partIter = this.perCommandDetails().partIter;
      Preconditions.checkState(partIter.hasPrevious(), "Trying to unconsume nothing");
      partIter.previous();
   }

   private void bind(CommandPart part, boolean exact) {
      this.argBindings.put(part, exact);
   }

   private void switchToCommand(Command subCommand) {
      if (this.perCommandDetails != null) {
         this.fillInDefaults();
      }

      this.parseResult.addCommand(subCommand);
      this.perCommandDetails = new CommandParser.PerCommandDetails(this.commandInfoCache.getInfo(subCommand));
      if (!this.testCondition(subCommand.getCondition())) {
         throw this.conditionFailed();
      }
   }

   private void fillInDefaults() {
      for (ArgAcceptingCommandPart part : this.perCommandDetails().defaultsNeeded) {
         this.addValueFull(part, v -> v.values(part.getDefaults()));
      }

      for (ArgAcceptingCommandFlag flag : this.perCommandDetails().argFlagsNeeded) {
         this.addValueFull(flag, v -> v.value(""));
      }
   }

   private void finalizeCommand() {
      CommandParser.PerCommandDetails details = this.perCommandDetails();
      if (details.remainingRequiredParts > 0) {
         Iterator<ArgConsumingCommandPart> requiredIter = Iterators.filter(details.partIter, CommandPart::isRequired);
         if (requiredIter.hasNext()) {
            ArgConsumingCommandPart missing = requiredIter.next();
            if (missing instanceof CommandArgument) {
               throw this.usageException(
                  TextComponent.builder("Missing argument for ").append(missing.getTextRepresentation()).append(TextComponent.of(".")).build()
               );
            }

            Preconditions.checkState(missing instanceof SubCommandPart, "Unknown part interface: %s", missing.getClass());
            throw this.usageException(
               TextComponent.of(
                  "No sub-command provided. Options: "
                     + ((SubCommandPart)missing).getCommands().stream().distinct().map(Command::getName).collect(Collectors.joining(", "))
               )
            );
         }
      }
   }

   CommandParseResult parse() {
      logParseStart();

      while (this.hasNextArgument()) {
         String token = this.nextArgument();
         log("Consuming argument `{}`", token);
         CommandParser.PerCommandDetails details = this.perCommandDetails();
         if (this.isFlag(token)) {
            if (token.equals("--")) {
               log("Encountered `--`, turning off flag matching.");
               details.canMatchFlags = false;
            } else {
               this.parseFlags(token.substring(1));
            }
         } else if (!this.parseRegularArgument(token)) {
            if (this.lastFailedOptional != null) {
               throw this.conversionFailedException(this.lastFailedOptional, token);
            }

            throw this.tooManyArgumentsException();
         }
      }

      log("Finished looking at arguments. Finalizing command.");
      this.finalizeCommand();
      this.buildParseResult();
      return this.getResult();
   }

   private boolean isFlag(String token) {
      if (token.length() <= 1 || !this.perCommandDetails().canMatchFlags) {
         return false;
      } else if (!token.startsWith("-")) {
         return false;
      } else {
         return token.equals("--") ? true : token.codePoints().skip(1L).allMatch(cp -> this.perCommandDetails().commandInfo.flags.containsKey((char)cp));
      }
   }

   private boolean parseSubCommand(SubCommandPart part, String token) {
      CommandInfo commandInfo = this.perCommandDetails().commandInfo;
      ImmutableMap<String, Command> subCommands = commandInfo.subCommandTable.row(part);
      Command sub = (Command)subCommands.get(token);
      if (sub == null) {
         return false;
      } else {
         this.bind(part, true);
         if (part.isRequired()) {
            this.perCommandDetails().remainingRequiredParts--;
         }

         this.switchToCommand(sub);
         return true;
      }
   }

   private TextComponent invalidSubCommandMessage(String token, ImmutableMap<String, Command> subCommands) {
      return TextComponent.builder()
         .append("Invalid sub-command '")
         .append(ColorConfig.mainText().wrap(token))
         .append("'. Options: ")
         .append(
            subCommands.values()
               .stream()
               .distinct()
               .map(Command::getName)
               .map(ColorConfig.mainText()::wrap)
               .collect(ComponentHelper.joiningTexts(TextComponent.empty(), TextComponent.of(", "), TextComponent.empty()))
         )
         .build();
   }

   private boolean parseRegularArgument(String token) {
      CommandParser.PerCommandDetails details = this.perCommandDetails();
      if (!this.hasNextPart()) {
         log("parseRegularArgument: no arguments to attempt matching");
      }

      CommandArgument lastFailedOptionalLocal = null;

      while (this.hasNextPart()) {
         ArgConsumingCommandPart nextArg = this.nextPart();
         if (nextArg instanceof SubCommandPart subCommandPart) {
            if (this.parseSubCommand(subCommandPart, token)) {
               return true;
            }

            if (nextArg.isRequired()) {
               throw this.usageException(this.invalidSubCommandMessage(token, details.commandInfo.subCommandTable.row(subCommandPart)));
            }
         } else {
            Preconditions.checkState(nextArg instanceof CommandArgument, "Unknown part interface: %s", nextArg.getClass());
            CommandArgument argPart = (CommandArgument)nextArg;
            if (nextArg.isRequired()) {
               CommandParser.AcceptInfo acceptInfo = this.getAcceptInfoFromTypeParsers(argPart, token);
               if (!acceptInfo.isAccepted()) {
                  throw this.conversionFailedException(argPart, token);
               }

               details.remainingRequiredParts--;
               this.addValueFull(nextArg, v -> v.values(this.consumeArguments(argPart, token, acceptInfo == CommandParser.AcceptInfo.ACCEPTED_EXACT)));
               return true;
            }

            if (details.commandInfo.subCommandTable.isEmpty()) {
               int remainingArguments = this.remainingNonFlagArguments();
               int diff = remainingArguments - details.remainingRequiredParts;
               if (diff < 0) {
                  throw this.notEnoughArgumentsException();
               }

               if (diff == 0) {
                  continue;
               }
            }

            CommandParser.AcceptInfo acceptInfo = this.getAcceptInfoFromTypeParsers(argPart, token);
            if (acceptInfo.isAccepted()) {
               details.defaultsNeeded.remove(nextArg);
               this.addValueFull(nextArg, v -> v.values(this.consumeArguments(argPart, token, acceptInfo == CommandParser.AcceptInfo.ACCEPTED_EXACT)));
               return true;
            }

            lastFailedOptionalLocal = argPart;
         }
      }

      this.lastFailedOptional = lastFailedOptionalLocal;
      return false;
   }

   private ImmutableList<String> consumeArguments(CommandArgument nextArg, String first, boolean exact) {
      Builder<String> result = ImmutableList.builder();
      this.bind(nextArg, exact);
      result.add(first);
      if (nextArg.isVariable()) {
         while (this.hasNextArgument()) {
            String next = this.nextArgument();
            CommandParser.AcceptInfo acceptInfo = this.getAcceptInfoFromTypeParsers(nextArg, next);
            if (!acceptInfo.isAccepted()) {
               this.unconsumeArgument();
               break;
            }

            this.bind(nextArg, acceptInfo == CommandParser.AcceptInfo.ACCEPTED_EXACT);
            result.add(next);
         }
      }

      return result.build();
   }

   private CommandParser.AcceptInfo getAcceptInfoFromTypeParsers(ArgAcceptingCommandPart part, String next) {
      ImmutableSet<Key<?>> types = part.getTypes();
      if (types.isEmpty()) {
         return CommandParser.AcceptInfo.ACCEPTED_EXACT;
      } else {
         boolean acceptedInexact = false;
         UnmodifiableIterator var5 = types.iterator();

         while (var5.hasNext()) {
            Key<?> type = (Key<?>)var5.next();
            Optional<? extends ArgumentConverter<?>> argumentConverter = this.converters.getConverter(type);
            if (!argumentConverter.isPresent()) {
               throw new IllegalStateException("No argument converter for " + type);
            }

            ConversionResult<?> result = argumentConverter.get().convert(next, this.context);
            if (result.isSuccessful()) {
               if (((SuccessfulConversion)result).isExactMatch()) {
                  return CommandParser.AcceptInfo.ACCEPTED_EXACT;
               }

               acceptedInexact = true;
            }
         }

         return acceptedInexact ? CommandParser.AcceptInfo.ACCEPTED_INEXACT : CommandParser.AcceptInfo.REJECTED;
      }
   }

   private void parseFlags(String flags) {
      for (int i = 0; i < flags.length(); i++) {
         char c = flags.charAt(i);
         CommandFlag flag = (CommandFlag)this.perCommandDetails().commandInfo.flags.get(c);
         if (flag == null) {
            this.buildParseResult();
            throw new NoSuchFlagException(this.getResult(), c);
         }

         if (this.seenFlags.contains(flag)) {
            throw this.usageException(TextComponent.builder("Flag ").append(flag.getTextRepresentation()).append(" has already been specified.").build());
         }

         if (flag instanceof ArgAcceptingCommandFlag) {
            if (i + 1 < flags.length()) {
               throw this.usageException(TextComponent.of("Argument-accepting flags must be at the end of combined flag groups."));
            }

            this.bind(flag, true);
            ArgAcceptingCommandFlag argPart = (ArgAcceptingCommandFlag)flag;
            if (!this.hasNextArgument()) {
               throw this.notEnoughArgumentsException();
            }

            String nextToken = this.nextArgument();
            CommandParser.AcceptInfo acceptInfo = this.getAcceptInfoFromTypeParsers(argPart, nextToken);
            if (!acceptInfo.isAccepted()) {
               throw this.conversionFailedException(argPart, nextToken);
            }

            this.addValueFull(flag, v -> v.value(nextToken));
            this.bind(flag, acceptInfo == CommandParser.AcceptInfo.ACCEPTED_EXACT);
            this.perCommandDetails().defaultsNeeded.remove(flag);
            this.perCommandDetails().argFlagsNeeded.remove(flag);
         } else {
            Preconditions.checkState(flag instanceof NoArgCommandFlag);
            this.bind(flag, true);
            this.parameters.addPresentPart(flag);
         }

         this.seenFlags.add(flag);
      }
   }

   private void addValueFull(CommandPart part, Consumer<CommandValueImpl.Builder> valueAdder) {
      this.parameters.addPresentPart(part);
      CommandValueImpl.Builder builder = CommandValueImpl.builder();
      valueAdder.accept(builder);
      this.parameters
         .addValue(part, builder.commandContextSupplier(this::getResult).partContext(part).injectedValues(this.context).manager(this.converters).build());
   }

   private static enum AcceptInfo {
      REJECTED,
      ACCEPTED_INEXACT,
      ACCEPTED_EXACT;

      boolean isAccepted() {
         return this != REJECTED;
      }
   }

   private static final class PerCommandDetails {
      final CommandInfo commandInfo;
      final Set<ArgAcceptingCommandPart> defaultsNeeded;
      final Set<ArgAcceptingCommandFlag> argFlagsNeeded;
      final ListIterator<ArgConsumingCommandPart> partIter;
      boolean canMatchFlags = true;
      int remainingRequiredParts;

      private PerCommandDetails(CommandInfo commandInfo) {
         this.commandInfo = commandInfo;
         this.defaultsNeeded = new HashSet<>(commandInfo.defaultProvided);
         this.argFlagsNeeded = StreamHelper.cast(commandInfo.flags.values().stream(), ArgAcceptingCommandFlag.class)
            .filter(arg -> !this.defaultsNeeded.contains(arg))
            .collect(Collectors.toCollection(HashSet::new));
         this.partIter = commandInfo.arguments.listIterator();
         this.remainingRequiredParts = commandInfo.requiredParts;
      }
   }
}
