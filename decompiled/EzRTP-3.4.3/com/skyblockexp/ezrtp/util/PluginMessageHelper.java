package com.skyblockexp.ezrtp.util;

import com.skyblockexp.ezrtp.EzRtpPlugin;
import com.skyblockexp.ezrtp.config.EzRtpConfiguration;
import com.skyblockexp.ezrtp.message.MessageKey;
import com.skyblockexp.ezrtp.platform.PlatformSenderBridge;
import com.skyblockexp.ezrtp.platform.PlatformSenderBridgeRegistry;
import java.util.Map;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

public final class PluginMessageHelper {
   private PluginMessageHelper() {
   }

   public static void sendCooldownMessage(CommandSender sender, EzRtpPlugin plugin, EzRtpConfiguration configuration, long waitSeconds) {
      if (sender != null && plugin != null) {
         String timeValue = configuration != null && configuration.isHumanReadableCooldown()
            ? TimeUtil.formatCooldownTime(waitSeconds)
            : String.valueOf(waitSeconds);
         Component message = plugin.getMessageProvider().format(MessageKey.COOLDOWN, Map.of("seconds", timeValue));
         if (!tryPlatformSend(sender, message)) {
            MessageUtil.send(sender, message);
         }
      }
   }

   public static void sendLimitMessage(CommandSender sender, EzRtpPlugin plugin, String limitType) {
      if (sender != null && plugin != null && limitType != null) {
         MessageKey key = "daily".equalsIgnoreCase(limitType) ? MessageKey.LIMIT_DAILY : MessageKey.LIMIT_WEEKLY;
         Component message = plugin.getMessageProvider().format(key);
         if (!tryPlatformSend(sender, message)) {
            MessageUtil.send(sender, message);
         }
      }
   }

   private static boolean tryPlatformSend(CommandSender sender, Component component) {
      PlatformSenderBridge bridge = PlatformSenderBridgeRegistry.get();
      if (bridge == null) {
         return false;
      } else {
         try {
            bridge.sendToSender(sender, component);
         } catch (Throwable var4) {
         }

         return true;
      }
   }
}
