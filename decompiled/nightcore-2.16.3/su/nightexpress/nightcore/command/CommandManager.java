package su.nightexpress.nightcore.command;

import java.util.HashSet;
import java.util.Set;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.NightPlugin;
import su.nightexpress.nightcore.command.api.NightPluginCommand;
import su.nightexpress.nightcore.command.base.HelpSubCommand;
import su.nightexpress.nightcore.command.experimental.ImprovedCommands;
import su.nightexpress.nightcore.command.experimental.RootCommand;
import su.nightexpress.nightcore.command.experimental.ServerCommand;
import su.nightexpress.nightcore.command.experimental.node.ChainedNode;
import su.nightexpress.nightcore.command.impl.BaseCommand;
import su.nightexpress.nightcore.manager.SimpleManager;
import su.nightexpress.nightcore.util.CommandUtil;
import su.nightexpress.nightcore.util.Lists;

@Deprecated
public class CommandManager extends SimpleManager<NightPlugin> {
   private final Set<NightPluginCommand> commands = new HashSet<>();
   private final Set<ServerCommand> serverCommands = new HashSet<>();
   private BaseCommand mainCommand;
   private RootCommand<NightPlugin, ChainedNode> rootCommand;

   public CommandManager(@NonNull NightPlugin plugin) {
      super(plugin);
   }

   @Override
   public void onLoad() {
      String[] aliases = this.plugin.getCommandAliases();
      if (aliases != null && aliases.length != 0) {
         if (this.plugin instanceof ImprovedCommands) {
            this.rootCommand = RootCommand.chained(this.plugin, aliases, builder -> builder.localized(this.plugin.getNameLocalized()));
            this.registerCommand(this.rootCommand);
         } else {
            this.mainCommand = new BaseCommand(this.plugin);
            this.mainCommand.addDefaultCommand(new HelpSubCommand(this.plugin));
            this.registerCommand(this.mainCommand);
         }
      } else {
         this.plugin.error("Could not register plugin commands!");
      }
   }

   @Override
   public void onShutdown() {
      this.serverCommands.forEach(ServerCommand::unregister);
      this.serverCommands.clear();

      for (NightPluginCommand command : new HashSet<>(this.getCommands())) {
         this.unregisterCommand(command);
         command.getChildrens().clear();
      }

      this.getCommands().clear();
   }

   @Deprecated
   @NonNull
   public Set<NightPluginCommand> getCommands() {
      return this.commands;
   }

   @NonNull
   public Set<ServerCommand> getServerCommands() {
      return this.serverCommands;
   }

   @Deprecated
   @NonNull
   public BaseCommand getMainCommand() {
      return this.mainCommand;
   }

   @NonNull
   public RootCommand<NightPlugin, ChainedNode> getRootCommand() {
      return this.rootCommand;
   }

   @Nullable
   public NightPluginCommand getCommand(@NonNull String alias) {
      return this.getCommands().stream().filter(command -> Lists.contains(command.getAliases(), alias)).findFirst().orElse(null);
   }

   @Nullable
   public ServerCommand getServerCommand(@NonNull String alias) {
      return this.serverCommands
         .stream()
         .filter(command -> command.getNode().getName().equalsIgnoreCase(alias) || Lists.contains(command.getNode().getAliases(), alias))
         .findFirst()
         .orElse(null);
   }

   @Deprecated
   public void registerCommand(@NonNull NightPluginCommand command) {
      if (this.commands.add(command)) {
         CommandUtil.register(this.plugin, command);
      }
   }

   public boolean unregisterCommand(@NonNull String alias) {
      NightPluginCommand command = this.getCommand(alias);
      return command != null ? this.unregisterCommand(command) : false;
   }

   @Deprecated
   public boolean unregisterCommand(@NonNull NightPluginCommand command) {
      return this.commands.remove(command) ? CommandUtil.unregister(command.getAliases()[0]) : false;
   }

   public void registerCommand(@NonNull ServerCommand command) {
      if (!this.serverCommands.contains(command) && command.register()) {
         this.serverCommands.add(command);
      }
   }

   public boolean unregisterServerCommand(@NonNull String alias) {
      ServerCommand command = this.getServerCommand(alias);
      return command != null ? this.unregisterCommand(command) : false;
   }

   public boolean unregisterCommand(@NonNull ServerCommand command) {
      return this.serverCommands.remove(command) && command.unregister();
   }
}
