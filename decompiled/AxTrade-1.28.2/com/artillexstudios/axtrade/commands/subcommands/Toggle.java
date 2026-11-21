package com.artillexstudios.axtrade.commands.subcommands;

import com.artillexstudios.axtrade.AxTrade;
import com.artillexstudios.axtrade.libs.lamp.bukkit.exception.SenderNotPlayerException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public enum Toggle {
   INSTANCE;

   public void execute(CommandSender sender, Player other) {
      if (other != null) {
         sender = other;
      }

      if (sender instanceof Player player) {
         boolean toggled = AxTrade.TOGGLED.getBoolean("toggled." + player.getUniqueId(), false);
         if (toggled) {
            AxTrade.TOGGLED.getBackingDocument().remove("toggled." + player.getUniqueId());
            AxTrade.MESSAGEUTILS.sendLang(player, "toggle.enabled");
         } else {
            AxTrade.TOGGLED.set("toggled." + player.getUniqueId(), true);
            AxTrade.MESSAGEUTILS.sendLang(player, "toggle.disabled");
         }

         AxTrade.TOGGLED.save();
      } else {
         throw new SenderNotPlayerException();
      }
   }
}
