package net.ess3.api.events;

import java.util.Optional;
import net.ess3.api.IUser;
import org.bukkit.event.HandlerList;

public class MuteStatusChangeEvent extends StatusChangeEvent {
   private static final HandlerList handlers = new HandlerList();
   private final Long timestamp;
   private final String reason;

   public MuteStatusChangeEvent(final IUser affected, final IUser controller, final boolean value, final Long timestamp, final String reason) {
      super(affected, controller, value);
      this.timestamp = timestamp;
      this.reason = reason == null ? null : (reason.isEmpty() ? null : reason);
   }

   public Optional<Long> getTimestamp() {
      return Optional.ofNullable(this.timestamp <= 0L ? null : this.timestamp);
   }

   public String getReason() {
      return this.reason;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }
}
