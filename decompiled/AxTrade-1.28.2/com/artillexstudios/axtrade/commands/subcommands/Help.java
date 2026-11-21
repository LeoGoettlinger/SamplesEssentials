package com.artillexstudios.axtrade.commands.subcommands;

import com.artillexstudios.axtrade.AxTrade;
import com.artillexstudios.axtrade.libs.axapi.utils.StringUtils;
import org.bukkit.command.CommandSender;

public enum Help {
   INSTANCE;

   public void execute(CommandSender sender) {
      if (sender.hasPermission("axtrade.admin")) {
         for (String m : AxTrade.LANG.getStringList("admin-help")) {
            sender.sendMessage(StringUtils.formatToString(m));
         }
      } else {
         for (String m : AxTrade.LANG.getStringList("player-help")) {
            sender.sendMessage(StringUtils.formatToString(m));
         }
      }
   }
}
