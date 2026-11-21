package com.artillexstudios.axtrade.hooks.currency;

import com.artillexstudios.axhoes.api.AxHoesAPI;
import com.artillexstudios.axtrade.AxTrade;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.NotNull;

public class AxHoesHook implements CurrencyHook {
   @Override
   public void setup() {
   }

   @Override
   public String getName() {
      return "AxHoes";
   }

   @Override
   public Map<String, Object> getSettings() {
      return AxTrade.HOOKS.getSection("currencies." + this.getName()).getStringRouteMappedValues(true);
   }

   @Override
   public boolean worksOffline() {
      return true;
   }

   @Override
   public boolean usesDouble() {
      return false;
   }

   @Override
   public boolean isPersistent() {
      return false;
   }

   @Override
   public double getBalance(@NotNull UUID player) {
      return AxHoesAPI.getEssenceHook() == null ? 0.0 : AxHoesAPI.getEssenceHook().getBalance(player);
   }

   @Override
   public CompletableFuture<Boolean> giveBalance(@NotNull UUID player, double amount) {
      if (AxHoesAPI.getEssenceHook() == null) {
         return CompletableFuture.completedFuture(false);
      } else {
         CompletableFuture<Boolean> cf = new CompletableFuture<>();
         AxHoesAPI.getEssenceHook().giveBalance(player, amount).thenAccept(cf::complete);
         return cf;
      }
   }

   @Override
   public CompletableFuture<Boolean> takeBalance(@NotNull UUID player, double amount) {
      if (AxHoesAPI.getEssenceHook() == null) {
         return CompletableFuture.completedFuture(false);
      } else {
         CompletableFuture<Boolean> cf = new CompletableFuture<>();
         AxHoesAPI.getEssenceHook().takeBalance(player, amount).thenAccept(cf::complete);
         return cf;
      }
   }
}
