package com.oheers.fish.economy;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.economy.EconomyType;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.messages.EMFSingleMessage;
import java.util.logging.Level;
import net.kyori.adventure.text.Component;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class PlayerPointsEconomyType implements EconomyType {
   private PlayerPointsAPI economy = null;

   @Override
   public String getIdentifier() {
      return "PlayerPoints";
   }

   @Override
   public void load() {
      if (MainConfig.getInstance().isEconomyEnabled(this)) {
         EvenMoreFish emf = EvenMoreFish.getInstance();
         emf.getLogger().log(Level.INFO, "Economy attempting to hook into PlayerPoints.");
         if (Bukkit.getPluginManager().isPluginEnabled("PlayerPoints")) {
            this.economy = PlayerPoints.getInstance().getAPI();
            emf.getLogger().log(Level.INFO, "Economy hooked into PlayerPoints.");
         }
      }
   }

   @Override
   public double getMultiplier() {
      return MainConfig.getInstance().getEconomyMultiplier(this);
   }

   @Override
   public boolean deposit(@NonNull OfflinePlayer player, double amount, boolean allowMultiplier) {
      return !this.isAvailable() ? false : this.economy.give(player.getUniqueId(), (int)this.prepareValue(amount, allowMultiplier));
   }

   @Override
   public boolean withdraw(@NonNull OfflinePlayer player, double amount, boolean allowMultiplier) {
      return !this.isAvailable() ? false : this.economy.take(player.getUniqueId(), (int)this.prepareValue(amount, allowMultiplier));
   }

   @Override
   public boolean has(@NonNull OfflinePlayer player, double amount) {
      return !this.isAvailable() ? false : this.get(player) >= amount;
   }

   @Override
   public double get(@NonNull OfflinePlayer player) {
      return !this.isAvailable() ? 0.0 : this.economy.look(player.getUniqueId());
   }

   @Override
   public double prepareValue(double value, boolean applyMultiplier) {
      return applyMultiplier ? Math.floor(value * this.getMultiplier()) : Math.floor(value);
   }

   @Nullable
   @Override
   public Component formatWorth(double totalWorth, boolean applyMultiplier) {
      if (!this.isAvailable()) {
         return null;
      } else {
         int worth = (int)this.prepareValue(totalWorth, applyMultiplier);
         String display = MainConfig.getInstance().getEconomyDisplay(this);
         if (display == null) {
            display = "{amount} Player Point(s)";
         }

         EMFSingleMessage message = EMFSingleMessage.fromString(display);
         message.setVariable("{amount}", worth);
         return message.getComponentMessage();
      }
   }

   @Override
   public boolean isAvailable() {
      return MainConfig.getInstance().isEconomyEnabled(this) && this.economy != null;
   }
}
