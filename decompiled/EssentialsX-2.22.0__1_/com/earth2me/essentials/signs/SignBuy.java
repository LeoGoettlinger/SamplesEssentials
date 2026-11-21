package com.earth2me.essentials.signs;

import com.earth2me.essentials.ChargeException;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import java.math.BigDecimal;
import net.ess3.api.IEssentials;
import net.ess3.api.MaxMoneyException;
import net.ess3.api.events.SignTransactionEvent;
import org.bukkit.inventory.ItemStack;

public class SignBuy extends EssentialsSign {
   public SignBuy() {
      super("Buy");
   }

   @Override
   protected boolean onSignCreate(final EssentialsSign.ISign sign, final User player, final String username, final IEssentials ess) throws SignException {
      this.validateTrade(sign, 1, 2, player, ess);
      this.validateTrade(sign, 3, ess);
      return true;
   }

   @Override
   protected boolean onSignInteract(final EssentialsSign.ISign sign, final User player, final String username, final IEssentials ess) throws SignException, ChargeException, MaxMoneyException {
      Trade items = this.getTrade(sign, 1, 2, player, ess);
      Trade charge = this.getTrade(sign, 3, ess);
      if (ess.getSettings().isAllowBulkBuySell() && player.getBase().isSneaking()) {
         ItemStack heldItem = player.getItemInHand();
         if (items.getItemStack().isSimilar(heldItem)) {
            int initialItemAmount = items.getItemStack().getAmount();
            int newItemAmount = heldItem.getAmount();
            ItemStack item = items.getItemStack();
            item.setAmount(newItemAmount);
            items = new Trade(item, ess);
            BigDecimal chargeAmount = charge.getMoney();
            BigDecimal pricePerSingleItem = chargeAmount.divide(new BigDecimal(initialItemAmount));
            pricePerSingleItem = pricePerSingleItem.multiply(new BigDecimal(newItemAmount));
            charge = new Trade(pricePerSingleItem, ess);
         }
      }

      charge.isAffordableFor(player);
      SignTransactionEvent signTransactionEvent = new SignTransactionEvent(
         sign, this, player, items.getItemStack(), SignTransactionEvent.TransactionType.BUY, charge.getMoney()
      );
      ess.getServer().getPluginManager().callEvent(signTransactionEvent);
      if (signTransactionEvent.isCancelled()) {
         return true;
      } else if (!items.pay(player)) {
         throw new ChargeException("inventoryFull");
      } else {
         charge.charge(player);
         Trade.log("Sign", "Buy", "Interact", username, charge, username, items, sign.getBlock().getLocation(), player.getMoney(), ess);
         return true;
      }
   }
}
