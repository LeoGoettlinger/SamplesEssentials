package com.artillexstudios.axtrade.hooks.currency;

import com.artillexstudios.axtrade.AxTrade;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import me.rivaldev.harvesterhoes.Main;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public class RivalHarvesterHoesHook implements CurrencyHook {
   @Override
   public void setup() {
   }

   @Override
   public String getName() {
      return "RivalHarvesterHoes";
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
      return true;
   }

   @Override
   public boolean isPersistent() {
      return false;
   }

   @Override
   public double getBalance(@NotNull UUID player) {
      return Main.instance.getEconomy().getEconomyAmount(Bukkit.getOfflinePlayer(player));
   }

   @Override
   public CompletableFuture<Boolean> giveBalance(@NotNull UUID player, double amount) {
      Main.instance.getEconomy().giveEconomyAmount(Bukkit.getOfflinePlayer(player), amount);
      return CompletableFuture.completedFuture(true);
   }

   @Override
   public CompletableFuture<Boolean> takeBalance(@NotNull UUID player, double amount) {
      Main.instance.getEconomy().removeEconomyAmount(Bukkit.getOfflinePlayer(player), amount);
      return CompletableFuture.completedFuture(true);
   }
}
