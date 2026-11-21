package net.essentialsx.api.v2.events;

import java.util.Collection;
import net.ess3.api.IUser;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class UserActionEvent extends Event {
   private static final HandlerList handlers = new HandlerList();
   private final IUser user;
   private final String message;
   private final Collection<Player> recipients;

   public UserActionEvent(IUser user, String message, Collection<Player> recipients) {
      this.user = user;
      this.message = message;
      this.recipients = recipients;
   }

   public IUser getUser() {
      return this.user;
   }

   public String getMessage() {
      return this.message;
   }

   public Collection<Player> getRecipients() {
      return this.recipients;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }
}
