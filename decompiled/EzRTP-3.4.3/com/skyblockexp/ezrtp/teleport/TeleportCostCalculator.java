package com.skyblockexp.ezrtp.teleport;

import com.skyblockexp.ezrtp.config.RandomTeleportSettings;
import com.skyblockexp.ezrtp.economy.EconomyService;
import java.util.function.BiFunction;
import org.bukkit.entity.Player;

public final class TeleportCostCalculator {
   private EconomyService economyService;
   private BiFunction<Player, RandomTeleportSettings, Double> costResolver;

   public TeleportCostCalculator(EconomyService economyService, BiFunction<Player, RandomTeleportSettings, Double> costResolver) {
      this.economyService = economyService != null ? economyService : EconomyService.disabled();
      this.costResolver = costResolver;
   }

   public double calculateCost(Player player, RandomTeleportSettings teleportSettings) {
      if (teleportSettings == null) {
         return 0.0;
      } else if (this.costResolver == null) {
         return teleportSettings.getTeleportCost();
      } else {
         Double resolved = this.costResolver.apply(player, teleportSettings);
         return resolved != null ? resolved : teleportSettings.getTeleportCost();
      }
   }

   public boolean requiresPayment(TeleportReason reason, double cost) {
      return reason == TeleportReason.COMMAND && cost > 0.0;
   }

   public boolean hasBalance(Player player, double cost) {
      return this.economyService.hasBalance(player, cost);
   }

   public boolean withdraw(Player player, double cost) {
      return this.economyService.withdraw(player, cost);
   }

   public void setEconomyService(EconomyService economyService) {
      this.economyService = economyService != null ? economyService : EconomyService.disabled();
   }

   public void setCostResolver(BiFunction<Player, RandomTeleportSettings, Double> costResolver) {
      this.costResolver = costResolver;
   }
}
