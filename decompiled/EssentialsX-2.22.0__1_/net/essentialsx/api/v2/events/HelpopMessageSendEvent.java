package net.essentialsx.api.v2.events;

import com.earth2me.essentials.messaging.IMessageRecipient;
import java.util.List;
import net.ess3.api.IUser;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class HelpopMessageSendEvent extends Event {
   private static final HandlerList handlers = new HandlerList();
   private final IMessageRecipient sender;
   private final List<IUser> recipients;
   private final String message;

   public HelpopMessageSendEvent(final IMessageRecipient sender, final List<IUser> recipients, final String message) {
      this.sender = sender;
      this.recipients = recipients;
      this.message = message;
   }

   public IMessageRecipient getSender() {
      return this.sender;
   }

   public List<IUser> getRecipients() {
      return this.recipients;
   }

   public String getMessage() {
      return this.message;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }
}
