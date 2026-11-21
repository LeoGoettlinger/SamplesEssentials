package net.essentialsx.api.v2.events;

import com.earth2me.essentials.CommandSource;
import com.google.common.base.Preconditions;
import java.math.BigDecimal;
import net.ess3.api.IUser;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class PreTransactionEvent extends TransactionEvent implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   private boolean cancelled;

   public PreTransactionEvent(final CommandSource requester, final IUser target, final BigDecimal amount) {
      super(!Bukkit.isPrimaryThread(), requester, target, amount);
   }

   public void setAmount(final BigDecimal decimal) {
      Preconditions.checkNotNull(decimal, "decimal cannot be null");
      Preconditions.checkArgument(decimal.compareTo(BigDecimal.ZERO) >= 0, "decimal cannot be negative");
      this.amount = decimal;
   }

   public boolean isCancelled() {
      return this.cancelled;
   }

   public void setCancelled(boolean cancelled) {
      this.cancelled = cancelled;
   }

   @Override
   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }
}
