package com.artillexstudios.axrankmenu.hooks;

import com.artillexstudios.axrankmenu.AxRankMenu;
import com.artillexstudios.axrankmenu.hooks.currency.BeastTokensHook;
import com.artillexstudios.axrankmenu.hooks.currency.CoinsEngineHook;
import com.artillexstudios.axrankmenu.hooks.currency.CurrencyHook;
import com.artillexstudios.axrankmenu.hooks.currency.ExcellentEconomyHook;
import com.artillexstudios.axrankmenu.hooks.currency.PlayerPointsHook;
import com.artillexstudios.axrankmenu.hooks.currency.RoyaleEconomyHook;
import com.artillexstudios.axrankmenu.hooks.currency.UltraEconomyHook;
import com.artillexstudios.axrankmenu.hooks.currency.VaultHook;
import com.artillexstudios.axrankmenu.libs.axapi.reflection.ClassUtils;
import com.artillexstudios.axrankmenu.libs.axapi.utils.StringUtils;
import java.util.ArrayList;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HookManager {
   private static final ArrayList<CurrencyHook> currency = new ArrayList<>();
   private static PlaceholderAPIHook papi = null;

   public void updateHooks() {
      currency.removeIf(currencyHook -> !currencyHook.isPersistent());
      if (AxRankMenu.CONFIG.getBoolean("hooks.Vault.register", true) && Bukkit.getPluginManager().getPlugin("Vault") != null) {
         currency.add(new VaultHook());
         Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#33FF33[AxRankMenu] Hooked into Vault!"));
      }

      if (AxRankMenu.CONFIG.getBoolean("hooks.PlayerPoints.register", true) && Bukkit.getPluginManager().getPlugin("PlayerPoints") != null) {
         currency.add(new PlayerPointsHook());
         Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#33FF33[AxRankMenu] Hooked into PlayerPoints!"));
      }

      if (ClassUtils.INSTANCE.classExists("su.nightexpress.excellenteconomy.api.ExcellentEconomyAPI")) {
         if (AxRankMenu.CONFIG.getBoolean("hooks.ExcellentEconomy.register", true)) {
            currency.add(new ExcellentEconomyHook());
            Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#33FF33[AxRankMenu] Hooked into ExcellentEconomy!"));
         }
      } else if (AxRankMenu.CONFIG.getBoolean("hooks.CoinsEngine.register", true) && Bukkit.getPluginManager().getPlugin("CoinsEngine") != null) {
         currency.add(new CoinsEngineHook());
         Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#33FF33[AxRankMenu] Hooked into CoinsEngine!"));
      }

      if (AxRankMenu.CONFIG.getBoolean("hooks.RoyaleEconomy.register", true) && Bukkit.getPluginManager().getPlugin("RoyaleEconomy") != null) {
         currency.add(new RoyaleEconomyHook());
         Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#33FF33[AxRankMenu] Hooked into RoyaleEconomy!"));
      }

      if (AxRankMenu.CONFIG.getBoolean("hooks.UltraEconomy.register", true) && Bukkit.getPluginManager().getPlugin("UltraEconomy") != null) {
         currency.add(new UltraEconomyHook());
         Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#33FF33[AxRankMenu] Hooked into UltraEconomy!"));
      }

      if (AxRankMenu.CONFIG.getBoolean("hooks.BeastTokens.register", true) && Bukkit.getPluginManager().getPlugin("BeastTokens") != null) {
         currency.add(new BeastTokensHook());
         Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#33FF33[AxRankMenu] Hooked into BeastTokens!"));
      }

      for (CurrencyHook hook : currency) {
         hook.setup();
      }

      if (currency.isEmpty()) {
         Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FF3333[AxRankMenu] Currency hook not found! Please check your config.yml!"));
      }

      if (AxRankMenu.CONFIG.getBoolean("hooks.PlaceholderAPI.register", true) && Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
         papi = new PlaceholderAPIHook();
         Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#33FF33[AxRankMenu] Hooked into PlaceholderAPI!"));
      }
   }

   public static void registerCurrencyHook(@NotNull Plugin plugin, @NotNull CurrencyHook currencyHook) {
      currency.add(currencyHook);
      Bukkit.getConsoleSender()
         .sendMessage(
            StringUtils.formatToString(
               "&#33FF33[AxRankMenu] Hooked into "
                  + plugin.getName()
                  + "! Note: You must set the currency provider to CUSTOM or it will be overridden after reloading!"
            )
         );
   }

   @NotNull
   public static ArrayList<CurrencyHook> getCurrency() {
      return currency;
   }

   @Nullable
   public static CurrencyHook getCurrencyHook(@NotNull String name) {
      for (CurrencyHook hook : currency) {
         if (hook.getName().equals(name)) {
            return hook;
         }
      }

      return null;
   }

   @Nullable
   public static PlaceholderAPIHook getPapi() {
      return papi;
   }
}
