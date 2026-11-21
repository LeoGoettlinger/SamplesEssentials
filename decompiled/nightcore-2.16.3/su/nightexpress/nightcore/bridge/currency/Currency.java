package su.nightexpress.nightcore.bridge.currency;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.UnaryOperator;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;

public interface Currency {
   boolean isDummy();

   @NonNull
   UnaryOperator<String> replacePlaceholders();

   double floorIfNeeded(double var1);

   @NonNull
   String formatValue(double var1);

   @NonNull
   String format(double var1);

   @NonNull
   String applyFormat(@NonNull String var1, double var2);

   @Deprecated(
      forRemoval = true
   )
   default double getBalance(@NonNull Player player) {
      return this.queryBalance(player);
   }

   @Deprecated(
      forRemoval = true
   )
   default double getBalance(@NonNull UUID playerId) {
      return this.queryBalance(playerId);
   }

   @Deprecated(
      forRemoval = true
   )
   default void give(@NonNull Player player, double amount) {
      this.give(player.getUniqueId(), amount);
   }

   @Deprecated(
      forRemoval = true
   )
   default void give(@NonNull UUID playerId, double amount) {
      this.depositAsync(playerId, amount);
   }

   @Deprecated(
      forRemoval = true
   )
   default void take(@NonNull Player player, double amount) {
      this.take(player.getUniqueId(), amount);
   }

   @Deprecated(
      forRemoval = true
   )
   default void take(@NonNull UUID playerId, double amount) {
      this.withdrawAsync(playerId, amount);
   }

   default boolean canAfford(@NonNull Player player, double amount) {
      return this.queryBalance(player) >= amount;
   }

   default boolean canAfford(@NonNull UUID playerId, double amount) {
      return this.queryBalance(playerId) >= amount;
   }

   @NonNull
   default CompletableFuture<Boolean> canAffordAsync(@NonNull Player player, double amount) {
      return this.queryBalanceAsync(player).thenApply(balance -> balance >= amount);
   }

   @NonNull
   default CompletableFuture<Boolean> canAffordAsync(@NonNull UUID playerId, double amount) {
      return this.queryBalanceAsync(playerId).thenApply(balance -> balance >= amount);
   }

   double queryBalance(@NonNull Player var1);

   double queryBalance(@NonNull UUID var1);

   @NonNull
   CompletableFuture<Double> queryBalanceAsync(@NonNull Player var1);

   @NonNull
   CompletableFuture<Double> queryBalanceAsync(@NonNull UUID var1);

   void deposit(@NonNull Player var1, double var2);

   void deposit(@NonNull UUID var1, double var2);

   @NonNull
   CompletableFuture<Boolean> depositAsync(@NonNull Player var1, double var2);

   @NonNull
   CompletableFuture<Boolean> depositAsync(@NonNull UUID var1, double var2);

   void withdraw(@NonNull Player var1, double var2);

   void withdraw(@NonNull UUID var1, double var2);

   @NonNull
   CompletableFuture<Boolean> withdrawAsync(@NonNull Player var1, double var2);

   @NonNull
   CompletableFuture<Boolean> withdrawAsync(@NonNull UUID var1, double var2);

   boolean canHandleDecimals();

   boolean canHandleOffline();

   @NonNull
   String getOriginalId();

   @NonNull
   String getInternalId();

   @NonNull
   String getName();

   @NonNull
   String getFormat();

   @NonNull
   ItemStack getIcon();
}
