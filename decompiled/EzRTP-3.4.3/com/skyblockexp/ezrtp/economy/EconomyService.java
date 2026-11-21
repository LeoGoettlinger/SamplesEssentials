package com.skyblockexp.ezrtp.economy;

import org.bukkit.entity.Player;

public interface EconomyService {
   boolean isEnabled();

   boolean hasBalance(Player var1, double var2);

   boolean withdraw(Player var1, double var2);

   static EconomyService disabled() {
      return EconomyService.DisabledEconomyService.INSTANCE;
   }

   public static enum DisabledEconomyService implements EconomyService {
      INSTANCE;

      @Override
      public boolean isEnabled() {
         return false;
      }

      @Override
      public boolean hasBalance(Player player, double amount) {
         return true;
      }

      @Override
      public boolean withdraw(Player player, double amount) {
         return true;
      }
   }
}
