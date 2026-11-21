package net.ess3.api.events;

import com.earth2me.essentials.signs.EssentialsSign;
import java.math.BigDecimal;
import net.ess3.api.IUser;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class SignTransactionEvent extends SignInteractEvent implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   private final ItemStack itemStack;
   private final SignTransactionEvent.TransactionType transactionType;
   private final BigDecimal transactionValue;
   private boolean isCancelled = false;

   public SignTransactionEvent(
      EssentialsSign.ISign sign,
      EssentialsSign essSign,
      IUser user,
      ItemStack itemStack,
      SignTransactionEvent.TransactionType transactionType,
      BigDecimal transactionValue
   ) {
      super(sign, essSign, user);
      this.itemStack = itemStack;
      this.transactionType = transactionType;
      this.transactionValue = transactionValue;
   }

   @Override
   public boolean isCancelled() {
      return this.isCancelled;
   }

   @Override
   public void setCancelled(boolean cancelled) {
      this.isCancelled = cancelled;
   }

   @NotNull
   public ItemStack getItemStack() {
      return this.itemStack.clone();
   }

   @NotNull
   public SignTransactionEvent.TransactionType getTransactionType() {
      return this.transactionType;
   }

   public BigDecimal getTransactionValue() {
      return this.transactionValue;
   }

   @Override
   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   public static enum TransactionType {
      BUY,
      SELL;
   }
}
