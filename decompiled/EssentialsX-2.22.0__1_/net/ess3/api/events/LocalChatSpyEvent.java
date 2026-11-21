package net.ess3.api.events;

import com.earth2me.essentials.I18n;
import java.util.Set;
import net.essentialsx.api.v2.ChatType;
import net.essentialsx.api.v2.events.chat.ChatEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public class LocalChatSpyEvent extends ChatEvent {
   private static final HandlerList handlers = new HandlerList();

   public LocalChatSpyEvent(final boolean async, final Player player, final String format, final String message, final Set<Player> recipients) {
      super(async, ChatType.SPY, player, I18n.tlLiteral("chatTypeLocal").concat(I18n.tlLiteral("chatTypeSpy")).concat(format), message, recipients);
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   public HandlerList getHandlers() {
      return handlers;
   }
}
