package com.earth2me.essentials.signs;

import com.earth2me.essentials.ChargeException;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import com.earth2me.essentials.craftbukkit.Inventories;
import com.earth2me.essentials.utils.MaterialUtil;
import com.earth2me.essentials.utils.NumberUtil;
import java.math.BigDecimal;
import java.util.Map;
import net.ess3.api.IEssentials;
import net.ess3.api.MaxMoneyException;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class SignTrade extends EssentialsSign {
   private static final int MAX_STOCK_LINE_LENGTH = 15;

   public SignTrade() {
      super("Trade");
   }

   @Override
   protected boolean onSignCreate(final EssentialsSign.ISign sign, final User player, final String username, final IEssentials ess) throws SignException, ChargeException {
      this.validateTrade(sign, 1, false, ess);
      this.validateTrade(sign, 2, true, ess);
      Trade trade = this.getTrade(sign, 2, SignTrade.AmountType.ROUNDED, true, true, ess);
      Trade charge = this.getTrade(sign, 1, SignTrade.AmountType.ROUNDED, false, true, ess);
      if (trade.getType() != charge.getType() || trade.getType() == Trade.TradeType.ITEM && !trade.getItemStack().isSimilar(charge.getItemStack())) {
         trade.isAffordableFor(player);
         this.setOwner(ess, player, sign, 3, "§8");
         trade.charge(player);
         Trade.log("Sign", "Trade", "Create", username, trade, username, null, sign.getBlock().getLocation(), player.getMoney(), ess);
         return true;
      } else {
         throw new SignException("tradeSignSameType");
      }
   }

   @Override
   protected boolean onSignInteract(final EssentialsSign.ISign sign, final User player, final String username, final IEssentials ess) throws SignException, ChargeException, MaxMoneyException {
      if (this.isOwner(ess, player, sign, 3, "§8")) {
         Trade store = this.rechargeSign(sign, ess, player);

         try {
            Trade stored = this.getTrade(sign, 1, SignTrade.AmountType.TOTAL, true, true, ess);
            this.subtractAmount(sign, 1, stored, ess, false);
            Map<Integer, ItemStack> withdraw = stored.pay(player, Trade.OverflowType.RETURN);
            if (withdraw == null) {
               Trade.log("Sign", "Trade", "Withdraw", username, store, username, null, sign.getBlock().getLocation(), player.getMoney(), ess);
            } else {
               this.setAmount(sign, 1, BigDecimal.valueOf((long)withdraw.get(0).getAmount()), ess, false);
               Trade.log(
                  "Sign",
                  "Trade",
                  "Withdraw",
                  username,
                  stored,
                  username,
                  new Trade(withdraw.get(0), ess),
                  sign.getBlock().getLocation(),
                  player.getMoney(),
                  ess
               );
            }
         } catch (SignException var8) {
            if (store == null) {
               throw new SignException(var8, "tradeSignEmptyOwner");
            }
         }

         Trade.log("Sign", "Trade", "Deposit", username, store, username, null, sign.getBlock().getLocation(), player.getMoney(), ess);
      } else {
         Trade charge = this.getTrade(sign, 1, SignTrade.AmountType.COST, false, true, ess);
         Trade trade = this.getTrade(sign, 2, SignTrade.AmountType.COST, true, true, ess);
         charge.isAffordableFor(player);
         this.addAmount(sign, 1, charge, ess, true);
         this.subtractAmount(sign, 2, trade, ess, true);
         this.addAmount(sign, 1, charge, ess, false);
         this.subtractAmount(sign, 2, trade, ess, false);
         if (!trade.pay(player)) {
            this.subtractAmount(sign, 1, charge, ess, false);
            this.addAmount(sign, 2, trade, ess, false);
            throw new ChargeException("inventoryFull");
         }

         charge.charge(player);
         Trade.log("Sign", "Trade", "Interact", sign.getLine(3).substring(2), charge, username, trade, sign.getBlock().getLocation(), player.getMoney(), ess);
      }

      sign.updateSign();
      return true;
   }

   private Trade rechargeSign(final EssentialsSign.ISign sign, final IEssentials ess, final User player) throws SignException, ChargeException {
      Trade trade = this.getTrade(sign, 2, SignTrade.AmountType.COST, false, true, ess);
      ItemStack stack = Inventories.getItemInHand(player.getBase());
      if (trade.getItemStack() != null
         && stack != null
         && !MaterialUtil.isAir(stack.getType())
         && trade.getItemStack().getType() == stack.getType()
         && MaterialUtil.getDamage(trade.getItemStack()) == MaterialUtil.getDamage(stack)
         && trade.getItemStack().getEnchantments().equals(stack.getEnchantments())) {
         if (MaterialUtil.isPotion(trade.getItemStack().getType()) && !trade.getItemStack().isSimilar(stack)) {
            return null;
         }

         int amount = trade.getItemStack().getAmount();
         if (Inventories.containsAtLeast(player.getBase(), trade.getItemStack(), amount)) {
            stack = stack.clone();
            stack.setAmount(amount);
            Trade store = new Trade(stack, ess);
            this.addAmount(sign, 2, store, ess, false);
            store.charge(player);
            return store;
         }
      }

      return null;
   }

   @Override
   protected boolean onSignBreak(final EssentialsSign.ISign sign, final User player, final String username, final IEssentials ess) throws SignException, MaxMoneyException {
      String signOwner = sign.getLine(3);
      boolean isOwner = this.isOwner(ess, player, sign, 3, "§8");
      boolean canBreak = isOwner || player.isAuthorized("essentials.signs.trade.override");
      boolean canCollect = isOwner || player.isAuthorized("essentials.signs.trade.override.collect");
      if (canBreak) {
         try {
            Trade stored1 = this.getTrade(sign, 1, SignTrade.AmountType.TOTAL, false, true, ess);
            Trade stored2 = this.getTrade(sign, 2, SignTrade.AmountType.TOTAL, false, true, ess);
            if (!canCollect) {
               Trade.log("Sign", "Trade", "Destroy", signOwner.substring(2), stored2, username, stored1, sign.getBlock().getLocation(), player.getMoney(), ess);
               return true;
            } else {
               Map<Integer, ItemStack> withdraw1 = stored1.pay(player, Trade.OverflowType.DROP);
               Map<Integer, ItemStack> withdraw2 = stored2.pay(player, Trade.OverflowType.DROP);
               if (withdraw1 == null && withdraw2 == null) {
                  Trade.log("Sign", "Trade", "Break", signOwner.substring(2), stored2, username, stored1, sign.getBlock().getLocation(), player.getMoney(), ess);
                  return true;
               } else {
                  this.setAmount(sign, 1, BigDecimal.valueOf(withdraw1 == null ? 0L : withdraw1.get(0).getAmount()), ess, false);
                  Trade.log(
                     "Sign",
                     "Trade",
                     "Withdraw",
                     signOwner.substring(2),
                     stored1,
                     username,
                     withdraw1 == null ? null : new Trade(withdraw1.get(0), ess),
                     sign.getBlock().getLocation(),
                     player.getMoney(),
                     ess
                  );
                  this.setAmount(sign, 2, BigDecimal.valueOf(withdraw2 == null ? 0L : withdraw2.get(0).getAmount()), ess, false);
                  Trade.log(
                     "Sign",
                     "Trade",
                     "Withdraw",
                     signOwner.substring(2),
                     stored2,
                     username,
                     withdraw2 == null ? null : new Trade(withdraw2.get(0), ess),
                     sign.getBlock().getLocation(),
                     player.getMoney(),
                     ess
                  );
                  sign.updateSign();
                  return false;
               }
            }
         } catch (SignException var13) {
            if (player.isAuthorized("essentials.signs.trade.override")) {
               return true;
            } else {
               throw var13;
            }
         }
      } else {
         return false;
      }
   }

   private void validateSignLength(final String newLine) throws SignException {
      if (newLine.length() > 15) {
         throw new SignException("tradeSignFull");
      }
   }

   protected final void validateTrade(final EssentialsSign.ISign sign, final int index, final boolean amountNeeded, final IEssentials ess) throws SignException {
      String line = sign.getLine(index).trim();
      if (line.isEmpty()) {
         throw new SignException("emptySignLine", index + 1);
      } else {
         String[] split = line.split("[ :]+");
         if (split.length == 1 && !amountNeeded) {
            BigDecimal money = this.getMoney(split[0], ess);
            if (money != null) {
               String newLine = NumberUtil.shortCurrency(money, ess) + ":0";
               this.validateSignLength(newLine);
               sign.setLine(index, newLine);
               return;
            }
         }

         if (split.length == 2 && amountNeeded) {
            BigDecimal money = this.getMoney(split[0], ess);
            BigDecimal amount = this.getBigDecimalPositive(split[1], ess);
            if (money != null && amount != null) {
               amount = amount.subtract(amount.remainder(money));
               if (amount.compareTo(MINTRANSACTION) >= 0 && money.compareTo(MINTRANSACTION) >= 0) {
                  String newLine = NumberUtil.shortCurrency(money, ess) + ":" + NumberUtil.formatAsCurrency(amount);
                  this.validateSignLength(newLine);
                  sign.setLine(index, newLine);
                  return;
               }

               throw new SignException("moreThanZero");
            }
         }

         if (split.length == 2 && !amountNeeded) {
            int amount = this.getIntegerPositive(split[0]);
            if (amount < 1) {
               throw new SignException("moreThanZero");
            } else if (!split[1].equalsIgnoreCase("exp")
               && !split[1].equalsIgnoreCase("xp")
               && this.getItemStack(split[1], amount, ess).getType() == Material.AIR) {
               throw new SignException("moreThanZero");
            } else {
               String newline = amount + " " + split[1] + ":0";
               this.validateSignLength(newline);
               sign.setLine(index, newline);
            }
         } else if (split.length == 3 && amountNeeded) {
            int stackamount = this.getIntegerPositive(split[0]);
            int amount = this.getIntegerPositive(split[2]);
            amount -= amount % stackamount;
            if (amount >= 1 && stackamount >= 1) {
               if (!split[1].equalsIgnoreCase("exp")
                  && !split[1].equalsIgnoreCase("xp")
                  && this.getItemStack(split[1], stackamount, ess).getType() == Material.AIR) {
                  throw new SignException("moreThanZero");
               } else {
                  String newline = stackamount + " " + split[1] + ":" + amount;
                  this.validateSignLength(newline);
                  sign.setLine(index, newline);
               }
            } else {
               throw new SignException("moreThanZero");
            }
         } else {
            throw new SignException("invalidSignLine", index + 1);
         }
      }
   }

   protected final Trade getTrade(
      final EssentialsSign.ISign sign, final int index, final SignTrade.AmountType amountType, final boolean notEmpty, final IEssentials ess
   ) throws SignException {
      return this.getTrade(sign, index, amountType, notEmpty, false, ess);
   }

   protected final Trade getTrade(
      final EssentialsSign.ISign sign,
      final int index,
      final SignTrade.AmountType amountType,
      final boolean notEmpty,
      final boolean allowId,
      final IEssentials ess
   ) throws SignException {
      String line = sign.getLine(index).trim();
      if (line.isEmpty()) {
         throw new SignException("emptySignLine", index + 1);
      } else {
         String[] split = line.split("[ :]+");
         if (split.length == 2) {
            try {
               BigDecimal money = this.getMoney(split[0], ess);
               BigDecimal amount = notEmpty ? this.getBigDecimalPositive(split[1], ess) : this.getBigDecimal(split[1], ess);
               if (money != null && amount != null) {
                  return new Trade(amountType == SignTrade.AmountType.COST ? money : amount, ess);
               }
            } catch (SignException var12) {
               throw new SignException(var12, "tradeSignEmpty");
            }
         }

         if (split.length != 3) {
            throw new SignException("invalidSignLine", index + 1);
         } else {
            int stackAmount = this.getIntegerPositive(split[0]);
            if (!split[1].equalsIgnoreCase("exp") && !split[1].equalsIgnoreCase("xp")) {
               ItemStack item = this.getItemStack(split[1], stackAmount, allowId, ess);
               int amount = this.getInteger(split[2]);
               if (amountType == SignTrade.AmountType.ROUNDED) {
                  amount -= amount % stackAmount;
               }

               if (!notEmpty || amount >= 1 && stackAmount >= 1 && item.getType() != Material.AIR && amount >= stackAmount) {
                  item.setAmount(amountType == SignTrade.AmountType.COST ? stackAmount : amount);
                  return new Trade(item, ess);
               } else {
                  throw new SignException("tradeSignEmpty");
               }
            } else {
               int amountx = this.getInteger(split[2]);
               if (amountType == SignTrade.AmountType.ROUNDED) {
                  amountx -= amountx % stackAmount;
               }

               if (!notEmpty || amountx >= 1 && stackAmount >= 1) {
                  return new Trade(amountType == SignTrade.AmountType.COST ? stackAmount : amountx, ess);
               } else {
                  throw new SignException("tradeSignEmpty");
               }
            }
         }
      }
   }

   protected final void subtractAmount(final EssentialsSign.ISign sign, final int index, final Trade trade, final IEssentials ess, final boolean validationRun) throws SignException {
      BigDecimal money = trade.getMoney();
      if (money != null) {
         this.changeAmount(sign, index, money.negate(), ess, validationRun);
      }

      ItemStack item = trade.getItemStack();
      if (item != null) {
         this.changeAmount(sign, index, BigDecimal.valueOf((long)(-item.getAmount())), ess, validationRun);
      }

      Integer exp = trade.getExperience();
      if (exp != null) {
         this.changeAmount(sign, index, BigDecimal.valueOf((long)(-exp)), ess, validationRun);
      }
   }

   protected final void addAmount(final EssentialsSign.ISign sign, final int index, final Trade trade, final IEssentials ess, final boolean validationRun) throws SignException {
      BigDecimal money = trade.getMoney();
      if (money != null) {
         this.changeAmount(sign, index, money, ess, validationRun);
      }

      ItemStack item = trade.getItemStack();
      if (item != null) {
         this.changeAmount(sign, index, BigDecimal.valueOf((long)item.getAmount()), ess, validationRun);
      }

      Integer exp = trade.getExperience();
      if (exp != null) {
         this.changeAmount(sign, index, BigDecimal.valueOf((long)exp.intValue()), ess, validationRun);
      }
   }

   private void changeAmount(final EssentialsSign.ISign sign, final int index, final BigDecimal value, final IEssentials ess, final boolean validationRun) throws SignException {
      String line = sign.getLine(index).trim();
      if (line.isEmpty()) {
         throw new SignException("emptySignLine", index + 1);
      } else {
         String[] split = line.split("[ :]+");
         if (split.length == 2) {
            BigDecimal amount = this.getBigDecimal(split[1], ess).add(value);
            this.setAmount(sign, index, amount, ess, validationRun);
         } else if (split.length == 3) {
            BigDecimal amount = this.getBigDecimal(split[2], ess).add(value);
            this.setAmount(sign, index, amount, ess, validationRun);
         } else {
            throw new SignException("invalidSignLine", index + 1);
         }
      }
   }

   private void setAmount(final EssentialsSign.ISign sign, final int index, final BigDecimal value, final IEssentials ess, final boolean validationRun) throws SignException {
      String line = sign.getLine(index).trim();
      if (line.isEmpty()) {
         throw new SignException("emptySignLine", index + 1);
      } else {
         String[] split = line.split("[ :]+");
         if (split.length == 2) {
            BigDecimal money = this.getMoney(split[0], ess);
            BigDecimal amount = this.getBigDecimal(split[1], ess);
            if (money != null && amount != null) {
               String newline = NumberUtil.shortCurrency(money, ess) + ":" + NumberUtil.formatAsCurrency(value);
               this.validateSignLength(newline);
               if (!validationRun) {
                  sign.setLine(index, newline);
               }

               return;
            }
         }

         if (split.length != 3) {
            throw new SignException("invalidSignLine", index + 1);
         } else {
            int stackAmount = this.getIntegerPositive(split[0]);
            if (!split[1].equalsIgnoreCase("exp") && !split[1].equalsIgnoreCase("xp")) {
               this.getItemStack(split[1], stackAmount, ess);
               String newline = stackAmount + " " + split[1] + ":" + value.intValueExact();
               this.validateSignLength(newline);
               if (!validationRun) {
                  sign.setLine(index, newline);
               }
            } else {
               String newline = stackAmount + " " + split[1] + ":" + value.intValueExact();
               this.validateSignLength(newline);
               if (!validationRun) {
                  sign.setLine(index, newline);
               }
            }
         }
      }
   }

   public static enum AmountType {
      TOTAL,
      ROUNDED,
      COST;
   }
}
