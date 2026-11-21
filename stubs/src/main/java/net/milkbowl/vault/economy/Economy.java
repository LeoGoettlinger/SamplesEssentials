package net.milkbowl.vault.economy;

import org.bukkit.OfflinePlayer;

import java.util.List;

/**
 * Compile-time mirror of the Vault {@code net.milkbowl.vault.economy.Economy}
 * interface (as shipped by VaultUnlocked 2.20.2, which stays installed on the
 * server). Only the members used by SamplesEssentials are declared; the type
 * is never bundled into the plugin jar, so at runtime the real interface from
 * the Vault plugin is used - method signatures and names match exactly.
 */
public interface Economy {

    boolean isEnabled();

    String getName();

    boolean hasBankSupport();

    int fractionalDigits();

    String format(double amount);

    String currencyNamePlural();

    String currencyNameSingular();

    boolean hasAccount(String playerName);

    boolean hasAccount(OfflinePlayer player);

    double getBalance(String playerName);

    double getBalance(OfflinePlayer player);

    double getBalance(String playerName, String world);

    double getBalance(OfflinePlayer player, String world);

    boolean has(String playerName, double amount);

    boolean has(OfflinePlayer player, double amount);

    boolean has(String playerName, String worldName, double amount);

    boolean has(OfflinePlayer player, String worldName, double amount);

    EconomyResponse withdrawPlayer(String playerName, double amount);

    EconomyResponse withdrawPlayer(OfflinePlayer player, double amount);

    EconomyResponse withdrawPlayer(String playerName, String worldName, double amount);

    EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount);

    EconomyResponse depositPlayer(String playerName, double amount);

    EconomyResponse depositPlayer(OfflinePlayer player, double amount);

    EconomyResponse depositPlayer(String playerName, String worldName, double amount);

    EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount);

    boolean createPlayerAccount(String playerName);

    boolean createPlayerAccount(OfflinePlayer player);

    boolean createPlayerAccount(String playerName, String worldName);

    boolean createPlayerAccount(OfflinePlayer player, String worldName);

    List<String> getBanks();
}
