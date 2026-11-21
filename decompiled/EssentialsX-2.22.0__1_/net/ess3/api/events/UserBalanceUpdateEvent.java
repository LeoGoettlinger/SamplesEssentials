package net.ess3.api.events;

import com.google.common.base.Preconditions;
import java.math.BigDecimal;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class UserBalanceUpdateEvent extends Event {
   private static final HandlerList handlers = new HandlerList();
   private final Player player;
   private final BigDecimal originalBalance;
   private final UserBalanceUpdateEvent.Cause cause;
   private BigDecimal balance;

   @Deprecated
   public UserBalanceUpdateEvent(final Player player, final BigDecimal originalBalance, final BigDecimal balance) {
      this(player, originalBalance, balance, UserBalanceUpdateEvent.Cause.UNKNOWN);
   }

   public UserBalanceUpdateEvent(final Player player, final BigDecimal originalBalance, final BigDecimal balance, final UserBalanceUpdateEvent.Cause cause) {
      super(!Bukkit.getServer().isPrimaryThread());
      this.player = player;
      this.originalBalance = originalBalance;
      this.balance = balance;
      this.cause = cause;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public Player getPlayer() {
      return this.player;
   }

   public BigDecimal getNewBalance() {
      return this.balance;
   }

   public void setNewBalance(final BigDecimal newBalance) {
      Preconditions.checkNotNull(newBalance, "newBalance cannot be null.");
      this.balance = newBalance;
   }

   public BigDecimal getOldBalance() {
      return this.originalBalance;
   }

   public UserBalanceUpdateEvent.Cause getCause() {
      return this.cause;
   }

   public static enum Cause {
      COMMAND_ECO,
      COMMAND_PAY,
      COMMAND_SELL,
      API,
      SPECIAL,
      UNKNOWN;
   }
}
