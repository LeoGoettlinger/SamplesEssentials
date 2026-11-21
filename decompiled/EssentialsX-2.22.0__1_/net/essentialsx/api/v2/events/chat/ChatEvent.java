package net.essentialsx.api.v2.events.chat;

import java.util.IllegalFormatException;
import java.util.Set;
import net.essentialsx.api.v2.ChatType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public abstract class ChatEvent extends Event implements Cancellable {
   private final ChatType chatType;
   private final Player player;
   private final Set<Player> recipients;
   private String message;
   private String format;
   private boolean cancelled = false;

   public ChatEvent(final boolean async, final ChatType chatType, final Player player, final String format, final String message, final Set<Player> recipients) {
      super(async);
      this.chatType = chatType;
      this.player = player;
      this.format = format;
      this.message = message;
      this.recipients = recipients;
   }

   public String getMessage() {
      return this.message;
   }

   public void setMessage(final String message) {
      this.message = message;
   }

   public String getFormat() {
      return this.format;
   }

   public void setFormat(final String format) throws IllegalFormatException, NullPointerException {
      try {
         String.format(format, this.player, this.message);
      } catch (RuntimeException var3) {
         var3.fillInStackTrace();
         throw var3;
      }

      this.format = format;
   }

   public Set<Player> getRecipients() {
      return this.recipients;
   }

   public final Player getPlayer() {
      return this.player;
   }

   public ChatType getChatType() {
      return this.chatType;
   }

   public boolean isCancelled() {
      return this.cancelled;
   }

   public void setCancelled(final boolean cancel) {
      this.cancelled = cancel;
   }
}
