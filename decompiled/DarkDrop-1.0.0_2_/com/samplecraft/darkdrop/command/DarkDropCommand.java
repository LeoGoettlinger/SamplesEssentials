package com.samplecraft.darkdrop.command;

import com.samplecraft.darkdrop.DarkDrop;
import com.samplecraft.darkdrop.gui.SettingsGUI;
import com.samplecraft.darkdrop.model.DropSettings;
import com.samplecraft.darkdrop.model.DropTarget;
import com.samplecraft.darkdrop.util.MessageUtil;
import com.samplecraft.darkdrop.util.PositionUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class DarkDropCommand implements CommandExecutor, TabCompleter {
   private final DarkDrop plugin;

   public DarkDropCommand(DarkDrop plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
      if (args.length == 0) {
         this.sendHelp(sender);
         return true;
      } else {
         String sub = args[0].toLowerCase(Locale.ROOT);
         switch (sub) {
            case "reload":
               if (!sender.hasPermission("darkdrop.admin")) {
                  sender.sendMessage(this.plugin.getConfigManager().component("no-permission"));
                  return true;
               }

               this.plugin.getConfigManager().load();
               sender.sendMessage(this.plugin.getConfigManager().component("reload-success"));
               return true;
            case "spawn":
               this.handleSpawn(sender, Arrays.copyOfRange(args, 1, args.length));
               break;
            case "help":
               this.sendHelp(sender);
               break;
            default:
               this.sendHelp(sender);
         }

         return true;
      }
   }

   private void handleSpawn(CommandSender sender, String[] subArgs) {
      if (!(sender instanceof Player player)) {
         sender.sendMessage(this.plugin.getConfigManager().component("player-only"));
      } else if (!sender.hasPermission("darkdrop.use")) {
         sender.sendMessage(this.plugin.getConfigManager().component("no-permission"));
      } else if (subArgs.length == 0) {
         SettingsGUI.Context context = SettingsGUI.Context.player(player);
         SettingsGUI gui = new SettingsGUI(this.plugin, player, context);
         gui.open();
      } else if (subArgs[0].equalsIgnoreCase("random")) {
         boolean openMenu = subArgs.length > 1 && subArgs[1].equalsIgnoreCase("menu");
         if (openMenu) {
            SettingsGUI.Context context = SettingsGUI.Context.random(player.getLocation());
            SettingsGUI gui = new SettingsGUI(this.plugin, player, context);
            gui.open();
         } else {
            this.startWithoutMenu(player, SettingsGUI.Context.random(player.getLocation()));
         }
      } else if (subArgs.length < 3) {
         sender.sendMessage(this.plugin.getConfigManager().component("invalid-coordinates"));
      } else {
         Double x = PositionUtil.parseCoordinate(subArgs[0], player.getLocation().getX());
         Double y = PositionUtil.parseCoordinate(subArgs[1], player.getLocation().getY());
         Double z = PositionUtil.parseCoordinate(subArgs[2], player.getLocation().getZ());
         if (x != null && y != null && z != null) {
            boolean openMenu = subArgs.length >= 4 && subArgs[3].equalsIgnoreCase("menu");
            SettingsGUI.Context context = SettingsGUI.Context.coords(x, y, z, player.getWorld().getUID());
            if (openMenu) {
               SettingsGUI gui = new SettingsGUI(this.plugin, player, context);
               gui.open();
            } else {
               this.startWithoutMenu(player, context);
            }
         } else {
            String bad = x == null ? subArgs[0] : (y == null ? subArgs[1] : subArgs[2]);
            sender.sendMessage(this.plugin.getConfigManager().component("invalid-number", "value", bad));
         }
      }
   }

   private void startWithoutMenu(Player player, SettingsGUI.Context context) {
      DropSettings settings = this.plugin.getSettingsManager().getOrCreate(player.getUniqueId());
      DropTarget target = context.toTarget(player);
      if (target == null) {
         player.sendMessage(this.plugin.getConfigManager().component("random-failed"));
      } else {
         this.plugin.getDropManager().startDrop(player, settings, target);
      }
   }

   private void sendHelp(CommandSender sender) {
      sender.sendMessage(MessageUtil.toComponent(this.plugin.getConfigManager().getPrefix() + "&5DarkDrop &7Befehle:"));
      sender.sendMessage(MessageUtil.toComponent(this.plugin.getConfigManager().getPrefix() + "&7/darkdrop spawn &8- &fMenue mit Spieler-Position"));
      sender.sendMessage(MessageUtil.toComponent(this.plugin.getConfigManager().getPrefix() + "&7/darkdrop spawn random &8- &fZufaellige Position"));
      sender.sendMessage(MessageUtil.toComponent(this.plugin.getConfigManager().getPrefix() + "&7/darkdrop spawn random menu &8- &fZufaellig + Menue"));
      sender.sendMessage(MessageUtil.toComponent(this.plugin.getConfigManager().getPrefix() + "&7/darkdrop spawn <x> <y> <z> &8- &fExakte/relative Position"));
      sender.sendMessage(MessageUtil.toComponent(this.plugin.getConfigManager().getPrefix() + "&7/darkdrop spawn <x> <y> <z> menu &8- &fMit Menue"));
      if (sender.hasPermission("darkdrop.admin")) {
         sender.sendMessage(MessageUtil.toComponent(this.plugin.getConfigManager().getPrefix() + "&7/darkdrop reload &8- &fKonfiguration neu laden"));
      }
   }

   public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
      List<String> result = new ArrayList<>();
      if (args.length == 1) {
         result.add("spawn");
         if (sender.hasPermission("darkdrop.admin")) {
            result.add("reload");
         }

         result.add("help");
         return this.filterStartsWith(result, args[0]);
      } else if (args.length == 2 && args[0].equalsIgnoreCase("spawn")) {
         result.add("random");
         result.add("~");
         result.add("~ ~ ~");
         return this.filterStartsWith(result, args[1]);
      } else {
         if (args.length >= 2 && args[0].equalsIgnoreCase("spawn")) {
            if (args.length == 4) {
               result.add("menu");
               return this.filterStartsWith(result, args[3]);
            }

            if (args.length == 3 && args[1].equalsIgnoreCase("random")) {
               result.add("menu");
               return this.filterStartsWith(result, args[2]);
            }

            if (args.length == 2 && args[1].equalsIgnoreCase("random")) {
               result.add("menu");
               return this.filterStartsWith(result, args[1]);
            }
         }

         return result;
      }
   }

   private List<String> filterStartsWith(List<String> options, String prefix) {
      List<String> filtered = new ArrayList<>();

      for (String s : options) {
         if (s.toLowerCase(Locale.ROOT).startsWith(prefix.toLowerCase(Locale.ROOT))) {
            filtered.add(s);
         }
      }

      return filtered;
   }
}
