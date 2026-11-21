package com.artillexstudios.axtrade.hooks;

import com.artillexstudios.axtrade.AxTrade;
import com.artillexstudios.axtrade.hooks.currency.AxHoesHook;
import com.artillexstudios.axtrade.hooks.currency.AxQuestBoardHook;
import com.artillexstudios.axtrade.hooks.currency.BeastTokensHook;
import com.artillexstudios.axtrade.hooks.currency.CoinsEngineHook;
import com.artillexstudios.axtrade.hooks.currency.CurrencyHook;
import com.artillexstudios.axtrade.hooks.currency.EcoBitsHook;
import com.artillexstudios.axtrade.hooks.currency.ExcellentEconomyHook;
import com.artillexstudios.axtrade.hooks.currency.ExperienceHook;
import com.artillexstudios.axtrade.hooks.currency.KingdomsXHook;
import com.artillexstudios.axtrade.hooks.currency.PlaceholderCurrencyHook;
import com.artillexstudios.axtrade.hooks.currency.PlayerPointsHook;
import com.artillexstudios.axtrade.hooks.currency.RedisEconomyHook;
import com.artillexstudios.axtrade.hooks.currency.RivalCreditsHook;
import com.artillexstudios.axtrade.hooks.currency.RivalHarvesterHoesHook;
import com.artillexstudios.axtrade.hooks.currency.RoyaleEconomyHook;
import com.artillexstudios.axtrade.hooks.currency.SuperMobCoinsHook;
import com.artillexstudios.axtrade.hooks.currency.TheOnlyMobCoins;
import com.artillexstudios.axtrade.hooks.currency.TokenManagerHook;
import com.artillexstudios.axtrade.hooks.currency.UltraEconomyHook;
import com.artillexstudios.axtrade.hooks.currency.VaultHook;
import com.artillexstudios.axtrade.hooks.other.AxShulkersHook;
import com.artillexstudios.axtrade.hooks.other.Placeholders;
import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.block.implementation.Section;
import com.artillexstudios.axtrade.libs.axapi.reflection.ClassUtils;
import com.artillexstudios.axtrade.libs.axapi.utils.StringUtils;
import java.util.ArrayList;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HookManager {
   private static final ArrayList<CurrencyHook> currency = new ArrayList<>();
   private static AxShulkersHook axShulkersHook = null;

   public static void setupHooks() {
      updateHooks();
      if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
         new Placeholders();
      }

      if (Bukkit.getPluginManager().getPlugin("AxShulkers") != null) {
         axShulkersHook = new AxShulkersHook();
      }
   }

   public static void updateHooks() {
      currency.removeIf(currencyHook -> !currencyHook.isPersistent());
      if (AxTrade.HOOKS.getBoolean("currencies.Experience.register", true)) {
         currency.add(new ExperienceHook());
      }

      if (AxTrade.HOOKS.getBoolean("currencies.Vault.register", true) && Bukkit.getPluginManager().getPlugin("Vault") != null) {
         currency.add(new VaultHook());
         Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#33FF33[AxTrade] Hooked into Vault!"));
      }

      if (AxTrade.HOOKS.getBoolean("currencies.PlayerPoints.register", true) && Bukkit.getPluginManager().getPlugin("PlayerPoints") != null) {
         currency.add(new PlayerPointsHook());
         Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#33FF33[AxTrade] Hooked into PlayerPoints!"));
      }

      if (ClassUtils.INSTANCE.classExists("su.nightexpress.excellenteconomy.api.ExcellentEconomyAPI")) {
         if (AxTrade.HOOKS.getBoolean("currencies.ExcellentEconomy.register", true)) {
            for (Map<Object, Object> curr : AxTrade.HOOKS.getMapList("currencies.ExcellentEconomy.enabled")) {
               currency.add(new ExcellentEconomyHook(curr));
            }

            Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#33FF33[AxTrade] Hooked into ExcellentEconomy!"));
         }
      } else if (AxTrade.HOOKS.getBoolean("currencies.CoinsEngine.register", true) && Bukkit.getPluginManager().getPlugin("CoinsEngine") != null) {
         for (Map<Object, Object> curr : AxTrade.HOOKS.getMapList("currencies.CoinsEngine.enabled")) {
            currency.add(new CoinsEngineHook(curr));
         }

         Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#33FF33[AxTrade] Hooked into CoinsEngine!"));
      }

      if (AxTrade.HOOKS.getBoolean("currencies.RoyaleEconomy.register", true) && Bukkit.getPluginManager().getPlugin("RoyaleEconomy") != null) {
         currency.add(new RoyaleEconomyHook());
         Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#33FF33[AxTrade] Hooked into RoyaleEconomy!"));
      }

      if (AxTrade.HOOKS.getBoolean("currencies.UltraEconomy.register", true) && Bukkit.getPluginManager().getPlugin("UltraEconomy") != null) {
         for (Map<Object, Object> curr : AxTrade.HOOKS.getMapList("currencies.UltraEconomy.enabled")) {
            currency.add(new UltraEconomyHook(curr));
         }

         Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#33FF33[AxTrade] Hooked into UltraEconomy!"));
      }

      if (AxTrade.HOOKS.getBoolean("currencies.KingdomsX.register", true) && Bukkit.getPluginManager().getPlugin("Kingdoms") != null) {
         currency.add(new KingdomsXHook());
         Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#33FF33[AxTrade] Hooked into KingdomsX!"));
      }

      if (AxTrade.HOOKS.getBoolean("currencies.RivalHarvesterHoes.register", true) && Bukkit.getPluginManager().getPlugin("RivalHarvesterHoes") != null) {
         currency.add(new RivalHarvesterHoesHook());
         Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#33FF33[AxTrade] Hooked into RivalHarvesterHoes!"));
      }

      if (AxTrade.HOOKS.getBoolean("currencies.SuperMobCoins.register", true) && Bukkit.getPluginManager().getPlugin("SuperMobCoins") != null) {
         currency.add(new SuperMobCoinsHook());
         Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#33FF33[AxTrade] Hooked into SuperMobCoins!"));
      }

      if (AxTrade.HOOKS.getBoolean("currencies.TheOnly-MobCoins.register", true) && Bukkit.getPluginManager().getPlugin("TheOnly-MobCoins") != null) {
         currency.add(new TheOnlyMobCoins());
         Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#33FF33[AxTrade] Hooked into TheOnly-MobCoins!"));
      }

      if (AxTrade.HOOKS.getBoolean("currencies.TokenManager.register", true) && Bukkit.getPluginManager().getPlugin("TokenManager") != null) {
         currency.add(new TokenManagerHook());
         Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#33FF33[AxTrade] Hooked into TokenManager!"));
      }

      if (AxTrade.HOOKS.getBoolean("currencies.AxQuestBoard.register", true) && Bukkit.getPluginManager().getPlugin("AxQuestBoard") != null) {
         currency.add(new AxQuestBoardHook());
         Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#33FF33[AxTrade] Hooked into AxQuestBoard!"));
      }

      if (AxTrade.HOOKS.getBoolean("currencies.AxHoes.register", true) && Bukkit.getPluginManager().getPlugin("AxHoes") != null) {
         currency.add(new AxHoesHook());
         Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#33FF33[AxTrade] Hooked into AxHoes!"));
      }

      if (AxTrade.HOOKS.getBoolean("currencies.RedisEconomy.register", true) && Bukkit.getPluginManager().getPlugin("RedisEconomy") != null) {
         for (Map<Object, Object> curr : AxTrade.HOOKS.getMapList("currencies.RedisEconomy.enabled")) {
            currency.add(new RedisEconomyHook(curr));
         }

         Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#33FF33[AxTrade] Hooked into RedisEconomy!"));
      }

      if (AxTrade.HOOKS.getBoolean("currencies.BeastTokens.register", true) && Bukkit.getPluginManager().getPlugin("BeastTokens") != null) {
         currency.add(new BeastTokensHook());
         Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#33FF33[AxTrade] Hooked into BeastTokens!"));
      }

      if (AxTrade.HOOKS.getBoolean("currencies.EcoBits.register", true) && Bukkit.getPluginManager().getPlugin("EcoBits") != null) {
         for (Map<Object, Object> curr : AxTrade.HOOKS.getMapList("currencies.EcoBits.enabled")) {
            currency.add(new EcoBitsHook(curr));
         }

         Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#33FF33[AxTrade] Hooked into EcoBits!"));
      }

      if (AxTrade.HOOKS.getBoolean("currencies.RivalCredits.register", true) && Bukkit.getPluginManager().getPlugin("RivalCredits") != null) {
         currency.add(new RivalCreditsHook());
         Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#33FF33[AxTrade] Hooked into RivalCredits!"));
      }

      for (String str : AxTrade.HOOKS.getSection("placeholder-currencies").getRoutesAsStrings(false)) {
         if (AxTrade.HOOKS.getBoolean("placeholder-currencies." + str + ".register", false)) {
            currency.add(new PlaceholderCurrencyHook(str, AxTrade.HOOKS.getSection("placeholder-currencies." + str)));
            Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#33FF33[AxTrade] Loaded placeholder currency " + str + "!"));
         }
      }

      for (CurrencyHook hook : currency) {
         hook.setup();
      }
   }

   public static void registerCurrencyHook(@NotNull Plugin plugin, @NotNull CurrencyHook currencyHook) {
      currency.add(currencyHook);
      Section section = AxTrade.HOOKS.getSection("currencies." + currencyHook.getName());
      if (section == null) {
         section = AxTrade.HOOKS.getBackingDocument().createSection("currencies." + currencyHook.getName());
         section.set("enabled", true);
         section.set("name", currencyHook.getName());
         section.set("tax", 0);
         AxTrade.HOOKS.save();
      }

      Bukkit.getConsoleSender()
         .sendMessage(StringUtils.formatToString("&#33FF33[AxTrade] Hooked into " + plugin.getName() + "! Note: Check the currencies.yml for settings!"));
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

   public static AxShulkersHook getAxShulkersHook() {
      return axShulkersHook;
   }
}
