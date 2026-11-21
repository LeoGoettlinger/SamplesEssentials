package com.donutorders.manager;

import com.donutorders.DonutOrders;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissionAttachmentInfo;

public class OrderLimitManager {
   private static final String LIMIT_PREFIX = "donutorders.limit.";
   private static final Pattern LIMIT_PATTERN = Pattern.compile("donutorders\\.limit\\.(\\d+)");
   private final Map<UUID, OrderLimitManager.ResolvedLimit> cache = new ConcurrentHashMap<>();

   public OrderLimitManager.ResolvedLimit getLimit(Player player) {
      return this.cache.computeIfAbsent(player.getUniqueId(), uuid -> this.resolveLimit(player));
   }

   public int getLimitValue(Player player) {
      return this.getLimit(player).limit();
   }

   public OrderLimitManager.ResolvedLimit resolveLimit(Permissible permissible) {
      return this.resolveFromPermissions(this.collectEffectivePermissions(permissible), permissible::hasPermission);
   }

   public OrderLimitManager.ResolvedLimit resolveFromPermissions(Map<String, Boolean> permissions, Predicate<String> hasPermission) {
      int highestPerm = -1;
      String bestPerm = null;

      for (Entry<String, Boolean> entry : permissions.entrySet()) {
         if (Boolean.TRUE.equals(entry.getValue())) {
            String permission = entry.getKey();
            if (permission.startsWith("donutorders.limit.")) {
               Matcher matcher = LIMIT_PATTERN.matcher(permission);
               if (matcher.matches()) {
                  int value = Integer.parseInt(matcher.group(1));
                  if (value > highestPerm) {
                     highestPerm = value;
                     bestPerm = permission;
                  }
               }
            }
         }
      }

      if (bestPerm != null) {
         return new OrderLimitManager.ResolvedLimit(highestPerm, bestPerm);
      } else {
         int highestRank = -1;
         String bestRank = null;
         ConfigurationSection rankLimits = DonutOrders.getInstance().getConfig().getConfigurationSection("rank-limits");
         if (rankLimits != null) {
            for (String rank : rankLimits.getKeys(false)) {
               if (!rank.equalsIgnoreCase("default") && hasPermission.test("group." + rank)) {
                  int value = rankLimits.getInt(rank);
                  if (value > highestRank) {
                     highestRank = value;
                     bestRank = "rank-limits." + rank;
                  }
               }
            }
         }

         if (bestRank != null) {
            return new OrderLimitManager.ResolvedLimit(highestRank, bestRank);
         } else {
            int configDefault = this.getConfigDefaultLimit();
            return new OrderLimitManager.ResolvedLimit(configDefault, "config (default: " + configDefault + ")");
         }
      }
   }

   public int getConfigDefaultLimit() {
      return this.getConfigDefaultLimitInternal();
   }

   private Map<String, Boolean> collectEffectivePermissions(Permissible permissible) {
      Map<String, Boolean> permissions = new HashMap<>();

      for (PermissionAttachmentInfo attachment : permissible.getEffectivePermissions()) {
         permissions.put(attachment.getPermission(), attachment.getValue());
      }

      return permissions;
   }

   public void refresh(Player player) {
      this.cache.put(player.getUniqueId(), this.resolveLimit(player));
   }

   public void evict(UUID playerId) {
      this.cache.remove(playerId);
   }

   public void clearCache() {
      this.cache.clear();
   }

   public void refreshAllOnline() {
      this.cache.clear();

      for (Player player : DonutOrders.getInstance().getServer().getOnlinePlayers()) {
         this.refresh(player);
      }
   }

   private int getConfigDefaultLimitInternal() {
      FileConfiguration config = DonutOrders.getInstance().getConfig();
      if (config.contains("orders.default-limit")) {
         return config.getInt("orders.default-limit");
      } else if (config.contains("orders.max-per-player")) {
         return config.getInt("orders.max-per-player");
      } else {
         ConfigurationSection rankLimits = config.getConfigurationSection("rank-limits");
         return rankLimits != null && rankLimits.contains("default") ? rankLimits.getInt("default") : 10;
      }
   }

   public record ResolvedLimit(int limit, String source) {
   }
}
