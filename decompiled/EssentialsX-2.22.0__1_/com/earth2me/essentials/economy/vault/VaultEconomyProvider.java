package com.earth2me.essentials.economy.vault;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.OfflinePlayerStub;
import com.earth2me.essentials.api.NoLoanPermittedException;
import com.earth2me.essentials.api.UserDoesNotExistException;
import com.earth2me.essentials.config.EssentialsUserConfiguration;
import com.earth2me.essentials.utils.NumberUtil;
import com.earth2me.essentials.utils.StringUtil;
import com.google.common.base.Charsets;
import java.io.File;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import net.ess3.api.MaxMoneyException;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class VaultEconomyProvider implements Economy {
   private static final String WARN_NPC_RECREATE_1 = "Account creation was requested for NPC user {0}, but an account file with UUID {1} already exists.";
   private static final String WARN_NPC_RECREATE_2 = "Essentials will create a new account as requested by the other plugin, but this is almost certainly a bug and should be reported.";
   private final Essentials ess;

   public VaultEconomyProvider(Essentials essentials) {
      this.ess = essentials;
   }

   public boolean isEnabled() {
      return this.ess.isEnabled();
   }

   public String getName() {
      return "EssentialsX Economy";
   }

   public boolean hasBankSupport() {
      return false;
   }

   public int fractionalDigits() {
      return -1;
   }

   public String format(double amount) {
      return this.ess.getAdventureFacet().miniToLegacy(NumberUtil.displayCurrency(BigDecimal.valueOf(amount), this.ess));
   }

   public String currencyNamePlural() {
      return this.currencyNameSingular();
   }

   public String currencyNameSingular() {
      return this.ess.getSettings().getCurrencySymbol();
   }

   public boolean hasAccount(String playerName) {
      return com.earth2me.essentials.api.Economy.playerExists(playerName)
         ? true
         : com.earth2me.essentials.api.Economy.playerExists(UUID.nameUUIDFromBytes(("NPC:" + StringUtil.safeString(playerName)).getBytes(Charsets.UTF_8)));
   }

   public boolean hasAccount(OfflinePlayer player) {
      return com.earth2me.essentials.api.Economy.playerExists(player.getUniqueId());
   }

   public boolean hasAccount(String playerName, String worldName) {
      return this.hasAccount(playerName);
   }

   public boolean hasAccount(OfflinePlayer player, String worldName) {
      return this.hasAccount(player);
   }

   public double getBalance(String playerName) {
      try {
         return this.getDoubleValue(com.earth2me.essentials.api.Economy.getMoneyExact(playerName));
      } catch (UserDoesNotExistException var3) {
         this.createPlayerAccount(playerName);
         return this.getDoubleValue(this.ess.getSettings().getStartingBalance());
      }
   }

   public double getBalance(OfflinePlayer player) {
      try {
         return this.getDoubleValue(com.earth2me.essentials.api.Economy.getMoneyExact(player.getUniqueId()));
      } catch (UserDoesNotExistException var3) {
         this.createPlayerAccount(player);
         return this.getDoubleValue(this.ess.getSettings().getStartingBalance());
      }
   }

   private double getDoubleValue(final BigDecimal value) {
      double amount = value.doubleValue();
      if (new BigDecimal(amount).compareTo(value) > 0) {
         amount = Math.nextAfter(amount, Double.NEGATIVE_INFINITY);
      }

      return amount;
   }

   public double getBalance(String playerName, String world) {
      return this.getBalance(playerName);
   }

   public double getBalance(OfflinePlayer player, String world) {
      return this.getBalance(player);
   }

   public boolean has(String playerName, double amount) {
      try {
         return com.earth2me.essentials.api.Economy.hasEnough(playerName, amount);
      } catch (UserDoesNotExistException var5) {
         return false;
      }
   }

   public boolean has(OfflinePlayer player, double amount) {
      try {
         return com.earth2me.essentials.api.Economy.hasEnough(player.getUniqueId(), BigDecimal.valueOf(amount));
      } catch (UserDoesNotExistException var5) {
         return false;
      }
   }

   public boolean has(String playerName, String worldName, double amount) {
      return this.has(playerName, amount);
   }

   public boolean has(OfflinePlayer player, String worldName, double amount) {
      return this.has(player, amount);
   }

   public EconomyResponse withdrawPlayer(String playerName, double amount) {
      if (playerName == null) {
         return new EconomyResponse(0.0, 0.0, ResponseType.FAILURE, "Player name cannot be null!");
      } else if (amount < 0.0) {
         return new EconomyResponse(0.0, 0.0, ResponseType.FAILURE, "Cannot withdraw negative funds!");
      } else {
         try {
            com.earth2me.essentials.api.Economy.subtract(playerName, amount);
            return new EconomyResponse(amount, this.getBalance(playerName), ResponseType.SUCCESS, null);
         } catch (UserDoesNotExistException var5) {
            return new EconomyResponse(0.0, 0.0, ResponseType.FAILURE, "User does not exist!");
         } catch (NoLoanPermittedException var6) {
            return new EconomyResponse(0.0, this.getBalance(playerName), ResponseType.FAILURE, "Loan was not permitted!");
         } catch (MaxMoneyException var7) {
            return new EconomyResponse(0.0, this.getBalance(playerName), ResponseType.FAILURE, "User goes over maximum money limit!");
         }
      }
   }

   public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
      if (player == null) {
         return new EconomyResponse(0.0, 0.0, ResponseType.FAILURE, "Player cannot be null!");
      } else if (amount < 0.0) {
         return new EconomyResponse(0.0, 0.0, ResponseType.FAILURE, "Cannot withdraw negative funds!");
      } else {
         try {
            com.earth2me.essentials.api.Economy.subtract(player.getUniqueId(), BigDecimal.valueOf(amount));
            return new EconomyResponse(amount, this.getBalance(player), ResponseType.SUCCESS, null);
         } catch (UserDoesNotExistException var5) {
            return new EconomyResponse(0.0, 0.0, ResponseType.FAILURE, "User does not exist!");
         } catch (NoLoanPermittedException var6) {
            return new EconomyResponse(0.0, this.getBalance(player), ResponseType.FAILURE, "Loan was not permitted!");
         } catch (MaxMoneyException var7) {
            return new EconomyResponse(0.0, this.getBalance(player), ResponseType.FAILURE, "User goes over maximum money limit!");
         }
      }
   }

   public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
      return this.withdrawPlayer(playerName, amount);
   }

   public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
      return this.withdrawPlayer(player, amount);
   }

   public EconomyResponse depositPlayer(String playerName, double amount) {
      if (playerName == null) {
         return new EconomyResponse(0.0, 0.0, ResponseType.FAILURE, "Player name can not be null.");
      } else if (amount < 0.0) {
         return new EconomyResponse(0.0, 0.0, ResponseType.FAILURE, "Cannot deposit negative funds");
      } else {
         try {
            com.earth2me.essentials.api.Economy.add(playerName, amount);
            return new EconomyResponse(amount, this.getBalance(playerName), ResponseType.SUCCESS, null);
         } catch (UserDoesNotExistException var5) {
            return new EconomyResponse(0.0, 0.0, ResponseType.FAILURE, "User does not exist!");
         } catch (NoLoanPermittedException var6) {
            return new EconomyResponse(0.0, this.getBalance(playerName), ResponseType.FAILURE, "Loan was not permitted!");
         } catch (MaxMoneyException var7) {
            return new EconomyResponse(0.0, this.getBalance(playerName), ResponseType.FAILURE, "User goes over maximum money limit!");
         }
      }
   }

   public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
      if (player == null) {
         return new EconomyResponse(0.0, 0.0, ResponseType.FAILURE, "Player can not be null.");
      } else if (amount < 0.0) {
         return new EconomyResponse(0.0, 0.0, ResponseType.FAILURE, "Cannot deposit negative funds");
      } else {
         try {
            com.earth2me.essentials.api.Economy.add(player.getUniqueId(), BigDecimal.valueOf(amount));
            return new EconomyResponse(amount, this.getBalance(player), ResponseType.SUCCESS, null);
         } catch (UserDoesNotExistException var5) {
            return new EconomyResponse(0.0, 0.0, ResponseType.FAILURE, "User does not exist!");
         } catch (NoLoanPermittedException var6) {
            return new EconomyResponse(0.0, this.getBalance(player), ResponseType.FAILURE, "Loan was not permitted!");
         } catch (MaxMoneyException var7) {
            return new EconomyResponse(0.0, this.getBalance(player), ResponseType.FAILURE, "User goes over maximum money limit!");
         }
      }
   }

   public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
      return this.depositPlayer(playerName, amount);
   }

   public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
      return this.depositPlayer(player, amount);
   }

   public boolean createPlayerAccount(String playerName) {
      return this.hasAccount(playerName) ? false : com.earth2me.essentials.api.Economy.createNPC(playerName);
   }

   public boolean createPlayerAccount(OfflinePlayer player) {
      if (this.hasAccount(player)) {
         return false;
      } else if (player.getUniqueId().version() != 3 && player.getUniqueId().version() != 2) {
         if (this.ess.getSettings().isDebug()) {
            this.ess.getLogger().info("Vault requested a player account creation for a v4 UUID: " + player);
         }

         Player userPlayer;
         if (player instanceof Player) {
            userPlayer = (Player)player;
         } else {
            OfflinePlayerStub essPlayer = new OfflinePlayerStub(player.getUniqueId(), this.ess.getServer());
            essPlayer.setName(player.getName());
            userPlayer = essPlayer;
         }

         this.ess.getUsers().getUser(userPlayer);
         return true;
      } else {
         File folder = new File(this.ess.getDataFolder(), "userdata");
         if (!folder.exists() && !folder.mkdirs()) {
            throw new RuntimeException("Error while creating userdata directory!");
         } else {
            File npcFile = new File(folder, player.getUniqueId() + ".yml");
            if (npcFile.exists()) {
               this.ess
                  .getLogger()
                  .log(
                     Level.SEVERE,
                     MessageFormat.format(
                        "Account creation was requested for NPC user {0}, but an account file with UUID {1} already exists.",
                        player.getName(),
                        player.getUniqueId().toString()
                     ),
                     (Throwable)(new RuntimeException())
                  );
               this.ess
                  .getLogger()
                  .log(
                     Level.SEVERE,
                     "Essentials will create a new account as requested by the other plugin, but this is almost certainly a bug and should be reported."
                  );
            }

            EssentialsUserConfiguration npcConfig = new EssentialsUserConfiguration(player.getName(), player.getUniqueId(), npcFile);
            npcConfig.load();
            npcConfig.setProperty("npc", true);
            npcConfig.setProperty("last-account-name", player.getName());
            npcConfig.setProperty("money", this.ess.getSettings().getStartingBalance());
            npcConfig.blockingSave();
            this.ess.getUsers().addCachedNpcName(player.getUniqueId(), player.getName());
            this.ess.getUsers().getUser(player.getUniqueId());
            return true;
         }
      }
   }

   public boolean createPlayerAccount(String playerName, String worldName) {
      return this.createPlayerAccount(playerName);
   }

   public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
      return this.createPlayerAccount(player);
   }

   public EconomyResponse createBank(String name, String player) {
      return new EconomyResponse(0.0, 0.0, ResponseType.NOT_IMPLEMENTED, "EssentialsX does not support bank accounts!");
   }

   public EconomyResponse createBank(String name, OfflinePlayer player) {
      return new EconomyResponse(0.0, 0.0, ResponseType.NOT_IMPLEMENTED, "EssentialsX does not support bank accounts!");
   }

   public EconomyResponse deleteBank(String name) {
      return new EconomyResponse(0.0, 0.0, ResponseType.NOT_IMPLEMENTED, "EssentialsX does not support bank accounts!");
   }

   public EconomyResponse bankBalance(String name) {
      return new EconomyResponse(0.0, 0.0, ResponseType.NOT_IMPLEMENTED, "EssentialsX does not support bank accounts!");
   }

   public EconomyResponse bankHas(String name, double amount) {
      return new EconomyResponse(0.0, 0.0, ResponseType.NOT_IMPLEMENTED, "EssentialsX does not support bank accounts!");
   }

   public EconomyResponse bankWithdraw(String name, double amount) {
      return new EconomyResponse(0.0, 0.0, ResponseType.NOT_IMPLEMENTED, "EssentialsX does not support bank accounts!");
   }

   public EconomyResponse bankDeposit(String name, double amount) {
      return new EconomyResponse(0.0, 0.0, ResponseType.NOT_IMPLEMENTED, "EssentialsX does not support bank accounts!");
   }

   public EconomyResponse isBankOwner(String name, String playerName) {
      return new EconomyResponse(0.0, 0.0, ResponseType.NOT_IMPLEMENTED, "EssentialsX does not support bank accounts!");
   }

   public EconomyResponse isBankOwner(String name, OfflinePlayer player) {
      return new EconomyResponse(0.0, 0.0, ResponseType.NOT_IMPLEMENTED, "EssentialsX does not support bank accounts!");
   }

   public EconomyResponse isBankMember(String name, String playerName) {
      return new EconomyResponse(0.0, 0.0, ResponseType.NOT_IMPLEMENTED, "EssentialsX does not support bank accounts!");
   }

   public EconomyResponse isBankMember(String name, OfflinePlayer player) {
      return new EconomyResponse(0.0, 0.0, ResponseType.NOT_IMPLEMENTED, "EssentialsX does not support bank accounts!");
   }

   public List<String> getBanks() {
      return Collections.emptyList();
   }
}
