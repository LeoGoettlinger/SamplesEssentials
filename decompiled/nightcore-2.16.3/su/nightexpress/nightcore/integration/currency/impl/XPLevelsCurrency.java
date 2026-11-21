package su.nightexpress.nightcore.integration.currency.impl;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.integration.currency.CurrencySettings;
import su.nightexpress.nightcore.integration.currency.type.IncompleteCurrency;
import su.nightexpress.nightcore.util.Players;
import su.nightexpress.nightcore.util.bukkit.NightItem;

public class XPLevelsCurrency extends IncompleteCurrency {
   public XPLevelsCurrency() {
      super("xp_level");
   }

   @Override
   public boolean canHandleDecimals() {
      return false;
   }

   @Override
   public boolean canHandleOffline() {
      return false;
   }

   @NonNull
   @Override
   public CurrencySettings getDefaultSettings() {
      return CurrencySettings.createDefault("XP Levels", NightItem.fromType(Material.EXPERIENCE_BOTTLE));
   }

   @NonNull
   @Override
   public CompletableFuture<Double> queryBalanceAsync(@NonNull Player player) {
      return CompletableFuture.completedFuture(this.queryLevel(player));
   }

   @NonNull
   @Override
   public CompletableFuture<Double> queryBalanceAsync(@NonNull UUID playerId) {
      return CompletableFuture.completedFuture(Players.findById(playerId).map(this::queryLevel).orElse(0.0));
   }

   @Override
   protected double queryBalanceDirect(@NonNull Player player) {
      return this.queryLevel(player);
   }

   @NonNull
   @Override
   public CompletableFuture<Boolean> depositAsync(@NonNull Player player, double amount) {
      return CompletableFuture.completedFuture(this.depositLevel(player, amount));
   }

   @NonNull
   @Override
   public CompletableFuture<Boolean> depositAsync(@NonNull UUID playerId, double amount) {
      return CompletableFuture.completedFuture(Players.findById(playerId).map(player -> this.depositLevel(player, amount)).orElse(false));
   }

   @Override
   protected void depositDirect(@NonNull Player player, double amount) {
      this.depositLevel(player, amount);
   }

   @NonNull
   @Override
   public CompletableFuture<Boolean> withdrawAsync(@NonNull Player player, double amount) {
      return CompletableFuture.completedFuture(this.withdrawLevel(player, amount));
   }

   @NonNull
   @Override
   public CompletableFuture<Boolean> withdrawAsync(@NonNull UUID playerId, double amount) {
      return CompletableFuture.completedFuture(Players.findById(playerId).map(player -> this.withdrawLevel(player, amount)).orElse(false));
   }

   @Override
   protected void withdrawDirect(@NonNull Player player, double amount) {
      this.withdrawLevel(player, amount);
   }

   private double queryLevel(@NonNull Player player) {
      return player.getLevel();
   }

   private boolean depositLevel(@NonNull Player player, double amount) {
      player.setLevel(player.getLevel() + (int)amount);
      return true;
   }

   private boolean withdrawLevel(@NonNull Player player, double amount) {
      player.setLevel(Math.max(0, player.getLevel() - (int)amount));
      return true;
   }
}
