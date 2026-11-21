package net.milkbowl.vault.papi;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.milkbowl.vault.Vault;
import net.milkbowl.vault2.economy.AccountPermission;
import net.milkbowl.vault2.economy.Economy;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EconomyPlaceholders extends PlaceholderExpansion {
   @NotNull
   public String getIdentifier() {
      return "vaultunlocked";
   }

   @NotNull
   public String getAuthor() {
      return "creatorfromhell";
   }

   @NotNull
   public String getVersion() {
      return "2.13.1";
   }

   public boolean persist() {
      return true;
   }

   @Nullable
   public String onRequest(final OfflinePlayer player, @NotNull final String params) {
      Optional<Economy> economyOpt = Vault.instance().modernProvider();
      if (!economyOpt.isPresent()) {
         return this.onRequestLegacy(player, params);
      } else {
         Economy economy = economyOpt.get();
         String[] args = params.toLowerCase().split("_");
         UUID uuid = player != null ? player.getUniqueId() : null;
         String pluginName = "VaultUnlocked";

         try {
            String ignore = args[0];
            switch (ignore) {
               case "balance":
                  if (uuid == null) {
                     return null;
                  }

                  if (args.length == 3 && args[1].equals("currency")) {
                     String currency = this.decode(args[2]);
                     return economy.balance("VaultUnlocked", uuid, "world", currency).toPlainString();
                  }

                  if (args.length == 5 && args[1].equals("currency") && args[3].equals("world")) {
                     String currency = this.decode(args[2]);
                     String world = args[4];
                     return economy.balance("VaultUnlocked", uuid, world, currency).toPlainString();
                  }

                  if (args.length == 2) {
                     return economy.balance("VaultUnlocked", uuid, args[1]).toPlainString();
                  }

                  return economy.balance("VaultUnlocked", uuid).toPlainString();
               case "balanceformatted":
                  if (uuid == null) {
                     return null;
                  }

                  if (args.length == 3 && args[1].equals("currency")) {
                     String currency = this.decode(args[2]);
                     BigDecimal amount = economy.balance("VaultUnlocked", uuid, "world", currency);
                     return economy.format("VaultUnlocked", amount, currency);
                  }

                  if (args.length == 5 && args[1].equals("currency") && args[3].equals("world")) {
                     String currency = this.decode(args[2]);
                     String world = args[4];
                     BigDecimal amount = economy.balance("VaultUnlocked", uuid, world, currency);
                     return economy.format("VaultUnlocked", amount, currency);
                  }

                  return economy.format("VaultUnlocked", economy.balance("VaultUnlocked", uuid));
               case "account":
                  if (args.length < 2) {
                     return null;
                  }

                  UUID accountId = UUID.fromString(args[1]);
                  if (args.length == 2) {
                     return economy.balance("VaultUnlocked", accountId).toPlainString();
                  }

                  if (args.length == 4 && args[2].equals("currency")) {
                     String currency = this.decode(args[3]);
                     return economy.balance("VaultUnlocked", accountId, "world", currency).toPlainString();
                  }

                  if (args.length == 5 && args[2].equals("currency") && args[4].equals("formatted")) {
                     String currency = this.decode(args[3]);
                     BigDecimal balance = economy.balance("VaultUnlocked", accountId, "world", currency);
                     return economy.format("VaultUnlocked", balance, currency);
                  }

                  if (args.length == 6 && args[2].equals("currency") && args[4].equals("world")) {
                     String currency = this.decode(args[3]);
                     String world = args[5];
                     return economy.balance("VaultUnlocked", accountId, world, currency).toPlainString();
                  }

                  if (args.length == 7 && args[2].equals("currency") && args[4].equals("world") && args[6].equals("formatted")) {
                     String currency = this.decode(args[3]);
                     String world = args[5];
                     BigDecimal balance = economy.balance("VaultUnlocked", accountId, world, currency);
                     return economy.format("VaultUnlocked", balance, currency);
                  }
                  break;
               case "can":
                  if (args.length != 3 || uuid == null) {
                     return null;
                  }

                  if (!economy.hasSharedAccountSupport()) {
                     return "no";
                  }

                  String action = args[1];
                  UUID targetAccountId = UUID.fromString(args[2]);
                  switch (action) {
                     case "deposit":
                        return this.booleanToYesNo(economy.hasAccountPermission("VaultUnlocked", targetAccountId, uuid, AccountPermission.DEPOSIT));
                     case "withdraw":
                        return this.booleanToYesNo(economy.hasAccountPermission("VaultUnlocked", targetAccountId, uuid, AccountPermission.WITHDRAW));
                     case "balance":
                        return this.booleanToYesNo(economy.hasAccountPermission("VaultUnlocked", targetAccountId, uuid, AccountPermission.BALANCE));
                     case "transfer":
                        return this.booleanToYesNo(economy.hasAccountPermission("VaultUnlocked", targetAccountId, uuid, AccountPermission.TRANSFER_OWNERSHIP));
                     case "invite":
                        return this.booleanToYesNo(economy.hasAccountPermission("VaultUnlocked", targetAccountId, uuid, AccountPermission.INVITE_MEMBER));
                     case "remove":
                        return this.booleanToYesNo(economy.hasAccountPermission("VaultUnlocked", targetAccountId, uuid, AccountPermission.REMOVE_MEMBER));
                     case "modify":
                        return this.booleanToYesNo(
                           economy.hasAccountPermission("VaultUnlocked", targetAccountId, uuid, AccountPermission.CHANGE_MEMBER_PERMISSION)
                        );
                     case "delete":
                        return this.booleanToYesNo(economy.hasAccountPermission("VaultUnlocked", targetAccountId, uuid, AccountPermission.DELETE));
                     default:
                        return null;
                  }
               case "accounts":
                  if (uuid == null) {
                     return null;
                  }

                  if (args.length == 1) {
                     List<String> accounts = economy.accountsMemberOf("VaultUnlocked", uuid);
                     StringBuilder joined = new StringBuilder();

                     for (int i = 0; i < accounts.size(); i++) {
                        if (i > 0) {
                           joined.append(", ");
                        }

                        joined.append(accounts.get(i));
                     }

                     return joined.toString();
                  }

                  if (args.length == 2 && args[1].equals("count")) {
                     List<String> accounts = economy.accountsMemberOf("VaultUnlocked", uuid);
                     return String.valueOf(accounts.size());
                  }
                  break;
               case "currency":
                  return economy.defaultCurrencyNameSingular("VaultUnlocked");
               case "currencyplural":
                  return economy.defaultCurrencyNamePlural("VaultUnlocked");
            }

            return null;
         } catch (Exception var16) {
            return null;
         }
      }
   }

   @Nullable
   private String onRequestLegacy(final OfflinePlayer player, @NotNull final String params) {
      Optional<net.milkbowl.vault.economy.Economy> economyOpt = Vault.instance().legacyProvider();
      if (!economyOpt.isPresent()) {
         return null;
      } else {
         net.milkbowl.vault.economy.Economy economy = economyOpt.get();
         String lowerParams = params.toLowerCase();
         String[] args = lowerParams.split("_");

         try {
            if (args.length < 2 || !args[0].equals("eco") || !args[1].equals("balance")) {
               return null;
            } else if (player == null) {
               return null;
            } else {
               double balance = economy.getBalance(player);
               if (args.length == 2) {
                  return BigDecimal.valueOf(balance).toPlainString();
               } else {
                  String modifier = args[2];
                  if (modifier.matches("\\d+dp")) {
                     int dp = Integer.parseInt(modifier.substring(0, modifier.length() - 2));
                     return BigDecimal.valueOf(balance).setScale(dp, RoundingMode.HALF_UP).toPlainString();
                  } else {
                     switch (modifier) {
                        case "fixed": {
                           int digits = economy.fractionalDigits();
                           if (digits < 0) {
                              return BigDecimal.valueOf(balance).toPlainString();
                           }

                           return BigDecimal.valueOf(balance).setScale(digits, RoundingMode.HALF_UP).toPlainString();
                        }
                        case "formatted":
                           return economy.format(balance);
                        case "commas": {
                           int digits = Math.max(0, economy.fractionalDigits());
                           DecimalFormat df = new DecimalFormat("#,##0" + (digits > 0 ? "." + "0".repeat(digits) : ""));
                           return df.format(balance);
                        }
                        default:
                           return null;
                     }
                  }
               }
            }
         } catch (Exception var14) {
            return null;
         }
      }
   }

   private String booleanToYesNo(final boolean bool) {
      return bool ? "yes" : "no";
   }

   private String decode(final String input) {
      return input.replace("%20", " ").replace("%24", "$").replace("%2B", "+").replace("%26", "&").replace("%2F", "/").replace("%3D", "=");
   }
}
