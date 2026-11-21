package su.nightexpress.nightcore.integration.currency.impl;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.integration.currency.CurrencySettings;
import su.nightexpress.nightcore.integration.currency.type.IncompleteCurrency;
import su.nightexpress.nightcore.util.bukkit.NightItem;

public class PlayerPointsCurrency extends IncompleteCurrency {
   private final PlayerPointsAPI api = PlayerPoints.getInstance().getAPI();

   public PlayerPointsCurrency() {
      super("playerpoints");
   }

   @Override
   public boolean canHandleDecimals() {
      return false;
   }

   @Override
   public boolean canHandleOffline() {
      return true;
   }

   @NonNull
   @Override
   public CurrencySettings getDefaultSettings() {
      return CurrencySettings.createDefault("Points", NightItem.fromType(Material.SUNFLOWER));
   }

   @NonNull
   @Override
   public CompletableFuture<Double> queryBalanceAsync(@NonNull Player player) {
      return this.queryBalanceAsync(player.getUniqueId());
   }

   @NonNull
   @Override
   public CompletableFuture<Double> queryBalanceAsync(@NonNull UUID playerId) {
      return CompletableFuture.supplyAsync(() -> (double)this.api.look(playerId));
   }

   @Override
   protected double queryBalanceDirect(@NonNull Player player) {
      return this.api.look(player.getUniqueId());
   }

   @NonNull
   @Override
   public CompletableFuture<Boolean> depositAsync(@NonNull Player player, double amount) {
      return this.depositAsync(player.getUniqueId(), amount);
   }

   @NonNull
   @Override
   public CompletableFuture<Boolean> depositAsync(@NonNull UUID playerId, double amount) {
      return CompletableFuture.supplyAsync(() -> this.api.give(playerId, (int)amount));
   }

   @Override
   protected void depositDirect(@NonNull Player player, double amount) {
      this.api.give(player.getUniqueId(), (int)amount);
   }

   @NonNull
   @Override
   public CompletableFuture<Boolean> withdrawAsync(@NonNull Player player, double amount) {
      return this.withdrawAsync(player.getUniqueId(), amount);
   }

   @NonNull
   @Override
   public CompletableFuture<Boolean> withdrawAsync(@NonNull UUID playerId, double amount) {
      return CompletableFuture.supplyAsync(() -> this.api.take(playerId, (int)amount));
   }

   @Override
   protected void withdrawDirect(@NonNull Player player, double amount) {
      this.api.take(player.getUniqueId(), (int)amount);
   }
}
