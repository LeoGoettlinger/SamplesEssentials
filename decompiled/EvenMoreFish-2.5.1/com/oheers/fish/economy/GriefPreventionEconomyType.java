package com.oheers.fish.economy;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.economy.EconomyType;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.messages.EMFSingleMessage;
import java.util.logging.Level;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.PlayerData;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class GriefPreventionEconomyType implements EconomyType {
   private GriefPrevention economy = null;

   @Override
   public String getIdentifier() {
      return "GriefPrevention";
   }

   @Override
   public void load() {
      if (MainConfig.getInstance().isEconomyEnabled(this)) {
         EvenMoreFish emf = EvenMoreFish.getInstance();
         emf.getLogger().log(Level.INFO, "Economy attempting to hook into GriefPrevention.");
         if (Bukkit.getPluginManager().isPluginEnabled("GriefPrevention")) {
            this.economy = GriefPrevention.instance;
            emf.getLogger().log(Level.INFO, "Economy hooked into GriefPrevention.");
         }
      }
   }

   @Override
   public double getMultiplier() {
      return MainConfig.getInstance().getEconomyMultiplier(this);
   }

   @Override
   public boolean deposit(@NonNull OfflinePlayer player, double amount, boolean allowMultiplier) {
      if (!this.isAvailable()) {
         return false;
      } else {
         PlayerData data = this.economy.dataStore.getPlayerData(player.getUniqueId());
         data.setBonusClaimBlocks(data.getBonusClaimBlocks() + (int)this.prepareValue(amount, allowMultiplier));
         return true;
      }
   }

   @Override
   public boolean withdraw(@NonNull OfflinePlayer player, double amount, boolean allowMultiplier) {
      if (!this.isAvailable()) {
         return false;
      } else {
         PlayerData data = this.economy.dataStore.getPlayerData(player.getUniqueId());
         int total = data.getBonusClaimBlocks();
         int finalTotal = total - (int)this.prepareValue(amount, allowMultiplier);
         if (finalTotal < 0) {
            return false;
         } else {
            data.setBonusClaimBlocks(finalTotal);
            return true;
         }
      }
   }

   @Override
   public boolean has(@NonNull OfflinePlayer player, double amount) {
      return !this.isAvailable() ? false : this.get(player) >= amount;
   }

   @Override
   public double get(@NonNull OfflinePlayer player) {
      return !this.isAvailable() ? 0.0 : this.economy.dataStore.getPlayerData(player.getUniqueId()).getBonusClaimBlocks();
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
            display = "{amount} Claim Block(s)";
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
