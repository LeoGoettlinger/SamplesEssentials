package net.ess3.api.events;

import com.earth2me.essentials.messaging.IMessageRecipient;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PrivateMessagePreSendEvent extends Event implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   private final IMessageRecipient sender;
   private final IMessageRecipient recipient;
   private boolean cancelled;
   private String message;

   public PrivateMessagePreSendEvent(final IMessageRecipient sender, final IMessageRecipient recipient, final String message) {
      this.sender = sender;
      this.recipient = recipient;
      this.message = message;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   public IMessageRecipient getSender() {
      return this.sender;
   }

   public IMessageRecipient getRecipient() {
      return this.recipient;
   }

   public String getMessage() {
      return this.message;
   }

   public void setMessage(final String message) {
      this.message = message;
   }

   public boolean isCancelled() {
      return this.cancelled;
   }

   public void setCancelled(final boolean b) {
      this.cancelled = b;
   }

   public HandlerList getHandlers() {
      return handlers;
   }
}
