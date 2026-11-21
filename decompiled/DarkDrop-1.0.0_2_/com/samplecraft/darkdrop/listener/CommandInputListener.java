package com.samplecraft.darkdrop.listener;

import com.samplecraft.darkdrop.DarkDrop;
import com.samplecraft.darkdrop.gui.CommandInputSession;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class CommandInputListener implements Listener {
   private final DarkDrop plugin;

   public CommandInputListener(DarkDrop plugin) {
      this.plugin = plugin;
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onChat(AsyncChatEvent event) {
      Player player = event.getPlayer();
      if (this.plugin.getCommandInputSession().isActive(player)) {
         event.setCancelled(true);
         CommandInputSession.Session session = this.plugin.getCommandInputSession().consume(player);
         if (session != null) {
            String message;
            if (event.message() instanceof TextComponent textComponent) {
               message = textComponent.content();
            } else {
               message = PlainTextComponentSerializer.plainText().serialize(event.message());
            }

            message = message.trim();
            if (!message.equalsIgnoreCase("cancel") && !message.isEmpty()) {
               String cmd = message;
               if (message.startsWith("/")) {
                  cmd = message.substring(1);
               }

               String finalCmd = cmd;
               this.plugin.getServer().getScheduler().runTask(this.plugin, () -> session.gui.applyCommandFromChat(session.index, finalCmd));
            } else {
               player.sendMessage(this.plugin.getConfigManager().component("commands-cancelled"));
               this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
                  if (session.gui != null) {
                     session.gui.open();
                  }
               });
            }
         }
      }
   }
}
