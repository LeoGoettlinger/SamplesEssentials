package com.artillexstudios.axtrade.hooks.currency;

import com.artillexstudios.axquestboard.api.AxQuestBoardAPI;
import com.artillexstudios.axtrade.AxTrade;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.NotNull;

public class AxQuestBoardHook implements CurrencyHook {
   @Override
   public void setup() {
   }

   @Override
   public String getName() {
      return "AxQuestBoard";
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
      return ((Integer)AxQuestBoardAPI.getQuestPoints(player).join()).intValue();
   }

   @Override
   public CompletableFuture<Boolean> giveBalance(@NotNull UUID player, double amount) {
      CompletableFuture<Boolean> cf = new CompletableFuture<>();
      AxQuestBoardAPI.giveQuestPoints(player, (int)amount).thenAccept(cf::complete);
      return cf;
   }

   @Override
   public CompletableFuture<Boolean> takeBalance(@NotNull UUID player, double amount) {
      CompletableFuture<Boolean> cf = new CompletableFuture<>();
      AxQuestBoardAPI.giveQuestPoints(player, (int)(-amount)).thenAccept(cf::complete);
      return cf;
   }
}
