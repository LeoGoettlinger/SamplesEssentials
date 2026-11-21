package org.sethomegui.Commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HomeAdminTabCompleter implements TabCompleter {
   @Nullable
   public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
      if (!sender.hasPermission("sethome.admin")) {
         return Collections.emptyList();
      } else {
         List<String> completions = new ArrayList<>();
         if (args.length == 1) {
            List<String> subCommands = new ArrayList<>();
            subCommands.add("gui");
            subCommands.add("view");
            subCommands.add("import");
            subCommands.add("reload");
            subCommands.add("version");
            StringUtil.copyPartialMatches(args[0], subCommands, completions);
            Collections.sort(completions);
            return completions;
         } else {
            if (args.length == 2) {
               if (args[0].equalsIgnoreCase("import")) {
                  List<String> sources = new ArrayList<>();
                  sources.add("Essentials");
                  sources.add("HuskHomes");
                  StringUtil.copyPartialMatches(args[1], sources, completions);
                  Collections.sort(completions);
                  return completions;
               }

               if (args[0].equalsIgnoreCase("view")) {
                  List<String> playerNames = new ArrayList<>();

                  for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                     playerNames.add(onlinePlayer.getName());
                  }

                  StringUtil.copyPartialMatches(args[1], playerNames, completions);
                  Collections.sort(completions);
                  return completions;
               }
            }

            return Collections.emptyList();
         }
      }
   }
}
