package net.milkbowl.vault2.economy;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;

public interface Economy {
   boolean isEnabled();

   @NotNull
   String getName();

   boolean hasSharedAccountSupport();

   boolean hasMultiCurrencySupport();

   default boolean supportsAsync() {
      return this.async().isPresent();
   }

   default Optional<AsyncEconomy> async() {
      return Optional.empty();
   }

   int fractionalDigits(@NotNull final String pluginName);

   default int fractionalDigits(@NotNull final String pluginName, @NotNull final String currency) {
      return this.fractionalDigits(pluginName);
   }

   @Deprecated
   @NotNull
   String format(@NotNull final BigDecimal amount);

   @NotNull
   String format(@NotNull final String pluginName, @NotNull final BigDecimal amount);

   @Deprecated
   @NotNull
   String format(@NotNull final BigDecimal amount, @NotNull final String currency);

   @NotNull
   String format(@NotNull final String pluginName, @NotNull final BigDecimal amount, @NotNull final String currency);

   boolean hasCurrency(@NotNull final String currency);

   @NotNull
   String getDefaultCurrency(@NotNull final String pluginName);

   @NotNull
   String defaultCurrencyNamePlural(@NotNull final String pluginName);

   @NotNull
   String defaultCurrencyNameSingular(@NotNull final String pluginName);

   @NotNull
   Collection<String> currencies();

   @Deprecated
   boolean createAccount(@NotNull final UUID accountID, @NotNull final String name);

   boolean createAccount(@NotNull final UUID accountID, @NotNull final String name, final boolean player);

   @Deprecated
   boolean createAccount(@NotNull final UUID accountID, @NotNull final String name, @NotNull final String worldName);

   boolean createAccount(@NotNull final UUID accountID, @NotNull final String name, @NotNull final String worldName, final boolean player);

   @NotNull
   Map<UUID, String> getUUIDNameMap();

   Optional<String> getAccountName(@NotNull final UUID accountID);

   boolean hasAccount(@NotNull final UUID accountID);

   boolean hasAccount(@NotNull final UUID accountID, @NotNull final String worldName);

   boolean renameAccount(@NotNull final UUID accountID, @NotNull final String name);

   boolean renameAccount(@NotNull final String pluginName, @NotNull final UUID accountID, @NotNull final String name);

   boolean deleteAccount(@NotNull final String pluginName, @NotNull final UUID accountID);

   boolean accountSupportsCurrency(@NotNull final String pluginName, @NotNull final UUID accountID, @NotNull final String currency);

   boolean accountSupportsCurrency(@NotNull final String pluginName, @NotNull final UUID accountID, @NotNull final String currency, @NotNull final String world);

   @Deprecated
   @NotNull
   BigDecimal getBalance(@NotNull final String pluginName, @NotNull final UUID accountID);

   @Deprecated
   @NotNull
   BigDecimal getBalance(@NotNull final String pluginName, @NotNull final UUID accountID, @NotNull final String world);

   @Deprecated
   @NotNull
   BigDecimal getBalance(@NotNull final String pluginName, @NotNull final UUID accountID, @NotNull final String world, @NotNull final String currency);

   @NotNull
   default BigDecimal balance(@NotNull final String pluginName, @NotNull final UUID accountID) {
      return this.getBalance(pluginName, accountID);
   }

   @NotNull
   default BigDecimal balance(@NotNull final String pluginName, @NotNull final UUID accountID, @NotNull final String world) {
      return this.getBalance(pluginName, accountID, world);
   }

   @NotNull
   default BigDecimal balance(@NotNull final String pluginName, @NotNull final UUID accountID, @NotNull final String world, @NotNull final String currency) {
      return this.getBalance(pluginName, accountID, world, currency);
   }

   boolean has(@NotNull final String pluginName, @NotNull final UUID accountID, @NotNull final BigDecimal amount);

   boolean has(@NotNull final String pluginName, @NotNull final UUID accountID, @NotNull final String worldName, @NotNull final BigDecimal amount);

   boolean has(
      @NotNull final String pluginName,
      @NotNull final UUID accountID,
      @NotNull final String worldName,
      @NotNull final String currency,
      @NotNull final BigDecimal amount
   );

   default EconomyResponse set(@NotNull final String pluginName, @NotNull final UUID accountID, @NotNull final BigDecimal amount) {
      BigDecimal balance = this.balance(pluginName, accountID);
      int compare = balance.compareTo(amount);
      if (compare > 0) {
         return this.withdraw(pluginName, accountID, balance.subtract(amount));
      } else {
         return compare < 0
            ? this.deposit(pluginName, accountID, amount.subtract(balance))
            : new EconomyResponse(BigDecimal.ZERO, amount, EconomyResponse.ResponseType.SUCCESS, "");
      }
   }

   default EconomyResponse set(
      @NotNull final String pluginName, @NotNull final UUID accountID, @NotNull final String worldName, @NotNull final BigDecimal amount
   ) {
      BigDecimal balance = this.balance(pluginName, accountID, worldName);
      int compare = balance.compareTo(amount);
      if (compare > 0) {
         return this.withdraw(pluginName, accountID, worldName, balance.subtract(amount));
      } else {
         return compare < 0
            ? this.deposit(pluginName, accountID, worldName, amount.subtract(balance))
            : new EconomyResponse(BigDecimal.ZERO, amount, EconomyResponse.ResponseType.SUCCESS, "");
      }
   }

   default EconomyResponse set(
      @NotNull final String pluginName,
      @NotNull final UUID accountID,
      @NotNull final String worldName,
      @NotNull final String currency,
      @NotNull final BigDecimal amount
   ) {
      BigDecimal balance = this.balance(pluginName, accountID, worldName, currency);
      int compare = balance.compareTo(amount);
      if (compare > 0) {
         return this.withdraw(pluginName, accountID, worldName, currency, balance.subtract(amount));
      } else {
         return compare < 0
            ? this.deposit(pluginName, accountID, worldName, currency, amount.subtract(balance))
            : new EconomyResponse(BigDecimal.ZERO, amount, EconomyResponse.ResponseType.SUCCESS, "");
      }
   }

   default MultiEconomyResponse transfer(@NotNull final String pluginName, @NotNull final UUID from, @NotNull final UUID to, @NotNull final BigDecimal amount) {
      MultiEconomyResponse successResponse = new MultiEconomyResponse(amount, EconomyResponse.ResponseType.SUCCESS, "");
      EconomyResponse withdrawResponse = this.withdraw(pluginName, from, amount);
      if (withdrawResponse.type != EconomyResponse.ResponseType.SUCCESS) {
         return new MultiEconomyResponse(amount, withdrawResponse.type, withdrawResponse.errorMessage);
      } else {
         EconomyResponse depositResponse = this.deposit(pluginName, to, withdrawResponse.amount);
         if (depositResponse.type != EconomyResponse.ResponseType.SUCCESS) {
            this.deposit(pluginName, from, amount);
            return new MultiEconomyResponse(amount, depositResponse.type, depositResponse.errorMessage);
         } else {
            successResponse.addBalance(from, withdrawResponse.balance);
            successResponse.addBalance(to, depositResponse.balance);
            return successResponse;
         }
      }
   }

   default MultiEconomyResponse transfer(
      @NotNull final String pluginName, @NotNull final UUID from, @NotNull final UUID to, @NotNull final String worldName, @NotNull final BigDecimal amount
   ) {
      MultiEconomyResponse successResponse = new MultiEconomyResponse(amount, EconomyResponse.ResponseType.SUCCESS, "");
      EconomyResponse withdrawResponse = this.withdraw(pluginName, from, worldName, amount);
      if (withdrawResponse.type != EconomyResponse.ResponseType.SUCCESS) {
         return new MultiEconomyResponse(amount, withdrawResponse.type, withdrawResponse.errorMessage);
      } else {
         EconomyResponse depositResponse = this.deposit(pluginName, to, worldName, withdrawResponse.amount);
         if (depositResponse.type != EconomyResponse.ResponseType.SUCCESS) {
            this.deposit(pluginName, from, worldName, amount);
            return new MultiEconomyResponse(amount, depositResponse.type, depositResponse.errorMessage);
         } else {
            successResponse.addBalance(from, withdrawResponse.balance);
            successResponse.addBalance(to, depositResponse.balance);
            return successResponse;
         }
      }
   }

   default MultiEconomyResponse transfer(
      @NotNull final String pluginName,
      @NotNull final UUID from,
      @NotNull final UUID to,
      @NotNull final String worldName,
      @NotNull final String currency,
      @NotNull final BigDecimal amount
   ) {
      MultiEconomyResponse successResponse = new MultiEconomyResponse(amount, EconomyResponse.ResponseType.SUCCESS, "");
      EconomyResponse withdrawResponse = this.withdraw(pluginName, from, worldName, currency, amount);
      if (withdrawResponse.type != EconomyResponse.ResponseType.SUCCESS) {
         return new MultiEconomyResponse(amount, withdrawResponse.type, withdrawResponse.errorMessage);
      } else {
         EconomyResponse depositResponse = this.deposit(pluginName, to, worldName, currency, withdrawResponse.amount);
         if (depositResponse.type != EconomyResponse.ResponseType.SUCCESS) {
            this.deposit(pluginName, from, worldName, currency, amount);
            return new MultiEconomyResponse(amount, depositResponse.type, depositResponse.errorMessage);
         } else {
            successResponse.addBalance(from, withdrawResponse.balance);
            successResponse.addBalance(to, depositResponse.balance);
            return successResponse;
         }
      }
   }

   @NotNull
   default EconomyResponse canWithdraw(@NotNull final String pluginName, @NotNull final UUID accountID, @NotNull final BigDecimal amount) {
      return new EconomyResponse(
         BigDecimal.ZERO, BigDecimal.ZERO, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "canWithdraw is not implemented by this economy provider."
      );
   }

   @NotNull
   default EconomyResponse canWithdraw(
      @NotNull final String pluginName, @NotNull final UUID accountID, @NotNull final String worldName, @NotNull final BigDecimal amount
   ) {
      return new EconomyResponse(
         BigDecimal.ZERO, BigDecimal.ZERO, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "canWithdraw is not implemented by this economy provider."
      );
   }

   @NotNull
   default EconomyResponse canWithdraw(
      @NotNull final String pluginName,
      @NotNull final UUID accountID,
      @NotNull final String worldName,
      @NotNull final String currency,
      @NotNull final BigDecimal amount
   ) {
      return new EconomyResponse(
         BigDecimal.ZERO, BigDecimal.ZERO, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "canWithdraw is not implemented by this economy provider."
      );
   }

   @NotNull
   EconomyResponse withdraw(@NotNull final String pluginName, @NotNull final UUID accountID, @NotNull final BigDecimal amount);

   @NotNull
   EconomyResponse withdraw(@NotNull final String pluginName, @NotNull final UUID accountID, @NotNull final String worldName, @NotNull final BigDecimal amount);

   @NotNull
   EconomyResponse withdraw(
      @NotNull final String pluginName,
      @NotNull final UUID accountID,
      @NotNull final String worldName,
      @NotNull final String currency,
      @NotNull final BigDecimal amount
   );

   @NotNull
   default EconomyResponse canDeposit(@NotNull final String pluginName, @NotNull final UUID accountID, @NotNull final BigDecimal amount) {
      return new EconomyResponse(
         BigDecimal.ZERO, BigDecimal.ZERO, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "canDeposit is not implemented by this economy provider."
      );
   }

   @NotNull
   default EconomyResponse canDeposit(
      @NotNull final String pluginName, @NotNull final UUID accountID, @NotNull final String worldName, @NotNull final BigDecimal amount
   ) {
      return new EconomyResponse(
         BigDecimal.ZERO, BigDecimal.ZERO, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "canDeposit is not implemented by this economy provider."
      );
   }

   @NotNull
   default EconomyResponse canDeposit(
      @NotNull final String pluginName,
      @NotNull final UUID accountID,
      @NotNull final String worldName,
      @NotNull final String currency,
      @NotNull final BigDecimal amount
   ) {
      return new EconomyResponse(
         BigDecimal.ZERO, BigDecimal.ZERO, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "canDeposit is not implemented by this economy provider."
      );
   }

   @NotNull
   EconomyResponse deposit(@NotNull final String pluginName, @NotNull final UUID accountID, @NotNull final BigDecimal amount);

   @NotNull
   EconomyResponse deposit(@NotNull final String pluginName, @NotNull final UUID accountID, @NotNull final String worldName, @NotNull final BigDecimal amount);

   @NotNull
   EconomyResponse deposit(
      @NotNull final String pluginName,
      @NotNull final UUID accountID,
      @NotNull final String worldName,
      @NotNull final String currency,
      @NotNull final BigDecimal amount
   );

   boolean createSharedAccount(@NotNull final String pluginName, @NotNull final UUID accountID, @NotNull final String name, @NotNull final UUID owner);

   /** @deprecated */
   default List<String> accountsOwnedBy(@NotNull final String pluginName, @NotNull final UUID accountID) {
      return this.accountsAccessTo(pluginName, accountID, AccountPermission.OWNER);
   }

   default List<UUID> accountsWithOwnerOf(@NotNull final String pluginName, @NotNull final UUID accountID) {
      return this.accountsWithAccessTo(pluginName, accountID, AccountPermission.OWNER);
   }

   /** @deprecated */
   default List<String> accountsMemberOf(@NotNull final String pluginName, @NotNull final UUID accountID) {
      return this.accountsAccessTo(pluginName, accountID, AccountPermission.BALANCE, AccountPermission.DEPOSIT, AccountPermission.WITHDRAW);
   }

   default List<UUID> accountsWithMembershipTo(@NotNull final String pluginName, @NotNull final UUID accountID) {
      return this.accountsWithAccessTo(pluginName, accountID, AccountPermission.BALANCE, AccountPermission.DEPOSIT, AccountPermission.WITHDRAW);
   }

   @Deprecated
   default List<String> accountsAccessTo(@NotNull final String pluginName, @NotNull final UUID accountID, @NotNull final AccountPermission... permissions) {
      return Collections.unmodifiableList(new ArrayList<>());
   }

   default List<UUID> accountsWithAccessTo(@NotNull final String pluginName, @NotNull final UUID accountID, @NotNull final AccountPermission... permissions) {
      return Collections.unmodifiableList(new ArrayList<>());
   }

   boolean isAccountOwner(@NotNull final String pluginName, @NotNull final UUID accountID, @NotNull final UUID uuid);

   boolean setOwner(@NotNull final String pluginName, @NotNull final UUID accountID, @NotNull final UUID uuid);

   boolean isAccountMember(@NotNull final String pluginName, @NotNull final UUID accountID, @NotNull final UUID uuid);

   boolean addAccountMember(@NotNull final String pluginName, @NotNull final UUID accountID, @NotNull final UUID uuid);

   boolean addAccountMember(
      @NotNull final String pluginName, @NotNull final UUID accountID, @NotNull final UUID uuid, @NotNull final AccountPermission... initialPermissions
   );

   boolean removeAccountMember(@NotNull final String pluginName, @NotNull final UUID accountID, @NotNull final UUID uuid);

   boolean hasAccountPermission(
      @NotNull final String pluginName, @NotNull final UUID accountID, @NotNull final UUID uuid, @NotNull final AccountPermission permission
   );

   boolean updateAccountPermission(
      @NotNull final String pluginName,
      @NotNull final UUID accountID,
      @NotNull final UUID uuid,
      @NotNull final AccountPermission permission,
      final boolean value
   );
}
