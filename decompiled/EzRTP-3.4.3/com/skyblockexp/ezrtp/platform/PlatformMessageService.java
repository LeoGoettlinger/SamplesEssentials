package com.skyblockexp.ezrtp.platform;

import com.skyblockexp.ezrtp.util.MessageUtil;
import java.util.logging.Logger;
import net.kyori.adventure.text.Component;

public interface PlatformMessageService {
   String resolvePlaceholders(Object var1, String var2, Logger var3);

   boolean sendToSender(Object var1, Component var2);

   default void close() {
   }

   default Component resolvePlaceholdersComponent(Object player, Component component, Logger logger) {
      if (component == null) {
         return Component.empty();
      } else {
         try {
            String mm = MessageUtil.serializeToMiniMessage(component);
            String replaced = this.resolvePlaceholders(player, mm, logger);
            return MessageUtil.parseMiniMessage(replaced);
         } catch (Throwable var6) {
            return component;
         }
      }
   }
}
