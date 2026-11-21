package com.artillexstudios.axtrade.trade;

import com.artillexstudios.axtrade.AxTrade;
import com.artillexstudios.axtrade.libs.axapi.scheduler.Scheduler;
import com.artillexstudios.axtrade.request.Request;
import com.artillexstudios.axtrade.request.Requests;
import java.util.Iterator;
import java.util.Map;

public class TradeTicker {
   public static void start() {
      Scheduler.get().runTimer(scheduledTask -> {
         for (Trade trade : Trades.getTrades()) {
            if (!trade.isEnded() && trade.player1.hasConfirmed() && trade.player2.hasConfirmed()) {
               if (trade.player1.getConfirmed() == 1) {
                  trade.complete();
               } else {
                  trade.player1.tick();
                  trade.player2.tick();
               }
            }
         }

         Iterator<Request> iterator = Requests.getRequests().iterator();

         while (iterator.hasNext()) {
            Request request = iterator.next();
            if (System.currentTimeMillis() - request.getTime() > AxTrade.CONFIG.getInt("trade-request-expire-seconds", 60) * 1000L) {
               if (request.isActive()) {
                  AxTrade.MESSAGEUTILS.sendLang(request.getSender(), "request.expired", Map.of("%player%", request.getReceiver().getName()));
               }

               iterator.remove();
            }
         }
      }, 20L, 20L);
   }
}
