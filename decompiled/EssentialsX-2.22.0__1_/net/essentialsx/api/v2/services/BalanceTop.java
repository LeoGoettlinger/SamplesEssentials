package net.essentialsx.api.v2.services;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface BalanceTop {
   CompletableFuture<Void> calculateBalanceTopMapAsync();

   Map<UUID, BalanceTop.Entry> getBalanceTopCache();

   long getCacheAge();

   BigDecimal getBalanceTopTotal();

   boolean isCacheLocked();

   public static class Entry {
      private final UUID uuid;
      private final String displayName;
      private final BigDecimal balance;

      public Entry(UUID uuid, String displayName, BigDecimal balance) {
         this.uuid = uuid;
         this.displayName = displayName;
         this.balance = balance;
      }

      public UUID getUuid() {
         return this.uuid;
      }

      public String getDisplayName() {
         return this.displayName;
      }

      public BigDecimal getBalance() {
         return this.balance;
      }
   }
}
