package com.earth2me.essentials;

import com.earth2me.essentials.adventure.AdventureUtil;
import com.earth2me.essentials.craftbukkit.Inventories;
import com.earth2me.essentials.craftbukkit.SetExpFix;
import com.earth2me.essentials.utils.NumberUtil;
import com.earth2me.essentials.utils.VersionUtil;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import net.ess3.api.MaxMoneyException;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public class Trade {
   private static FileWriter fw = null;
   private final transient String command;
   private final transient Trade fallbackTrade;
   private final transient BigDecimal money;
   private final transient ItemStack itemStack;
   private final transient Integer exp;
   private final transient net.ess3.api.IEssentials ess;

   public Trade(final String command, final net.ess3.api.IEssentials ess) {
      this(command, null, null, null, null, ess);
   }

   public Trade(final String command, final Trade fallback, final net.ess3.api.IEssentials ess) {
      this(command, fallback, null, null, null, ess);
   }

   @Deprecated
   public Trade(final double money, final IEssentials ess) {
      this(null, null, BigDecimal.valueOf(money), null, null, (net.ess3.api.IEssentials)ess);
   }

   public Trade(final BigDecimal money, final net.ess3.api.IEssentials ess) {
      this(null, null, money, null, null, ess);
   }

   public Trade(final ItemStack items, final net.ess3.api.IEssentials ess) {
      this(null, null, null, items, null, ess);
   }

   public Trade(final int exp, final net.ess3.api.IEssentials ess) {
      this(null, null, null, null, exp, ess);
   }

   private Trade(
      final String command, final Trade fallback, final BigDecimal money, final ItemStack item, final Integer exp, final net.ess3.api.IEssentials ess
   ) {
      this.command = command;
      this.fallbackTrade = fallback;
      this.money = money;
      this.itemStack = item;
      this.exp = exp;
      this.ess = ess;
   }

   public static void log(
      final String type,
      final String subtype,
      final String event,
      final String sender,
      final Trade charge,
      final String receiver,
      final Trade pay,
      final Location loc,
      final BigDecimal endBalance,
      final net.ess3.api.IEssentials ess
   ) {
      if ((loc != null || ess.getSettings().isEcoLogUpdateEnabled()) && (loc == null || ess.getSettings().isEcoLogEnabled())) {
         if (fw == null) {
            try {
               fw = new FileWriter(new File(ess.getDataFolder(), "trade.log"), true);
            } catch (IOException var14) {
               Essentials.getWrappedLogger().log(Level.SEVERE, null, var14);
            }
         }

         StringBuilder sb = new StringBuilder();
         sb.append(type).append(",").append(subtype).append(",").append(event).append(",\"");
         sb.append(DateFormat.getDateTimeInstance(0, 0).format(new Date()));
         sb.append("\",\"");
         if (sender != null) {
            String senderIdentifier = sender;
            if (ess.getSettings().isEcoLogUUIDEnabled()) {
               UUID uuid = ess.getUsers().getNameCache().get(sender);
               if (uuid != null) {
                  senderIdentifier = uuid.toString();
               }
            }

            sb.append(senderIdentifier);
         }

         sb.append("\",");
         if (charge == null) {
            sb.append("\"\",\"\",\"\"");
         } else {
            if (charge.getItemStack() != null) {
               sb.append(charge.getItemStack().getAmount()).append(",");
               sb.append(charge.getItemStack().getType()).append(",");
               if (VersionUtil.PRE_FLATTENING) {
                  sb.append(charge.getItemStack().getDurability());
               }
            }

            if (charge.getMoney() != null) {
               sb.append(charge.getMoney()).append(",");
               sb.append("money").append(",");
               sb.append(ess.getSettings().getCurrencySymbol());
            }

            if (charge.getExperience() != null) {
               sb.append(charge.getExperience()).append(",");
               sb.append("exp").append(",");
               sb.append("\"\"");
            }
         }

         sb.append(",\"");
         if (receiver != null) {
            String receiverIdentifier = receiver;
            if (ess.getSettings().isEcoLogUUIDEnabled()) {
               UUID uuid = ess.getUsers().getNameCache().get(receiver);
               if (uuid != null) {
                  receiverIdentifier = uuid.toString();
               }
            }

            sb.append(receiverIdentifier);
         }

         sb.append("\",");
         if (pay == null) {
            sb.append("\"\",\"\",\"\"");
         } else {
            if (pay.getItemStack() != null) {
               sb.append(pay.getItemStack().getAmount()).append(",");
               sb.append(pay.getItemStack().getType()).append(",");
               if (VersionUtil.PRE_FLATTENING) {
                  sb.append(pay.getItemStack().getDurability());
               }
            }

            if (pay.getMoney() != null) {
               sb.append(pay.getMoney()).append(",");
               sb.append("money").append(",");
               sb.append(ess.getSettings().getCurrencySymbol());
            }

            if (pay.getExperience() != null) {
               sb.append(pay.getExperience()).append(",");
               sb.append("exp").append(",");
               sb.append("\"\"");
            }
         }

         if (loc == null) {
            sb.append(",\"\",\"\",\"\",\"\"");
         } else {
            sb.append(",\"");
            sb.append(loc.getWorld().getName()).append("\",");
            sb.append(loc.getBlockX()).append(",");
            sb.append(loc.getBlockY()).append(",");
            sb.append(loc.getBlockZ()).append(",");
         }

         if (endBalance == null) {
            sb.append(",");
         } else {
            sb.append(endBalance);
            sb.append(",");
         }

         sb.append("\n");

         try {
            fw.write(sb.toString());
            fw.flush();
         } catch (IOException var13) {
            Essentials.getWrappedLogger().log(Level.SEVERE, null, var13);
         }
      }
   }

   public static void closeLog() {
      if (fw != null) {
         try {
            fw.close();
         } catch (IOException var1) {
            Essentials.getWrappedLogger().log(Level.SEVERE, null, var1);
         }

         fw = null;
      }
   }

   public void isAffordableFor(final net.ess3.api.IUser user) throws ChargeException {
      CompletableFuture<Boolean> future = new CompletableFuture<>();
      this.isAffordableFor(user, future);
      if (future.isCompletedExceptionally()) {
         try {
            future.get();
         } catch (InterruptedException var4) {
            var4.printStackTrace();
         } catch (ExecutionException var5) {
            throw (ChargeException)var5.getCause();
         }
      }
   }

   public void isAffordableFor(final net.ess3.api.IUser user, final CompletableFuture<Boolean> future) {
      if (this.ess.getSettings().isDebug()) {
         this.ess.getLogger().log(Level.INFO, "checking if " + user.getName() + " can afford charge.");
      }

      if (this.getMoney() != null && this.getMoney().signum() > 0 && !user.canAfford(this.getMoney())) {
         future.completeExceptionally(new ChargeException("notEnoughMoney", AdventureUtil.parsed(NumberUtil.displayCurrency(this.getMoney(), this.ess))));
      } else if (this.getItemStack() != null && !Inventories.containsAtLeast(user.getBase(), this.itemStack, this.itemStack.getAmount())) {
         future.completeExceptionally(new ChargeException("missingItems", this.getItemStack().getAmount(), this.ess.getItemDb().name(this.getItemStack())));
      } else {
         BigDecimal money;
         if (this.command != null && !this.command.isEmpty() && (money = this.getCommandCost(user)).signum() > 0 && !user.canAfford(money)) {
            future.completeExceptionally(new ChargeException("notEnoughMoney", AdventureUtil.parsed(NumberUtil.displayCurrency(money, this.ess))));
         } else {
            if (this.exp != null && this.exp > 0 && SetExpFix.getTotalExperience(user.getBase()) < this.exp) {
               future.completeExceptionally(new ChargeException("notEnoughExperience"));
            }
         }
      }
   }

   public boolean pay(final net.ess3.api.IUser user) throws MaxMoneyException {
      return this.pay(user, Trade.OverflowType.ABORT) == null;
   }

   public Map<Integer, ItemStack> pay(final net.ess3.api.IUser user, final Trade.OverflowType type) throws MaxMoneyException {
      if (this.getMoney() != null && this.getMoney().signum() > 0) {
         if (this.ess.getSettings().isDebug()) {
            this.ess.getLogger().log(Level.INFO, "paying user " + user.getName() + " via trade " + this.getMoney().toPlainString());
         }

         user.giveMoney(this.getMoney());
      }

      if (this.getItemStack() != null) {
         if (type == Trade.OverflowType.ABORT && !Inventories.hasSpace(user.getBase(), 0, false, this.getItemStack())) {
            if (this.ess.getSettings().isDebug()) {
               this.ess
                  .getLogger()
                  .log(Level.INFO, "abort paying " + user.getName() + " itemstack " + this.getItemStack().toString() + " due to lack of inventory space ");
            }

            return Collections.singletonMap(0, this.getItemStack());
         }

         Map<Integer, ItemStack> leftover = Inventories.addItem(user.getBase(), this.getItemStack());
         user.getBase().updateInventory();
         if (leftover.isEmpty()) {
            if (this.ess.getSettings().isDebug()) {
               this.ess.getLogger().log(Level.INFO, "paying " + user.getName() + " itemstack " + this.getItemStack().toString());
            }
         } else {
            if (type == Trade.OverflowType.RETURN) {
               if (this.ess.getSettings().isDebug()) {
                  this.ess
                     .getLogger()
                     .log(
                        Level.INFO,
                        "paying " + user.getName() + " partial itemstack " + this.getItemStack().toString() + " with overflow " + leftover.get(0).toString()
                     );
               }

               return leftover;
            }

            for (ItemStack itemStack : leftover.values()) {
               for (int spillAmount = itemStack.getAmount(); spillAmount > 0; spillAmount -= itemStack.getAmount()) {
                  itemStack.setAmount(Math.min(spillAmount, itemStack.getMaxStackSize()));
                  user.getBase().getWorld().dropItemNaturally(user.getBase().getLocation(), itemStack);
               }
            }

            if (this.ess.getSettings().isDebug()) {
               this.ess
                  .getLogger()
                  .log(
                     Level.INFO,
                     "paying "
                        + user.getName()
                        + " partial itemstack "
                        + this.getItemStack().toString()
                        + " and dropping overflow "
                        + leftover.get(0).toString()
                  );
            }
         }

         user.getBase().updateInventory();
      }

      if (this.getExperience() != null) {
         SetExpFix.setTotalExperience(user.getBase(), SetExpFix.getTotalExperience(user.getBase()) + this.getExperience());
      }

      return null;
   }

   public void charge(final net.ess3.api.IUser user) throws ChargeException {
      CompletableFuture<Boolean> future = new CompletableFuture<>();
      this.charge(user, future);
      if (future.isCompletedExceptionally()) {
         try {
            future.get();
         } catch (InterruptedException var4) {
            var4.printStackTrace();
         } catch (ExecutionException var5) {
            throw (ChargeException)var5.getCause();
         }
      }
   }

   public void charge(final net.ess3.api.IUser user, final CompletableFuture<Boolean> future) {
      if (this.ess.getSettings().isDebug()) {
         this.ess.getLogger().log(Level.INFO, "attempting to charge user " + user.getName());
      }

      if (this.getMoney() != null) {
         if (this.ess.getSettings().isDebug()) {
            this.ess.getLogger().log(Level.INFO, "charging user " + user.getName() + " money " + this.getMoney().toPlainString());
         }

         if (!user.canAfford(this.getMoney()) && this.getMoney().signum() > 0) {
            future.completeExceptionally(new ChargeException("notEnoughMoney", AdventureUtil.parsed(NumberUtil.displayCurrency(this.getMoney(), this.ess))));
            return;
         }

         user.takeMoney(this.getMoney());
      }

      if (this.getItemStack() != null) {
         if (this.ess.getSettings().isDebug()) {
            this.ess.getLogger().log(Level.INFO, "charging user " + user.getName() + " itemstack " + this.getItemStack().toString());
         }

         if (!Inventories.containsAtLeast(user.getBase(), this.getItemStack(), this.getItemStack().getAmount())) {
            future.completeExceptionally(
               new ChargeException(
                  "missingItems", this.getItemStack().getAmount(), this.getItemStack().getType().toString().toLowerCase(Locale.ENGLISH).replace("_", " ")
               )
            );
            return;
         }

         Inventories.removeItemAmount(user.getBase(), this.getItemStack(), this.getItemStack().getAmount());
         user.getBase().updateInventory();
      }

      if (this.command != null) {
         BigDecimal cost = this.getCommandCost(user);
         if (!user.canAfford(cost) && cost.signum() > 0) {
            future.completeExceptionally(new ChargeException("notEnoughMoney", AdventureUtil.parsed(NumberUtil.displayCurrency(cost, this.ess))));
            return;
         }

         user.takeMoney(cost);
      }

      if (this.getExperience() != null) {
         if (this.ess.getSettings().isDebug()) {
            this.ess.getLogger().log(Level.INFO, "charging user " + user.getName() + " exp " + this.getExperience());
         }

         int experience = SetExpFix.getTotalExperience(user.getBase());
         if (experience < this.getExperience() && this.getExperience() > 0) {
            future.completeExceptionally(new ChargeException("notEnoughExperience"));
            return;
         }

         SetExpFix.setTotalExperience(user.getBase(), experience - this.getExperience());
      }

      if (this.ess.getSettings().isDebug()) {
         this.ess.getLogger().log(Level.INFO, "charge user " + user.getName() + " completed");
      }
   }

   public BigDecimal getMoney() {
      return this.money;
   }

   public ItemStack getItemStack() {
      return this.itemStack;
   }

   public Integer getExperience() {
      return this.exp;
   }

   public Trade.TradeType getType() {
      if (this.getExperience() != null) {
         return Trade.TradeType.EXP;
      } else {
         return this.getItemStack() != null ? Trade.TradeType.ITEM : Trade.TradeType.MONEY;
      }
   }

   public BigDecimal getCommandCost(final net.ess3.api.IUser user) {
      BigDecimal cost = BigDecimal.ZERO;
      if (this.command != null && !this.command.isEmpty()) {
         cost = this.ess.getSettings().getCommandCost(this.command.charAt(0) == '/' ? this.command.substring(1) : this.command);
         if (cost.signum() == 0 && this.fallbackTrade != null) {
            cost = this.fallbackTrade.getCommandCost(user);
         }

         if (this.ess.getSettings().isDebug()) {
            this.ess.getLogger().log(Level.INFO, "calculated command (" + this.command + ") cost for " + user.getName() + " as " + cost);
         }
      }

      return cost.signum() == 0 || !user.isAuthorized("essentials.nocommandcost.all") && !user.isAuthorized("essentials.nocommandcost." + this.command)
         ? cost
         : BigDecimal.ZERO;
   }

   public static enum OverflowType {
      ABORT,
      DROP,
      RETURN;
   }

   public static enum TradeType {
      MONEY,
      EXP,
      ITEM;
   }
}
