package su.nightexpress.nightcore.integration.currency.impl;

import com.magmaguy.elitemobs.economy.EconomyHandler;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.integration.currency.CurrencySettings;
import su.nightexpress.nightcore.integration.currency.type.IncompleteCurrency;
import su.nightexpress.nightcore.util.bukkit.NightItem;

public class EliteMobsCurrency extends IncompleteCurrency {
   public EliteMobsCurrency() {
      super("elitemobs");
   }

   @Override
   public boolean canHandleDecimals() {
      return true;
   }

   @Override
   public boolean canHandleOffline() {
      return true;
   }

   @NonNull
   @Override
   public CurrencySettings getDefaultSettings() {
      return CurrencySettings.createDefault("Elite Tokens", NightItem.fromType(Material.EMERALD));
   }

   @NonNull
   @Override
   public CompletableFuture<Double> queryBalanceAsync(@NonNull Player player) {
      return this.queryBalanceAsync(player.getUniqueId());
   }

   @NonNull
   @Override
   public CompletableFuture<Double> queryBalanceAsync(@NonNull UUID playerId) {
      return CompletableFuture.supplyAsync(() -> EconomyHandler.checkCurrency(playerId, true));
   }

   @Override
   protected double queryBalanceDirect(@NonNull Player player) {
      return EconomyHandler.checkCurrency(player.getUniqueId(), false);
   }

   @NonNull
   @Override
   public CompletableFuture<Boolean> depositAsync(@NonNull Player player, double amount) {
      return this.depositAsync(player.getUniqueId(), amount);
   }

   @NonNull
   @Override
   public CompletableFuture<Boolean> depositAsync(@NonNull UUID playerId, double amount) {
      EconomyHandler.addCurrency(playerId, amount);
      return CompletableFuture.completedFuture(true);
   }

   @Override
   protected void depositDirect(@NonNull Player player, double amount) {
      EconomyHandler.addCurrency(player.getUniqueId(), amount);
   }

   @NonNull
   @Override
   public CompletableFuture<Boolean> withdrawAsync(@NonNull Player player, double amount) {
      return this.withdrawAsync(player.getUniqueId(), amount);
   }

   @NonNull
   @Override
   public CompletableFuture<Boolean> withdrawAsync(@NonNull UUID playerId, double amount) {
      EconomyHandler.subtractCurrency(playerId, amount);
      return CompletableFuture.completedFuture(true);
   }

   @Override
   protected void withdrawDirect(@NonNull Player player, double amount) {
      EconomyHandler.subtractCurrency(player.getUniqueId(), amount);
   }
}
