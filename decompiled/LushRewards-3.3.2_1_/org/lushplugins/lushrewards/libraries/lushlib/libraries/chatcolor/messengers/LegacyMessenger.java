package org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.messengers;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.ChatColorHandler;

public class LegacyMessenger extends AbstractMessenger {
   @Override
   public void sendMessage(@NotNull CommandSender recipient, @Nullable String message) {
      if (message != null && !message.isBlank()) {
         recipient.sendMessage(ChatColorHandler.translate(message, recipient instanceof Player player ? player : null));
      }
   }

   @Override
   public void broadcastMessage(@Nullable String message) {
      if (message != null && !message.isBlank()) {
         Bukkit.broadcastMessage(ChatColorHandler.translate(message));
      }
   }

   @Override
   public void sendActionBarMessage(@NotNull Player player, @Nullable String message) {
      if (message != null && !message.isBlank()) {
         player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColorHandler.translate(message, player)));
      }
   }

   @Override
   public void sendTitle(@NotNull Player player, @Nullable String title, @Nullable String subtitle, int fadeIn, int stay, int fadeOut) {
      if (title != null && !title.isBlank() || subtitle != null && !subtitle.isBlank()) {
         player.sendTitle(ChatColorHandler.translate(title, player), ChatColorHandler.translate(subtitle, player), fadeIn, stay, fadeOut);
      }
   }
}
