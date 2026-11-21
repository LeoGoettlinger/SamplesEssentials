package eu.raidersheaven.rhsignitem.handlers;

import eu.raidersheaven.rhsignitem.Main;
import eu.raidersheaven.rhsignitem.configurations.ConfigFile;
import java.util.function.Supplier;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class VaultMoneyHandler {
   public static boolean isVaultAvailable() {
      return Bukkit.getPluginManager().getPlugin("Vault") != null && Main.getEconomy() != null;
   }

   public static boolean shouldCancelStatus(Player player, Supplier<Boolean> isCommandEnabled) {
      if (!ConfigFile.vaultEnabled || !isCommandEnabled.get()) {
         return false;
      } else if (!isVaultAvailable()) {
         return false;
      } else {
         Economy economy = Main.getEconomy();
         double cost = ConfigFile.vaultCost;
         if (!economy.has(player, cost)) {
            if (ConfigFile.vaultMessagesPaymentFailed) {
               player.sendMessage(
                  ColorFormatHandler.formatComponent(LocaleHandler.vaultFail.replace("%prefix%", LocaleHandler.prefix).replace("%cost%", String.valueOf(cost)))
               );
            }

            SoundHandler.fail(player);
            return true;
         } else {
            return false;
         }
      }
   }

   public static boolean shouldCancelPayment(Player player, Supplier<Boolean> isCommandEnabled) {
      if (!ConfigFile.vaultEnabled || !isCommandEnabled.get()) {
         return false;
      } else if (!isVaultAvailable()) {
         return false;
      } else {
         Economy economy = Main.getEconomy();
         double cost = ConfigFile.vaultCost;
         if (!economy.has(player, cost)) {
            if (ConfigFile.vaultMessagesPaymentFailed) {
               player.sendMessage(
                  ColorFormatHandler.formatComponent(LocaleHandler.vaultFail.replace("%prefix%", LocaleHandler.prefix).replace("%cost%", String.valueOf(cost)))
               );
            }

            SoundHandler.fail(player);
            return true;
         } else {
            economy.withdrawPlayer(player, cost);
            if (ConfigFile.vaultMessagesPaymentSuccessful) {
               player.sendMessage(
                  ColorFormatHandler.formatComponent(
                     LocaleHandler.vaultSuccess.replace("%prefix%", LocaleHandler.prefix).replace("%cost%", String.valueOf(cost))
                  )
               );
            }

            return false;
         }
      }
   }
}
