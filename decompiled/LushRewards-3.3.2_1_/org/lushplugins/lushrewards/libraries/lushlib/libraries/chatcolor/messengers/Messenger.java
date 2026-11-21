package org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.messengers;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Messenger {
   void sendMessage(@NotNull CommandSender var1, @Nullable String var2);

   void sendMessage(@NotNull CommandSender var1, @Nullable String... var2);

   void sendMessage(CommandSender[] var1, @Nullable String var2);

   void sendMessage(CommandSender[] var1, @Nullable String... var2);

   void broadcastMessage(@Nullable String var1);

   void broadcastMessage(@NotNull String... var1);

   void sendActionBarMessage(@NotNull Player var1, @Nullable String var2);

   void sendActionBarMessage(@NotNull Player[] var1, @Nullable String var2);

   void sendTitle(@NotNull Player var1, @Nullable String var2);

   void sendTitle(@NotNull Player var1, @Nullable String var2, @Nullable String var3);

   void sendTitle(@NotNull Player var1, @Nullable String var2, @Nullable String var3, int var4, int var5);

   void sendTitle(@NotNull Player var1, @Nullable String var2, @Nullable String var3, int var4, int var5, int var6);

   void sendTitle(@NotNull Player[] var1, @Nullable String var2);

   void sendTitle(@NotNull Player[] var1, @Nullable String var2, @Nullable String var3);

   void sendTitle(@NotNull Player[] var1, @Nullable String var2, @Nullable String var3, int var4, int var5);

   void sendTitle(@NotNull Player[] var1, @Nullable String var2, @Nullable String var3, int var4, int var5, int var6);
}
