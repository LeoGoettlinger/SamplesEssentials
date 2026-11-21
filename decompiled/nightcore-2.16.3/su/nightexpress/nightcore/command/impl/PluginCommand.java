package su.nightexpress.nightcore.command.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.NightCorePlugin;
import su.nightexpress.nightcore.command.api.NightCommand;
import su.nightexpress.nightcore.command.api.NightPluginCommand;
import su.nightexpress.nightcore.util.Lists;

@Deprecated
public abstract class PluginCommand<P extends NightCorePlugin> extends AbstractCommand<P> implements NightPluginCommand {
   private Command backend;
   private NightCommand defaultCommand;

   public PluginCommand(@NonNull P plugin, String[] aliases) {
      this(plugin, aliases, (String)null);
   }

   public PluginCommand(@NonNull P plugin, String[] aliases, @Nullable Permission permission) {
      super(plugin, aliases, permission);
   }

   public PluginCommand(@NonNull P plugin, String[] aliases, @Nullable String permission) {
      super(plugin, aliases, permission);
   }

   @Override
   public void addDefaultCommand(@NonNull NightCommand command) {
      this.addChildren(command);
      this.defaultCommand = command;
   }

   @Nullable
   @Override
   public NightCommand getDefaultCommand() {
      return this.defaultCommand;
   }

   @Override
   public Command getBackend() {
      return this.backend;
   }

   @Override
   public void setBackend(@NonNull Command backend) {
      this.backend = backend;
   }

   @NonNull
   private NightCommand findChildren(String[] args) {
      NightCommand command = this;
      int childCount = 0;

      while (args.length > childCount) {
         NightCommand child = command.getChildren(args[childCount++]);
         if (child == null) {
            break;
         }

         command = child;
      }

      return command;
   }

   public boolean onCommand(@NonNull CommandSender sender, @NonNull Command cmd, @NonNull String label, String[] args) {
      NightCommand command = this.findChildren(args);
      if (command instanceof NightPluginCommand pluginCommand && pluginCommand.getDefaultCommand() != null) {
         command = pluginCommand.getDefaultCommand();
      }

      command.execute(sender, label, args);
      return true;
   }

   public List<String> onTabComplete(@NonNull CommandSender sender, @NonNull Command cmd, @NonNull String label, String[] args) {
      if (sender instanceof Player player && args.length != 0) {
         NightCommand command = this.findChildren(args);
         if (!command.hasPermission(sender)) {
            return Collections.emptyList();
         } else {
            List<String> list = new ArrayList<>();
            if (!command.getChildrens().isEmpty()) {
               command.getChildrens().stream().filter(child -> child.hasPermission(sender)).forEach(child -> list.addAll(Arrays.asList(child.getAliases())));
            } else {
               list.addAll(command.getTab(player, command.equals(this) ? args.length : args.length - 1, args));
            }

            return Lists.getSequentialMatches(list, args[args.length - 1]);
         }
      } else {
         return Collections.emptyList();
      }
   }
}
