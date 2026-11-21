package ch.njol.skript.bukkitutil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event.Result;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;

public class ClickEventTracker {
   final Map<UUID, ClickEventTracker.TrackedEvent> firstEvents = new HashMap<>();
   private final Set<Cancellable> modifiedEvents = new HashSet<>();

   public ClickEventTracker(JavaPlugin plugin) {
      Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
         this.firstEvents.clear();
         this.modifiedEvents.clear();
      }, 1L, 1L);
   }

   public boolean checkEvent(Player player, Cancellable event, EquipmentSlot hand) {
      UUID uuid = player.getUniqueId();
      ClickEventTracker.TrackedEvent first = this.firstEvents.get(uuid);
      if (first == null || first.event == event) {
         this.firstEvents.put(uuid, new ClickEventTracker.TrackedEvent(event, hand));
         return true;
      } else if (!this.modifiedEvents.contains(first.event)) {
         return false;
      } else {
         if (event instanceof PlayerInteractEvent current) {
            Cancellable previous = first.event;
            if (previous instanceof PlayerInteractEvent prevClick) {
               current.setUseInteractedBlock(prevClick.useInteractedBlock());
               current.setUseItemInHand(prevClick.useItemInHand());
            } else {
               Result newResult = previous.isCancelled() ? Result.DENY : Result.DEFAULT;
               current.setUseInteractedBlock(newResult);
               current.setUseItemInHand(newResult);
            }
         } else {
            event.setCancelled(first.event.isCancelled());
         }

         return false;
      }
   }

   public void eventModified(Cancellable event) {
      this.modifiedEvents.add(event);
   }

   private static class TrackedEvent {
      final Cancellable event;
      final EquipmentSlot hand;

      public TrackedEvent(Cancellable event, EquipmentSlot hand) {
         this.event = event;
         this.hand = hand;
      }
   }
}
