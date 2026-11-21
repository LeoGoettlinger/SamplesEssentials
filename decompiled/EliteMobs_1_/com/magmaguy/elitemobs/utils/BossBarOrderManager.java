package com.magmaguy.elitemobs.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class BossBarOrderManager {
   private static final Map<UUID, TreeMap<BossBarOrderManager.SortKey, BossBar>> playerBars = new HashMap<>();
   private static final Map<UUID, Set<BossBarOrderManager.Suspension>> suspensionsByPlayer = new HashMap<>();

   private BossBarOrderManager() {
   }

   public static long sortKeyFor(UUID owner) {
      return owner.getMostSignificantBits();
   }

   public static void show(Player player, BossBar bossBar, long sortKey) {
      if (player != null && bossBar != null && player.isOnline()) {
         UUID playerUUID = player.getUniqueId();
         TreeMap<BossBarOrderManager.SortKey, BossBar> bars = playerBars.computeIfAbsent(playerUUID, keyx -> new TreeMap<>());
         BossBarOrderManager.SortKey key = new BossBarOrderManager.SortKey(sortKey, System.identityHashCode(bossBar));
         boolean alreadyRegistered = bars.get(key) == bossBar;
         if (!alreadyRegistered) {
            bars.values().remove(bossBar);
            bars.put(key, bossBar);
         }

         if (suspensionsByPlayer.containsKey(playerUUID)) {
            bossBar.removePlayer(player);
         } else if (alreadyRegistered) {
            if (!bossBar.getPlayers().contains(player)) {
               bossBar.addPlayer(player);
            }
         } else {
            for (BossBar tailBar : bars.tailMap(key, true).values()) {
               tailBar.removePlayer(player);
               tailBar.addPlayer(player);
            }
         }
      }
   }

   public static void show(Player player, BossBar bossBar) {
      show(player, bossBar, Integer.toUnsignedLong(System.identityHashCode(bossBar)));
   }

   public static void hide(Player player, BossBar bossBar) {
      if (bossBar != null) {
         if (player != null) {
            bossBar.removePlayer(player);
            TreeMap<BossBarOrderManager.SortKey, BossBar> bars = playerBars.get(player.getUniqueId());
            if (bars != null) {
               bars.values().remove(bossBar);
               if (bars.isEmpty()) {
                  playerBars.remove(player.getUniqueId());
               }
            }
         }
      }
   }

   public static BossBarOrderManager.Suspension suspendPlayer(Player player) {
      if (player == null) {
         return null;
      } else {
         UUID playerUUID = player.getUniqueId();
         BossBarOrderManager.Suspension suspension = new BossBarOrderManager.Suspension(playerUUID);
         Set<BossBarOrderManager.Suspension> suspensions = suspensionsByPlayer.computeIfAbsent(playerUUID, ignored -> new HashSet<>());
         suspensions.add(suspension);
         TreeMap<BossBarOrderManager.SortKey, BossBar> bars = playerBars.get(playerUUID);
         if (bars == null) {
            return suspension;
         } else {
            try {
               for (BossBar bossBar : bars.values()) {
                  bossBar.removePlayer(player);
               }

               return suspension;
            } catch (Error | RuntimeException var10) {
               Throwable failure = var10;
               suspensions.remove(suspension);
               if (suspensions.isEmpty()) {
                  suspensionsByPlayer.remove(playerUUID);

                  for (BossBar bossBar : bars.values()) {
                     try {
                        bossBar.addPlayer(player);
                     } catch (Error | RuntimeException var9) {
                        failure.addSuppressed(var9);
                     }
                  }
               }

               throw failure;
            }
         }
      }
   }

   public static void resumePlayer(Player player, BossBarOrderManager.Suspension suspension) {
      Set<BossBarOrderManager.Suspension> suspensions = suspensionsFor(player, suspension);
      if (suspensions != null) {
         if (suspensions.size() <= 1 && player.isOnline()) {
            TreeMap<BossBarOrderManager.SortKey, BossBar> bars = playerBars.get(suspension.playerUUID);
            if (bars != null) {
               try {
                  for (BossBar bossBar : bars.values()) {
                     bossBar.removePlayer(player);
                     bossBar.addPlayer(player);
                  }
               } catch (RuntimeException var9) {
                  RuntimeException failure = var9;

                  for (BossBar bossBar : bars.values()) {
                     try {
                        bossBar.removePlayer(player);
                     } catch (RuntimeException var8) {
                        failure.addSuppressed(var8);
                     }
                  }

                  throw failure;
               }
            }

            removeSuspension(suspension, suspensions);
         } else {
            removeSuspension(suspension, suspensions);
         }
      }
   }

   public static void discardSuspension(Player player, BossBarOrderManager.Suspension suspension) {
      Set<BossBarOrderManager.Suspension> suspensions = suspensionsFor(player, suspension);
      if (suspensions != null) {
         removeSuspension(suspension, suspensions);
      }
   }

   private static Set<BossBarOrderManager.Suspension> suspensionsFor(Player player, BossBarOrderManager.Suspension suspension) {
      if (player != null && suspension != null && player.getUniqueId().equals(suspension.playerUUID)) {
         Set<BossBarOrderManager.Suspension> suspensions = suspensionsByPlayer.get(suspension.playerUUID);
         return suspensions != null && suspensions.contains(suspension) ? suspensions : null;
      } else {
         return null;
      }
   }

   private static void removeSuspension(BossBarOrderManager.Suspension suspension, Set<BossBarOrderManager.Suspension> suspensions) {
      suspensions.remove(suspension);
      if (suspensions.isEmpty()) {
         suspensionsByPlayer.remove(suspension.playerUUID);
      }
   }

   public static void clearPlayer(Player player) {
      if (player != null) {
         UUID playerUUID = player.getUniqueId();
         TreeMap<BossBarOrderManager.SortKey, BossBar> bars = playerBars.remove(playerUUID);
         RuntimeException failure = null;

         try {
            if (bars != null) {
               for (BossBar bossBar : bars.values()) {
                  try {
                     bossBar.removePlayer(player);
                  } catch (RuntimeException var10) {
                     failure = appendFailure(failure, var10);
                  }
               }
            }
         } finally {
            suspensionsByPlayer.remove(playerUUID);
         }

         if (failure != null) {
            throw failure;
         }
      }
   }

   public static void shutdown() {
      Set<BossBar> bars = Collections.newSetFromMap(new IdentityHashMap<>());
      playerBars.values().forEach(playerBossBars -> bars.addAll(playerBossBars.values()));
      RuntimeException failure = null;

      try {
         for (BossBar bossBar : bars) {
            try {
               bossBar.removeAll();
            } catch (RuntimeException var8) {
               failure = appendFailure(failure, var8);
            }
         }
      } finally {
         playerBars.clear();
         suspensionsByPlayer.clear();
      }

      if (failure != null) {
         throw failure;
      }
   }

   private static RuntimeException appendFailure(RuntimeException failure, RuntimeException exception) {
      if (failure == null) {
         return exception;
      } else {
         failure.addSuppressed(exception);
         return failure;
      }
   }

   public static class BossBarOrderManagerEvents implements Listener {
      @EventHandler
      public void onQuit(PlayerQuitEvent event) {
         BossBarOrderManager.clearPlayer(event.getPlayer());
      }
   }

   private record SortKey(long primary, int tiebreak) implements Comparable<BossBarOrderManager.SortKey> {
      public int compareTo(BossBarOrderManager.SortKey other) {
         int comparison = Long.compare(this.primary, other.primary);
         return comparison != 0 ? comparison : Integer.compare(this.tiebreak, other.tiebreak);
      }
   }

   public static final class Suspension {
      private final UUID playerUUID;

      private Suspension(UUID playerUUID) {
         this.playerUUID = playerUUID;
      }
   }
}
