package com.earth2me.essentials.economy;

import java.math.BigDecimal;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;

public interface EconomyLayer {
   String getName();

   String getBackendName();

   void enable(Plugin plugin);

   boolean onServerLoad();

   void disable();

   String getPluginName();

   String getPluginVersion();

   boolean hasAccount(OfflinePlayer player);

   boolean createPlayerAccount(OfflinePlayer player);

   BigDecimal getBalance(OfflinePlayer player);

   boolean deposit(OfflinePlayer player, BigDecimal amount);

   boolean withdraw(OfflinePlayer player, BigDecimal amount);

   default boolean set(OfflinePlayer player, BigDecimal amount) {
      return !this.withdraw(player, this.getBalance(player)) ? false : amount.equals(BigDecimal.ZERO) || this.deposit(player, amount);
   }
}
