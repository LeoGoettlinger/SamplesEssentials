package com.artillexstudios.axtrade.hooks.currency;

import com.artillexstudios.axtrade.AxTrade;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import me.swanis.mobcoins.MobCoinsAPI;
import me.swanis.mobcoins.profile.Profile;
import org.jetbrains.annotations.NotNull;

public class SuperMobCoinsHook implements CurrencyHook {
   @Override
   public void setup() {
   }

   @Override
   public String getName() {
      return "SuperMobCoins";
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
      return MobCoinsAPI.getProfileManager().getProfile(player).getMobCoins();
   }

   @Override
   public CompletableFuture<Boolean> giveBalance(@NotNull UUID player, double amount) {
      Profile profile = MobCoinsAPI.getProfileManager().getProfile(player);
      profile.setMobCoins((long)(profile.getMobCoins() + amount));
      return CompletableFuture.completedFuture(true);
   }

   @Override
   public CompletableFuture<Boolean> takeBalance(@NotNull UUID player, double amount) {
      Profile profile = MobCoinsAPI.getProfileManager().getProfile(player);
      profile.setMobCoins((long)(profile.getMobCoins() - amount));
      return CompletableFuture.completedFuture(true);
   }
}
