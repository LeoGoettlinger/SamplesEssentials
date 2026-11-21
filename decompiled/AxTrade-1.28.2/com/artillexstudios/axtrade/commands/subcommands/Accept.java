package com.artillexstudios.axtrade.commands.subcommands;

import com.artillexstudios.axtrade.AxTrade;
import com.artillexstudios.axtrade.request.Requests;
import java.util.Map;
import org.bukkit.entity.Player;

public enum Accept {
   INSTANCE;

   public void execute(Player sender, Player other) {
      com.artillexstudios.axtrade.request.Request request = Requests.getRequest(sender, other);
      if (request != null && !request.getSender().equals(sender) && request.isActive()) {
         Requests.addRequest(sender, other);
      } else {
         AxTrade.MESSAGEUTILS.sendLang(sender, "request.no-request", Map.of("%player%", other.getName()));
      }
   }
}
