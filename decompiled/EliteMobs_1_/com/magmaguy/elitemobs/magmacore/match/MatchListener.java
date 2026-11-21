package com.magmaguy.elitemobs.magmacore.match;

import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

final class MatchListener implements Listener {
   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onJoin(PlayerJoinEvent event) {
      Player player = event.getPlayer();
      if (MatchCore.participant(player) == null) {
         for (PlayerCustody custody : MatchCore.custodies()) {
            if (!player.isOnline()) {
               return;
            }

            custody.recover(player);
         }
      }
   }

   @EventHandler
   public void onQuit(PlayerQuitEvent event) {
      PendingMatch request = MatchCore.pendingOf(event.getPlayer());
      if (request != null) {
         request.cancel(PendingMatch.CancelReason.QUIT);
      }

      Match match = MatchCore.matchOf(event.getPlayer());
      if (match != null) {
         match.leave(event.getPlayer(), LeaveReason.DISCONNECT);
      }
   }

   @EventHandler(
      ignoreCancelled = true,
      priority = EventPriority.HIGHEST
   )
   public void onDamage(EntityDamageEvent event) {
      if (event.getEntity() instanceof Player player) {
         Match match = MatchCore.matchOf(player);
         if (match != null) {
            MatchPlayer participant = match.getMatchPlayer(player);
            if (participant != null) {
               if (event.getCause() == DamageCause.WORLD_BORDER) {
                  Location location = player.getLocation();
                  Logger.warn(
                     "Player "
                        + player.getName()
                        + " took WORLD_BORDER damage inside match world '"
                        + (location.getWorld() != null ? location.getWorld().getName() : "?")
                        + "' at "
                        + location.getX()
                        + ","
                        + location.getY()
                        + ","
                        + location.getZ()
                        + ". The world's border probably does not cover the play area. On Paper 1.21.6+ border data lives in its own file inside the dimension data folder, and a copied world may not include it."
                  );
               }

               if (event.getCause() == DamageCause.VOID && participant.role == MatchRole.PLAYER && match.startDestination() != null) {
                  event.setCancelled(true);
                  MatchWatchdog.rescue(match, participant);
               } else if (!(event.getFinalDamage() < player.getHealth())) {
                  event.setCancelled(true);
                  if (match.phase != MatchPhase.ONGOING) {
                     match.leave(player, LeaveReason.DIED);
                  } else if (participant.role == MatchRole.PLAYER) {
                     match.handleLethalDamage(participant);
                  }
               }
            }
         }
      }
   }

   @EventHandler
   public void onBannerBreak(BlockBreakEvent event) {
      reviveAt(event.getBlock());
   }

   @EventHandler
   public void onBannerHit(BlockDamageEvent event) {
      reviveAt(event.getBlock());
   }

   private static void reviveAt(Block block) {
      for (Match match : List.copyOf(MatchCore.matches())) {
         if (match.phase == MatchPhase.ONGOING) {
            ReviveBanner banner = match.reviveBanners.get(block);
            if (banner != null) {
               banner.clear(true);
            }
         }
      }
   }
}
