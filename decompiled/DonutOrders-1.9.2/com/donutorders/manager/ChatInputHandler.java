package com.donutorders.manager;

import com.donutorders.DonutOrders;
import com.donutorders.scheduler.FoliaScheduler;
import com.donutorders.util.ChatInputParser;
import com.donutorders.util.MessageHelper;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import org.bukkit.entity.Player;

public class ChatInputHandler {
   private static final long SESSION_TIMEOUT_MS = 120000L;
   private final ConcurrentHashMap<UUID, ChatInputHandler.ChatSession> sessions = new ConcurrentHashMap<>();

   public void requestInput(Player player, String prompt, Consumer<String> onInput, Runnable onCancel) {
      this.sessions.put(player.getUniqueId(), new ChatInputHandler.ChatSession(onInput, onCancel));
      player.sendMessage(DonutOrders.colorize(prompt));
   }

   public boolean handleInput(Player player, String message) {
      ChatInputHandler.ChatSession session = this.sessions.remove(player.getUniqueId());
      if (session == null) {
         return false;
      } else {
         String cancelWord = MessageHelper.cancelKeyword();
         if (ChatInputParser.isCancel(message, cancelWord)) {
            FoliaScheduler.runAtEntity(player, session.onCancel::run);
         } else {
            String trimmed = ChatInputParser.normalize(message);
            FoliaScheduler.runAtEntity(player, () -> session.onInput.accept(trimmed), () -> FoliaScheduler.runGlobal(() -> session.onInput.accept(trimmed)));
         }

         return true;
      }
   }

   public void cancelSession(Player player) {
      ChatInputHandler.ChatSession session = this.sessions.remove(player.getUniqueId());
      if (session != null && session.onCancel != null) {
         FoliaScheduler.runGlobal(session.onCancel);
      }
   }

   public boolean hasPendingSession(UUID playerUUID) {
      return this.sessions.containsKey(playerUUID);
   }

   public void tickTimeouts() {
      this.sessions.entrySet().removeIf(entry -> {
         if (entry.getValue().isExpired()) {
            ChatInputHandler.ChatSession s = entry.getValue();
            if (s.onCancel != null) {
               FoliaScheduler.runGlobal(s.onCancel);
            }

            return true;
         } else {
            return false;
         }
      });
   }

   private static final class ChatSession {
      final Consumer<String> onInput;
      final Runnable onCancel;
      final long expiresAt;

      ChatSession(Consumer<String> onInput, Runnable onCancel) {
         this.onInput = onInput;
         this.onCancel = onCancel;
         this.expiresAt = System.currentTimeMillis() + 120000L;
      }

      boolean isExpired() {
         return System.currentTimeMillis() > this.expiresAt;
      }
   }
}
