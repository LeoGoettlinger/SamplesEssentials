package com.donutorders.command;

import com.donutorders.manager.GUIManager;
import com.donutorders.util.MessageHelper;
import java.util.Collections;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class OrderCommand implements CommandExecutor, TabCompleter {
   private final GUIManager guiManager;

   public OrderCommand(GUIManager guiManager) {
      this.guiManager = guiManager;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (sender instanceof Player player) {
         if (!player.hasPermission("donutorders.use")) {
            MessageHelper.send(player, "no-permission", "&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪꜱꜱɪᴏɴ ᴛᴏ ᴅᴏ ᴛʜᴀᴛ.");
            return true;
         } else if (args.length >= 1 && args[0].equalsIgnoreCase("history")) {
            this.guiManager.openYourOrderHistory(player, 0);
            return true;
         } else if (args.length >= 1) {
            MessageHelper.send(player, "command.order.usage", "&cᴜꜱᴀɢᴇ: /order [history]");
            return true;
         } else {
            this.guiManager.openYourActiveOrders(player, 0);
            return true;
         }
      } else {
         MessageHelper.send(sender, "player-only", "&cᴏɴʟʏ ᴘʟᴀʏᴇʀꜱ ᴄᴀɴ ᴜꜱᴇ ᴛʜɪꜱ ᴄᴏᴍᴍᴀɴᴅ.");
         return true;
      }
   }

   public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
      return args.length == 1 ? Collections.singletonList("history") : Collections.emptyList();
   }
}
