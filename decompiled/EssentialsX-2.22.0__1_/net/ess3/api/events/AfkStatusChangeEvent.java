package net.ess3.api.events;

import net.ess3.api.IUser;
import org.bukkit.event.HandlerList;

public class AfkStatusChangeEvent extends StatusChangeEvent {
   private static final HandlerList handlers = new HandlerList();
   private final AfkStatusChangeEvent.Cause cause;

   @Deprecated
   public AfkStatusChangeEvent(final IUser affected, final boolean value) {
      this(affected, value, AfkStatusChangeEvent.Cause.UNKNOWN);
   }

   public AfkStatusChangeEvent(final IUser affected, final boolean value, final AfkStatusChangeEvent.Cause cause) {
      super(affected, affected, value);
      this.cause = cause;
   }

   public AfkStatusChangeEvent.Cause getCause() {
      return this.cause;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   public static enum Cause {
      ACTIVITY,
      MOVE,
      INTERACT,
      COMMAND,
      JOIN,
      CHAT,
      QUIT,
      UNKNOWN;
   }
}
