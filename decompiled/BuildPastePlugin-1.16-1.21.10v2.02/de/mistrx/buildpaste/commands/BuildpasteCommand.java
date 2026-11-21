package de.mistrx.buildpaste.commands;

import de.mistrx.buildpaste.util.Functions;
import de.mistrx.buildpaste.util.Variables;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.hover.content.Content;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BuildpasteCommand implements CommandExecutor {
   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      boolean infoFeedback = false;
      if (args.length == 1) {
         if (sender.isOp()) {
            if (args[0].equals("allowall")) {
               Functions.everyoneAllowed = true;
               sender.sendMessage("Allowed pasting for everyone");
               return false;
            }

            if (args[0].equals("disallowall")) {
               Functions.everyoneAllowed = false;
               sender.sendMessage("Disallowed pasting for everyone");
               return false;
            }

            infoFeedback = true;
         } else {
            infoFeedback = true;
         }
      } else if (args.length == 3) {
         if (sender.isOp()) {
            String removePlayerPermissionMessage = "Successfully removed permission(s) from player";
            String addPlayerPermissionMessage = "Successfully added permission(s) to player";
            String playerNotFoundMessage = "We couldn't find the player you are looking for. You can only add and remove permissions from online players";
            String arg1 = args[0];
            String arg2 = args[1];
            String arg3 = args[2];
            if (arg1.equals("pastePermission")) {
               Player permissionPlayer = Bukkit.getPlayer(arg2);
               if (permissionPlayer == null) {
                  sender.sendMessage(playerNotFoundMessage);
                  return false;
               }

               if (arg3.equals("allow")) {
                  if (!Variables.permissions_paste.contains(permissionPlayer)) {
                     Variables.permissions_paste.add(permissionPlayer);
                     sender.sendMessage(addPlayerPermissionMessage);
                     return false;
                  }
               } else if (arg3.equals("disallow") && Variables.permissions_paste.contains(permissionPlayer)) {
                  Variables.permissions_paste.remove(permissionPlayer);
                  sender.sendMessage(removePlayerPermissionMessage);
                  return false;
               }
            }

            if (arg1.equals("uploadPermission")) {
               Player permissionPlayerx = Bukkit.getPlayer(arg2);
               if (permissionPlayerx == null) {
                  sender.sendMessage(playerNotFoundMessage);
                  return false;
               }

               if (arg3.equals("allow")) {
                  if (!Variables.permissions_upload.contains(permissionPlayerx)) {
                     Variables.permissions_upload.add(permissionPlayerx);
                     sender.sendMessage(addPlayerPermissionMessage);
                     return false;
                  }
               } else if (arg3.equals("disallow") && Variables.permissions_upload.contains(permissionPlayerx)) {
                  Variables.permissions_upload.remove(permissionPlayerx);
                  sender.sendMessage(removePlayerPermissionMessage);
                  return false;
               }
            }

            if (arg1.equals("constructPermission")) {
               Player permissionPlayerxx = Bukkit.getPlayer(arg2);
               if (permissionPlayerxx == null) {
                  sender.sendMessage(playerNotFoundMessage);
                  return false;
               }

               if (arg3.equals("allow")) {
                  if (!Variables.permissions_construct.contains(permissionPlayerxx)) {
                     Variables.permissions_construct.add(permissionPlayerxx);
                     sender.sendMessage(addPlayerPermissionMessage);
                     return false;
                  }
               } else if (arg3.equals("disallow") && Variables.permissions_construct.contains(permissionPlayerxx)) {
                  Variables.permissions_construct.remove(permissionPlayerxx);
                  sender.sendMessage(removePlayerPermissionMessage);
                  return false;
               }
            }

            if (arg1.equals("allPermissions")) {
               Player permissionPlayerxxx = Bukkit.getPlayer(arg2);
               if (permissionPlayerxxx == null) {
                  sender.sendMessage(playerNotFoundMessage);
                  return false;
               }

               if (arg3.equals("allow")) {
                  if (!Variables.permissions_construct.contains(permissionPlayerxxx)) {
                     Variables.permissions_construct.add(permissionPlayerxxx);
                  }

                  if (!Variables.permissions_paste.contains(permissionPlayerxxx)) {
                     Variables.permissions_paste.add(permissionPlayerxxx);
                  }

                  if (!Variables.permissions_upload.contains(permissionPlayerxxx)) {
                     Variables.permissions_upload.add(permissionPlayerxxx);
                  }

                  sender.sendMessage(addPlayerPermissionMessage);
                  return false;
               }

               if (arg3.equals("disallow")) {
                  if (Variables.permissions_construct.contains(permissionPlayerxxx)) {
                     Variables.permissions_construct.remove(permissionPlayerxxx);
                  }

                  if (Variables.permissions_upload.contains(permissionPlayerxxx)) {
                     Variables.permissions_upload.remove(permissionPlayerxxx);
                  }

                  if (Variables.permissions_paste.contains(permissionPlayerxxx)) {
                     Variables.permissions_paste.remove(permissionPlayerxxx);
                  }

                  sender.sendMessage(removePlayerPermissionMessage);
                  return false;
               }
            }
         }
      } else {
         infoFeedback = true;
      }

      if (infoFeedback) {
         if (sender instanceof Player) {
            Player player = (Player)sender;
            TextComponent message = new TextComponent("Buildpaste is a plugin which makes it easy to paste any type of build into your server!\n");
            TextComponent finishUpload = new TextComponent("Visit Buildpaste.net");
            finishUpload.setColor(ChatColor.AQUA);
            finishUpload.setClickEvent(new ClickEvent(Action.OPEN_URL, "https://buildpaste.net"));
            finishUpload.setHoverEvent(
               new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, new Content[]{new Text("Open buildpaste.net to start")})
            );
            TextComponent paste = new TextComponent("Paste an Example");
            paste.setColor(ChatColor.GREEN);
            paste.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/paste qRi3mpfl4inn4juICRJO"));
            paste.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, new Content[]{new Text("Paste an Example Build")}));
            message.addExtra("[");
            message.addExtra(finishUpload);
            message.addExtra("] [");
            message.addExtra(paste);
            message.addExtra("]");
            player.spigot().sendMessage(message);
         } else {
            sender.sendMessage("Buildpaste is a plugin which makes it easy to paste any type of build into your server");
         }
      }

      return false;
   }
}
