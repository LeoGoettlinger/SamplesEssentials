package com.viaversion.viaversion.api.command;

import com.viaversion.viaversion.util.ChatColorUtil;
import java.util.Collections;
import java.util.List;

public interface ViaSubCommand {
   String name();

   String description();

   default String usage() {
      return this.name();
   }

   default String permission() {
      return "viaversion.admin." + this.name();
   }

   boolean execute(ViaCommandSender var1, String[] var2);

   default List<String> onTabComplete(ViaCommandSender sender, String[] args) {
      return Collections.emptyList();
   }

   static String color(String s) {
      return ChatColorUtil.translateAlternateColorCodes(s);
   }

   default void sendMessage(ViaCommandSender sender, String message, Object... args) {
      sender.sendMessage(color(args == null ? message : String.format(message, args)));
   }
}
