package com.artillexstudios.axtrade.commands.subcommands;

import com.artillexstudios.axtrade.AxTrade;
import com.artillexstudios.axtrade.request.Requests;
import com.artillexstudios.axtrade.utils.SoundUtils;
import java.util.Map;
import org.bukkit.entity.Player;

public enum Deny {
   INSTANCE;

   public void execute(Player sender, Player other) {
      com.artillexstudios.axtrade.request.Request request = Requests.getRequest(sender, other);
      if (request != null && !request.getSender().equals(sender) && request.isActive()) {
         request.deactivate();
         AxTrade.MESSAGEUTILS.sendLang(request.getSender(), "request.deny-sender", Map.of("%player%", request.getReceiver().getName()));
         AxTrade.MESSAGEUTILS.sendLang(request.getReceiver(), "request.deny-receiver", Map.of("%player%", request.getSender().getName()));
         SoundUtils.playSound(request.getSender(), "deny");
         SoundUtils.playSound(request.getReceiver(), "deny");
      } else {
         AxTrade.MESSAGEUTILS.sendLang(sender, "request.no-request", Map.of("%player%", other.getName()));
      }
   }
}
