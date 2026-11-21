package net.milkbowl.vault2.economy;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.NotNull;

public final class EconomyFutures {
   private EconomyFutures() {
      throw new UnsupportedOperationException("Utility class");
   }

   public static CompletableFuture<Boolean> createAccount(
      @NotNull final Economy economy, @NotNull final UUID accountID, @NotNull final String name, final boolean player
   ) {
      return economy.async()
         .map(async -> async.createAccount(accountID, name, player))
         .orElseGet(() -> CompletableFuture.completedFuture(economy.createAccount(accountID, name, player)));
   }

   public static CompletableFuture<Boolean> createAccount(
      @NotNull final Economy economy, @NotNull final UUID accountID, @NotNull final String name, @NotNull final String worldName, final boolean player
   ) {
      return economy.async()
         .map(async -> async.createAccount(accountID, name, worldName, player))
         .orElseGet(() -> CompletableFuture.completedFuture(economy.createAccount(accountID, name, worldName, player)));
   }

   public static CompletableFuture<Map<UUID, String>> getUUIDNameMap(@NotNull final Economy economy) {
      return economy.async().map(AsyncEconomy::getUUIDNameMap).orElseGet(() -> CompletableFuture.completedFuture(economy.getUUIDNameMap()));
   }

   public static CompletableFuture<Optional<String>> getAccountName(@NotNull final Economy economy, @NotNull final UUID accountID) {
      return economy.async()
         .map(async -> async.getAccountName(accountID))
         .orElseGet(() -> CompletableFuture.completedFuture(economy.getAccountName(accountID)));
   }

   public static CompletableFuture<Boolean> hasAccount(@NotNull final Economy economy, @NotNull final UUID accountID) {
      return economy.async().map(async -> async.hasAccount(accountID)).orElseGet(() -> CompletableFuture.completedFuture(economy.hasAccount(accountID)));
   }

   public static CompletableFuture<Boolean> hasAccount(@NotNull final Economy economy, @NotNull final UUID accountID, @NotNull final String worldName) {
      return economy.async()
         .map(async -> async.hasAccount(accountID, worldName))
         .orElseGet(() -> CompletableFuture.completedFuture(economy.hasAccount(accountID, worldName)));
   }

   public static CompletableFuture<Boolean> renameAccount(@NotNull final Economy economy, @NotNull final UUID accountID, @NotNull final String name) {
      return renameAccount(economy, "Vault", accountID, name);
   }

   public static CompletableFuture<Boolean> renameAccount(
      @NotNull final Economy economy, @NotNull final String pluginName, @NotNull final UUID accountID, @NotNull final String name
   ) {
      return economy.async()
         .map(async -> async.renameAccount(pluginName, accountID, name))
         .orElseGet(() -> CompletableFuture.completedFuture(economy.renameAccount(pluginName, accountID, name)));
   }

   public static CompletableFuture<Boolean> deleteAccount(@NotNull final Economy economy, @NotNull final String pluginName, @NotNull final UUID accountID) {
      return economy.async()
         .map(async -> async.deleteAccount(pluginName, accountID))
         .orElseGet(() -> CompletableFuture.completedFuture(economy.deleteAccount(pluginName, accountID)));
   }

   public static CompletableFuture<Boolean> accountSupportsCurrency(
      @NotNull final Economy economy, @NotNull final String pluginName, @NotNull final UUID accountID, @NotNull final String currency
   ) {
      return economy.async()
         .map(async -> async.accountSupportsCurrency(pluginName, accountID, currency))
         .orElseGet(() -> CompletableFuture.completedFuture(economy.accountSupportsCurrency(pluginName, accountID, currency)));
   }

   public static CompletableFuture<Boolean> accountSupportsCurrency(
      @NotNull final Economy economy,
      @NotNull final String pluginName,
      @NotNull final UUID accountID,
      @NotNull final String currency,
      @NotNull final String world
   ) {
      return economy.async()
         .map(async -> async.accountSupportsCurrency(pluginName, accountID, currency, world))
         .orElseGet(() -> CompletableFuture.completedFuture(economy.accountSupportsCurrency(pluginName, accountID, currency, world)));
   }

   public static CompletableFuture<BigDecimal> balance(@NotNull final Economy economy, @NotNull final String pluginName, @NotNull final UUID accountID) {
      return economy.async()
         .map(async -> async.balance(pluginName, accountID))
         .orElseGet(() -> CompletableFuture.completedFuture(economy.balance(pluginName, accountID)));
   }

   public static CompletableFuture<BigDecimal> balance(
      @NotNull final Economy economy, @NotNull final String pluginName, @NotNull final UUID accountID, @NotNull final String world
   ) {
      return economy.async()
         .map(async -> async.balance(pluginName, accountID, world))
         .orElseGet(() -> CompletableFuture.completedFuture(economy.balance(pluginName, accountID, world)));
   }

   public static CompletableFuture<BigDecimal> balance(
      @NotNull final Economy economy,
      @NotNull final String pluginName,
      @NotNull final UUID accountID,
      @NotNull final String world,
      @NotNull final String currency
   ) {
      return economy.async()
         .map(async -> async.balance(pluginName, accountID, world, currency))
         .orElseGet(() -> CompletableFuture.completedFuture(economy.balance(pluginName, accountID, world, currency)));
   }

   public static CompletableFuture<Boolean> has(
      @NotNull final Economy economy, @NotNull final String pluginName, @NotNull final UUID accountID, @NotNull final BigDecimal amount
   ) {
      return economy.async()
         .map(async -> async.has(pluginName, accountID, amount))
         .orElseGet(() -> CompletableFuture.completedFuture(economy.has(pluginName, accountID, amount)));
   }

   public static CompletableFuture<Boolean> has(
      @NotNull final Economy economy,
      @NotNull final String pluginName,
      @NotNull final UUID accountID,
      @NotNull final String worldName,
      @NotNull final BigDecimal amount
   ) {
      return economy.async()
         .map(async -> async.has(pluginName, accountID, worldName, amount))
         .orElseGet(() -> CompletableFuture.completedFuture(economy.has(pluginName, accountID, worldName, amount)));
   }

   public static CompletableFuture<Boolean> has(
      @NotNull final Economy economy,
      @NotNull final String pluginName,
      @NotNull final UUID accountID,
      @NotNull final String worldName,
      @NotNull final String currency,
      @NotNull final BigDecimal amount
   ) {
      return economy.async()
         .map(async -> async.has(pluginName, accountID, worldName, currency, amount))
         .orElseGet(() -> CompletableFuture.completedFuture(economy.has(pluginName, accountID, worldName, currency, amount)));
   }

   public static CompletableFuture<EconomyResponse> set(
      @NotNull final Economy economy, @NotNull final String pluginName, @NotNull final UUID accountID, @NotNull final BigDecimal amount
   ) {
      return economy.async()
         .map(async -> async.set(pluginName, accountID, amount))
         .orElseGet(() -> CompletableFuture.completedFuture(economy.set(pluginName, accountID, amount)));
   }

   public static CompletableFuture<EconomyResponse> set(
      @NotNull final Economy economy,
      @NotNull final String pluginName,
      @NotNull final UUID accountID,
      @NotNull final String worldName,
      @NotNull final BigDecimal amount
   ) {
      return economy.async()
         .map(async -> async.set(pluginName, accountID, worldName, amount))
         .orElseGet(() -> CompletableFuture.completedFuture(economy.set(pluginName, accountID, worldName, amount)));
   }

   public static CompletableFuture<EconomyResponse> set(
      @NotNull final Economy economy,
      @NotNull final String pluginName,
      @NotNull final UUID accountID,
      @NotNull final String worldName,
      @NotNull final String currency,
      @NotNull final BigDecimal amount
   ) {
      return economy.async()
         .map(async -> async.set(pluginName, accountID, worldName, currency, amount))
         .orElseGet(() -> CompletableFuture.completedFuture(economy.set(pluginName, accountID, worldName, currency, amount)));
   }

   public static CompletableFuture<MultiEconomyResponse> transfer(
      @NotNull final Economy economy, @NotNull final String pluginName, @NotNull final UUID from, @NotNull final UUID to, @NotNull final BigDecimal amount
   ) {
      return economy.async()
         .map(async -> async.transfer(pluginName, from, to, amount))
         .orElseGet(() -> CompletableFuture.completedFuture(economy.transfer(pluginName, from, to, amount)));
   }

   public static CompletableFuture<MultiEconomyResponse> transfer(
      @NotNull final Economy economy,
      @NotNull final String pluginName,
      @NotNull final UUID from,
      @NotNull final UUID to,
      @NotNull final String worldName,
      @NotNull final BigDecimal amount
   ) {
      return economy.async()
         .map(async -> async.transfer(pluginName, from, to, worldName, amount))
         .orElseGet(() -> CompletableFuture.completedFuture(economy.transfer(pluginName, from, to, worldName, amount)));
   }

   public static CompletableFuture<MultiEconomyResponse> transfer(
      @NotNull final Economy economy,
      @NotNull final String pluginName,
      @NotNull final UUID from,
      @NotNull final UUID to,
      @NotNull final String worldName,
      @NotNull final String currency,
      @NotNull final BigDecimal amount
   ) {
      return economy.async()
         .map(async -> async.transfer(pluginName, from, to, worldName, currency, amount))
         .orElseGet(() -> CompletableFuture.completedFuture(economy.transfer(pluginName, from, to, worldName, currency, amount)));
   }

   public static CompletableFuture<EconomyResponse> canWithdraw(
      @NotNull final Economy economy, @NotNull final String pluginName, @NotNull final UUID accountID, @NotNull final BigDecimal amount
   ) {
      return economy.async()
         .map(async -> async.canWithdraw(pluginName, accountID, amount))
         .orElseGet(() -> CompletableFuture.completedFuture(economy.canWithdraw(pluginName, accountID, amount)));
   }

   public static CompletableFuture<EconomyResponse> canWithdraw(
      @NotNull final Economy economy,
      @NotNull final String pluginName,
      @NotNull final UUID accountID,
      @NotNull final String worldName,
      @NotNull final BigDecimal amount
   ) {
      return economy.async()
         .map(async -> async.canWithdraw(pluginName, accountID, worldName, amount))
         .orElseGet(() -> CompletableFuture.completedFuture(economy.canWithdraw(pluginName, accountID, worldName, amount)));
   }

   public static CompletableFuture<EconomyResponse> canWithdraw(
      @NotNull final Economy economy,
      @NotNull final String pluginName,
      @NotNull final UUID accountID,
      @NotNull final String worldName,
      @NotNull final String currency,
      @NotNull final BigDecimal amount
   ) {
      return economy.async()
         .map(async -> async.canWithdraw(pluginName, accountID, worldName, currency, amount))
         .orElseGet(() -> CompletableFuture.completedFuture(economy.canWithdraw(pluginName, accountID, worldName, currency, amount)));
   }

   public static CompletableFuture<EconomyResponse> withdraw(
      @NotNull final Economy economy, @NotNull final String pluginName, @NotNull final UUID accountID, @NotNull final BigDecimal amount
   ) {
      return economy.async()
         .map(async -> async.withdraw(pluginName, accountID, amount))
         .orElseGet(() -> CompletableFuture.completedFuture(economy.withdraw(pluginName, accountID, amount)));
   }

   public static CompletableFuture<EconomyResponse> withdraw(
      @NotNull final Economy economy,
      @NotNull final String pluginName,
      @NotNull final UUID accountID,
      @NotNull final String worldName,
      @NotNull final BigDecimal amount
   ) {
      return economy.async()
         .map(async -> async.withdraw(pluginName, accountID, worldName, amount))
         .orElseGet(() -> CompletableFuture.completedFuture(economy.withdraw(pluginName, accountID, worldName, amount)));
   }

   public static CompletableFuture<EconomyResponse> withdraw(
      @NotNull final Economy economy,
      @NotNull final String pluginName,
      @NotNull final UUID accountID,
      @NotNull final String worldName,
      @NotNull final String currency,
      @NotNull final BigDecimal amount
   ) {
      return economy.async()
         .map(async -> async.withdraw(pluginName, accountID, worldName, currency, amount))
         .orElseGet(() -> CompletableFuture.completedFuture(economy.withdraw(pluginName, accountID, worldName, currency, amount)));
   }

   public static CompletableFuture<EconomyResponse> canDeposit(
      @NotNull final Economy economy, @NotNull final String pluginName, @NotNull final UUID accountID, @NotNull final BigDecimal amount
   ) {
      return economy.async()
         .map(async -> async.canDeposit(pluginName, accountID, amount))
         .orElseGet(() -> CompletableFuture.completedFuture(economy.canDeposit(pluginName, accountID, amount)));
   }

   public static CompletableFuture<EconomyResponse> canDeposit(
      @NotNull final Economy economy,
      @NotNull final String pluginName,
      @NotNull final UUID accountID,
      @NotNull final String worldName,
      @NotNull final BigDecimal amount
   ) {
      return economy.async()
         .map(async -> async.canDeposit(pluginName, accountID, worldName, amount))
         .orElseGet(() -> CompletableFuture.completedFuture(economy.canDeposit(pluginName, accountID, worldName, amount)));
   }

   public static CompletableFuture<EconomyResponse> canDeposit(
      @NotNull final Economy economy,
      @NotNull final String pluginName,
      @NotNull final UUID accountID,
      @NotNull final String worldName,
      @NotNull final String currency,
      @NotNull final BigDecimal amount
   ) {
      return economy.async()
         .map(async -> async.canDeposit(pluginName, accountID, worldName, currency, amount))
         .orElseGet(() -> CompletableFuture.completedFuture(economy.canDeposit(pluginName, accountID, worldName, currency, amount)));
   }

   public static CompletableFuture<EconomyResponse> deposit(
      @NotNull final Economy economy, @NotNull final String pluginName, @NotNull final UUID accountID, @NotNull final BigDecimal amount
   ) {
      return economy.async()
         .map(async -> async.deposit(pluginName, accountID, amount))
         .orElseGet(() -> CompletableFuture.completedFuture(economy.deposit(pluginName, accountID, amount)));
   }

   public static CompletableFuture<EconomyResponse> deposit(
      @NotNull final Economy economy,
      @NotNull final String pluginName,
      @NotNull final UUID accountID,
      @NotNull final String worldName,
      @NotNull final BigDecimal amount
   ) {
      return economy.async()
         .map(async -> async.deposit(pluginName, accountID, worldName, amount))
         .orElseGet(() -> CompletableFuture.completedFuture(economy.deposit(pluginName, accountID, worldName, amount)));
   }

   public static CompletableFuture<EconomyResponse> deposit(
      @NotNull final Economy economy,
      @NotNull final String pluginName,
      @NotNull final UUID accountID,
      @NotNull final String worldName,
      @NotNull final String currency,
      @NotNull final BigDecimal amount
   ) {
      return economy.async()
         .map(async -> async.deposit(pluginName, accountID, worldName, currency, amount))
         .orElseGet(() -> CompletableFuture.completedFuture(economy.deposit(pluginName, accountID, worldName, currency, amount)));
   }

   public static CompletableFuture<Boolean> createSharedAccount(
      @NotNull final Economy economy, @NotNull final String pluginName, @NotNull final UUID accountID, @NotNull final String name, @NotNull final UUID owner
   ) {
      return economy.async()
         .map(async -> async.createSharedAccount(pluginName, accountID, name, owner))
         .orElseGet(() -> CompletableFuture.completedFuture(economy.createSharedAccount(pluginName, accountID, name, owner)));
   }

   public static CompletableFuture<List<UUID>> accountsWithOwnerOf(
      @NotNull final Economy economy, @NotNull final String pluginName, @NotNull final UUID accountID
   ) {
      return economy.async()
         .map(async -> async.accountsWithOwnerOf(pluginName, accountID))
         .orElseGet(() -> CompletableFuture.completedFuture(economy.accountsWithOwnerOf(pluginName, accountID)));
   }

   public static CompletableFuture<List<UUID>> accountsWithMembershipTo(
      @NotNull final Economy economy, @NotNull final String pluginName, @NotNull final UUID accountID
   ) {
      return economy.async()
         .map(async -> async.accountsWithMembershipTo(pluginName, accountID))
         .orElseGet(() -> CompletableFuture.completedFuture(economy.accountsWithMembershipTo(pluginName, accountID)));
   }

   public static CompletableFuture<List<UUID>> accountsWithAccessTo(
      @NotNull final Economy economy, @NotNull final String pluginName, @NotNull final UUID accountID, @NotNull final AccountPermission... permissions
   ) {
      return economy.async()
         .map(async -> async.accountsWithAccessTo(pluginName, accountID, permissions))
         .orElseGet(() -> CompletableFuture.completedFuture(economy.accountsWithAccessTo(pluginName, accountID, permissions)));
   }

   public static CompletableFuture<Boolean> isAccountOwner(
      @NotNull final Economy economy, @NotNull final String pluginName, @NotNull final UUID accountID, @NotNull final UUID uuid
   ) {
      return economy.async()
         .map(async -> async.isAccountOwner(pluginName, accountID, uuid))
         .orElseGet(() -> CompletableFuture.completedFuture(economy.isAccountOwner(pluginName, accountID, uuid)));
   }

   public static CompletableFuture<Boolean> setOwner(
      @NotNull final Economy economy, @NotNull final String pluginName, @NotNull final UUID accountID, @NotNull final UUID uuid
   ) {
      return economy.async()
         .map(async -> async.setOwner(pluginName, accountID, uuid))
         .orElseGet(() -> CompletableFuture.completedFuture(economy.setOwner(pluginName, accountID, uuid)));
   }

   public static CompletableFuture<Boolean> isAccountMember(
      @NotNull final Economy economy, @NotNull final String pluginName, @NotNull final UUID accountID, @NotNull final UUID uuid
   ) {
      return economy.async()
         .map(async -> async.isAccountMember(pluginName, accountID, uuid))
         .orElseGet(() -> CompletableFuture.completedFuture(economy.isAccountMember(pluginName, accountID, uuid)));
   }

   public static CompletableFuture<Boolean> addAccountMember(
      @NotNull final Economy economy, @NotNull final String pluginName, @NotNull final UUID accountID, @NotNull final UUID uuid
   ) {
      return economy.async()
         .map(async -> async.addAccountMember(pluginName, accountID, uuid))
         .orElseGet(() -> CompletableFuture.completedFuture(economy.addAccountMember(pluginName, accountID, uuid)));
   }

   public static CompletableFuture<Boolean> addAccountMember(
      @NotNull final Economy economy,
      @NotNull final String pluginName,
      @NotNull final UUID accountID,
      @NotNull final UUID uuid,
      @NotNull final AccountPermission... initialPermissions
   ) {
      return economy.async()
         .map(async -> async.addAccountMember(pluginName, accountID, uuid, initialPermissions))
         .orElseGet(() -> CompletableFuture.completedFuture(economy.addAccountMember(pluginName, accountID, uuid, initialPermissions)));
   }

   public static CompletableFuture<Boolean> removeAccountMember(
      @NotNull final Economy economy, @NotNull final String pluginName, @NotNull final UUID accountID, @NotNull final UUID uuid
   ) {
      return economy.async()
         .map(async -> async.removeAccountMember(pluginName, accountID, uuid))
         .orElseGet(() -> CompletableFuture.completedFuture(economy.removeAccountMember(pluginName, accountID, uuid)));
   }

   public static CompletableFuture<Boolean> hasAccountPermission(
      @NotNull final Economy economy,
      @NotNull final String pluginName,
      @NotNull final UUID accountID,
      @NotNull final UUID uuid,
      @NotNull final AccountPermission permission
   ) {
      return economy.async()
         .map(async -> async.hasAccountPermission(pluginName, accountID, uuid, permission))
         .orElseGet(() -> CompletableFuture.completedFuture(economy.hasAccountPermission(pluginName, accountID, uuid, permission)));
   }

   public static CompletableFuture<Boolean> updateAccountPermission(
      @NotNull final Economy economy,
      @NotNull final String pluginName,
      @NotNull final UUID accountID,
      @NotNull final UUID uuid,
      @NotNull final AccountPermission permission,
      final boolean value
   ) {
      return economy.async()
         .map(async -> async.updateAccountPermission(pluginName, accountID, uuid, permission, value))
         .orElseGet(() -> CompletableFuture.completedFuture(economy.updateAccountPermission(pluginName, accountID, uuid, permission, value)));
   }
}
