package su.nightexpress.nightcore.integration.currency.impl;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.integration.currency.type.AbstractCurrency;

public class DummyCurrency extends AbstractCurrency {
   public static final DummyCurrency INSTANCE = new DummyCurrency();

   DummyCurrency() {
      super("dummy");
   }

   @Override
   public boolean canHandleDecimals() {
      return true;
   }

   @Override
   public boolean canHandleOffline() {
      return false;
   }

   @NonNull
   @Override
   public String getName() {
      return "Dummy";
   }

   @NonNull
   @Override
   public String getFormat() {
      return "%amount% [Dummy]";
   }

   @NonNull
   @Override
   public ItemStack getIcon() {
      return new ItemStack(Material.BRICK);
   }

   @NonNull
   @Override
   public CompletableFuture<Double> queryBalanceAsync(@NonNull Player player) {
      return CompletableFuture.completedFuture(0.0);
   }

   @NonNull
   @Override
   public CompletableFuture<Double> queryBalanceAsync(@NonNull UUID playerId) {
      return CompletableFuture.completedFuture(0.0);
   }

   @Override
   protected double queryBalanceDirect(@NonNull Player player) {
      return 0.0;
   }

   @NonNull
   @Override
   public CompletableFuture<Boolean> depositAsync(@NonNull UUID playerId, double amount) {
      return CompletableFuture.completedFuture(false);
   }

   @NonNull
   @Override
   public CompletableFuture<Boolean> depositAsync(@NonNull Player player, double amount) {
      return CompletableFuture.completedFuture(false);
   }

   @Override
   protected void depositDirect(@NonNull Player player, double amount) {
   }

   @NonNull
   @Override
   public CompletableFuture<Boolean> withdrawAsync(@NonNull Player player, double amount) {
      return CompletableFuture.completedFuture(false);
   }

   @NonNull
   @Override
   public CompletableFuture<Boolean> withdrawAsync(@NonNull UUID playerId, double amount) {
      return CompletableFuture.completedFuture(false);
   }

   @Override
   protected void withdrawDirect(@NonNull Player player, double amount) {
   }
}
