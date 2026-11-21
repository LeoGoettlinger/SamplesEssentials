package net.milkbowl.vault.economy;

import java.util.List;
import org.bukkit.OfflinePlayer;

/** @deprecated */
public interface Economy {
   boolean isEnabled();

   String getName();

   boolean hasBankSupport();

   int fractionalDigits();

   String format(double amount);

   String currencyNamePlural();

   String currencyNameSingular();

   @Deprecated
   boolean hasAccount(String playerName);

   boolean hasAccount(OfflinePlayer player);

   @Deprecated
   boolean hasAccount(String playerName, String worldName);

   boolean hasAccount(OfflinePlayer player, String worldName);

   @Deprecated
   double getBalance(String playerName);

   double getBalance(OfflinePlayer player);

   @Deprecated
   double getBalance(String playerName, String world);

   double getBalance(OfflinePlayer player, String world);

   @Deprecated
   boolean has(String playerName, double amount);

   boolean has(OfflinePlayer player, double amount);

   @Deprecated
   boolean has(String playerName, String worldName, double amount);

   boolean has(OfflinePlayer player, String worldName, double amount);

   @Deprecated
   EconomyResponse withdrawPlayer(String playerName, double amount);

   EconomyResponse withdrawPlayer(OfflinePlayer player, double amount);

   @Deprecated
   EconomyResponse withdrawPlayer(String playerName, String worldName, double amount);

   EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount);

   @Deprecated
   EconomyResponse depositPlayer(String playerName, double amount);

   EconomyResponse depositPlayer(OfflinePlayer player, double amount);

   @Deprecated
   EconomyResponse depositPlayer(String playerName, String worldName, double amount);

   EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount);

   @Deprecated
   EconomyResponse createBank(String name, String player);

   EconomyResponse createBank(String name, OfflinePlayer player);

   EconomyResponse deleteBank(String name);

   EconomyResponse bankBalance(String name);

   EconomyResponse bankHas(String name, double amount);

   EconomyResponse bankWithdraw(String name, double amount);

   EconomyResponse bankDeposit(String name, double amount);

   @Deprecated
   EconomyResponse isBankOwner(String name, String playerName);

   EconomyResponse isBankOwner(String name, OfflinePlayer player);

   @Deprecated
   EconomyResponse isBankMember(String name, String playerName);

   EconomyResponse isBankMember(String name, OfflinePlayer player);

   List<String> getBanks();

   @Deprecated
   boolean createPlayerAccount(String playerName);

   boolean createPlayerAccount(OfflinePlayer player);

   @Deprecated
   boolean createPlayerAccount(String playerName, String worldName);

   boolean createPlayerAccount(OfflinePlayer player, String worldName);
}
