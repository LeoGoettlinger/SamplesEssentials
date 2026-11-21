package com.oheers.fish.economy;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.Logging;
import com.oheers.fish.api.economy.EconomyType;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.messages.EMFSingleMessage;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.evenmorefish.fish.libs.daisylib.external.vault.VaultWrapper;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class VaultEconomyType implements EconomyType {
   @Override
   public String getIdentifier() {
      return "Vault";
   }

   @Override
   public void load() {
      if (MainConfig.getInstance().isEconomyEnabled(this)) {
         if (!Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            EvenMoreFish.getInstance().debug("Attempting to register Vault but it is not available.. ignoring");
         }
      }
   }

   @Override
   public double getMultiplier() {
      return MainConfig.getInstance().getEconomyMultiplier(this);
   }

   @Override
   public boolean deposit(@NonNull OfflinePlayer player, double amount, boolean allowMultiplier) {
      return !this.isAvailable()
         ? false
         : VaultWrapper.get().getEconomy().depositPlayer(player, this.prepareValue(amount, allowMultiplier)).transactionSuccess();
   }

   @Override
   public boolean withdraw(@NonNull OfflinePlayer player, double amount, boolean allowMultiplier) {
      return !this.isAvailable()
         ? false
         : VaultWrapper.get().getEconomy().withdrawPlayer(player, this.prepareValue(amount, allowMultiplier)).transactionSuccess();
   }

   @Override
   public boolean has(@NonNull OfflinePlayer player, double amount) {
      return !this.isAvailable() ? false : VaultWrapper.get().getEconomy().has(player, amount);
   }

   @Override
   public double get(@NonNull OfflinePlayer player) {
      return !this.isAvailable() ? 0.0 : VaultWrapper.get().getEconomy().getBalance(player);
   }

   @Override
   public double prepareValue(double value, boolean applyMultiplier) {
      return applyMultiplier ? value * this.getMultiplier() : value;
   }

   @Nullable
   @Override
   public Component formatWorth(double totalWorth, boolean applyMultiplier) {
      if (!this.isAvailable()) {
         return null;
      } else {
         double worth = this.prepareValue(totalWorth, applyMultiplier);
         String worthFormatted = VaultWrapper.get().getEconomy().format(worth);
         String display = MainConfig.getInstance().getEconomyDisplay(this);
         if (display == null) {
            return EMFSingleMessage.fromString(worthFormatted).getComponentMessage();
         } else {
            EMFSingleMessage message = EMFSingleMessage.fromString(display);
            message.setVariable("{amount}", worthFormatted);
            message.setVariable("{raw-amount}", FishUtils.roundDouble(worth, VaultWrapper.get().getEconomy().fractionalDigits()));
            return message.getComponentMessage();
         }
      }
   }

   @Override
   public boolean isAvailable() {
      if (!MainConfig.getInstance().isEconomyEnabled(this)) {
         return false;
      } else if (!VaultWrapper.get().isEconomyAvailable()) {
         Logging.warn("There is no registered economy provider. Cannot use Vault EconomyType.");
         return false;
      } else {
         return true;
      }
   }
}
