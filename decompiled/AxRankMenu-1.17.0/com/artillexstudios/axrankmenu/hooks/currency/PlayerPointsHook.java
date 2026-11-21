package com.artillexstudios.axrankmenu.hooks.currency;

import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlayerPointsHook implements CurrencyHook {
   private PlayerPointsAPI econ = null;

   @Override
   public void setup() {
      this.econ = PlayerPoints.getInstance().getAPI();
   }

   @Override
   public String getName() {
      return "PlayerPoints";
   }

   @Override
   public boolean isPersistent() {
      return false;
   }

   @Override
   public double getBalance(@NotNull Player p) {
      return this.econ.look(p.getUniqueId());
   }

   @Override
   public void giveBalance(@NotNull Player p, double amount) {
      this.econ.give(p.getUniqueId(), (int)amount);
   }

   @Override
   public void takeBalance(@NotNull Player p, double amount) {
      this.econ.take(p.getUniqueId(), (int)Math.round(amount));
   }
}
