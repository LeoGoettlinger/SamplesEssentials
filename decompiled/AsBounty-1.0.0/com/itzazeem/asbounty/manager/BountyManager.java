package com.itzazeem.asbounty.manager;

import com.itzazeem.asbounty.AsBounty;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Bukkit;

public class BountyManager {
   private final AsBounty plugin;
   private final Map<UUID, Double> bountyCache;
   private final Map<UUID, Long> bountyTimestampCache;
   private final Map<UUID, Entry<UUID, Entry<String, Double>>> pendingBounties;

   public BountyManager(AsBounty plugin) {
      this.plugin = plugin;
      this.bountyCache = new HashMap<>();
      this.bountyTimestampCache = new HashMap<>();
      this.pendingBounties = new HashMap<>();
      this.loadAllBounties();
   }

   private void loadAllBounties() {
      try (
         Statement stmt = this.plugin.getDatabaseConnection().createStatement();
         ResultSet rs = stmt.executeQuery(
            "SELECT target_uuid, SUM(amount) as total, MAX(timestamp) as last FROM bounties WHERE claimed = 0 GROUP BY target_uuid"
         );
      ) {
         while (rs.next()) {
            UUID uuid = UUID.fromString(rs.getString("target_uuid"));
            this.bountyCache.put(uuid, rs.getDouble("total"));
            this.bountyTimestampCache.put(uuid, rs.getLong("last"));
         }
      } catch (SQLException var9) {
         var9.printStackTrace();
      }
   }

   public CompletableFuture<Boolean> addBounty(UUID target, String targetName, UUID adder, double amount) {
      return CompletableFuture.supplyAsync(
         () -> {
            try {
               Boolean var7;
               try (PreparedStatement stmt = this.plugin
                     .getDatabaseConnection()
                     .prepareStatement("INSERT INTO bounties (target_uuid, target_name, amount, added_by, timestamp) VALUES (?, ?, ?, ?, ?)")) {
                  stmt.setString(1, target.toString());
                  stmt.setString(2, targetName);
                  stmt.setDouble(3, amount);
                  stmt.setString(4, adder != null ? adder.toString() : null);
                  stmt.setLong(5, System.currentTimeMillis());
                  stmt.execute();
                  this.bountyCache.merge(target, amount, Double::sum);
                  this.bountyTimestampCache.put(target, System.currentTimeMillis());
                  var7 = true;
               }

               return var7;
            } catch (SQLException var11) {
               var11.printStackTrace();
               return false;
            }
         }
      );
   }

   public CompletableFuture<Double> claimBounty(UUID target, UUID killer) {
      return CompletableFuture.supplyAsync(
         () -> {
            double total = this.getBounty(target);
            if (total <= 0.0) {
               return 0.0;
            } else {
               try {
                  Double var5;
                  try (PreparedStatement stmt = this.plugin
                        .getDatabaseConnection()
                        .prepareStatement("UPDATE bounties SET claimed = 1 WHERE target_uuid = ? AND claimed = 0")) {
                     stmt.setString(1, target.toString());
                     stmt.execute();
                     this.bountyCache.remove(target);
                     this.bountyTimestampCache.remove(target);
                     var5 = total;
                  }

                  return var5;
               } catch (SQLException var9) {
                  var9.printStackTrace();
                  return 0.0;
               }
            }
         }
      );
   }

   public double getBounty(UUID target) {
      return this.bountyCache.getOrDefault(target, 0.0);
   }

   public long getBountyTimestamp(UUID target) {
      return this.bountyTimestampCache.getOrDefault(target, 0L);
   }

   public List<Entry<UUID, Double>> getTopBounties(int limit) {
      return this.bountyCache.entrySet().stream().sorted(Entry.<UUID, Double>comparingByValue().reversed()).limit(limit).toList();
   }

   public List<Entry<UUID, Double>> getAllBountiesSorted(String sortBy) {
      return this.bountyCache.entrySet().stream().sorted(switch (sortBy) {
         case "oldest" -> Comparator.comparing(e -> this.bountyTimestampCache.getOrDefault(e.getKey(), 0L));
         case "highest_price" -> Entry.comparingByValue().reversed();
         case "lowest_price" -> Entry.comparingByValue();
         default -> Comparator.<Entry>comparingLong(e -> this.bountyTimestampCache.getOrDefault(e.getKey(), 0L)).reversed();
      }).toList();
   }

   public void addPendingBounty(UUID adder, UUID targetUUID, String targetName, double amount) {
      this.pendingBounties.put(adder, Map.entry(targetUUID, Map.entry(targetName, amount)));

      try {
         Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.removePendingBounty(adder), 600L);
      } catch (UnsupportedOperationException var9) {
         try {
            Bukkit.getGlobalRegionScheduler().runDelayed(this.plugin, task -> this.removePendingBounty(adder), 600L);
         } catch (Throwable var8) {
         }
      }
   }

   public Entry<UUID, Entry<String, Double>> getPendingBounty(UUID adder) {
      return this.pendingBounties.get(adder);
   }

   public boolean hasPendingBounty(UUID adder) {
      return this.pendingBounties.containsKey(adder);
   }

   public void removePendingBounty(UUID adder) {
      this.pendingBounties.remove(adder);
   }
}
