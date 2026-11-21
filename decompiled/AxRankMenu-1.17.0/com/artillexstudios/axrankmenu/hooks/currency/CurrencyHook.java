package com.artillexstudios.axrankmenu.hooks.currency;

import org.bukkit.entity.Player;

public interface CurrencyHook {
   void setup();

   String getName();

   boolean isPersistent();

   double getBalance(Player var1);

   void giveBalance(Player var1, double var2);

   void takeBalance(Player var1, double var2);
}
