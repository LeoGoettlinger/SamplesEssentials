package com.skyblockexp.ezrtp.tools;

import com.skyblockexp.ezrtp.platform.PlatformMessageService;
import com.skyblockexp.ezrtp.platform.PlatformMessageServiceRegistry;
import com.skyblockexp.ezrtp.util.MessageUtil;
import java.util.logging.Logger;
import net.kyori.adventure.text.Component;

public class MessageDeliveryBenchmark {
   public static void main(String[] args) throws Exception {
      Logger logger = Logger.getLogger("benchmark");
      String template = "Hello <#2fff7a>user</#2fff7a> %player_name%! This is a long message to test performance.";
      Object fakePlayer = new Object();

      for (int i = 0; i < 200; i++) {
         MessageUtil.serialize(template);
      }

      long t0 = System.nanoTime();

      for (int i = 0; i < 10000; i++) {
         String resolved;
         try {
            PlatformMessageService svc = PlatformMessageServiceRegistry.get();
            if (svc != null) {
               resolved = svc.resolvePlaceholders(fakePlayer, template, logger);
            } else {
               resolved = template;
            }
         } catch (Throwable var13) {
            resolved = template;
         }

         Component comp = MessageUtil.parseMiniMessage(resolved);
         String out = MessageUtil.serializeComponent(comp);
         if (out == null) {
            throw new RuntimeException("null");
         }
      }

      long t1 = System.nanoTime();
      System.out.println("MessageUtil path time ms: " + (t1 - t0) / 1000000.0);
      PlatformMessageService mockSvc = new PlatformMessageService() {
         @Override
         public String resolvePlaceholders(Object player, String text, Logger loggerx) {
            return text.replace("%player_name%", "benchmark");
         }

         @Override
         public boolean sendToSender(Object sender, Component component) {
            return true;
         }
      };
      PlatformMessageServiceRegistry.register(mockSvc);
      long t2 = System.nanoTime();

      for (int i = 0; i < 10000; i++) {
         String replaced = PlatformMessageServiceRegistry.get().resolvePlaceholders(fakePlayer, template, logger);
         PlatformMessageServiceRegistry.get().sendToSender(fakePlayer, MessageUtil.parseMiniMessage(replaced));
      }

      long t3 = System.nanoTime();
      System.out.println("PlatformService path time ms: " + (t3 - t2) / 1000000.0);
      PlatformMessageServiceRegistry.unregister();
   }
}
