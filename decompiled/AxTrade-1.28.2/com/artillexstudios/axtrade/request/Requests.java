package com.artillexstudios.axtrade.request;

import com.artillexstudios.axtrade.AxTrade;
import com.artillexstudios.axtrade.api.events.AxTradeRequestEvent;
import com.artillexstudios.axtrade.libs.axapi.nms.wrapper.ServerPlayerWrapper;
import com.artillexstudios.axtrade.libs.axapi.utils.StringUtils;
import com.artillexstudios.axtrade.safety.SafetyManager;
import com.artillexstudios.axtrade.trade.Trades;
import com.artillexstudios.axtrade.utils.SoundUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Requests {
   private static final List<Request> requests = new ArrayList<>();

   public static void addRequest(@NotNull Player sender, @NotNull Player receiver) {
      if (!SafetyManager.TRADING.get()) {
         AxTrade.MESSAGEUTILS.sendLang(sender, "safety");
      } else {
         Map<String, String> replacements = Map.of("%player%", receiver.getName());
         boolean disallowSameIp = AxTrade.CONFIG.getBoolean("disallow-same-ip-trade", false);
         if (disallowSameIp
            && sender.getAddress() != null
            && receiver.getAddress() != null
            && sender.getAddress().getAddress().equals(receiver.getAddress().getAddress())) {
            AxTrade.MESSAGEUTILS.sendLang(sender, "request.same-ip", replacements);
         } else {
            List<String> disallowed = AxTrade.CONFIG.getStringList("disallowed-gamemodes");
            if (!disallowed.contains(sender.getGameMode().name()) && !disallowed.contains(receiver.getGameMode().name())) {
               List<String> blacklisted = AxTrade.CONFIG.getStringList("blacklisted-worlds");
               if (blacklisted.contains(sender.getWorld().getName()) || blacklisted.contains(receiver.getWorld().getName())) {
                  AxTrade.MESSAGEUTILS.sendLang(sender, "request.blacklisted-world", replacements);
               } else if (!sender.isDead() && !receiver.isDead()) {
                  if (AxTrade.TOGGLED.getBoolean("toggled." + receiver.getUniqueId())) {
                     AxTrade.MESSAGEUTILS.sendLang(sender, "request.not-accepting", replacements);
                  } else if (AxTrade.TOGGLED.getBoolean("toggled." + sender.getUniqueId())) {
                     AxTrade.MESSAGEUTILS.sendLang(sender, "request.disabled-trading", replacements);
                  } else if (sender.equals(receiver)) {
                     AxTrade.MESSAGEUTILS.sendLang(sender, "request.cant-trade-self", replacements);
                  } else if (!Trades.isTrading(receiver) && !Trades.isTrading(sender)) {
                     int maxDist = AxTrade.CONFIG.getInt("trade-max-distance", 10);
                     if (maxDist == -1 || sender.getWorld() == receiver.getWorld() && !(maxDist < sender.getLocation().distance(receiver.getLocation()))) {
                        Request request = getRequest(sender, receiver);
                        if (request != null) {
                           if (request.isActive() && !request.getSender().equals(sender)) {
                              Trades.addTrade(sender, receiver);
                              requests.remove(request);
                              request.deactivate();
                              return;
                           }

                           if (!request.isActive()
                              || System.currentTimeMillis() - request.getTime() < AxTrade.CONFIG.getInt("trade-request-expire-seconds", 60) * 1000L) {
                              AxTrade.MESSAGEUTILS.sendLang(sender, "request.already-sent", replacements);
                              return;
                           }
                        }

                        AxTradeRequestEvent apiEvent = new AxTradeRequestEvent(sender, receiver);
                        Bukkit.getPluginManager().callEvent(apiEvent);
                        if (!apiEvent.isCancelled()) {
                           requests.add(new Request(sender, receiver));
                           AxTrade.MESSAGEUTILS.sendLang(sender, "request.sent-sender", replacements);
                           Map<String, String> replacements2 = Map.of("%player%", sender.getName());
                           if (AxTrade.LANG.getSection("request.sent-receiver") == null) {
                              AxTrade.MESSAGEUTILS.sendLang(receiver, "request.sent-receiver", replacements2);
                           } else {
                              ServerPlayerWrapper receiverWrap = ServerPlayerWrapper.wrap(receiver);
                              receiverWrap.message(
                                 StringUtils.format(AxTrade.CONFIG.getString("prefix") + AxTrade.LANG.getString("request.sent-receiver.info"), replacements2)
                              );
                              receiverWrap.message(
                                 StringUtils.format(AxTrade.LANG.getString("request.sent-receiver.accept.message"), replacements2)
                                    .hoverEvent(
                                       HoverEvent.showText(StringUtils.format(AxTrade.LANG.getString("request.sent-receiver.accept.hover"), replacements2))
                                    )
                                    .clickEvent(ClickEvent.runCommand("/trade accept " + sender.getName()))
                              );
                              receiverWrap.message(
                                 StringUtils.format(AxTrade.LANG.getString("request.sent-receiver.deny.message"), replacements2)
                                    .hoverEvent(
                                       HoverEvent.showText(StringUtils.format(AxTrade.LANG.getString("request.sent-receiver.deny.hover"), replacements2))
                                    )
                                    .clickEvent(ClickEvent.runCommand("/trade deny " + sender.getName()))
                              );
                           }

                           SoundUtils.playSound(sender, "requested");
                           SoundUtils.playSound(receiver, "requested");
                        }
                     } else {
                        AxTrade.MESSAGEUTILS.sendLang(sender, "request.too-far", replacements);
                     }
                  } else {
                     AxTrade.MESSAGEUTILS.sendLang(sender, "request.already-in-trade", replacements);
                  }
               } else {
                  AxTrade.MESSAGEUTILS.sendLang(sender, "request.not-accepting", replacements);
               }
            } else {
               AxTrade.MESSAGEUTILS.sendLang(sender, "request.disallowed-gamemode", replacements);
            }
         }
      }
   }

   @Nullable
   public static Request getRequest(Player p1, Player p2) {
      for (Request rq : requests) {
         if (p1.equals(rq.getSender()) && p2.equals(rq.getReceiver())) {
            return rq;
         }

         if (p2.equals(rq.getSender()) && p1.equals(rq.getReceiver())) {
            return rq;
         }
      }

      return null;
   }

   public static boolean hasRequest(Player p1, Player p2) {
      return getRequest(p1, p2) != null;
   }

   public static List<Request> getRequests() {
      return requests;
   }
}
