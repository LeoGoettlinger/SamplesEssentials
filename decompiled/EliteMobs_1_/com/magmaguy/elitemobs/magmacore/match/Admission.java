package com.magmaguy.elitemobs.magmacore.match;

import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

final class Admission {
   private Admission() {
   }

   static AdmissionResult admit(Match match, Collection<? extends Player> requested, BooleanSupplier stillAuthorized) {
      if (stillAuthorized != null && stillAuthorized.getAsBoolean()) {
         LinkedHashMap<UUID, Player> unique = new LinkedHashMap<>();

         for (Player player : requested) {
            if (player != null) {
               unique.putIfAbsent(player.getUniqueId(), player);
            }
         }

         if (unique.isEmpty()) {
            return AdmissionResult.UNAVAILABLE;
         } else {
            List<Player> group = unique.values().stream().filter(playerx -> match.getMatchPlayer(playerx) == null).toList();
            if (group.isEmpty()) {
               return AdmissionResult.ALREADY_IN_MATCH;
            } else {
               AdmissionResult check = check(match, group);
               if (check != AdmissionResult.ADMITTED) {
                  return refuse(match, group, check);
               } else {
                  MatchApi api = match.getSettings().getApi();

                  for (Player playerx : group) {
                     if (!match.guard("joinAttempt", () -> api.joinAttempt(match, player), false)) {
                        return AdmissionResult.VETOED;
                     }

                     if (!stillAuthorized.getAsBoolean()) {
                        return AdmissionResult.NOT_AUTHORIZED;
                     }
                  }

                  if (!stillAuthorized.getAsBoolean()) {
                     return AdmissionResult.NOT_AUTHORIZED;
                  } else {
                     check = check(match, group);
                     if (check != AdmissionResult.ADMITTED) {
                        return refuse(match, group, check);
                     } else if (!match.guard("reserveAdmission", match::reserveAdmission, false)) {
                        return AdmissionResult.NOT_ACCEPTING;
                     } else {
                        MatchSettings settings = match.getSettings();
                        PlayerCustody custody = settings.getCustody();
                        List<MatchPlayer> registered = new ArrayList<>();
                        List<MatchPlayer> captured = new ArrayList<>();
                        List<BukkitTask> entries = new ArrayList<>();

                        try {
                           for (Player playerx : group) {
                              registered.add(register(match, playerx));
                           }

                           for (MatchPlayer participant : registered) {
                              custody.capture(participant.getPlayer());
                              captured.add(participant);
                           }

                           if (settings.getGameMode() != null) {
                              registered.forEach(participantx -> participantx.getPlayer().setGameMode(settings.getGameMode()));
                           }

                           for (MatchPlayer participant : registered) {
                              entries.add(scheduleEntry(match, participant));
                           }
                        } catch (LinkageError | RuntimeException var15) {
                           entries.forEach(BukkitTask::cancel);

                           for (MatchPlayer participant : captured) {
                              match.guard("custody release", () -> custody.restore(participant.getPlayer()));
                           }

                           rollback(match, registered);
                           Logger.warn("Match " + match.getRuntimeId() + ": admission failed: " + var15);
                           return AdmissionResult.FAILED;
                        }

                        registered.forEach(participantx -> announce(match, participantx));
                        if (settings.getCountdownSeconds() == 0
                           && match.phase == MatchPhase.WAITING
                           && match.getActivePlayers().size() >= settings.getMinPlayers()) {
                           match.start();
                        }

                        return AdmissionResult.ADMITTED;
                     }
                  }
               }
            }
         }
      } else {
         return AdmissionResult.NOT_AUTHORIZED;
      }
   }

   static AdmissionResult admitSpectator(Match match, Player player) {
      AdmissionResult check = checkSpectator(match, player);
      if (check != AdmissionResult.ADMITTED) {
         return check;
      } else {
         MatchSettings settings = match.getSettings();
         if (!match.guard("joinAttempt", () -> settings.getApi().joinAttempt(match, player), false)) {
            return AdmissionResult.VETOED;
         } else {
            check = checkSpectator(match, player);
            if (check != AdmissionResult.ADMITTED) {
               return check;
            } else if (!match.guard("reserveAdmission", match::reserveAdmission, false)) {
               return AdmissionResult.NOT_ACCEPTING;
            } else {
               List<MatchPlayer> registered = new ArrayList<>();
               boolean captured = false;
               boolean admitted = false;

               try {
                  MatchPlayer participant = register(match, player);
                  registered.add(participant);
                  participant.role = MatchRole.SPECTATOR;
                  participant.lives = 0;
                  settings.getCustody().capture(player);
                  captured = true;
                  player.setGameMode(GameMode.SPECTATOR);
                  Location destination = match.spectatorDestination(participant);
                  admitted = destination != null && MatchMovement.moveForMatch(match, player, participant, destination, MoveReason.ENTRY);
                  participant.entered = admitted;
               } catch (LinkageError | RuntimeException var12) {
                  Logger.warn("Match " + match.getRuntimeId() + ": spectator admission failed: " + var12);
               } finally {
                  if (!admitted) {
                     if (captured) {
                        match.guard("custody release", () -> settings.getCustody().restore(player));
                     }

                     if (!registered.isEmpty()) {
                        player.setGameMode(registered.getFirst().getPreviousGameMode());
                     }

                     rollback(match, registered);
                  }
               }

               if (!admitted) {
                  return AdmissionResult.FAILED;
               } else {
                  MatchPlayer participant = registered.getFirst();
                  MatchMessages messages = settings.getMessages();
                  match.guard("spectator feedback", () -> {
                     Feedback.message(player, messages.getSpectatorMessage(), "$player", player.getName());
                     Feedback.title(player, messages.getSpectatorTitle(), messages.getSpectatorSubtitle(), 60, 180, 60, "$player", player.getName());
                  });
                  match.guard("onJoin", () -> match.onJoin(participant));
                  match.guard("joined callback", () -> settings.getApi().joined(match, participant));
                  return AdmissionResult.ADMITTED;
               }
            }
         }
      }
   }

   private static AdmissionResult checkSpectator(Match match, Player player) {
      if (player == null || !player.isOnline() || !player.isValid()) {
         return AdmissionResult.UNAVAILABLE;
      } else if (!match.isOpen()) {
         return AdmissionResult.NOT_ACCEPTING;
      } else if (!MatchMarker.occupied(player) && MatchCore.participant(player) == null) {
         return !match.guard("acceptsSpectator", () -> match.acceptsSpectator(player), false) ? AdmissionResult.NOT_ACCEPTING : AdmissionResult.ADMITTED;
      } else {
         return AdmissionResult.ALREADY_IN_MATCH;
      }
   }

   private static AdmissionResult check(Match match, List<Player> group) {
      MatchSettings settings = match.getSettings();
      if (match.isOpen() && match.phase == MatchPhase.WAITING && match.guard("acceptsPlayers", match::acceptsPlayers, false)) {
         if (match.getActivePlayers().size() + group.size() > settings.getMaxPlayers()) {
            return AdmissionResult.FULL;
         } else {
            for (Player player : group) {
               if (player.isOnline() && player.isValid()) {
                  if (!MatchMarker.occupied(player) && MatchCore.participant(player) == null) {
                     String permission = match.requiredPermission();
                     if (permission != null && !player.hasPermission(permission)) {
                        return AdmissionResult.NO_PERMISSION;
                     }
                     continue;
                  }

                  return AdmissionResult.ALREADY_IN_MATCH;
               }

               return AdmissionResult.UNAVAILABLE;
            }

            return AdmissionResult.ADMITTED;
         }
      } else {
         return AdmissionResult.NOT_ACCEPTING;
      }
   }

   private static AdmissionResult refuse(Match match, List<Player> group, AdmissionResult result) {
      MatchMessages messages = match.getSettings().getMessages();

      String template = switch (result) {
         case NOT_ACCEPTING -> messages.getNotAccepting();
         case FULL -> messages.getFull();
         case NO_PERMISSION -> messages.getNoPermission();
         case ALREADY_IN_MATCH -> messages.getAlreadyInMatch();
         default -> null;
      };
      Feedback.message(group.getFirst(), template);
      return result;
   }

   private static MatchPlayer register(Match match, Player player) {
      MatchSettings settings = match.getSettings();
      MatchPlayer participant = match.createPlayer(player);
      participant.lives = settings.getDeath().initialLives();
      match.participants.add(participant);
      MatchCore.track(participant);
      MatchMarker.set(player, match);
      return participant;
   }

   static void rollback(Match match, List<MatchPlayer> registered) {
      for (MatchPlayer participant : registered) {
         match.guard("onAdmissionRolledBack", () -> match.onAdmissionRolledBack(participant));
         match.participants.remove(participant);
         MatchCore.untrack(participant);
         MatchMarker.clear(participant.getPlayer());
         if (match.getSettings().getGameMode() != null) {
            participant.getPlayer().setGameMode(participant.getPreviousGameMode());
         }
      }

      match.guard("abortAdmission", match::abortAdmission);
   }

   private static BukkitTask scheduleEntry(Match match, MatchPlayer participant) {
      return Bukkit.getScheduler().runTaskLater(MatchCore.plugin(), () -> {
         Player player = participant.getPlayer();
         if (player.isOnline() && MatchCore.participant(player) == participant && !match.isDestroyed()) {
            Location destination = null;

            try {
               destination = match.entryDestination(participant);
            } catch (LinkageError | RuntimeException var5) {
               Logger.warn("Match " + match.getRuntimeId() + ": entryDestination failed: " + var5);
            }

            if (destination != null && MatchMovement.moveForMatch(match, player, destination, MoveReason.ENTRY)) {
               participant.entered = true;
            } else {
               match.leave(player, LeaveReason.QUIT);
            }
         }
      }, 1L);
   }

   private static void announce(Match match, MatchPlayer participant) {
      MatchSettings settings = match.getSettings();
      MatchMessages messages = settings.getMessages();
      Player player = participant.getPlayer();
      match.guard(
         "join feedback",
         () -> {
            Feedback.message(player, messages.getJoinedMessage(), "$count", settings.getMinPlayers(), "$player", player.getName());
            Feedback.title(
               player, messages.getJoinedTitle(), messages.getJoinedSubtitle(), 60, 180, 60, "$count", settings.getMinPlayers(), "$player", player.getName()
            );
         }
      );
      match.guard("onJoin", () -> match.onJoin(participant));
      match.guard("joined callback", () -> settings.getApi().joined(match, participant));
   }
}
