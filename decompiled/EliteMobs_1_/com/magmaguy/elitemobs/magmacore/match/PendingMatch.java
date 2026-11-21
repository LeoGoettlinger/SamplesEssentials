package com.magmaguy.elitemobs.magmacore.match;

import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public final class PendingMatch {
   private final List<Player> roster;
   private final List<Consumer<PendingMatch.CancelReason>> cleanups = new ArrayList<>();
   private BukkitTask timeout;
   private boolean active = true;
   private PendingMatch.CancelReason cancelledWith;

   private PendingMatch(List<Player> roster) {
      this.roster = roster;
   }

   public static PendingMatch request(Collection<? extends Player> roster, long timeoutTicks) {
      MatchCore.plugin();
      LinkedHashMap<UUID, Player> unique = new LinkedHashMap<>();

      for (Player player : roster) {
         if (player != null) {
            unique.putIfAbsent(player.getUniqueId(), player);
         }
      }

      if (unique.isEmpty()) {
         return null;
      } else {
         for (Player playerx : unique.values()) {
            if (playerx.isOnline() && playerx.isValid()) {
               if (!MatchMarker.occupied(playerx) && MatchCore.participant(playerx) == null) {
                  continue;
               }

               return null;
            }

            return null;
         }

         PendingMatch pending = new PendingMatch(List.copyOf(unique.values()));

         for (Player playerx : pending.roster) {
            MatchMarker.setPending(playerx);
            MatchCore.trackPending(playerx, pending);
         }

         pending.timeout = Bukkit.getScheduler().runTaskLater(MatchCore.plugin(), () -> pending.cancel(PendingMatch.CancelReason.TIMEOUT), timeoutTicks);
         return pending;
      }
   }

   public boolean isActive() {
      return this.active;
   }

   public List<Player> getRoster() {
      return this.roster;
   }

   public void onCancel(Consumer<PendingMatch.CancelReason> cleanup) {
      if (this.cancelledWith != null) {
         run(cleanup, this.cancelledWith);
      } else if (this.active) {
         this.cleanups.add(cleanup);
      }
   }

   public void cancel() {
      this.cancel(PendingMatch.CancelReason.CANCELLED);
   }

   void cancel(PendingMatch.CancelReason reason) {
      if (this.active) {
         this.active = false;
         this.cancelledWith = reason;
         this.release();

         for (Consumer<PendingMatch.CancelReason> cleanup : List.copyOf(this.cleanups)) {
            run(cleanup, reason);
         }

         this.cleanups.clear();
      }
   }

   public AdmissionResult complete(Match match) {
      if (!this.active) {
         match.destroy();
         return AdmissionResult.NOT_ACCEPTING;
      } else {
         this.active = false;
         this.cleanups.clear();
         this.release();
         if (!match.open()) {
            match.destroy();
            return AdmissionResult.NOT_ACCEPTING;
         } else {
            AdmissionResult result = match.admit(this.roster);
            if (result != AdmissionResult.ADMITTED) {
               match.destroy();
            }

            return result;
         }
      }
   }

   private void release() {
      if (this.timeout != null) {
         this.timeout.cancel();
      }

      for (Player player : this.roster) {
         MatchMarker.clear(player);
         MatchCore.untrackPending(player, this);
      }
   }

   private static void run(Consumer<PendingMatch.CancelReason> cleanup, PendingMatch.CancelReason reason) {
      try {
         cleanup.accept(reason);
      } catch (LinkageError | RuntimeException var3) {
         Logger.warn("A pending match cleanup failed: " + var3);
      }
   }

   public static enum CancelReason {
      QUIT,
      TIMEOUT,
      CANCELLED;
   }
}
