package su.nightexpress.nightcore.integration.currency;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.bridge.currency.Currency;
import su.nightexpress.nightcore.bridge.currency.EconomyBridgeAPI;
import su.nightexpress.nightcore.bridge.registry.NightRegistry;
import su.nightexpress.nightcore.integration.currency.impl.DummyCurrency;

public class EconomyBridge {
   private static EconomyBridgeAPI api;

   static void register(@NonNull EconomyBridgeAPI api) {
      EconomyBridge.api = api;
   }

   public static boolean initialized() {
      return api != null;
   }

   @NonNull
   public static EconomyBridgeAPI api() {
      if (api == null) {
         throw new IllegalStateException("API is not initialized!");
      } else {
         return api;
      }
   }

   @Deprecated(
      forRemoval = true
   )
   public static void unregisterAll() {
      registry().clear();
   }

   @Deprecated(
      forRemoval = true
   )
   @NonNull
   public static NightRegistry<String, Currency> registry() {
      throw new UnsupportedOperationException("Not supported");
   }

   @Deprecated(
      forRemoval = true
   )
   public static boolean hasCurrency(@NonNull String id) {
      return getCurrency(id) != null;
   }

   @Deprecated(
      forRemoval = true
   )
   public static boolean hasEconomy() {
      return hasCurrency("vault");
   }

   @Deprecated(
      forRemoval = true
   )
   public static boolean handle(@NonNull String id, @NonNull Consumer<Currency> consumer) {
      Currency currency = getCurrency(id);
      if (currency == null) {
         return false;
      } else {
         consumer.accept(currency);
         return true;
      }
   }

   @Deprecated(
      forRemoval = true
   )
   public static boolean hasEnough(@NonNull Player player, @NonNull String id, double amount) {
      return getBalance(player, id) >= amount;
   }

   @Deprecated(
      forRemoval = true
   )
   public static boolean hasEnough(@NonNull UUID playerId, @NonNull String id, double amount) {
      return getBalance(playerId, id) >= amount;
   }

   @Deprecated(
      forRemoval = true
   )
   public static double getBalance(@NonNull Player player, @NonNull String id) {
      Currency currency = getCurrency(id);
      return currency == null ? 0.0 : currency.queryBalance(player);
   }

   @Deprecated(
      forRemoval = true
   )
   public static double getBalance(@NonNull UUID playerId, @NonNull String id) {
      Currency currency = getCurrency(id);
      return currency == null ? 0.0 : currency.queryBalance(playerId);
   }

   @Deprecated(
      forRemoval = true
   )
   public static double getEconomyBalance(@NonNull Player player) {
      return getEconomyBalance(player.getUniqueId());
   }

   @Deprecated(
      forRemoval = true
   )
   public static double getEconomyBalance(@NonNull UUID playerId) {
      return getBalance(playerId, "vault");
   }

   @Deprecated(
      forRemoval = true
   )
   public static boolean deposit(@NonNull Player player, double amount) {
      deposit(player.getUniqueId(), amount);
      return true;
   }

   @Deprecated(
      forRemoval = true
   )
   public static boolean deposit(@NonNull UUID playerId, double amount) {
      api.deposit(playerId, "vault", amount);
      return true;
   }

   @Deprecated(
      forRemoval = true
   )
   public static boolean deposit(@NonNull Player player, @NonNull String id, double amount) {
      deposit(player.getUniqueId(), id, amount);
      return true;
   }

   @Deprecated(
      forRemoval = true
   )
   public static boolean deposit(@NonNull UUID playerId, @NonNull String id, double amount) {
      api.deposit(playerId, id, amount);
      return true;
   }

   @Deprecated(
      forRemoval = true
   )
   public static boolean depositEconomy(@NonNull Player player, double amount) {
      return depositEconomy(player.getUniqueId(), amount);
   }

   @Deprecated(
      forRemoval = true
   )
   public static boolean depositEconomy(@NonNull UUID playerId, double amount) {
      api.deposit(playerId, "vault", amount);
      return true;
   }

   @Deprecated(
      forRemoval = true
   )
   public static boolean withdraw(@NonNull Player player, double amount) {
      return withdraw(player.getUniqueId(), amount);
   }

   @Deprecated(
      forRemoval = true
   )
   public static boolean withdraw(@NonNull UUID playerId, double amount) {
      api.withdraw(playerId, "vault", amount);
      return true;
   }

   @Deprecated(
      forRemoval = true
   )
   public static boolean withdraw(@NonNull Player player, @NonNull String id, double amount) {
      withdraw(player.getUniqueId(), id, amount);
      return true;
   }

   @Deprecated(
      forRemoval = true
   )
   public static boolean withdraw(@NonNull UUID playerId, @NonNull String id, double amount) {
      api.withdraw(playerId, id, amount);
      return true;
   }

   @Deprecated(
      forRemoval = true
   )
   public static boolean withdrawEconomy(@NonNull Player player, double amount) {
      return withdrawEconomy(player.getUniqueId(), amount);
   }

   @Deprecated(
      forRemoval = true
   )
   public static boolean withdrawEconomy(@NonNull UUID playerId, double amount) {
      api.withdraw(playerId, "vault", amount);
      return true;
   }

   @Deprecated(
      forRemoval = true
   )
   public static boolean hasCurrency() {
      return api.hasAnyCurrency();
   }

   @Deprecated(
      forRemoval = true
   )
   public static void register(@NonNull Currency currency) {
      api.register(currency);
   }

   @Deprecated(
      forRemoval = true
   )
   @NonNull
   public static Set<Currency> getCurrencies() {
      return api.getCurrencies();
   }

   @Deprecated(
      forRemoval = true
   )
   @NonNull
   public static Set<String> getCurrencyIds() {
      return api.getCurrencyIds();
   }

   @Deprecated(
      forRemoval = true
   )
   @NonNull
   public static Optional<Currency> currency(@NonNull String internalId) {
      return Optional.ofNullable(getCurrency(internalId));
   }

   @Deprecated(
      forRemoval = true
   )
   @Nullable
   public static Currency getCurrency(@NonNull String internalId) {
      return api.getCurrency(internalId);
   }

   @Deprecated(
      forRemoval = true
   )
   @NonNull
   public static Currency getCurrencyOrDummy(@NonNull String internalId) {
      return api.getCurrencyOrDummy(internalId);
   }

   @Deprecated(
      forRemoval = true
   )
   @Nullable
   public static Currency getEconomyCurrency() {
      return getCurrency("vault");
   }

   @Deprecated(
      forRemoval = true
   )
   @NonNull
   public static Optional<Currency> economyCurrency() {
      return Optional.ofNullable(getEconomyCurrency());
   }

   @Deprecated(
      forRemoval = true
   )
   @NonNull
   public static DummyCurrency getDummyCurrency() {
      return DummyCurrency.INSTANCE;
   }
}
