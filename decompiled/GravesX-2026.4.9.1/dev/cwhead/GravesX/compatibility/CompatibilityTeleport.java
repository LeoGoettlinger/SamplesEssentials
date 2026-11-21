package dev.cwhead.GravesX.compatibility;

import com.ranull.graves.Graves;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public final class CompatibilityTeleport {
   private static final AtomicReference<Method> TELEPORT_ASYNC = new AtomicReference<>();

   private CompatibilityTeleport() {
   }

   public static CompletableFuture<Boolean> teleportSafely(Player player, Location target, Graves plugin) {
      return teleportSafely(player, target, TeleportCause.PLUGIN, plugin);
   }

   public static CompletableFuture<Boolean> teleportSafely(Player player, Location target, TeleportCause cause, Graves plugin) {
      CompletableFuture<Boolean> result = new CompletableFuture<>();
      Location safeTarget = target == null ? null : target.clone();
      if (player != null && plugin != null && safeTarget != null && safeTarget.getWorld() != null) {
         Method m = TELEPORT_ASYNC.get();
         if (m == null) {
            try {
               m = Player.class.getMethod("teleportAsync", Location.class, TeleportCause.class);
               TELEPORT_ASYNC.compareAndSet(null, m);
            } catch (Throwable var9) {
            }
         }

         if (m != null) {
            try {
               Object cf = m.invoke(player, safeTarget, cause);
               if (cf instanceof CompletableFuture) {
                  ((CompletableFuture)cf)
                     .whenComplete(
                        (ok, ex) -> {
                           if (ex == null && !Boolean.FALSE.equals(ok)) {
                              plugin.getSchedulerManager()
                                 .execute(safeTarget, () -> completeOnRegion(plugin, safeTarget, result, isAtTarget(player, safeTarget)));
                           } else {
                              fallbackRegionTeleport(player, safeTarget, cause, plugin)
                                 .whenComplete((ok2, ex2) -> completeOnRegion(plugin, safeTarget, result, ex2 == null && Boolean.TRUE.equals(ok2)));
                           }
                        }
                     );
                  return result;
               }
            } catch (Throwable var8) {
            }
         }

         fallbackRegionTeleport(player, safeTarget, cause, plugin)
            .whenComplete((ok, ex) -> completeOnRegion(plugin, safeTarget, result, ex == null && Boolean.TRUE.equals(ok)));
         return result;
      } else {
         if (player != null) {
            player.sendMessage(ChatColor.RED + "☠" + ChatColor.DARK_GRAY + " » " + ChatColor.RESET + "Teleport location unavailable.");
         }

         result.complete(false);
         return result;
      }
   }

   private static CompletableFuture<Boolean> fallbackRegionTeleport(Player player, Location safeTarget, TeleportCause cause, Graves plugin) {
      CompletableFuture<Boolean> fut = new CompletableFuture<>();

      try {
         plugin.getSchedulerManager().execute(safeTarget, () -> {
            try {
               boolean ok = player.teleport(safeTarget, cause);
               fut.complete(ok && isAtTarget(player, safeTarget));
            } catch (Throwable var5) {
               fut.complete(false);
            }
         });
      } catch (Throwable var6) {
         fut.complete(false);
      }

      return fut;
   }

   private static void completeOnRegion(Graves plugin, Location at, CompletableFuture<Boolean> fut, boolean value) {
      if (!fut.isDone()) {
         try {
            plugin.getSchedulerManager().execute(at, () -> {
               if (!fut.isDone()) {
                  fut.complete(value);
               }
            });
         } catch (Throwable var5) {
            if (!fut.isDone()) {
               fut.complete(value);
            }
         }
      }
   }

   private static boolean isAtTarget(Player player, Location target) {
      if (player != null && target != null && target.getWorld() != null) {
         Location now = player.getLocation();
         return now.getWorld() != null && now.getWorld() == target.getWorld() ? now.distanceSquared(target) <= 0.01 : false;
      } else {
         return false;
      }
   }
}
