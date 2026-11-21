package com.earth2me.essentials;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.essentialsx.api.v2.services.BalanceTop;
import org.bukkit.plugin.ServicePriority;

public class BalanceTopImpl implements BalanceTop {
   private final net.ess3.api.IEssentials ess;
   private LinkedHashMap<UUID, BalanceTop.Entry> topCache = new LinkedHashMap<>();
   private BigDecimal balanceTopTotal = BigDecimal.ZERO;
   private long cacheAge = 0L;
   private CompletableFuture<Void> cacheLock;

   public BalanceTopImpl(net.ess3.api.IEssentials ess) {
      this.ess = ess;
      ess.getServer().getServicesManager().register(BalanceTop.class, this, ess, ServicePriority.Normal);
   }

   private void calculateBalanceTopMap() {
      List<BalanceTop.Entry> entries = new LinkedList<>();
      BigDecimal newTotal = BigDecimal.ZERO;

      for (UUID u : this.ess.getUsers().getAllUserUUIDs()) {
         User user = this.ess.getUsers().loadUncachedUser(u);
         if (user != null && (this.ess.getSettings().isNpcsInBalanceRanking() || !user.isNPC()) && !user.isBaltopExempt()) {
            BigDecimal userMoney = user.getMoney();
            user.updateMoneyCache(userMoney);
            newTotal = newTotal.add(userMoney);
            String name;
            if (user.getBase() instanceof OfflinePlayerStub) {
               name = user.getLastAccountName();
            } else if (user.isHidden()) {
               name = user.getName();
            } else {
               name = user.getDisplayName();
            }

            entries.add(new BalanceTop.Entry(user.getUUID(), name, userMoney));
         }
      }

      LinkedHashMap<UUID, BalanceTop.Entry> sortedMap = new LinkedHashMap<>();
      entries.sort((entry1, entry2) -> entry2.getBalance().compareTo(entry1.getBalance()));
      int entryLimit = this.ess.getSettings().getBaltopEntryLimit();
      int limit = entryLimit == -1 ? entries.size() : Math.min(entryLimit, entries.size());

      for (int i = 0; i < limit; i++) {
         BalanceTop.Entry entry = entries.get(i);
         sortedMap.put(entry.getUuid(), entry);
      }

      this.topCache = sortedMap;
      this.balanceTopTotal = newTotal;
      this.cacheAge = System.currentTimeMillis();
      this.cacheLock.complete(null);
      this.cacheLock = null;
   }

   @Override
   public CompletableFuture<Void> calculateBalanceTopMapAsync() {
      if (this.cacheLock != null) {
         return this.cacheLock;
      } else {
         this.cacheLock = new CompletableFuture<>();
         this.ess.runTaskAsynchronously(this::calculateBalanceTopMap);
         return this.cacheLock;
      }
   }

   @Override
   public Map<UUID, BalanceTop.Entry> getBalanceTopCache() {
      return Collections.unmodifiableMap(this.topCache);
   }

   @Override
   public long getCacheAge() {
      return this.cacheAge;
   }

   @Override
   public BigDecimal getBalanceTopTotal() {
      return this.balanceTopTotal;
   }

   @Override
   public boolean isCacheLocked() {
      return this.cacheLock != null;
   }
}
