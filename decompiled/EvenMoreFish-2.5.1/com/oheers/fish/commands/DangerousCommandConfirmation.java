package com.oheers.fish.commands;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NonNull;

public final class DangerousCommandConfirmation {
   private static final long CONFIRMATION_WINDOW_MILLIS = 30000L;
   private static final Map<String, Long> pendingConfirmations = new ConcurrentHashMap<>();

   private DangerousCommandConfirmation() {
      throw new UnsupportedOperationException();
   }

   public static boolean confirmOrRequest(@NonNull CommandSender sender, @NonNull String confirmationId, @NonNull String confirmationCommand) {
      long now = System.currentTimeMillis();
      String key = buildKey(sender, confirmationId);
      Long expiresAt = pendingConfirmations.get(key);
      if (expiresAt != null && expiresAt >= now) {
         pendingConfirmations.remove(key);
         return true;
      } else {
         pendingConfirmations.put(key, now + 30000L);
         sender.sendMessage("This command permanently deletes EMF database data. Run " + confirmationCommand + " again within 30 seconds to confirm.");
         return false;
      }
   }

   @NonNull
   private static String buildKey(@NonNull CommandSender sender, @NonNull String confirmationId) {
      return sender.getClass().getName() + ":" + sender.getName() + ":" + confirmationId;
   }
}
