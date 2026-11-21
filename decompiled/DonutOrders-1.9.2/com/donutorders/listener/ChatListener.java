package com.donutorders.listener;

import com.donutorders.manager.ChatInputHandler;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {
   private static final boolean PAPER_ASYNC_CHAT = hasPaperAsyncChat();
   private final ChatInputHandler chatInputHandler;

   public ChatListener(ChatInputHandler chatInputHandler) {
      this.chatInputHandler = chatInputHandler;
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = false
   )
   public void onPaperChat(AsyncChatEvent event) {
      Player player = event.getPlayer();
      String message = PlainTextComponentSerializer.plainText().serialize(event.message());
      if (this.chatInputHandler.handleInput(player, message)) {
         event.setCancelled(true);
         event.viewers().clear();
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = false
   )
   public void onChat(AsyncPlayerChatEvent event) {
      if (!PAPER_ASYNC_CHAT) {
         Player player = event.getPlayer();
         String message = event.getMessage();
         if (this.chatInputHandler.handleInput(player, message)) {
            event.setCancelled(true);
         }
      }
   }

   private static boolean hasPaperAsyncChat() {
      try {
         Class.forName("io.papermc.paper.event.player.AsyncChatEvent");
         return true;
      } catch (ClassNotFoundException var1) {
         return false;
      }
   }
}
