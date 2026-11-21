package su.nightexpress.nightcore.bridge.currency;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public interface EconomyBridgeAPI {
   boolean hasCurrency(@NonNull String var1);

   boolean hasVaultCurrency();

   boolean hasAnyCurrency();

   void register(@NonNull Currency var1);

   boolean unregister(@NonNull Currency var1);

   boolean unregister(@NonNull String var1);

   @NonNull
   Set<Currency> getCurrencies();

   @NonNull
   Set<String> getCurrencyIds();

   @NonNull
   Optional<Currency> currency(@NonNull String var1);

   @Nullable
   Currency getCurrency(@NonNull String var1);

   @NonNull
   Currency getCurrencyOrDummy(@NonNull String var1);

   @Nullable
   Currency getVaultCurrency();

   @NonNull
   Optional<Currency> vaultCurrency();

   @NonNull
   Currency getDummyCurrency();

   boolean canAfford(@NonNull Player var1, double var2);

   boolean canAfford(@NonNull UUID var1, double var2);

   @NonNull
   CompletableFuture<Boolean> canAffordAsync(@NonNull Player var1, double var2);

   @NonNull
   CompletableFuture<Boolean> canAffordAsync(@NonNull UUID var1, double var2);

   boolean canAfford(@NonNull Player var1, @NonNull String var2, double var3);

   boolean canAfford(@NonNull UUID var1, @NonNull String var2, double var3);

   @NonNull
   CompletableFuture<Boolean> canAffordAsync(@NonNull Player var1, @NonNull String var2, double var3);

   @NonNull
   CompletableFuture<Boolean> canAffordAsync(@NonNull UUID var1, @NonNull String var2, double var3);

   double queryBalance(@NonNull Player var1);

   double queryBalance(@NonNull UUID var1);

   @NonNull
   CompletableFuture<Double> queryBalanceAsync(@NonNull Player var1);

   @NonNull
   CompletableFuture<Double> queryBalanceAsync(@NonNull UUID var1);

   double queryBalance(@NonNull Player var1, @NonNull String var2);

   double queryBalance(@NonNull UUID var1, @NonNull String var2);

   @NonNull
   CompletableFuture<Double> queryBalanceAsync(@NonNull Player var1, @NonNull String var2);

   @NonNull
   CompletableFuture<Double> queryBalanceAsync(@NonNull UUID var1, @NonNull String var2);

   void deposit(@NonNull Player var1, double var2);

   void deposit(@NonNull UUID var1, double var2);

   @NonNull
   CompletableFuture<Boolean> depositAsync(@NonNull Player var1, double var2);

   @NonNull
   CompletableFuture<Boolean> depositAsync(@NonNull UUID var1, double var2);

   void deposit(@NonNull Player var1, @NonNull String var2, double var3);

   void deposit(@NonNull UUID var1, @NonNull String var2, double var3);

   @NonNull
   CompletableFuture<Boolean> depositAsync(@NonNull Player var1, @NonNull String var2, double var3);

   @NonNull
   CompletableFuture<Boolean> depositAsync(@NonNull UUID var1, @NonNull String var2, double var3);

   void withdraw(@NonNull Player var1, double var2);

   void withdraw(@NonNull UUID var1, double var2);

   @NonNull
   CompletableFuture<Boolean> withdrawsync(@NonNull Player var1, double var2);

   @NonNull
   CompletableFuture<Boolean> withdrawAsync(@NonNull UUID var1, double var2);

   void withdraw(@NonNull Player var1, @NonNull String var2, double var3);

   void withdraw(@NonNull UUID var1, @NonNull String var2, double var3);

   @NonNull
   CompletableFuture<Boolean> withdrawsync(@NonNull Player var1, @NonNull String var2, double var3);

   @NonNull
   CompletableFuture<Boolean> withdrawAsync(@NonNull UUID var1, @NonNull String var2, double var3);
}
