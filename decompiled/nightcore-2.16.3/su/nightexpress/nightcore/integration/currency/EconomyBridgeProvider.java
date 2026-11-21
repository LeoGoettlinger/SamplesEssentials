package su.nightexpress.nightcore.integration.currency;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.bridge.currency.Currency;
import su.nightexpress.nightcore.bridge.currency.EconomyBridgeAPI;
import su.nightexpress.nightcore.integration.currency.impl.DummyCurrency;

public class EconomyBridgeProvider implements EconomyBridgeAPI {
   private final CurrencyRegistry registry;

   public EconomyBridgeProvider(@NonNull CurrencyRegistry registry) {
      this.registry = registry;
   }

   @Override
   public boolean hasCurrency(@NonNull String id) {
      return this.registry.contains(id);
   }

   @Override
   public boolean hasVaultCurrency() {
      return this.hasCurrency("vault");
   }

   @Override
   public boolean hasAnyCurrency() {
      return !this.registry.isEmpty();
   }

   @Override
   public void register(@NonNull Currency currency) {
      this.registry.register(currency);
   }

   @Override
   public boolean unregister(@NonNull Currency currency) {
      return this.unregister(currency.getInternalId());
   }

   @Override
   public boolean unregister(@NonNull String id) {
      return this.registry.unregister(id);
   }

   @NonNull
   @Override
   public Set<Currency> getCurrencies() {
      return this.registry.values();
   }

   @NonNull
   @Override
   public Set<String> getCurrencyIds() {
      return this.registry.keys();
   }

   @NonNull
   @Override
   public Optional<Currency> currency(@NonNull String internalId) {
      return this.registry.byId(internalId);
   }

   @Nullable
   @Override
   public Currency getCurrency(@NonNull String internalId) {
      return this.registry.getById(internalId);
   }

   @NonNull
   @Override
   public Currency getCurrencyOrDummy(@NonNull String internalId) {
      return this.registry.byId(internalId).orElse(DummyCurrency.INSTANCE);
   }

   @Nullable
   @Override
   public Currency getVaultCurrency() {
      return this.getCurrency("vault");
   }

   @NonNull
   @Override
   public Optional<Currency> vaultCurrency() {
      return Optional.ofNullable(this.getVaultCurrency());
   }

   @NonNull
   public DummyCurrency getDummyCurrency() {
      return DummyCurrency.INSTANCE;
   }

   @Override
   public boolean canAfford(@NonNull Player player, double amount) {
      return this.canAfford(player, "vault", amount);
   }

   @Override
   public boolean canAfford(@NonNull UUID playerId, double amount) {
      return this.canAfford(playerId, "vault", amount);
   }

   @Override
   public boolean canAfford(@NonNull Player player, @NonNull String id, double amount) {
      return this.currency(id).map(currency -> currency.canAfford(player, amount)).orElse(false);
   }

   @Override
   public boolean canAfford(@NonNull UUID playerId, @NonNull String id, double amount) {
      return this.currency(id).map(currency -> currency.canAfford(playerId, amount)).orElse(false);
   }

   @NonNull
   @Override
   public CompletableFuture<Boolean> canAffordAsync(@NonNull Player player, double amount) {
      return this.canAffordAsync(player, "vault", amount);
   }

   @NonNull
   @Override
   public CompletableFuture<Boolean> canAffordAsync(@NonNull UUID playerId, double amount) {
      return this.canAffordAsync(playerId, "vault", amount);
   }

   @NonNull
   @Override
   public CompletableFuture<Boolean> canAffordAsync(@NonNull Player player, @NonNull String id, double amount) {
      return this.currency(id).map(currency -> currency.canAffordAsync(player, amount)).orElse(CompletableFuture.completedFuture(false));
   }

   @NonNull
   @Override
   public CompletableFuture<Boolean> canAffordAsync(@NonNull UUID playerId, @NonNull String id, double amount) {
      return this.currency(id).map(currency -> currency.canAffordAsync(playerId, amount)).orElse(CompletableFuture.completedFuture(false));
   }

   @Override
   public double queryBalance(@NonNull Player player) {
      return this.queryBalance(player, "vault");
   }

   @Override
   public double queryBalance(@NonNull UUID playerId) {
      return this.queryBalance(playerId, "vault");
   }

   @NonNull
   @Override
   public CompletableFuture<Double> queryBalanceAsync(@NonNull Player player) {
      return this.queryBalanceAsync(player, "vault");
   }

   @NonNull
   @Override
   public CompletableFuture<Double> queryBalanceAsync(@NonNull UUID playerId) {
      return this.queryBalanceAsync(playerId, "vault");
   }

   @Override
   public double queryBalance(@NonNull Player player, @NonNull String id) {
      return this.currency(id).map(currency -> currency.queryBalance(player)).orElse(0.0);
   }

   @Override
   public double queryBalance(@NonNull UUID playerId, @NonNull String id) {
      return this.currency(id).map(currency -> currency.queryBalance(playerId)).orElse(0.0);
   }

   @NonNull
   @Override
   public CompletableFuture<Double> queryBalanceAsync(@NonNull Player player, @NonNull String id) {
      return this.currency(id).map(currency -> currency.queryBalanceAsync(player)).orElse(CompletableFuture.completedFuture(0.0));
   }

   @NonNull
   @Override
   public CompletableFuture<Double> queryBalanceAsync(@NonNull UUID playerId, @NonNull String id) {
      return this.currency(id).map(currency -> currency.queryBalanceAsync(playerId)).orElse(CompletableFuture.completedFuture(0.0));
   }

   @Override
   public void deposit(@NonNull Player player, double amount) {
      this.deposit(player.getUniqueId(), amount);
   }

   @Override
   public void deposit(@NonNull UUID playerId, double amount) {
      this.deposit(playerId, "vault", amount);
   }

   @NonNull
   @Override
   public CompletableFuture<Boolean> depositAsync(@NonNull Player player, double amount) {
      return this.depositAsync(player.getUniqueId(), amount);
   }

   @NonNull
   @Override
   public CompletableFuture<Boolean> depositAsync(@NonNull UUID playerId, double amount) {
      return this.depositAsync(playerId, "vault", amount);
   }

   @Override
   public void deposit(@NonNull Player player, @NonNull String id, double amount) {
      this.deposit(player.getUniqueId(), id, amount);
   }

   @Override
   public void deposit(@NonNull UUID playerId, @NonNull String id, double amount) {
      this.currency(id).ifPresent(currency -> currency.deposit(playerId, amount));
   }

   @NonNull
   @Override
   public CompletableFuture<Boolean> depositAsync(@NonNull Player player, @NonNull String id, double amount) {
      return this.currency(id).map(currency -> currency.depositAsync(player, amount)).orElse(CompletableFuture.completedFuture(false));
   }

   @NonNull
   @Override
   public CompletableFuture<Boolean> depositAsync(@NonNull UUID playerId, @NonNull String id, double amount) {
      return this.currency(id).map(currency -> currency.depositAsync(playerId, amount)).orElse(CompletableFuture.completedFuture(false));
   }

   @Override
   public void withdraw(@NonNull Player player, double amount) {
      this.withdraw(player.getUniqueId(), amount);
   }

   @Override
   public void withdraw(@NonNull UUID playerId, double amount) {
      this.withdraw(playerId, "vault", amount);
   }

   @NonNull
   @Override
   public CompletableFuture<Boolean> withdrawsync(@NonNull Player player, double amount) {
      return this.withdrawAsync(player.getUniqueId(), amount);
   }

   @NonNull
   @Override
   public CompletableFuture<Boolean> withdrawAsync(@NonNull UUID playerId, double amount) {
      return this.withdrawAsync(playerId, "vault", amount);
   }

   @Override
   public void withdraw(@NonNull Player player, @NonNull String id, double amount) {
      this.withdraw(player.getUniqueId(), id, amount);
   }

   @Override
   public void withdraw(@NonNull UUID playerId, @NonNull String id, double amount) {
      this.currency(id).ifPresent(currency -> currency.withdraw(playerId, amount));
   }

   @NonNull
   @Override
   public CompletableFuture<Boolean> withdrawsync(@NonNull Player player, @NonNull String id, double amount) {
      return this.currency(id).map(currency -> currency.withdrawAsync(player, amount)).orElse(CompletableFuture.completedFuture(false));
   }

   @NonNull
   @Override
   public CompletableFuture<Boolean> withdrawAsync(@NonNull UUID playerId, @NonNull String id, double amount) {
      return this.currency(id).map(currency -> currency.withdrawAsync(playerId, amount)).orElse(CompletableFuture.completedFuture(false));
   }
}
