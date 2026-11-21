package net.coreprotect.spigot;

import org.bukkit.command.CommandSender;

public interface SpigotInterface {
   void addHoverComponent(Object var1, String[] var2);

   void setHoverEvent(Object var1, String var2);

   void sendComponent(CommandSender var1, String var2, String var3);
}
