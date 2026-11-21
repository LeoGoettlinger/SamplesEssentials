package me.chancesd.pvpmanager.player;

import me.chancesd.pvpmanager.sdutils.utils.Log;
import me.chancesd.pvpmanager.sdutils.utils.Utils;
import me.chancesd.pvpmanager.setting.Conf;
import me.chancesd.pvpmanager.setting.Lang;
import me.chancesd.pvpmanager.utils.CombatUtils;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public abstract class EcoPlayer extends BasePlayer {
   private final Economy economy;

   protected EcoPlayer(@NotNull Player player, Economy economy) {
      super(player);
      this.economy = economy;
   }

   private boolean withdrawMoney(double amount) {
      EconomyResponse response = this.economy.withdrawPlayer(this.getPlayer(), amount);
      Log.debug(
         "Withdraw from " + this.getName() + " - Response: " + response.type + " " + response.amount + " " + response.balance + " " + response.errorMessage
      );
      return response.transactionSuccess() && response.amount != 0.0;
   }

   private boolean depositMoney(double amount) {
      EconomyResponse response = this.economy.depositPlayer(this.getPlayer(), amount);
      Log.debug("Deposit to " + this.getName() + " - Response: " + response.type + " " + response.amount + " " + response.balance + " " + response.errorMessage);
      return response.transactionSuccess() && response.amount != 0.0;
   }

   public double getBalance() {
      return this.economy.getBalance(this.getPlayer());
   }

   public final void applyFine() {
      double penalty = this.getMoneyPercentage(Conf.FINE_AMOUNT.asDouble());
      this.withdrawMoney(penalty);
   }

   public final void applyPenalty() {
      double penalty = this.getMoneyPercentage(Conf.MONEY_PENALTY.asDouble());
      this.withdrawMoney(penalty);
      this.message(Lang.MONEY_PENALTY, new Object[]{CombatUtils.formatTo2Digits(penalty)});
   }

   public final boolean applyPvPDisabledFee() {
      this.message(Lang.PVP_DISABLED_FEE, new Object[]{CombatUtils.formatTo2Digits(Conf.PVP_DISABLED_FEE.asInt())});
      return this.withdrawMoney(Conf.PVP_DISABLED_FEE.asInt());
   }

   public final void giveReward(EcoPlayer victim) {
      double moneyWon = this.getMoneyPercentage(Conf.MONEY_REWARD.asDouble());
      if (Conf.MONEY_STEAL.asBool()) {
         moneyWon = victim.getMoneyPercentage(Conf.MONEY_REWARD.asDouble());
         if (!victim.withdrawMoney(moneyWon)) {
            return;
         }

         victim.message(Lang.MONEY_STEAL, new Object[]{this.getPlayer().getName(), CombatUtils.formatTo2Digits(moneyWon)});
      }

      if (this.depositMoney(moneyWon)) {
         this.message(Lang.MONEY_REWARD.msg(victim.getPlayer().getName(), CombatUtils.formatTo2Digits(moneyWon)));
      }
   }

   public final int giveExp(EcoPlayer victim) {
      int expWon = 0;
      int exp = victim.getPlayer().getTotalExperience();
      if (Conf.EXP_STEAL.asDouble() <= 1.0) {
         expWon = (int)(Conf.EXP_STEAL.asDouble() * exp);
      } else {
         expWon = Math.min((int)Conf.EXP_STEAL.asDouble(), exp);
      }

      this.setExp(this.getPlayer().getTotalExperience() + expWon);
      this.message(Lang.EXP_WON, new Object[]{victim.getPlayer().getName(), expWon});
      return expWon;
   }

   public final void setExp(int exp) {
      this.getPlayer().setExp(0.0F);
      this.getPlayer().setLevel(0);
      this.getPlayer().giveExp(exp);
   }

   public double getMoneyPercentage(double percentage) {
      if (percentage > 1.0) {
         return percentage;
      } else {
         double balance = this.getBalance();
         double amount = Utils.roundTo2Decimal(percentage * balance);
         return Math.min(amount, balance);
      }
   }

   @Override
   public boolean equals(Object obj) {
      return super.equals(obj);
   }

   @Override
   public int hashCode() {
      return super.hashCode();
   }
}
