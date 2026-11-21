package com.skyblockexp.ezrtp.util;

import net.kyori.adventure.text.Component;

public final class PluginMessageHelperBukkit {
   private PluginMessageHelperBukkit() {
   }

   public static void sendToSender(Object sender, Component component) {
      MessageUtil.send(sender, component);
   }
}
