package org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.messengers;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractMessenger implements Messenger {
   @Override
   public void sendMessage(@NotNull CommandSender recipient, @Nullable String... messages) {
      if (messages != null) {
         for (String message : messages) {
            this.sendMessage(recipient, message);
         }
      }
   }

   @Override
   public void sendMessage(CommandSender[] recipients, @Nullable String message) {
      if (message != null && !message.isBlank()) {
         for (CommandSender recipient : recipients) {
            this.sendMessage(recipient, message);
         }
      }
   }

   @Override
   public void sendMessage(CommandSender[] recipients, @Nullable String... messages) {
      if (messages != null) {
         for (CommandSender recipient : recipients) {
            for (String message : messages) {
               this.sendMessage(recipient, message);
            }
         }
      }
   }

   @Override
   public void broadcastMessage(@NotNull String... messages) {
      if (messages != null) {
         for (String message : messages) {
            this.broadcastMessage(message);
         }
      }
   }

   @Override
   public void sendActionBarMessage(@NotNull Player[] players, @Nullable String message) {
      if (message != null && !message.isBlank()) {
         for (Player player : players) {
            this.sendActionBarMessage(player, message);
         }
      }
   }

   @Override
   public void sendTitle(@NotNull Player player, @Nullable String title) {
      this.sendTitle(player, title, null);
   }

   @Override
   public void sendTitle(@NotNull Player player, @Nullable String title, @Nullable String subtitle) {
      this.sendTitle(player, title, subtitle, 10, 20);
   }

   @Override
   public void sendTitle(@NotNull Player player, @Nullable String title, @Nullable String subtitle, int fadeIn, int fadeOut) {
      this.sendTitle(player, title, subtitle, fadeIn, 70, fadeOut);
   }

   @Override
   public void sendTitle(@NotNull Player[] players, @Nullable String title) {
      this.sendTitle(players, title, null);
   }

   @Override
   public void sendTitle(@NotNull Player[] players, @Nullable String title, @Nullable String subtitle) {
      this.sendTitle(players, title, subtitle, 10, 20);
   }

   @Override
   public void sendTitle(@NotNull Player[] players, @Nullable String title, @Nullable String subtitle, int fadeIn, int fadeOut) {
      this.sendTitle(players, title, subtitle, fadeIn, 70, fadeOut);
   }

   @Override
   public void sendTitle(@NotNull Player[] players, @Nullable String title, @Nullable String subtitle, int fadeIn, int stay, int fadeOut) {
      if (title != null && !title.isBlank() || subtitle != null && !subtitle.isBlank()) {
         for (Player player : players) {
            this.sendTitle(player, title, subtitle, fadeIn, stay, fadeOut);
         }
      }
   }
}
