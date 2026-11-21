package me.chancesd.pvpmanager.integration;

import me.chancesd.pvpmanager.setting.Conf;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;

public enum Hook {
   WORLDGUARD("WorldGuard"),
   ESSENTIALS("Essentials"),
   VAULT("Vault", "Vault not found! Features requiring Vault won't work!", () -> {
      Conf.FINE_AMOUNT.disable();
      Conf.MONEY_PENALTY.disable();
      Conf.MONEY_REWARD.disable();
   }),
   SIMPLECLANS("SimpleClans"),
   PLACEHOLDERAPI("PlaceholderAPI"),
   LIBSDISGUISES("LibsDisguises"),
   GRIEFPREVENTION("GriefPrevention"),
   TAB("TAB"),
   TOWNY("Towny"),
   HUSKTOWNS("HuskTowns"),
   HUSKCLAIMS("HuskClaims"),
   KINGDOMSX("Kingdoms");

   @NotNull
   private final String pluginName;
   private final String disabledWarning;
   private final Runnable disabledAction;

   private Hook(@NotNull final String name) {
      this(name, null, () -> {});
   }

   private Hook(@NotNull final String name, final String disabledWarning, final Runnable disabledAction) {
      this.pluginName = name;
      this.disabledWarning = disabledWarning;
      this.disabledAction = disabledAction;
   }

   public boolean isEnabled() {
      return Bukkit.getPluginManager().isPluginEnabled(this.pluginName);
   }

   public String getVersion() {
      return this.getDescription().getVersion();
   }

   public PluginDescriptionFile getDescription() {
      return this.getPlugin().getDescription();
   }

   public Plugin getPlugin() {
      return Bukkit.getPluginManager().getPlugin(this.pluginName);
   }

   public String getDisabledWarning() {
      return this.disabledWarning;
   }

   public Runnable getDisabledAction() {
      return this.disabledAction;
   }

   @Override
   public String toString() {
      return this.pluginName;
   }
}
