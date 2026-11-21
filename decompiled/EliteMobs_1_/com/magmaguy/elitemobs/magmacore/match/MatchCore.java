package com.magmaguy.elitemobs.magmacore.match;

import com.magmaguy.elitemobs.magmacore.util.WorldFolderResolver;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.plugin.java.JavaPlugin;

public final class MatchCore {
   public static final String MARKER_KEY = "magmacore_match";
   private static final Set<Match> matches = new LinkedHashSet<>();
   private static final Map<UUID, MatchPlayer> participants = new HashMap<>();
   private static final List<Listener> listeners = new ArrayList<>();
   private static final Set<PlayerCustody> custodies = new LinkedHashSet<>();
   private static final Map<UUID, PendingMatch> pending = new HashMap<>();
   private static JavaPlugin plugin;

   private MatchCore() {
   }

   public static void enable(JavaPlugin owner) {
      if (plugin == null) {
         plugin = Objects.requireNonNull(owner, "owner");
         listen(new MatchListener());
         listen(new TeleportGuard());
      }
   }

   public static boolean moveWithin(Player player, Location destination, TeleportCause cause) {
      return MatchMovement.moveWithin(player, destination, cause);
   }

   public static boolean moveOut(Player player, Location destination, TeleportCause cause) {
      return MatchMovement.moveOut(player, destination, cause);
   }

   public static void shutdown() {
      if (plugin != null) {
         try {
            for (PendingMatch request : List.copyOf(new LinkedHashSet<>(pending.values()))) {
               request.cancel(PendingMatch.CancelReason.CANCELLED);
            }

            for (Match match : new ArrayList<>(matches)) {
               match.destroyForShutdown();
            }
         } finally {
            pending.clear();
            matches.clear();
            participants.clear();
            custodies.clear();
            listeners.forEach(HandlerList::unregisterAll);
            listeners.clear();
            plugin = null;
         }
      }
   }

   public static void sweepLeftoverWorlds() {
      LeftoverWorlds.sweep(
         plugin().getName(),
         WorldFolderResolver.listAllWorldNames(),
         name -> WorldFolderResolver.resolve(name).toPath(),
         name -> Bukkit.getWorld(name) != null,
         WorldFolderResolver::deleteAllLayouts
      );
   }

   public static void registerCustody(PlayerCustody custody) {
      Objects.requireNonNull(custody, "custody");
      plugin();
      if (custodies.add(custody)) {
         for (Player player : List.copyOf(Bukkit.getOnlinePlayers())) {
            if (participants.get(player.getUniqueId()) == null && player.isOnline()) {
               custody.recover(player);
            }
         }
      }
   }

   static Collection<PlayerCustody> custodies() {
      return List.copyOf(custodies);
   }

   public static Collection<Match> matches() {
      return Collections.unmodifiableSet(matches);
   }

   public static Match matchOf(Player player) {
      MatchPlayer participant = participants.get(player.getUniqueId());
      return participant == null ? null : participant.getMatch();
   }

   static JavaPlugin plugin() {
      if (plugin == null) {
         throw new IllegalStateException("MatchCore.enable(plugin) has not been called");
      } else {
         return plugin;
      }
   }

   static void listen(Listener listener) {
      Bukkit.getPluginManager().registerEvents(listener, plugin());
      listeners.add(listener);
   }

   static void register(Match match) {
      matches.add(match);
   }

   static void unregister(Match match) {
      matches.remove(match);
   }

   static MatchPlayer participant(Player player) {
      return participants.get(player.getUniqueId());
   }

   static void track(MatchPlayer player) {
      participants.put(player.getUniqueId(), player);
   }

   static void untrack(MatchPlayer player) {
      participants.remove(player.getUniqueId(), player);
   }

   static PendingMatch pendingOf(Player player) {
      return pending.get(player.getUniqueId());
   }

   static void trackPending(Player player, PendingMatch request) {
      pending.put(player.getUniqueId(), request);
   }

   static void untrackPending(Player player, PendingMatch request) {
      pending.remove(player.getUniqueId(), request);
   }
}
