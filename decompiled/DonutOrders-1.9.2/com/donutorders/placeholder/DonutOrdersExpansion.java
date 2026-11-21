package com.donutorders.placeholder;

import com.donutorders.DonutOrders;
import com.donutorders.manager.OrderLimitManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DonutOrdersExpansion extends PlaceholderExpansion {
   private final DonutOrders plugin;
   private final OrderLimitManager orderLimitManager;

   public DonutOrdersExpansion(DonutOrders plugin, OrderLimitManager orderLimitManager) {
      this.plugin = plugin;
      this.orderLimitManager = orderLimitManager;
   }

   @NotNull
   public String getIdentifier() {
      return "donutorders";
   }

   @NotNull
   public String getAuthor() {
      return String.join(", ", this.plugin.getDescription().getAuthors());
   }

   @NotNull
   public String getVersion() {
      return this.plugin.getDescription().getVersion();
   }

   public boolean persist() {
      return true;
   }

   @Nullable
   public String onRequest(OfflinePlayer player, @NotNull String params) {
      if (player == null || !player.isOnline()) {
         return "";
      } else {
         return params.equalsIgnoreCase("order_limit") ? String.valueOf(this.orderLimitManager.getLimitValue(player.getPlayer())) : null;
      }
   }
}
