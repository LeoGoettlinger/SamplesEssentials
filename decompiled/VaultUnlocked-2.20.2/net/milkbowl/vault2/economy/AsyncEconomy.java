package net.milkbowl.vault2.economy;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.NotNull;

public interface AsyncEconomy {
   @NotNull
   CompletableFuture<Boolean> createAccount(@NotNull UUID accountID, @NotNull String name, boolean player);

   @NotNull
   CompletableFuture<Boolean> createAccount(@NotNull UUID accountID, @NotNull String name, @NotNull String worldName, boolean player);

   @NotNull
   CompletableFuture<Map<UUID, String>> getUUIDNameMap();

   @NotNull
   CompletableFuture<Optional<String>> getAccountName(@NotNull UUID accountID);

   @NotNull
   CompletableFuture<Boolean> hasAccount(@NotNull UUID accountID);

   @NotNull
   CompletableFuture<Boolean> hasAccount(@NotNull UUID accountID, @NotNull String worldName);

   @NotNull
   CompletableFuture<Boolean> renameAccount(@NotNull String pluginName, @NotNull UUID accountID, @NotNull String name);

   @NotNull
   CompletableFuture<Boolean> deleteAccount(@NotNull String pluginName, @NotNull UUID accountID);

   @NotNull
   CompletableFuture<Boolean> accountSupportsCurrency(@NotNull String pluginName, @NotNull UUID accountID, @NotNull String currency);

   @NotNull
   CompletableFuture<Boolean> accountSupportsCurrency(@NotNull String pluginName, @NotNull UUID accountID, @NotNull String currency, @NotNull String world);

   @NotNull
   CompletableFuture<BigDecimal> balance(@NotNull String pluginName, @NotNull UUID accountID);

   @NotNull
   CompletableFuture<BigDecimal> balance(@NotNull String pluginName, @NotNull UUID accountID, @NotNull String world);

   @NotNull
   CompletableFuture<BigDecimal> balance(@NotNull String pluginName, @NotNull UUID accountID, @NotNull String world, @NotNull String currency);

   @NotNull
   CompletableFuture<Boolean> has(@NotNull String pluginName, @NotNull UUID accountID, @NotNull BigDecimal amount);

   @NotNull
   CompletableFuture<Boolean> has(@NotNull String pluginName, @NotNull UUID accountID, @NotNull String world, @NotNull BigDecimal amount);

   @NotNull
   CompletableFuture<Boolean> has(
      @NotNull String pluginName, @NotNull UUID accountID, @NotNull String world, @NotNull String currency, @NotNull BigDecimal amount
   );

   @NotNull
   CompletableFuture<EconomyResponse> set(@NotNull String pluginName, @NotNull UUID accountID, @NotNull BigDecimal amount);

   @NotNull
   CompletableFuture<EconomyResponse> set(@NotNull String pluginName, @NotNull UUID accountID, @NotNull String world, @NotNull BigDecimal amount);

   @NotNull
   CompletableFuture<EconomyResponse> set(
      @NotNull String pluginName, @NotNull UUID accountID, @NotNull String world, @NotNull String currency, @NotNull BigDecimal amount
   );

   CompletableFuture<MultiEconomyResponse> transfer(
      @NotNull final String pluginName, @NotNull final UUID from, @NotNull final UUID to, @NotNull final BigDecimal amount
   );

   CompletableFuture<MultiEconomyResponse> transfer(
      @NotNull final String pluginName, @NotNull final UUID from, @NotNull final UUID to, @NotNull final String worldName, @NotNull final BigDecimal amount
   );

   CompletableFuture<MultiEconomyResponse> transfer(
      @NotNull final String pluginName,
      @NotNull final UUID from,
      @NotNull final UUID to,
      @NotNull final String worldName,
      @NotNull final String currency,
      @NotNull final BigDecimal amount
   );

   @NotNull
   CompletableFuture<EconomyResponse> canWithdraw(@NotNull String pluginName, @NotNull UUID accountID, @NotNull BigDecimal amount);

   @NotNull
   CompletableFuture<EconomyResponse> canWithdraw(@NotNull String pluginName, @NotNull UUID accountID, @NotNull String world, @NotNull BigDecimal amount);

   @NotNull
   CompletableFuture<EconomyResponse> canWithdraw(
      @NotNull String pluginName, @NotNull UUID accountID, @NotNull String world, @NotNull String currency, @NotNull BigDecimal amount
   );

   @NotNull
   CompletableFuture<EconomyResponse> withdraw(@NotNull String pluginName, @NotNull UUID accountID, @NotNull BigDecimal amount);

   @NotNull
   CompletableFuture<EconomyResponse> withdraw(@NotNull String pluginName, @NotNull UUID accountID, @NotNull String world, @NotNull BigDecimal amount);

   @NotNull
   CompletableFuture<EconomyResponse> withdraw(
      @NotNull String pluginName, @NotNull UUID accountID, @NotNull String world, @NotNull String currency, @NotNull BigDecimal amount
   );

   @NotNull
   CompletableFuture<EconomyResponse> canDeposit(@NotNull String pluginName, @NotNull UUID accountID, @NotNull BigDecimal amount);

   @NotNull
   CompletableFuture<EconomyResponse> canDeposit(@NotNull String pluginName, @NotNull UUID accountID, @NotNull String world, @NotNull BigDecimal amount);

   @NotNull
   CompletableFuture<EconomyResponse> canDeposit(
      @NotNull String pluginName, @NotNull UUID accountID, @NotNull String world, @NotNull String currency, @NotNull BigDecimal amount
   );

   @NotNull
   CompletableFuture<EconomyResponse> deposit(@NotNull String pluginName, @NotNull UUID accountID, @NotNull BigDecimal amount);

   @NotNull
   CompletableFuture<EconomyResponse> deposit(@NotNull String pluginName, @NotNull UUID accountID, @NotNull String world, @NotNull BigDecimal amount);

   @NotNull
   CompletableFuture<EconomyResponse> deposit(
      @NotNull String pluginName, @NotNull UUID accountID, @NotNull String world, @NotNull String currency, @NotNull BigDecimal amount
   );

   @NotNull
   CompletableFuture<Boolean> createSharedAccount(@NotNull String pluginName, @NotNull UUID accountID, @NotNull String name, @NotNull UUID owner);

   @NotNull
   CompletableFuture<List<UUID>> accountsWithOwnerOf(@NotNull String pluginName, @NotNull UUID accountID);

   @NotNull
   CompletableFuture<List<UUID>> accountsWithMembershipTo(@NotNull String pluginName, @NotNull UUID accountID);

   @NotNull
   CompletableFuture<List<UUID>> accountsWithAccessTo(@NotNull String pluginName, @NotNull UUID accountID, @NotNull AccountPermission... permissions);

   @NotNull
   CompletableFuture<Boolean> isAccountOwner(@NotNull String pluginName, @NotNull UUID accountID, @NotNull UUID uuid);

   @NotNull
   CompletableFuture<Boolean> setOwner(@NotNull String pluginName, @NotNull UUID accountID, @NotNull UUID uuid);

   @NotNull
   CompletableFuture<Boolean> isAccountMember(@NotNull String pluginName, @NotNull UUID accountID, @NotNull UUID uuid);

   @NotNull
   CompletableFuture<Boolean> addAccountMember(@NotNull String pluginName, @NotNull UUID accountID, @NotNull UUID uuid);

   @NotNull
   CompletableFuture<Boolean> addAccountMember(
      @NotNull String pluginName, @NotNull UUID accountID, @NotNull UUID uuid, @NotNull AccountPermission... initialPermissions
   );

   @NotNull
   CompletableFuture<Boolean> removeAccountMember(@NotNull String pluginName, @NotNull UUID accountID, @NotNull UUID uuid);

   @NotNull
   CompletableFuture<Boolean> hasAccountPermission(
      @NotNull String pluginName, @NotNull UUID accountID, @NotNull UUID uuid, @NotNull AccountPermission permission
   );

   @NotNull
   CompletableFuture<Boolean> updateAccountPermission(
      @NotNull String pluginName, @NotNull UUID accountID, @NotNull UUID uuid, @NotNull AccountPermission permission, boolean value
   );
}
