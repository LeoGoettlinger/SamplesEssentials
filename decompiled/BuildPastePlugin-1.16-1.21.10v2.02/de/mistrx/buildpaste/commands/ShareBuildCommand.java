package de.mistrx.buildpaste.commands;

import de.mistrx.buildpaste.firebase.Firebase;
import de.mistrx.buildpaste.util.Functions;
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

public class ShareBuildCommand implements CommandExecutor {
   public boolean onCommand(CommandSender sender, Command command, String label, String[] args_) {
      if (sender instanceof Player) {
         Player player = (Player)sender;
         String uuid = player.getUniqueId().toString();
         String playerName = player.getName();
         if (!Functions.checkPermissions(player, "upload")) {
            return false;
         }

         String buildID;
         if (args_.length >= 1) {
            buildID = args_[0];
         } else {
            buildID = Firebase.getSelectedBuildingID(uuid);
         }

         TextComponent message = new TextComponent(playerName + " shared a build!\n");
         TextComponent pasteBuild = new TextComponent("Paste");
         pasteBuild.setColor(ChatColor.GREEN);
         pasteBuild.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/paste " + buildID));
         pasteBuild.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, new Content[]{new Text("Paste the build")}));
         TextComponent copyBuild = new TextComponent("Copy");
         copyBuild.setColor(ChatColor.AQUA);
         copyBuild.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/setbuild " + buildID));
         copyBuild.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, new Content[]{new Text("Copy your build")}));
         message.addExtra("[");
         message.addExtra(pasteBuild);
         message.addExtra("] [");
         message.addExtra(copyBuild);
         message.addExtra("]");

         for (Player player2 : Bukkit.getServer().getOnlinePlayers()) {
            player2.spigot().sendMessage(message);
         }
      }

      return false;
   }
}
