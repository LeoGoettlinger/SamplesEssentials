package com.artillexstudios.axtrade.commands.subcommands;

import com.artillexstudios.axtrade.AxTrade;
import com.artillexstudios.axtrade.trade.Trades;
import org.bukkit.entity.Player;

public enum Force {
   INSTANCE;

   public void execute(Player sender, Player other) {
      if (sender.equals(other)) {
         AxTrade.MESSAGEUTILS.sendLang(sender, "request.cant-trade-self");
      } else {
         Trades.addTrade(sender, other);
      }
   }
}
