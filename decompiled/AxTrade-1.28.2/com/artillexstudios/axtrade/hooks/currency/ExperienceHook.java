package com.artillexstudios.axtrade.hooks.currency;

import com.artillexstudios.axtrade.AxTrade;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ExperienceHook implements CurrencyHook {
   @Override
   public void setup() {
   }

   @Override
   public String getName() {
      return "Experience";
   }

   @Override
   public Map<String, Object> getSettings() {
      return AxTrade.HOOKS.getSection("currencies." + this.getName()).getStringRouteMappedValues(true);
   }

   @Override
   public boolean worksOffline() {
      return false;
   }

   @Override
   public boolean usesDouble() {
      return false;
   }

   @Override
   public boolean isPersistent() {
      return false;
   }

   @Override
   public double getBalance(@NotNull UUID player) {
      Player pl = Bukkit.getPlayer(player);
      return this.getExp(pl);
   }

   @Override
   public CompletableFuture<Boolean> giveBalance(@NotNull UUID player, double amount) {
      Player pl = Bukkit.getPlayer(player);
      this.changeExp(pl, (int)amount);
      return CompletableFuture.completedFuture(true);
   }

   @Override
   public CompletableFuture<Boolean> takeBalance(@NotNull UUID player, double amount) {
      Player pl = Bukkit.getPlayer(player);
      this.changeExp(pl, (int)(amount * -1.0));
      return CompletableFuture.completedFuture(true);
   }

   private int getExp(@NotNull Player player) {
      return this.getExpFromLevel(player.getLevel()) + Math.round(this.getExpToNext(player.getLevel()) * player.getExp());
   }

   private int getExpFromLevel(int level) {
      if (level > 30) {
         return (int)(4.5 * level * level - 162.5 * level + 2220.0);
      } else {
         return level > 15 ? (int)(2.5 * level * level - 40.5 * level + 360.0) : level * level + 6 * level;
      }
   }

   private double getLevelFromExp(long exp) {
      int level = this.getIntLevelFromExp(exp);
      float remainder = (float)exp - this.getExpFromLevel(level);
      float progress = remainder / this.getExpToNext(level);
      return (double)level + progress;
   }

   private int getIntLevelFromExp(long exp) {
      if (exp > 1395L) {
         return (int)((Math.sqrt(72L * exp - 54215.0) + 325.0) / 18.0);
      } else if (exp > 315L) {
         return (int)(Math.sqrt(40L * exp - 7839.0) / 10.0 + 8.1);
      } else {
         return exp > 0L ? (int)(Math.sqrt(exp + 9.0) - 3.0) : 0;
      }
   }

   private int getExpToNext(int level) {
      if (level >= 30) {
         return level * 9 - 158;
      } else {
         return level >= 15 ? level * 5 - 38 : level * 2 + 7;
      }
   }

   private void changeExp(Player player, int exp) {
      exp += this.getExp(player);
      if (exp < 0) {
         exp = 0;
      }

      double levelAndExp = this.getLevelFromExp(exp);
      int level = (int)levelAndExp;
      player.setLevel(level);
      player.setExp((float)(levelAndExp - level));
   }
}
