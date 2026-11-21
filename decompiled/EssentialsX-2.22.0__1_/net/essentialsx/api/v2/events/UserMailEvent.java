package net.essentialsx.api.v2.events;

import net.ess3.api.IUser;
import net.essentialsx.api.v2.services.mail.MailMessage;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class UserMailEvent extends Event implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   private final IUser recipient;
   private final MailMessage message;
   private boolean canceled;

   public UserMailEvent(IUser recipient, MailMessage message) {
      super(!Bukkit.isPrimaryThread());
      this.recipient = recipient;
      this.message = message;
   }

   public IUser getRecipient() {
      return this.recipient;
   }

   public MailMessage getMessage() {
      return this.message;
   }

   public void setCancelled(boolean cancel) {
      this.canceled = cancel;
   }

   public boolean isCancelled() {
      return this.canceled;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }
}
