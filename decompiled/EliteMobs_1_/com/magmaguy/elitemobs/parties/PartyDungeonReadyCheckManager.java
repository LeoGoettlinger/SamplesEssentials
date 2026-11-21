package com.magmaguy.elitemobs.parties;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.PartyConfig;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.magmacore.util.SpigotMessage;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public final class PartyDungeonReadyCheckManager {
   private static final Map<UUID, PartyDungeonReadyCheckManager.ReadyCheck> checksByParty = new HashMap<>();

   private PartyDungeonReadyCheckManager() {
   }

   public static boolean request(Player initiator, Collection<UUID> memberIds, String dungeonDescription, PartyDungeonReadyCheckManager.DungeonLaunch launch) {
      List<UUID> roster = List.copyOf(memberIds);
      if (roster.size() <= 1) {
         return launch.launch(new PartyDungeonReadyCheckManager.LaunchReservation() {
            @Override
            public boolean isValid() {
               return true;
            }

            @Override
            public void release() {
            }
         });
      } else {
         Party party = PartyManager.getParty(initiator.getUniqueId());
         if (party != null && PartyManager.isDungeonEntryRosterCurrent(initiator, roster)) {
            if (checksByParty.containsKey(party.getId())) {
               PartyManager.sendConfiguredMessage(initiator, PartyConfig.getDungeonReadyCheckPendingMessage());
               return false;
            } else {
               UUID token = UUID.randomUUID();
               PartyDungeonReadyCheckManager.ReadyCheck check = new PartyDungeonReadyCheckManager.ReadyCheck(
                  party.getId(),
                  token,
                  initiator.getUniqueId(),
                  roster,
                  safeDescription(dungeonDescription),
                  launch,
                  System.nanoTime() + PartyConfig.getDungeonReadyCheckTimeoutSeconds() * 1000000000L
               );
               check.readyPlayerIds.add(initiator.getUniqueId());
               checksByParty.put(party.getId(), check);
               check.expirationTask = Bukkit.getScheduler()
                  .runTaskLater(MetadataHandler.PLUGIN, () -> expire(party.getId(), token), PartyConfig.getDungeonReadyCheckTimeoutSeconds() * 20L);
               broadcast(check, replaceStatus(PartyConfig.getDungeonReadyCheckStartedMessage(), check, initiator));

               for (UUID memberId : roster) {
                  Player member = Bukkit.getPlayer(memberId);
                  if (member != null && member.isOnline()) {
                     if (memberId.equals(initiator.getUniqueId())) {
                        member.spigot()
                           .sendMessage(
                              new BaseComponent[]{
                                 SpigotMessage.simpleMessage(PartyManager.format(PartyConfig.getDungeonReadyCheckCancelPromptMessage())),
                                 SpigotMessage.commandHoverMessage(
                                    PartyManager.format(PartyConfig.getDungeonReadyCheckCancelButton()),
                                    PartyManager.format(PartyConfig.getDungeonReadyCheckDeclineHover()),
                                    "/em party decline " + token
                                 )
                              }
                           );
                        if (PartyInventoryMenu.usesInventoryFallback(member)) {
                           PartyInventoryMenu.openReadyCheck(member, token, check.dungeonDescription, true, true);
                        }
                     } else {
                        member.spigot()
                           .sendMessage(
                              new BaseComponent[]{
                                 SpigotMessage.simpleMessage(
                                    PartyManager.format(PartyConfig.getDungeonReadyCheckPromptMessage().replace("$dungeon", check.dungeonDescription))
                                 ),
                                 SpigotMessage.commandHoverMessage(
                                    PartyManager.format(PartyConfig.getDungeonReadyCheckReadyButton()),
                                    PartyManager.format(PartyConfig.getDungeonReadyCheckReadyHover()),
                                    "/em party ready " + token
                                 ),
                                 SpigotMessage.commandHoverMessage(
                                    PartyManager.format(PartyConfig.getDungeonReadyCheckDeclineButton()),
                                    PartyManager.format(PartyConfig.getDungeonReadyCheckDeclineHover()),
                                    "/em party decline " + token
                                 )
                              }
                           );
                        if (PartyInventoryMenu.usesInventoryFallback(member)) {
                           PartyInventoryMenu.openReadyCheck(member, token, check.dungeonDescription, false, false);
                        }
                     }
                  }
               }

               return true;
            }
         } else {
            PartyManager.sendConfiguredMessage(initiator, PartyConfig.getDungeonPartyChangedMessage());
            return false;
         }
      }
   }

   public static void ready(Player player, String rawToken) {
      PartyDungeonReadyCheckManager.ReadyCheck check = resolveWaitingCheck(player, rawToken);
      if (check != null) {
         if (!rosterIsCurrent(check)) {
            cancelForRosterChange(PartyManager.getParty(player.getUniqueId()));
         } else if (!check.readyPlayerIds.add(player.getUniqueId())) {
            PartyManager.sendConfiguredMessage(player, PartyConfig.getDungeonReadyCheckAlreadyReadyMessage());
         } else {
            broadcast(check, replaceStatus(PartyConfig.getDungeonReadyCheckPlayerReadyMessage(), check, player));
            if (check.readyPlayerIds.containsAll(check.memberIds)) {
               launch(check);
            }
         }
      }
   }

   public static void decline(Player player, String rawToken) {
      PartyDungeonReadyCheckManager.ReadyCheck check = resolveWaitingCheck(player, rawToken);
      if (check != null) {
         remove(check);
         broadcast(check, PartyConfig.getDungeonReadyCheckDeclinedMessage().replace("$player", player.getName()).replace("$dungeon", check.dungeonDescription));
      }
   }

   static boolean openPendingInventory(Player player) {
      Party party = PartyManager.getParty(player.getUniqueId());
      PartyDungeonReadyCheckManager.ReadyCheck check = party == null ? null : checksByParty.get(party.getId());
      if (check != null
         && check.state == PartyDungeonReadyCheckManager.ReadyState.WAITING
         && check.memberIdSet.contains(player.getUniqueId())
         && check.expiresAtNanos > System.nanoTime()) {
         PartyInventoryMenu.openReadyCheck(
            player, check.token, check.dungeonDescription, check.initiatorId.equals(player.getUniqueId()), check.readyPlayerIds.contains(player.getUniqueId())
         );
         return true;
      } else {
         return false;
      }
   }

   static void cancelForRosterChange(Party party) {
      if (party != null) {
         PartyDungeonReadyCheckManager.ReadyCheck check = checksByParty.get(party.getId());
         if (check != null) {
            if (check.state == PartyDungeonReadyCheckManager.ReadyState.WAITING) {
               remove(check);
            } else {
               check.authorized.set(false);
            }

            broadcast(check, PartyConfig.getDungeonPartyChangedMessage());
         }
      }
   }

   static void shutdown() {
      for (PartyDungeonReadyCheckManager.ReadyCheck check : List.copyOf(checksByParty.values())) {
         remove(check);
      }

      checksByParty.clear();
   }

   private static PartyDungeonReadyCheckManager.ReadyCheck resolveWaitingCheck(Player player, String rawToken) {
      UUID token;
      try {
         token = UUID.fromString(rawToken);
      } catch (NullPointerException | IllegalArgumentException var5) {
         PartyManager.sendConfiguredMessage(player, PartyConfig.getDungeonReadyCheckNoPendingMessage());
         return null;
      }

      Party party = PartyManager.getParty(player.getUniqueId());
      PartyDungeonReadyCheckManager.ReadyCheck check = party == null ? null : checksByParty.get(party.getId());
      if (check == null
         || check.state != PartyDungeonReadyCheckManager.ReadyState.WAITING
         || !check.token.equals(token)
         || !check.memberIdSet.contains(player.getUniqueId())) {
         PartyManager.sendConfiguredMessage(player, PartyConfig.getDungeonReadyCheckNoPendingMessage());
         return null;
      } else if (check.expiresAtNanos <= System.nanoTime()) {
         expire(check.partyId, check.token);
         return null;
      } else {
         return check;
      }
   }

   private static boolean rosterIsCurrent(PartyDungeonReadyCheckManager.ReadyCheck check) {
      Player initiator = Bukkit.getPlayer(check.initiatorId);
      return initiator != null && initiator.isOnline() && PartyManager.isDungeonEntryRosterCurrent(initiator, check.memberIds);
   }

   private static void launch(final PartyDungeonReadyCheckManager.ReadyCheck check) {
      if (checksByParty.get(check.partyId) == check && check.state == PartyDungeonReadyCheckManager.ReadyState.WAITING) {
         if (!rosterIsCurrent(check)) {
            remove(check);
            broadcast(check, PartyConfig.getDungeonPartyChangedMessage());
         } else {
            cancelExpiration(check);
            check.state = PartyDungeonReadyCheckManager.ReadyState.LAUNCHING;
            PartyInventoryMenu.closeReadyCheck(check.token);
            broadcast(check, PartyConfig.getDungeonReadyCheckCompleteMessage().replace("$dungeon", check.dungeonDescription));
            PartyDungeonReadyCheckManager.LaunchReservation reservation = new PartyDungeonReadyCheckManager.LaunchReservation() {
               @Override
               public boolean isValid() {
                  return check.state == PartyDungeonReadyCheckManager.ReadyState.LAUNCHING && check.authorized.get();
               }

               @Override
               public void release() {
                  check.authorized.set(false);
                  PartyDungeonReadyCheckManager.checksByParty.remove(check.partyId, check);
               }
            };

            try {
               if (!check.launch.launch(reservation)) {
                  reservation.release();
               }
            } catch (RuntimeException var3) {
               reservation.release();
               Logger.warn("Failed to launch party dungeon after ready check: " + var3.getMessage());
               throw var3;
            }
         }
      }
   }

   private static void expire(UUID partyId, UUID token) {
      PartyDungeonReadyCheckManager.ReadyCheck check = checksByParty.get(partyId);
      if (check != null && check.state == PartyDungeonReadyCheckManager.ReadyState.WAITING && check.token.equals(token)) {
         remove(check);
         broadcast(check, PartyConfig.getDungeonReadyCheckExpiredMessage().replace("$dungeon", check.dungeonDescription));
      }
   }

   private static void remove(PartyDungeonReadyCheckManager.ReadyCheck check) {
      check.authorized.set(false);
      checksByParty.remove(check.partyId, check);
      cancelExpiration(check);
      PartyInventoryMenu.closeReadyCheck(check.token);
   }

   private static void cancelExpiration(PartyDungeonReadyCheckManager.ReadyCheck check) {
      if (check.expirationTask != null) {
         check.expirationTask.cancel();
         check.expirationTask = null;
      }
   }

   private static String replaceStatus(String message, PartyDungeonReadyCheckManager.ReadyCheck check, Player player) {
      return message.replace("$player", player.getName())
         .replace("$dungeon", check.dungeonDescription)
         .replace("$ready", String.valueOf(check.readyPlayerIds.size()))
         .replace("$total", String.valueOf(check.memberIds.size()));
   }

   private static void broadcast(PartyDungeonReadyCheckManager.ReadyCheck check, String message) {
      for (UUID memberId : check.memberIds) {
         Player member = Bukkit.getPlayer(memberId);
         if (member != null && member.isOnline()) {
            PartyManager.sendConfiguredMessage(member, message);
         }
      }
   }

   private static String safeDescription(String description) {
      return description != null && !description.isBlank() ? description : PartyConfig.getDungeonReadyCheckUnknownDungeonName();
   }

   @FunctionalInterface
   public interface DungeonLaunch {
      boolean launch(PartyDungeonReadyCheckManager.LaunchReservation var1);
   }

   public interface LaunchReservation {
      boolean isValid();

      void release();
   }

   private static final class ReadyCheck {
      private final UUID partyId;
      private final UUID token;
      private final UUID initiatorId;
      private final List<UUID> memberIds;
      private final Set<UUID> memberIdSet;
      private final String dungeonDescription;
      private final PartyDungeonReadyCheckManager.DungeonLaunch launch;
      private final long expiresAtNanos;
      private final Set<UUID> readyPlayerIds = new HashSet<>();
      private volatile PartyDungeonReadyCheckManager.ReadyState state = PartyDungeonReadyCheckManager.ReadyState.WAITING;
      private final AtomicBoolean authorized = new AtomicBoolean(true);
      private BukkitTask expirationTask;

      private ReadyCheck(
         UUID partyId,
         UUID token,
         UUID initiatorId,
         List<UUID> memberIds,
         String dungeonDescription,
         PartyDungeonReadyCheckManager.DungeonLaunch launch,
         long expiresAtNanos
      ) {
         this.partyId = partyId;
         this.token = token;
         this.initiatorId = initiatorId;
         this.memberIds = memberIds;
         this.memberIdSet = Set.copyOf(memberIds);
         this.dungeonDescription = dungeonDescription;
         this.launch = launch;
         this.expiresAtNanos = expiresAtNanos;
      }
   }

   private static enum ReadyState {
      WAITING,
      LAUNCHING;
   }
}
