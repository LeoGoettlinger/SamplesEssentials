package me.chancesd.pvpmanager.sdutils.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import me.chancesd.pvpmanager.sdutils.display.chat.ChatMenu;
import me.chancesd.pvpmanager.sdutils.display.chat.NavigationButtons;
import me.chancesd.pvpmanager.sdutils.display.chat.content.StaticContentProvider;
import me.chancesd.pvpmanager.sdutils.scheduler.ScheduleUtils;
import me.chancesd.pvpmanager.sdutils.utils.ChatUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

public abstract class BaseCommand implements TabExecutor {
   private static Function<CommandSender, String> noPermissionMessageProvider = sender -> "§cYou don't have permission to use this command.";
   private static Function<CommandSender, String> playerOnlyMessageProvider = sender -> "§cThis command can only be used by players.";
   private static final Map<ArgumentType, ArgumentLoader<?>> defaultLoaders = new HashMap<>();
   private BaseCommand parentCommand;
   private PluginCommand pluginCommand;
   private final List<String> aliases = new ArrayList<>();
   private final List<BaseCommand> subCommands = new ArrayList<>();
   private final Set<String> permissions = new HashSet<>();
   private final List<ArgumentInfo> argumentInfos = new ArrayList<>();
   private String commandDisplayName;
   private String description = "";
   private String usage = "";
   private boolean playerOnly = false;

   protected BaseCommand() {
      this.commandDisplayName = this.getClass().getSimpleName() + " Help";
   }

   protected BaseCommand(PluginCommand pluginCommand) {
      this();
      this.pluginCommand = pluginCommand;
      this.alias(pluginCommand.getAliases().toArray(new String[0]));
   }

   public static void setGlobalDefaults(
      Function<CommandSender, String> noPermission, Function<CommandSender, String> playerOnly, UnaryOperator<String> playerNotFound
   ) {
      noPermissionMessageProvider = noPermission;
      playerOnlyMessageProvider = playerOnly;
      ArgumentInfo.setPlayerNotFoundMessageProvider(playerNotFound);
   }

   public static void registerDefaultLoader(ArgumentType type, ArgumentLoader<?> loader) {
      defaultLoaders.put(type, loader);
   }

   static ArgumentLoader<?> getDefaultLoader(ArgumentType type) {
      return defaultLoaders.get(type);
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (this.playerOnly && !(sender instanceof Player)) {
         sender.sendMessage(playerOnlyMessageProvider.apply(sender));
         return true;
      } else if (!this.hasPermission(sender)) {
         sender.sendMessage(noPermissionMessageProvider.apply(sender));
         return true;
      } else {
         if (args.length > 0) {
            if (args[0].equalsIgnoreCase("help")) {
               int page = 1;
               if (args.length > 1) {
                  try {
                     page = Integer.parseInt(args[1]);
                     if (page < 1) {
                        page = 1;
                     }
                  } catch (NumberFormatException var14) {
                     sender.sendMessage("§cInvalid page number!");
                     return true;
                  }
               }

               this.showHelpMenu(sender, page);
               return true;
            }

            for (BaseCommand subCommand : this.subCommands) {
               if (subCommand.isAlias(args[0])) {
                  String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
                  return subCommand.onCommand(sender, command, args[0], subArgs);
               }
            }
         }

         List<CommandArgument> parsedArgs = new ArrayList<>();
         Set<String> userProvidedArgs = new HashSet<>();

         for (int i = 0; i < Math.min(args.length, this.argumentInfos.size()); i++) {
            userProvidedArgs.add(this.argumentInfos.get(i).getName());
         }

         for (int i = 0; i < this.argumentInfos.size(); i++) {
            ArgumentInfo argInfo = this.argumentInfos.get(i);
            if (i < args.length) {
               if (argInfo.hasPermission() && !sender.hasPermission(argInfo.getPermission())) {
                  sender.sendMessage(noPermissionMessageProvider.apply(sender));
                  return true;
               }

               if (argInfo.getType() == ArgumentType.STRING_ARRAY) {
                  String joinedValue = String.join(" ", Arrays.copyOfRange(args, i, args.length));
                  parsedArgs.add(new CommandArgument(argInfo.getName(), joinedValue));
                  break;
               }

               CommandArgument argument = new CommandArgument(argInfo.getName(), args[i]);
               if (!argInfo.isValid(argument)) {
                  sender.sendMessage(ChatUtils.colorize(argInfo.getValidationErrorMessage(argument)));
                  return true;
               }

               parsedArgs.add(argument);
            } else {
               if (argInfo.isRequired()) {
                  sender.sendMessage("§cMissing required argument: §e" + argInfo.getName());
                  if (!this.usage.isEmpty()) {
                     sender.sendMessage("§7Usage: §b" + this.usage);
                  }

                  return true;
               }

               if (argInfo.hasDependency()) {
                  if (userProvidedArgs.contains(argInfo.getDependsOn()) && argInfo.getDefaultValue() != null) {
                     parsedArgs.add(new CommandArgument(argInfo.getName(), argInfo.getDefaultValue()));
                  }
               } else if (argInfo.getDefaultValue() != null) {
                  parsedArgs.add(new CommandArgument(argInfo.getName(), argInfo.getDefaultValue()));
               }
            }
         }

         List<CompletableFuture<Void>> loaderFutures = new ArrayList<>();

         for (CommandArgument arg : parsedArgs) {
            ArgumentInfo argInfo = this.getArgumentInfo(arg.getName());
            if (argInfo != null && argInfo.hasLoader()) {
               ArgumentLoader<Object> loader = (ArgumentLoader<Object>)argInfo.getLoader();
               Optional<?> context = loader.validateAndGetContext(arg.getValue());
               if (context.isEmpty()) {
                  sender.sendMessage(loader.getValidationError(arg.getValue()));
                  return true;
               }

               CompletableFuture<Void> future = loader.loadWithContext(arg.getValue(), context.get())
                  .thenAccept(arg::setLoadedValue)
                  .exceptionally(throwable -> {
                     arg.setLoadError(throwable);
                     return null;
                  });
               loaderFutures.add(future);
            }
         }

         if (!loaderFutures.isEmpty()) {
            CompletableFuture<Void> allLoaders = CompletableFuture.allOf(loaderFutures.toArray(new CompletableFuture[0]));
            ScheduleUtils.thenRunSync(allLoaders, () -> {
               for (CommandArgument argx : parsedArgs) {
                  if (argx.hasLoadError()) {
                     sender.sendMessage("§cError loading argument '§e" + argx.getName() + "§c': " + argx.getLoadError().getMessage());
                     return;
                  }
               }

               this.execute(sender, label, parsedArgs);
            });
         } else {
            this.execute(sender, label, parsedArgs);
         }

         return true;
      }
   }

   private ArgumentInfo getArgumentInfo(String name) {
      for (ArgumentInfo info : this.argumentInfos) {
         if (info.getName().equals(name)) {
            return info;
         }
      }

      return null;
   }

   public abstract void execute(CommandSender var1, String var2, List<CommandArgument> var3);

   public boolean hasPermission(CommandSender sender) {
      if (!this.permissions.isEmpty()) {
         return this.permissions.stream().anyMatch(sender::hasPermission);
      } else {
         return this.parentCommand != null ? this.parentCommand.hasPermission(sender) : true;
      }
   }

   private boolean isAlias(String input) {
      return this.aliases.stream().anyMatch(alias -> alias.equalsIgnoreCase(input));
   }

   public BaseCommand alias(String... aliasNames) {
      this.aliases.addAll(Arrays.asList(aliasNames));
      return this;
   }

   public BaseCommand permission(String... permissionNodes) {
      this.permissions.addAll(Arrays.asList(permissionNodes));
      return this;
   }

   public BaseCommand subCommand(String name, BaseCommand subCommand) {
      subCommand.parentCommand = this;
      subCommand.aliases.add(name);
      this.subCommands.add(subCommand);
      return this;
   }

   public BaseCommand description(String descriptionText) {
      this.description = descriptionText;
      return this;
   }

   public BaseCommand usage(String usageText) {
      this.usage = usageText;
      return this;
   }

   public BaseCommand displayName(String displayNameText) {
      this.commandDisplayName = displayNameText;
      return this;
   }

   public BaseCommand playerOnly() {
      this.playerOnly = true;
      return this;
   }

   public ArgumentInfo argument(String name, ArgumentType type) {
      return new ArgumentInfo(name, type, this);
   }

   public ArgumentInfo argument(String name, ArgumentType type, ArgumentLoader<?> loader) {
      return new ArgumentInfo(name, type, loader, this);
   }

   public void addArgumentInfo(ArgumentInfo argumentInfo) {
      this.argumentInfos.add(argumentInfo);
   }

   public CommandArgument getArgument(List<CommandArgument> args, String name) {
      for (CommandArgument arg : args) {
         if (name.equals(arg.getName())) {
            return arg;
         }
      }

      return null;
   }

   public boolean hasArgument(List<CommandArgument> args, String name) {
      return this.getArgument(args, name) != null;
   }

   public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
      if (args.length > 1) {
         if (args[0].equalsIgnoreCase("help") && args.length == 2) {
            return ChatUtils.getMatchingEntries(args[1], Arrays.asList("1", "2", "3", "4", "5"));
         }

         for (BaseCommand subCommand : this.subCommands) {
            if (subCommand.isAlias(args[0]) && subCommand.hasPermission(sender)) {
               String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
               return subCommand.onTabComplete(sender, command, args[0], subArgs);
            }
         }
      }

      if (args.length == 1 && !this.subCommands.isEmpty()) {
         List<String> subCommandAliases = this.subCommands
            .stream()
            .filter(sub -> sub.hasPermission(sender))
            .flatMap(sub -> sub.aliases.stream())
            .collect(Collectors.toList());
         subCommandAliases.add("help");
         return ChatUtils.getMatchingEntries(args[0], subCommandAliases);
      } else {
         int argIndex = args.length - 1;
         if (argIndex < this.argumentInfos.size()) {
            ArgumentInfo argInfo = this.argumentInfos.get(argIndex);
            if (!argInfo.hasPermission() || sender.hasPermission(argInfo.getPermission())) {
               return argInfo.getTabCompletions(args[argIndex]);
            }
         }

         return new ArrayList<>();
      }
   }

   public String getDescription() {
      return this.description;
   }

   public String getUsage() {
      return this.usage;
   }

   public List<String> getAliases() {
      return new ArrayList<>(this.aliases);
   }

   public List<BaseCommand> getSubCommands() {
      return new ArrayList<>(this.subCommands);
   }

   public Set<String> getPermissions() {
      return new HashSet<>(this.permissions);
   }

   public boolean isPlayerOnly() {
      return this.playerOnly;
   }

   public ChatMenu generateHelpMenu(CommandSender sender) {
      StaticContentProvider contentProvider = new StaticContentProvider();
      if (!this.description.isEmpty()) {
         contentProvider.addLine("#FFEB3B&l► " + this.description, null, null);
         contentProvider.addLine("", null, null);
      }

      if (!this.usage.isEmpty()) {
         contentProvider.addLine("#9E9E9E&lUsage: &8" + this.usage, null, null);
         contentProvider.addLine("", null, null);
      }

      List<BaseCommand> availableSubCommands = this.subCommands.stream().filter(subCmdx -> subCmdx.hasPermission(sender)).collect(Collectors.toList());
      String mainCommandName = this.aliases.isEmpty() ? this.pluginCommand.getName().toLowerCase() : this.aliases.get(0);
      if (!availableSubCommands.isEmpty()) {
         contentProvider.addLine("#4CAF50&l► Available Commands:", null, null);

         for (BaseCommand subCmd : availableSubCommands) {
            String subCommandName = subCmd.aliases.isEmpty() ? "unknown" : subCmd.aliases.get(0);
            String subDescription = subCmd.getDescription().isEmpty() ? "No description available" : subCmd.getDescription();
            String subUsage = subCmd.usage.isEmpty() ? "" : subCmd.usage;
            String commandLine = "  #4CAF50/" + mainCommandName + " " + subCommandName + " #9E9E9E- &f" + subDescription;
            String hoverText = this.buildSubcommandHoverText(subDescription, subUsage, subCmd.getPermissions());
            String suggestCommand = "/" + mainCommandName + " " + subCommandName;
            contentProvider.addLine(commandLine, suggestCommand, hoverText);
         }
      } else {
         contentProvider.addLine("#FF5722No subcommands available or no permissions.", null, null);
      }

      return ChatMenu.builder()
         .header("#607D8B&l╔════════ #FFEB3B&l" + this.commandDisplayName + " #9E9E9E(Page {page}/{total}) #607D8B&l════════╗")
         .footer("#607D8B&l╚══════════════════════════════╝")
         .linesPerPage(14)
         .contentProvider(contentProvider)
         .navigation(
            NavigationButtons.builder()
               .navigationPrefix("/" + mainCommandName + " help")
               .previousText("#9E9E9E[#FF5722« Previous Page#9E9E9E]")
               .nextText("#9E9E9E[#FF5722Next Page »#9E9E9E]")
               .build()
         )
         .build();
   }

   public void showHelpMenu(CommandSender sender, int page) {
      this.generateHelpMenu(sender).show(sender, page);
   }

   public void showHelpMenu(CommandSender sender) {
      this.showHelpMenu(sender, 1);
   }

   private String buildSubcommandHoverText(String description, String usage, Set<String> permissions) {
      StringBuilder hover = new StringBuilder();
      hover.append("#9E9E9EClick to see command usage\n");
      hover.append("#9E9E9E&o").append(description);
      boolean hasUsage = !usage.isEmpty();
      boolean hasPermissions = !permissions.isEmpty();
      if (hasUsage || hasPermissions) {
         hover.append("\n");
      }

      if (hasUsage) {
         hover.append("\n#4CAF50&lUsage: &8").append(usage);
      }

      if (hasPermissions) {
         hover.append("\n#FF5722&lRequires: #c98d81").append(String.join(", ", permissions));
      }

      return hover.toString();
   }
}
