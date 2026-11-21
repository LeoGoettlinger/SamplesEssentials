package net.essentialsx.api.v2.events.chat;

import java.util.Set;
import net.essentialsx.api.v2.ChatType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class LocalChatEvent extends ChatEvent implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   private final long radius;

   public LocalChatEvent(final boolean async, final Player player, final String format, final String message, final Set<Player> recipients, final long radius) {
      super(async, ChatType.LOCAL, player, format, message, recipients);
      this.radius = radius;
   }

   public long getRadius() {
      return this.radius;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   public HandlerList getHandlers() {
      return handlers;
   }
}
