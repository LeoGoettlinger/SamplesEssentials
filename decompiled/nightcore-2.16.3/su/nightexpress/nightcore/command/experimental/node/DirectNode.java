package su.nightexpress.nightcore.command.experimental.node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.NightCorePlugin;
import su.nightexpress.nightcore.command.experimental.CommandContext;
import su.nightexpress.nightcore.command.experimental.TabContext;
import su.nightexpress.nightcore.command.experimental.argument.CommandArgument;
import su.nightexpress.nightcore.command.experimental.argument.ParsedArgument;
import su.nightexpress.nightcore.command.experimental.argument.ParsedArguments;
import su.nightexpress.nightcore.command.experimental.builder.DirectNodeBuilder;
import su.nightexpress.nightcore.command.experimental.flag.CommandFlag;
import su.nightexpress.nightcore.command.experimental.flag.ContentFlag;
import su.nightexpress.nightcore.core.CoreLang;
import su.nightexpress.nightcore.util.Lists;

@Deprecated
public class DirectNode extends CommandNode implements DirectExecutor {
   private final List<CommandArgument<?>> arguments;
   private final Map<String, CommandFlag> flags;
   private final DirectExecutor executor;
   private final int requiredArguments;

   public DirectNode(
      @NonNull NightCorePlugin plugin,
      @NonNull String name,
      String[] aliases,
      @NonNull String description,
      @Nullable String permission,
      boolean playerOnly,
      @NonNull List<CommandArgument<?>> arguments,
      @NonNull Map<String, CommandFlag> flags,
      @NonNull DirectExecutor executor
   ) {
      super(plugin, name, aliases, description, permission, playerOnly);
      this.arguments = Collections.unmodifiableList(arguments);
      this.flags = Collections.unmodifiableMap(flags);
      this.executor = executor;
      this.requiredArguments = (int)this.arguments.stream().filter(CommandArgument::isRequired).count();
   }

   @NonNull
   public static DirectNodeBuilder builder(@NonNull NightCorePlugin plugin, String... aliases) {
      return new DirectNodeBuilder(plugin, aliases);
   }

   @Override
   public boolean execute(@NonNull CommandContext context, @NonNull ParsedArguments arguments) {
      return this.executor.execute(context, arguments);
   }

   @Override
   protected boolean onRun(@NonNull CommandContext context) {
      CommandSender sender = context.getSender();
      String[] args = context.getArgs();
      int index = context.getArgumentIndex();
      ParsedArguments parsedArguments = new ParsedArguments();

      for (CommandArgument<?> argument : this.arguments) {
         if (index >= args.length) {
            break;
         }

         if (!argument.hasPermission(sender)) {
            context.errorPermission();
            return false;
         }

         String arg;
         if (argument.isComplex()) {
            arg = Stream.of(args).skip(index).collect(Collectors.joining(" "));
         } else {
            arg = args[index++];
         }

         ParsedArgument<?> parsedArgument = argument.parse(arg, context);
         if (parsedArgument == null) {
            context.send(argument.getFailureMessage(this.plugin), replacer -> replacer.replace("%value%", arg).replace("%name%", argument.getLocalized()));
            return false;
         }

         parsedArguments.add(argument, parsedArgument);
      }

      if (parsedArguments.getArgumentMap().size() < this.requiredArguments) {
         context.send(
            CoreLang.ERROR_COMMAND_USAGE.getMessage(this.plugin),
            replacer -> replacer.replace("%command_label%", this.getNameWithParents())
               .replace("%command_usage%", this.getUsage())
               .replace("%command_description%", this.getDescription())
         );
         return false;
      } else {
         if (!this.flags.isEmpty() && index < args.length) {
            for (int flagIndex = index; flagIndex < args.length; flagIndex++) {
               String argx = args[flagIndex];
               if (argx.charAt(0) == '-') {
                  int delimiterIndex = argx.indexOf(61);
                  boolean hasDelimiter = delimiterIndex != -1;
                  String flagName = (hasDelimiter ? argx.substring(0, delimiterIndex) : argx).substring(1);
                  CommandFlag flag = this.getFlag(flagName);
                  if (flag != null && !parsedArguments.hasFlag(flag) && flag.hasPermission(sender)) {
                     if (flag instanceof ContentFlag<?> contentFlag) {
                        if (hasDelimiter) {
                           String content = argx.substring(delimiterIndex + 1);
                           if (!content.isEmpty()) {
                              ParsedArgument<?> parsed = contentFlag.parse(content, context);
                              if (parsed == null) {
                                 context.send(
                                    CoreLang.ERROR_COMMAND_PARSE_FLAG.getMessage(this.plugin),
                                    replacer -> replacer.replace("%value%", content).replace("%name%", flag.getName())
                                 );
                              } else {
                                 parsedArguments.addFlag(flag, parsed);
                              }
                           }
                        }
                     } else {
                        parsedArguments.addFlag(flag, new ParsedArgument<>(true));
                     }
                  }
               }
            }
         }

         return this.execute(context, parsedArguments);
      }
   }

   @NonNull
   @Override
   public List<String> getTab(@NonNull TabContext context) {
      if (this.isPlayerOnly() && context.getPlayer() == null) {
         return Collections.emptyList();
      } else {
         int firstArgIndex = context.getLastCommandIndex();
         int tabLength = context.length();
         int argIndex = 0;
         List<String> samples = new ArrayList<>();

         for (int tabIndex = firstArgIndex; tabIndex < tabLength; tabIndex++) {
            String tabValue = context.getArg(tabIndex);
            boolean lastIndex = tabIndex == tabLength - 1;
            if (argIndex >= this.arguments.size()) {
               if (!this.arguments.isEmpty()) {
                  CommandArgument<?> lastArgument = this.arguments.getLast();
                  if (lastArgument.isComplex()) {
                     if (lastIndex) {
                        samples = this.getArgumentSamples(lastArgument, context);
                     }

                     context.appendArgumentCache(lastArgument, tabValue);
                  }
               }

               for (CommandFlag commandFlag : this.getFlags()) {
                  if (!context.hasCachedFlag(commandFlag) && commandFlag.hasPermission(context.getSender())) {
                     if (lastIndex) {
                        if (commandFlag instanceof ContentFlag<?> contentFlag) {
                           samples = Lists.newList(contentFlag.getSampled());
                        } else {
                           samples = Lists.newList(commandFlag.getPrefixed());
                        }
                     }

                     context.cacheFlag(commandFlag, tabValue);
                  }
               }
            } else {
               CommandArgument<?> argument = this.arguments.get(argIndex++);
               context.cacheArgument(argument, tabValue);
               if (lastIndex) {
                  samples = this.getArgumentSamples(argument, context);
               }
            }
         }

         return samples;
      }
   }

   private List<String> getArgumentSamples(@NonNull CommandArgument<?> argument, @NonNull TabContext context) {
      return !argument.hasPermission(context.getSender()) ? Collections.emptyList() : argument.getSamples(context);
   }

   @NonNull
   @Override
   public String getUsage() {
      StringBuilder labelBuilder = new StringBuilder();
      this.arguments.forEach(argument -> {
         if (!labelBuilder.isEmpty()) {
            labelBuilder.append(" ");
         }

         labelBuilder.append(argument.getLocalized());
      });
      StringBuilder flagBuilder = new StringBuilder();
      this.flags.values().forEach(commandFlag -> {
         if (!flagBuilder.isEmpty()) {
            flagBuilder.append(" ");
         }

         flagBuilder.append(commandFlag.getPrefixedFormatted());
      });
      if (!flagBuilder.isEmpty()) {
         labelBuilder.append(" ").append((CharSequence)flagBuilder);
      }

      return labelBuilder.toString();
   }

   @NonNull
   public List<CommandArgument<?>> getArguments() {
      return this.arguments;
   }

   @Nullable
   public CommandFlag getFlag(@NonNull String name) {
      return this.flags.get(name.toLowerCase());
   }

   @NonNull
   public Collection<CommandFlag> getFlags() {
      return this.flags.values();
   }
}
