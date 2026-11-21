package com.earth2me.essentials.api;

import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import com.earth2me.essentials.config.EssentialsUserConfiguration;
import com.earth2me.essentials.userstorage.ModernUserMap;
import com.earth2me.essentials.utils.NumberUtil;
import com.earth2me.essentials.utils.StringUtil;
import com.google.common.base.Charsets;
import java.io.File;
import java.math.BigDecimal;
import java.math.MathContext;
import java.text.MessageFormat;
import java.util.UUID;
import java.util.logging.Level;
import net.ess3.api.IEssentials;
import net.ess3.api.MaxMoneyException;
import net.ess3.api.events.UserBalanceUpdateEvent;
import org.bukkit.entity.Player;

public class Economy {
   public static final MathContext MATH_CONTEXT = MathContext.DECIMAL128;
   private static IEssentials ess;
   private static final String WARN_CALL_BEFORE_LOAD = "Essentials API is called before Essentials is loaded.";
   private static final String WARN_EXISTING_NPC_CREATE = "Account creation was requested for NPC account {0}, but account already exists (UUID: {1}). Not creating an account.";
   private static final String WARN_PLAYER_UUID_NO_NAME = "Found player {0} by UUID {1} but not by their actual name. They may have changed their username.";
   private static final String WARN_NPC_RECREATE_1 = "Account creation was requested for NPC user {0}, but an account file with UUID {1} already exists.";
   private static final String WARN_NPC_RECREATE_2 = "Essentials will create a new account as requested by the other plugin, but this is almost certainly a bug and should be reported.";

   protected Economy() {
   }

   public static void setEss(final IEssentials aEss) {
      ess = aEss;
   }

   private static void createNPCFile(final String unsanitizedName) {
      File folder = new File(ess.getDataFolder(), "userdata");
      String name = ess.getSettings().isSafeUsermap() ? StringUtil.safeString(unsanitizedName) : unsanitizedName;
      if (!folder.exists() && !folder.mkdirs()) {
         throw new RuntimeException("Error while creating userdata directory!");
      } else {
         UUID npcUUID = UUID.nameUUIDFromBytes(("NPC:" + name).getBytes(Charsets.UTF_8));
         File npcFile = new File(folder, npcUUID + ".yml");
         if (npcFile.exists()) {
            ess.getLogger()
               .log(
                  Level.SEVERE,
                  MessageFormat.format(
                     "Account creation was requested for NPC user {0}, but an account file with UUID {1} already exists.", name, npcUUID.toString()
                  ),
                  (Throwable)(new RuntimeException())
               );
            ess.getLogger()
               .log(
                  Level.SEVERE,
                  "Essentials will create a new account as requested by the other plugin, but this is almost certainly a bug and should be reported."
               );
         }

         EssentialsUserConfiguration npcConfig = new EssentialsUserConfiguration(name, npcUUID, npcFile);
         npcConfig.load();
         npcConfig.setProperty("npc", true);
         npcConfig.setProperty("last-account-name", name);
         npcConfig.setProperty("npc-name", unsanitizedName);
         npcConfig.setProperty("money", ess.getSettings().getStartingBalance());
         npcConfig.blockingSave();
         ((ModernUserMap)ess.getUsers()).addCachedNpcName(npcUUID, name);
         ess.getUsers().getUser(npcUUID);
      }
   }

   private static void deleteNPC(final String name) {
      User user = ess.getUser(name);
      user.reset();
   }

   private static User getUserByName(final String name) {
      if (ess == null) {
         throw new RuntimeException("Essentials API is called before Essentials is loaded.");
      } else if (name == null) {
         throw new IllegalArgumentException("Economy username cannot be null");
      } else {
         User user = ess.getUser(name);
         if (user == null) {
            Player player = ess.getServer().getPlayerExact(name);
            if (player != null) {
               user = ess.getUser(player.getUniqueId());
               if (user != null) {
                  ess.getLogger()
                     .log(
                        Level.INFO,
                        MessageFormat.format(
                           "Found player {0} by UUID {1} but not by their actual name. They may have changed their username.",
                           name,
                           player.getUniqueId().toString()
                        ),
                        (Throwable)(new RuntimeException())
                     );
               }
            }
         }

         if (user == null) {
            user = getUserByUUID(UUID.nameUUIDFromBytes(("NPC:" + StringUtil.safeString(name)).getBytes(Charsets.UTF_8)));
         }

         return user;
      }
   }

   private static User getUserByUUID(final UUID uuid) {
      if (ess == null) {
         throw new RuntimeException("Essentials API is called before Essentials is loaded.");
      } else if (uuid == null) {
         throw new IllegalArgumentException("Economy uuid cannot be null");
      } else {
         return ess.getUser(uuid);
      }
   }

   @Deprecated
   public static double getMoney(final String name) throws UserDoesNotExistException {
      BigDecimal exactAmount = getMoneyExact(name);
      double amount = exactAmount.doubleValue();
      if (new BigDecimal(amount).compareTo(exactAmount) > 0) {
         amount = Math.nextAfter(amount, Double.NEGATIVE_INFINITY);
      }

      return amount;
   }

   @Deprecated
   public static BigDecimal getMoneyExact(final String name) throws UserDoesNotExistException {
      User user = getUserByName(name);
      if (user == null) {
         throw new UserDoesNotExistException(name);
      } else {
         return getMoneyExact(user);
      }
   }

   public static BigDecimal getMoneyExact(final UUID uuid) throws UserDoesNotExistException {
      User user = getUserByUUID(uuid);
      if (user == null) {
         throw new UserDoesNotExistException(uuid);
      } else {
         return getMoneyExact(user);
      }
   }

   public static BigDecimal getMoneyExact(final User user) {
      if (user == null) {
         throw new IllegalArgumentException("Economy user cannot be null");
      } else {
         return user.getMoney();
      }
   }

   @Deprecated
   public static void setMoney(final String name, final double balance) throws UserDoesNotExistException, NoLoanPermittedException, MaxMoneyException {
      try {
         setMoney(name, BigDecimal.valueOf(balance));
      } catch (ArithmeticException var4) {
         ess.getLogger().log(Level.WARNING, "Failed to set balance of " + name + " to " + balance + ": " + var4.getMessage(), (Throwable)var4);
      }
   }

   @Deprecated
   public static void setMoney(final String name, final BigDecimal balance) throws UserDoesNotExistException, NoLoanPermittedException, MaxMoneyException {
      User user = getUserByName(name);
      if (user == null) {
         throw new UserDoesNotExistException(name);
      } else {
         setMoney(user, balance);
      }
   }

   public static void setMoney(final UUID uuid, final BigDecimal balance) throws NoLoanPermittedException, UserDoesNotExistException, MaxMoneyException {
      User user = getUserByUUID(uuid);
      if (user == null) {
         throw new UserDoesNotExistException(uuid);
      } else {
         setMoney(user, balance);
      }
   }

   public static void setMoney(final User user, final BigDecimal balance) throws NoLoanPermittedException, MaxMoneyException {
      if (user == null) {
         throw new IllegalArgumentException("Economy user cannot be null");
      } else if (balance.compareTo(ess.getSettings().getMinMoney()) < 0) {
         throw new NoLoanPermittedException();
      } else if (balance.signum() < 0 && !user.isAuthorized("essentials.eco.loan")) {
         throw new NoLoanPermittedException();
      } else {
         user.setMoney(balance, UserBalanceUpdateEvent.Cause.API);
         Trade.log("API", "Set", "API", user.getName(), new Trade(balance, ess), null, null, null, balance, ess);
      }
   }

   @Deprecated
   public static void add(final String name, final double amount) throws UserDoesNotExistException, NoLoanPermittedException, MaxMoneyException {
      try {
         add(name, BigDecimal.valueOf(amount));
      } catch (ArithmeticException var4) {
         ess.getLogger().log(Level.WARNING, "Failed to add " + amount + " to balance of " + name + ": " + var4.getMessage(), (Throwable)var4);
      }
   }

   @Deprecated
   public static void add(final String name, final BigDecimal amount) throws UserDoesNotExistException, NoLoanPermittedException, ArithmeticException, MaxMoneyException {
      User user = getUserByName(name);
      if (user == null) {
         throw new UserDoesNotExistException(name);
      } else {
         add(user, amount);
      }
   }

   public static void add(final UUID uuid, final BigDecimal amount) throws NoLoanPermittedException, ArithmeticException, UserDoesNotExistException, MaxMoneyException {
      User user = getUserByUUID(uuid);
      if (user == null) {
         throw new UserDoesNotExistException(uuid);
      } else {
         add(user, amount);
      }
   }

   public static void add(final User user, final BigDecimal amount) throws NoLoanPermittedException, ArithmeticException, MaxMoneyException {
      if (user == null) {
         throw new IllegalArgumentException("Economy user cannot be null");
      } else {
         BigDecimal result = getMoneyExact(user).add(amount, MATH_CONTEXT);
         if (amount.signum() <= 0) {
            setMoney(user, result);
         } else {
            user.setMoney(result, UserBalanceUpdateEvent.Cause.API);
         }

         Trade.log("API", "Add", "API", user.getName(), new Trade(amount, ess), null, null, null, result, ess);
      }
   }

   @Deprecated
   public static void subtract(final String name, final double amount) throws UserDoesNotExistException, NoLoanPermittedException, MaxMoneyException {
      try {
         substract(name, BigDecimal.valueOf(amount));
      } catch (ArithmeticException var4) {
         ess.getLogger().log(Level.WARNING, "Failed to subtract " + amount + " of balance of " + name + ": " + var4.getMessage(), (Throwable)var4);
      }
   }

   @Deprecated
   public static void substract(final String name, final BigDecimal amount) throws UserDoesNotExistException, NoLoanPermittedException, ArithmeticException, MaxMoneyException {
      BigDecimal result = getMoneyExact(name).subtract(amount, MATH_CONTEXT);
      setMoney(name, result);
      Trade.log("API", "Subtract", "API", name, new Trade(amount, ess), null, null, null, result, ess);
   }

   public static void subtract(final UUID uuid, final BigDecimal amount) throws NoLoanPermittedException, ArithmeticException, UserDoesNotExistException, MaxMoneyException {
      User user = getUserByUUID(uuid);
      if (user == null) {
         throw new UserDoesNotExistException(uuid);
      } else {
         subtract(user, amount);
      }
   }

   public static void subtract(final User user, final BigDecimal amount) throws NoLoanPermittedException, ArithmeticException, MaxMoneyException {
      if (user == null) {
         throw new IllegalArgumentException("Economy user cannot be null");
      } else {
         BigDecimal result = getMoneyExact(user).subtract(amount, MATH_CONTEXT);
         setMoney(user, result);
         Trade.log("API", "Subtract", "API", user.getName(), new Trade(amount, ess), null, null, null, result, ess);
      }
   }

   @Deprecated
   public static void divide(final String name, final double amount) throws UserDoesNotExistException, NoLoanPermittedException, MaxMoneyException {
      try {
         divide(name, BigDecimal.valueOf(amount));
      } catch (ArithmeticException var4) {
         ess.getLogger().log(Level.WARNING, "Failed to divide balance of " + name + " by " + amount + ": " + var4.getMessage(), (Throwable)var4);
      }
   }

   @Deprecated
   public static void divide(final String name, final BigDecimal amount) throws UserDoesNotExistException, NoLoanPermittedException, ArithmeticException, MaxMoneyException {
      User user = getUserByName(name);
      if (user == null) {
         throw new UserDoesNotExistException(name);
      } else {
         divide(user, amount);
      }
   }

   public static void divide(final UUID uuid, final BigDecimal amount) throws NoLoanPermittedException, ArithmeticException, UserDoesNotExistException, MaxMoneyException {
      User user = getUserByUUID(uuid);
      if (user == null) {
         throw new UserDoesNotExistException(uuid);
      } else {
         divide(user, amount);
      }
   }

   public static void divide(final User user, final BigDecimal amount) throws NoLoanPermittedException, ArithmeticException, MaxMoneyException {
      if (user == null) {
         throw new IllegalArgumentException("Economy user cannot be null");
      } else {
         BigDecimal result = getMoneyExact(user).divide(amount, MATH_CONTEXT);
         setMoney(user, result);
         Trade.log("API", "Divide", "API", user.getName(), new Trade(amount, ess), null, null, null, result, ess);
      }
   }

   @Deprecated
   public static void multiply(final String name, final double amount) throws UserDoesNotExistException, NoLoanPermittedException, MaxMoneyException {
      try {
         multiply(name, BigDecimal.valueOf(amount));
      } catch (ArithmeticException var4) {
         ess.getLogger().log(Level.WARNING, "Failed to multiply balance of " + name + " by " + amount + ": " + var4.getMessage(), (Throwable)var4);
      }
   }

   @Deprecated
   public static void multiply(final String name, final BigDecimal amount) throws UserDoesNotExistException, NoLoanPermittedException, ArithmeticException, MaxMoneyException {
      User user = getUserByName(name);
      if (user == null) {
         throw new UserDoesNotExistException(name);
      } else {
         multiply(user, amount);
      }
   }

   public static void multiply(final UUID uuid, final BigDecimal amount) throws NoLoanPermittedException, ArithmeticException, UserDoesNotExistException, MaxMoneyException {
      User user = getUserByUUID(uuid);
      if (user == null) {
         throw new UserDoesNotExistException(uuid);
      } else {
         multiply(user, amount);
      }
   }

   public static void multiply(final User user, final BigDecimal amount) throws NoLoanPermittedException, ArithmeticException, MaxMoneyException {
      if (user == null) {
         throw new IllegalArgumentException("Economy user cannot be null");
      } else {
         BigDecimal result = getMoneyExact(user).multiply(amount, MATH_CONTEXT);
         setMoney(user, result);
         Trade.log("API", "Multiply", "API", user.getName(), new Trade(amount, ess), null, null, null, result, ess);
      }
   }

   @Deprecated
   public static void resetBalance(final String name) throws UserDoesNotExistException, NoLoanPermittedException, MaxMoneyException {
      if (ess == null) {
         throw new RuntimeException("Essentials API is called before Essentials is loaded.");
      } else {
         setMoney(name, ess.getSettings().getStartingBalance());
         Trade.log("API", "Reset", "API", name, new Trade(BigDecimal.ZERO, ess), null, null, null, ess.getSettings().getStartingBalance(), ess);
      }
   }

   public static void resetBalance(final UUID uuid) throws NoLoanPermittedException, UserDoesNotExistException, MaxMoneyException {
      User user = getUserByUUID(uuid);
      if (user == null) {
         throw new UserDoesNotExistException(uuid);
      } else {
         resetBalance(user);
      }
   }

   public static void resetBalance(final User user) throws NoLoanPermittedException, MaxMoneyException {
      if (ess == null) {
         throw new RuntimeException("Essentials API is called before Essentials is loaded.");
      } else if (user == null) {
         throw new IllegalArgumentException("Economy user cannot be null");
      } else {
         setMoney(user, ess.getSettings().getStartingBalance());
         Trade.log("API", "Reset", "API", user.getName(), new Trade(BigDecimal.ZERO, ess), null, null, null, ess.getSettings().getStartingBalance(), ess);
      }
   }

   @Deprecated
   public static boolean hasEnough(final String name, final double amount) throws UserDoesNotExistException {
      try {
         return hasEnough(name, BigDecimal.valueOf(amount));
      } catch (ArithmeticException var4) {
         ess.getLogger().log(Level.WARNING, "Failed to compare balance of " + name + " with " + amount + ": " + var4.getMessage(), (Throwable)var4);
         return false;
      }
   }

   /** @deprecated */
   public static boolean hasEnough(final String name, final BigDecimal amount) throws UserDoesNotExistException, ArithmeticException {
      User user = getUserByName(name);
      if (user == null) {
         throw new UserDoesNotExistException(name);
      } else {
         return hasEnough(user, amount);
      }
   }

   public static boolean hasEnough(final UUID uuid, final BigDecimal amount) throws ArithmeticException, UserDoesNotExistException {
      User user = getUserByUUID(uuid);
      if (user == null) {
         throw new UserDoesNotExistException(uuid);
      } else {
         return hasEnough(user, amount);
      }
   }

   public static boolean hasEnough(final User user, final BigDecimal amount) throws ArithmeticException {
      if (user == null) {
         throw new IllegalArgumentException("Economy user cannot be null");
      } else {
         return amount.compareTo(getMoneyExact(user)) <= 0;
      }
   }

   @Deprecated
   public static boolean hasMore(final String name, final double amount) throws UserDoesNotExistException {
      try {
         return hasMore(name, BigDecimal.valueOf(amount));
      } catch (ArithmeticException var4) {
         ess.getLogger().log(Level.WARNING, "Failed to compare balance of " + name + " with " + amount + ": " + var4.getMessage(), (Throwable)var4);
         return false;
      }
   }

   @Deprecated
   public static boolean hasMore(final String name, final BigDecimal amount) throws UserDoesNotExistException, ArithmeticException {
      User user = getUserByName(name);
      if (user == null) {
         throw new UserDoesNotExistException(name);
      } else {
         return hasMore(user, amount);
      }
   }

   public static boolean hasMore(final UUID uuid, final BigDecimal amount) throws ArithmeticException, UserDoesNotExistException {
      User user = getUserByUUID(uuid);
      if (user == null) {
         throw new UserDoesNotExistException(uuid);
      } else {
         return hasMore(user, amount);
      }
   }

   public static boolean hasMore(final User user, final BigDecimal amount) throws ArithmeticException {
      if (user == null) {
         throw new IllegalArgumentException("Economy user cannot be null");
      } else {
         return amount.compareTo(getMoneyExact(user)) < 0;
      }
   }

   @Deprecated
   public static boolean hasLess(final String name, final double amount) throws UserDoesNotExistException {
      try {
         return hasLess(name, BigDecimal.valueOf(amount));
      } catch (ArithmeticException var4) {
         ess.getLogger().log(Level.WARNING, "Failed to compare balance of " + name + " with " + amount + ": " + var4.getMessage(), (Throwable)var4);
         return false;
      }
   }

   @Deprecated
   public static boolean hasLess(final String name, final BigDecimal amount) throws UserDoesNotExistException, ArithmeticException {
      User user = getUserByName(name);
      if (user == null) {
         throw new UserDoesNotExistException(name);
      } else {
         return hasLess(user, amount);
      }
   }

   public static boolean hasLess(final UUID uuid, final BigDecimal amount) throws ArithmeticException, UserDoesNotExistException {
      User user = getUserByUUID(uuid);
      if (user == null) {
         throw new UserDoesNotExistException(uuid);
      } else {
         return hasLess(user, amount);
      }
   }

   public static boolean hasLess(final User user, final BigDecimal amount) throws ArithmeticException {
      if (user == null) {
         throw new IllegalArgumentException("Economy user cannot be null");
      } else {
         return amount.compareTo(getMoneyExact(user)) > 0;
      }
   }

   @Deprecated
   public static boolean isNegative(final String name) throws UserDoesNotExistException {
      User user = getUserByName(name);
      if (user == null) {
         throw new UserDoesNotExistException(name);
      } else {
         return isNegative(user);
      }
   }

   public static boolean isNegative(final UUID uuid) throws UserDoesNotExistException {
      User user = getUserByUUID(uuid);
      if (user == null) {
         throw new UserDoesNotExistException(uuid);
      } else {
         return isNegative(user);
      }
   }

   public static boolean isNegative(final User user) {
      if (user == null) {
         throw new IllegalArgumentException("Economy user cannot be null");
      } else {
         return getMoneyExact(user).signum() < 0;
      }
   }

   @Deprecated
   public static String format(final double amount) {
      try {
         return format(BigDecimal.valueOf(amount));
      } catch (NumberFormatException var3) {
         ess.getLogger().log(Level.WARNING, "Failed to display " + amount + ": " + var3.getMessage(), (Throwable)var3);
         return "NaN";
      }
   }

   public static String format(final BigDecimal amount) {
      if (ess == null) {
         throw new RuntimeException("Essentials API is called before Essentials is loaded.");
      } else {
         return NumberUtil.displayCurrency(amount, ess);
      }
   }

   @Deprecated
   public static boolean playerExists(final String name) {
      return getUserByName(name) != null;
   }

   public static boolean playerExists(final UUID uuid) {
      return getUserByUUID(uuid) != null;
   }

   public static boolean isNPC(final String name) throws UserDoesNotExistException {
      User user = getUserByName(name);
      if (user == null) {
         throw new UserDoesNotExistException(name);
      } else {
         return user.isNPC();
      }
   }

   public static boolean createNPC(final String name) {
      User user = getUserByName(name);
      if (user == null) {
         createNPCFile(name);
         return true;
      } else {
         ess.getLogger()
            .log(
               Level.WARNING,
               MessageFormat.format(
                  "Account creation was requested for NPC account {0}, but account already exists (UUID: {1}). Not creating an account.",
                  name,
                  user.getConfigUUID()
               ),
               (Throwable)(new RuntimeException())
            );
         return false;
      }
   }

   public static void removeNPC(final String name) throws UserDoesNotExistException {
      User user = getUserByName(name);
      if (user == null) {
         throw new UserDoesNotExistException(name);
      } else {
         deleteNPC(name);
      }
   }
}
