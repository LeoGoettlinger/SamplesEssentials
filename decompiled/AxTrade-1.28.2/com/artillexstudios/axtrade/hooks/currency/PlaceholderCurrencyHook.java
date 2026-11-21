package com.artillexstudios.axtrade.hooks.currency;

import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.block.implementation.Section;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class PlaceholderCurrencyHook implements CurrencyHook {
   private final String name;
   private final Section section;
   private DecimalFormat df;

   public PlaceholderCurrencyHook(String name, Section section) {
      this.name = name;
      this.section = section;
   }

   @Override
   public void setup() {
      this.df = new DecimalFormat("#");
   }

   @Override
   public String getName() {
      return this.name;
   }

   @Override
   public Map<String, Object> getSettings() {
      return this.section.getStringRouteMappedValues(true);
   }

   @Override
   public boolean worksOffline() {
      return this.section.getBoolean("works-offline", false);
   }

   @Override
   public boolean usesDouble() {
      return this.section.getBoolean("uses-double", false);
   }

   @Override
   public boolean isPersistent() {
      return false;
   }

   @Override
   public double getBalance(@NotNull UUID player) {
      OfflinePlayer pl = Bukkit.getOfflinePlayer(player);
      String placeholder = this.section.getString("settings.raw-placeholder");
      return Double.parseDouble(PlaceholderAPI.setPlaceholders((OfflinePlayer)(pl.getPlayer() == null ? pl : pl.getPlayer()), placeholder));
   }

   @Override
   public CompletableFuture<Boolean> giveBalance(@NotNull UUID player, double amount) {
      OfflinePlayer pl = Bukkit.getOfflinePlayer(player);
      if (pl.getName() == null) {
         return CompletableFuture.completedFuture(false);
      } else {
         String placeholder = this.section.getString("settings.give-command").replace("%amount%", this.parseNumber(amount)).replace("%player%", pl.getName());
         return CompletableFuture.completedFuture(Bukkit.dispatchCommand(Bukkit.getConsoleSender(), placeholder));
      }
   }

   @Override
   public CompletableFuture<Boolean> takeBalance(@NotNull UUID player, double amount) {
      OfflinePlayer pl = Bukkit.getOfflinePlayer(player);
      if (pl.getName() == null) {
         return CompletableFuture.completedFuture(false);
      } else {
         String placeholder = this.section.getString("settings.take-command").replace("%amount%", this.parseNumber(amount)).replace("%player%", pl.getName());
         return CompletableFuture.completedFuture(Bukkit.dispatchCommand(Bukkit.getConsoleSender(), placeholder));
      }
   }

   private String parseNumber(double amount) {
      return this.df.format(this.usesDouble() ? amount : Math.round(amount));
   }
}
