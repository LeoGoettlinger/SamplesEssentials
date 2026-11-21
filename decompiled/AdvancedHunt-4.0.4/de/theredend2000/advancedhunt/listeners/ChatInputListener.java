package de.theredend2000.advancedhunt.listeners;

import de.theredend2000.advancedhunt.Main;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitTask;

public class ChatInputListener implements Listener {
   private final Main plugin;
   private final Map<UUID, ChatInputListener.InputRequest> awaitingInput = new ConcurrentHashMap<>();

   public ChatInputListener(Main plugin) {
      this.plugin = plugin;
   }

   public void requestInput(Player player, Consumer<String> callback) {
      UUID uuid = player.getUniqueId();
      this.cancelRequest(uuid);
      player.closeInventory();
      player.sendMessage(this.plugin.getMessageManager().getMessage("chat_input.request"));
      BukkitTask task = Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
         ChatInputListener.InputRequest request = this.awaitingInput.remove(uuid);
         if (request != null) {
            player.sendMessage(this.plugin.getMessageManager().getMessage("chat_input.timeout"));
         }
      }, 1200L);
      this.awaitingInput.put(uuid, new ChatInputListener.InputRequest(callback, task));
   }

   private void cancelRequestDueToAction(Player player) {
      UUID uuid = player.getUniqueId();
      if (this.awaitingInput.containsKey(uuid)) {
         this.cancelRequest(uuid);
         player.sendMessage(this.plugin.getMessageManager().getMessage("chat_input.canceled"));
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onChat(AsyncPlayerChatEvent event) {
      UUID playerId = event.getPlayer().getUniqueId();
      String message = event.getMessage();
      ChatInputListener.InputRequest request = this.awaitingInput.remove(playerId);
      if (request != null) {
         event.setCancelled(true);
         Bukkit.getScheduler().runTask(this.plugin, () -> {
            request.task().cancel();
            Player syncPlayer = Bukkit.getPlayer(playerId);
            if (syncPlayer != null && syncPlayer.isOnline()) {
               if (message.equalsIgnoreCase("cancel")) {
                  syncPlayer.sendMessage(this.plugin.getMessageManager().getMessage("chat_input.canceled"));
               } else {
                  request.callback().accept(message);
               }
            }
         });
      }
   }

   @EventHandler
   public void onMove(PlayerMoveEvent event) {
      if (event.getTo() != null) {
         if (event.getFrom().getBlockX() != event.getTo().getBlockX() || event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
            this.cancelRequestDueToAction(event.getPlayer());
         }
      }
   }

   @EventHandler
   public void onTeleport(PlayerTeleportEvent event) {
      this.cancelRequestDueToAction(event.getPlayer());
   }

   @EventHandler
   public void onInteract(PlayerInteractEvent event) {
      this.cancelRequestDueToAction(event.getPlayer());
   }

   @EventHandler
   public void onCommand(PlayerCommandPreprocessEvent event) {
      this.cancelRequestDueToAction(event.getPlayer());
   }

   @EventHandler
   public void onInventoryOpen(InventoryOpenEvent event) {
      if (event.getPlayer() instanceof Player) {
         Player player = (Player)event.getPlayer();
         this.cancelRequestDueToAction(player);
      }
   }

   @EventHandler
   public void onQuit(PlayerQuitEvent event) {
      this.cancelRequest(event.getPlayer().getUniqueId());
   }

   private void cancelRequest(UUID uuid) {
      ChatInputListener.InputRequest request = this.awaitingInput.remove(uuid);
      if (request != null) {
         request.task().cancel();
      }
   }

   private static final class InputRequest {
      private final Consumer<String> callback;
      private final BukkitTask task;

      private InputRequest(Consumer<String> callback, BukkitTask task) {
         this.callback = callback;
         this.task = task;
      }

      private Consumer<String> callback() {
         return this.callback;
      }

      private BukkitTask task() {
         return this.task;
      }
   }
}
