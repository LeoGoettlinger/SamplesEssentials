package net.milkbowl.vault.economy;

import org.bukkit.OfflinePlayer;

public abstract class AbstractEconomy implements Economy {
   @Override
   public boolean hasAccount(OfflinePlayer player) {
      return player.getName() == null ? false : this.hasAccount(player.getName());
   }

   @Override
   public boolean hasAccount(OfflinePlayer player, String worldName) {
      return player.getName() == null ? false : this.hasAccount(player.getName(), worldName);
   }

   @Override
   public double getBalance(OfflinePlayer player) {
      return this.getBalance(player.getName());
   }

   @Override
   public double getBalance(OfflinePlayer player, String world) {
      return this.getBalance(player.getName(), world);
   }

   @Override
   public boolean has(OfflinePlayer player, double amount) {
      return player.getName() == null ? false : this.has(player.getName(), amount);
   }

   @Override
   public boolean has(OfflinePlayer player, String worldName, double amount) {
      return player.getName() == null ? false : this.has(player.getName(), worldName, amount);
   }

   @Override
   public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
      return this.withdrawPlayer(player.getName(), amount);
   }

   @Override
   public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
      return this.withdrawPlayer(player.getName(), worldName, amount);
   }

   @Override
   public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
      return this.depositPlayer(player.getName(), amount);
   }

   @Override
   public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
      return this.depositPlayer(player.getName(), worldName, amount);
   }

   @Override
   public EconomyResponse createBank(String name, OfflinePlayer player) {
      return this.createBank(name, player.getName());
   }

   @Override
   public EconomyResponse isBankOwner(String name, OfflinePlayer player) {
      return this.isBankOwner(name, player.getName());
   }

   @Override
   public EconomyResponse isBankMember(String name, OfflinePlayer player) {
      return this.isBankMember(name, player.getName());
   }

   @Override
   public boolean createPlayerAccount(OfflinePlayer player) {
      return this.createPlayerAccount(player.getName());
   }

   @Override
   public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
      return this.createPlayerAccount(player.getName(), worldName);
   }
}
