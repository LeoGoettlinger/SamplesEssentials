package net.ess3.api.events;

import com.earth2me.essentials.messaging.IMessageRecipient;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PrivateMessageSentEvent extends Event {
   private static final HandlerList handlers = new HandlerList();
   private final IMessageRecipient sender;
   private final IMessageRecipient recipient;
   private final String message;
   private final IMessageRecipient.MessageResponse response;

   public PrivateMessageSentEvent(
      final IMessageRecipient sender, final IMessageRecipient recipient, final String message, final IMessageRecipient.MessageResponse response
   ) {
      this.sender = sender;
      this.recipient = recipient;
      this.message = message;
      this.response = response;
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

   public IMessageRecipient.MessageResponse getResponse() {
      return this.response;
   }

   public HandlerList getHandlers() {
      return handlers;
   }
}
