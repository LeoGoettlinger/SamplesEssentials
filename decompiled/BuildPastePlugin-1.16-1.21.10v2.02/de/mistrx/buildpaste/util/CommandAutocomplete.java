package de.mistrx.buildpaste.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class CommandAutocomplete implements TabCompleter {
   public List<String> onTabComplete(CommandSender sender, Command cde, String arg, String[] args) {
      String command = cde.getName();
      List<String> list = new ArrayList<>();
      if (command.equals("buildpaste")) {
         if (!sender.isOp()) {
            return list;
         } else {
            String[] permissions = new String[]{"pastePermission", "uploadPermission", "constructPermission", "allPermissions"};
            List<String> permissionsList = Arrays.asList(permissions);
            if (args.length == 1) {
               list.add("allowall");
               list.add("disallowall");

               for (int i = 0; i < permissions.length; i++) {
                  list.add(permissions[i]);
               }
            }

            if (args.length == 2 && permissionsList.contains(args[0])) {
               for (Player player2 : Bukkit.getServer().getOnlinePlayers()) {
                  list.add(player2.getName());
               }
            }

            if (args.length == 3 && permissionsList.contains(args[0])) {
               list.add("allow");
               list.add("disallow");
            }

            return list;
         }
      } else if (command.equals("paste")) {
         list.add("dontplaceair");
         return list;
      } else {
         return null;
      }
   }
}
