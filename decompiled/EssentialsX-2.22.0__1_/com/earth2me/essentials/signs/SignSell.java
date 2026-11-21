package com.earth2me.essentials.signs;

import com.earth2me.essentials.ChargeException;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import java.math.BigDecimal;
import net.ess3.api.IEssentials;
import net.ess3.api.MaxMoneyException;
import net.ess3.api.events.SignTransactionEvent;
import org.bukkit.inventory.ItemStack;

public class SignSell extends EssentialsSign {
   public SignSell() {
      super("Sell");
   }

   @Override
   protected boolean onSignCreate(final EssentialsSign.ISign sign, final User player, final String username, final IEssentials ess) throws SignException {
      this.validateTrade(sign, 1, 2, player, ess);
      this.validateTrade(sign, 3, ess);
      return true;
   }

   @Override
   protected boolean onSignInteract(final EssentialsSign.ISign sign, final User player, final String username, final IEssentials ess) throws SignException, ChargeException, MaxMoneyException {
      Trade charge = this.getTrade(sign, 1, 2, player, ess);
      Trade money = this.getTrade(sign, 3, ess);
      if (ess.getSettings().isAllowBulkBuySell() && player.getBase().isSneaking()) {
         ItemStack heldItem = player.getItemInHand();
         if (charge.getItemStack().isSimilar(heldItem)) {
            int initialItemAmount = charge.getItemStack().getAmount();
            int newItemAmount = heldItem.getAmount();
            ItemStack item = charge.getItemStack();
            item.setAmount(newItemAmount);
            charge = new Trade(item, ess);
            BigDecimal chargeAmount = money.getMoney();
            BigDecimal pricePerSingleItem = chargeAmount.divide(new BigDecimal(initialItemAmount));
            pricePerSingleItem = pricePerSingleItem.multiply(new BigDecimal(newItemAmount));
            pricePerSingleItem = pricePerSingleItem.multiply(ess.getSettings().getMultiplier(player));
            money = new Trade(pricePerSingleItem, ess);
         }
      }

      charge.isAffordableFor(player);
      SignTransactionEvent signTransactionEvent = new SignTransactionEvent(
         sign, this, player, charge.getItemStack(), SignTransactionEvent.TransactionType.SELL, money.getMoney()
      );
      ess.getServer().getPluginManager().callEvent(signTransactionEvent);
      if (signTransactionEvent.isCancelled()) {
         return false;
      } else {
         money.pay(player, Trade.OverflowType.DROP);
         charge.charge(player);
         Trade.log("Sign", "Sell", "Interact", username, charge, username, money, sign.getBlock().getLocation(), player.getMoney(), ess);
         return true;
      }
   }
}
