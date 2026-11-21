package com.skyblockexp.ezrtp.platform;

import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

public interface PlatformSenderBridge {
   boolean sendToSender(CommandSender var1, Component var2);

   default void close() {
   }
}
