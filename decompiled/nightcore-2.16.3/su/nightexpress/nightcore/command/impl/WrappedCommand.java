package su.nightexpress.nightcore.command.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.command.api.NightPluginCommand;
import su.nightexpress.nightcore.util.text.night.NightMessage;

@Deprecated
public class WrappedCommand extends Command implements PluginIdentifiableCommand {
   protected final Plugin plugin;
   protected final CommandExecutor executor;
   protected final TabCompleter tabCompleter;

   @Deprecated
   public WrappedCommand(@NonNull Plugin plugin, @NonNull NightPluginCommand command) {
      this(plugin, command, command, command.getAliases(), command.getDescription(), command.getUsage(), command.getPermission());
   }

   public WrappedCommand(
      @NonNull Plugin plugin,
      @NonNull CommandExecutor executor,
      @NonNull TabCompleter tabCompleter,
      String[] aliases,
      @NonNull String description,
      @NonNull String usage,
      @Nullable String permission
   ) {
      this(plugin, executor, tabCompleter, aliases[0], aliases, description, usage, permission);
   }

   public WrappedCommand(
      @NonNull Plugin plugin,
      @NonNull CommandExecutor executor,
      @NonNull TabCompleter tabCompleter,
      @NonNull String name,
      String[] aliases,
      @NonNull String description,
      @NonNull String usage,
      @Nullable String permission
   ) {
      super(name, NightMessage.stripTags(description), NightMessage.stripTags(usage), Arrays.asList(aliases));
      this.plugin = plugin;
      this.executor = executor;
      this.tabCompleter = tabCompleter;
      this.setPermission(permission);
   }

   @NonNull
   public Plugin getPlugin() {
      return this.plugin;
   }

   public boolean execute(@NonNull CommandSender sender, @NonNull String label, String[] args) {
      return this.executor.onCommand(sender, this, label, args);
   }

   @NonNull
   public List<String> tabComplete(@NonNull CommandSender sender, @NonNull String alias, String[] args) {
      List<String> list = this.tabCompleter.onTabComplete(sender, this, alias, args);
      return list == null ? Collections.emptyList() : list;
   }
}
