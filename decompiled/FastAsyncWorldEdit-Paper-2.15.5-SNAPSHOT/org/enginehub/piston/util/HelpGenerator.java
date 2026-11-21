package org.enginehub.piston.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;
import com.google.common.collect.ImmutableList.Builder;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import org.enginehub.piston.ArgBinding;
import org.enginehub.piston.Command;
import org.enginehub.piston.CommandMetadata;
import org.enginehub.piston.CommandParameters;
import org.enginehub.piston.CommandParseResult;
import org.enginehub.piston.NoInputCommandParameters;
import org.enginehub.piston.config.ColorConfig;
import org.enginehub.piston.config.TextConfig;
import org.enginehub.piston.inject.InjectedValueAccess;
import org.enginehub.piston.part.ArgAcceptingCommandFlag;
import org.enginehub.piston.part.ArgAcceptingCommandPart;
import org.enginehub.piston.part.CommandArgument;
import org.enginehub.piston.part.CommandFlag;
import org.enginehub.piston.part.CommandPart;
import org.enginehub.piston.part.SubCommandPart;

public class HelpGenerator {
   private final CommandParseResult parseResult;

   public static HelpGenerator create(Iterable<Command> commands) {
      final ImmutableList<Command> executionPath = ImmutableList.copyOf(commands);
      return create(new CommandParseResult() {
         @Override
         public ImmutableList<Command> getExecutionPath() {
            return executionPath;
         }

         @Override
         public ImmutableList<ArgBinding> getBoundArguments() {
            return ImmutableList.of();
         }

         @Override
         public CommandParameters getParameters() {
            return NoInputCommandParameters.builder().injectedValues(InjectedValueAccess.EMPTY).build();
         }
      });
   }

   public static HelpGenerator create(CommandParseResult parseResult) {
      return new HelpGenerator(parseResult);
   }

   private HelpGenerator(CommandParseResult parseResult) {
      this.parseResult = parseResult;
   }

   public Component getFullName() {
      TextComponent.Builder usage = TextComponent.builder();
      String name = ((Command)this.parseResult.getExecutionPath().get(0)).getName();
      CommandMetadata metadata = this.parseResult.getParameters().getMetadata();
      if (metadata != null) {
         name = metadata.getCalledName();
      }

      usage.append(ColorConfig.mainText().wrap(TextConfig.commandPrefixValue(), TextComponent.of(name)));

      for (String input : this.parseResult.getOriginalArguments()) {
         usage.append(TextComponent.space());
         usage.append(ColorConfig.mainText().wrap(input));
      }

      return usage.build();
   }

   public Component getUsage() {
      TextComponent.Builder usage = TextComponent.builder().append(ColorConfig.mainText().wrap(TextConfig.commandPrefixValue()));
      Iterator<Command> iterator = this.parseResult.getExecutionPath().iterator();

      while (iterator.hasNext()) {
         Command command = iterator.next();
         String name = command.getName();
         if (command == this.parseResult.getExecutionPath().get(0)) {
            CommandMetadata metadata = this.parseResult.getParameters().getMetadata();
            if (metadata != null) {
               name = metadata.getCalledName();
            }
         }

         usage.append(ColorConfig.mainText().wrap(name));
         List<CommandPart> reducedParts;
         if (!iterator.hasNext()) {
            reducedParts = command.getParts();
         } else {
            Builder<CommandPart> parts = ImmutableList.builder();
            UnmodifiableIterator var7 = command.getParts().iterator();

            while (var7.hasNext()) {
               CommandPart part = (CommandPart)var7.next();
               if (part instanceof SubCommandPart) {
                  break;
               }

               parts.add(part);
            }

            reducedParts = parts.build();
         }

         if (!reducedParts.isEmpty()) {
            usage.append(TextComponent.space());
         }

         PartHelper.appendUsage(reducedParts.stream(), usage);
         if (iterator.hasNext()) {
            usage.append(TextComponent.space());
         }
      }

      return usage.build();
   }

   public Component getFullHelp() {
      Command primary = this.parseResult.getPrimaryCommand();
      Builder<Component> builder = ImmutableList.builder();
      builder.add(primary.getDescription());
      builder.add(TextComponent.of("\nUsage: "));
      builder.add(this.getUsage());
      this.appendArguments(builder);
      this.appendFlags(builder);
      primary.getFooter().ifPresent(footer -> builder.add(TextComponent.newline()).add(footer));
      return ColorConfig.helpText().wrap(builder.build());
   }

   private void appendArguments(Builder<Component> builder) {
      Command primary = this.parseResult.getPrimaryCommand();
      List<CommandArgument> args = primary.getParts()
         .stream()
         .filter(x -> x instanceof CommandArgument)
         .map(x -> (CommandArgument)x)
         .collect(Collectors.toList());
      if (args.size() > 0) {
         builder.add(TextComponent.newline());
         builder.add(TextComponent.of("Arguments:\n"));
         Iterator<CommandArgument> iterator = args.iterator();

         while (iterator.hasNext()) {
            CommandArgument arg = iterator.next();
            builder.add(TextComponent.of("  ")).add(arg.getTextRepresentation());
            this.addDefaultInfo(builder, arg);
            builder.add(TextComponent.of(": ")).add(arg.getDescription());
            if (iterator.hasNext()) {
               builder.add(TextComponent.newline());
            }
         }
      }
   }

   private void appendFlags(Builder<Component> builder) {
      Command primary = this.parseResult.getPrimaryCommand();
      List<CommandFlag> flags = primary.getParts().stream().filter(x -> x instanceof CommandFlag).map(x -> (CommandFlag)x).collect(Collectors.toList());
      if (flags.size() > 0) {
         builder.add(TextComponent.newline());
         builder.add(TextComponent.of("Flags:\n"));
         Iterator<CommandFlag> iterator = flags.iterator();

         while (iterator.hasNext()) {
            CommandFlag flag = iterator.next();
            builder.add(ColorConfig.mainText().wrap("  -" + flag.getName()));
            if (flag instanceof ArgAcceptingCommandFlag) {
               this.addDefaultInfo(builder, (ArgAcceptingCommandFlag)flag);
            }

            builder.add(TextComponent.of(": ")).add(flag.getDescription());
            if (iterator.hasNext()) {
               builder.add(TextComponent.newline());
            }
         }
      }
   }

   private void addDefaultInfo(Builder<Component> builder, ArgAcceptingCommandPart arg) {
      if (!arg.getDefaults().isEmpty()) {
         builder.add(TextComponent.of(" (defaults to "));
         String value;
         if (arg.getDefaults().size() == 1) {
            value = (String)arg.getDefaults().get(0);
            if (value.trim().isEmpty()) {
               value = "none";
            }
         } else {
            value = arg.getDefaults().stream().filter(s -> s.trim().length() > 0).collect(Collectors.joining(", ", "[", "]"));
         }

         builder.add(TextComponent.of(value));
         builder.add(TextComponent.of(")"));
      }
   }
}
