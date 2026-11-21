package com.artillexstudios.axtrade.currency;

import com.artillexstudios.axtrade.hooks.currency.CurrencyHook;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import org.bukkit.entity.Player;

public class CurrencyProcessor {
   private final Player player;
   private final Set<Entry<CurrencyHook, Double>> currencies;
   private final Map<Entry<CurrencyHook, Double>, Boolean> states = new HashMap<>();

   public CurrencyProcessor(Player player, Set<Entry<CurrencyHook, Double>> currencies) {
      this.player = player;
      this.currencies = currencies;
   }

   public CompletableFuture<Boolean> run() {
      CompletableFuture<Boolean>[] futures = new CompletableFuture[this.currencies.size()];
      int i = 0;

      for (Entry<CurrencyHook, Double> entry : this.currencies) {
         futures[i] = entry.getKey().takeBalance(this.player.getUniqueId(), entry.getValue());
         i++;
      }

      return CompletableFuture.allOf(futures).thenApply(unused -> {
         boolean[] actions = new boolean[futures.length];
         boolean anyErrors = false;

         for (int j = 0; j < futures.length; j++) {
            actions[j] = futures[j].join();
            if (!actions[j]) {
               anyErrors = true;
            }
         }

         int jx = 0;

         for (Entry<CurrencyHook, Double> entryx : this.currencies) {
            this.states.put(entryx, actions[jx]);
            jx++;
         }

         if (anyErrors) {
            this.reverse();
         }

         return !anyErrors;
      });
   }

   public CompletableFuture<Boolean> reverse() {
      for (Entry<Entry<CurrencyHook, Double>, Boolean> entry : this.states.entrySet()) {
         if (entry.getValue()) {
            entry.getKey().getKey().giveBalance(this.player.getUniqueId(), entry.getKey().getValue());
         }
      }

      return null;
   }
}
