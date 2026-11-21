package com.magmaguy.elitemobs.parties;

import com.magmaguy.elitemobs.api.InstancedDungeonRemoveEvent;
import com.magmaguy.elitemobs.api.PlayerJoinDungeonEvent;
import com.magmaguy.elitemobs.config.PartyConfig;
import com.magmaguy.elitemobs.instanced.MatchInstance;
import com.magmaguy.elitemobs.instanced.dungeons.DungeonInstance;
import com.magmaguy.elitemobs.items.customloottable.SharedLootTable;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.magmacore.util.SpigotMessage;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.InstancedBossEntity;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.Predicate;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PartyManager implements Listener {
   private static final Map<UUID, Party> parties = new HashMap<>();
   private static final Map<UUID, UUID> partyByPlayer = new HashMap<>();
   private static final Map<UUID, PartyManager.PendingInvite> pendingInvites = new HashMap<>();
   private static final Map<UUID, UUID> dungeonParties = new HashMap<>();

   public static void initialize() {
      shutdown();
      if (PartyConfig.isEnabled()) {
         PartySidebar.initialize();
      }
   }

   public static void shutdown() {
      List<Player> onlineMembers = partyByPlayer.keySet().stream().<Player>map(Bukkit::getPlayer).filter(Objects::nonNull).toList();
      PartyDungeonReadyCheckManager.shutdown();
      PartyInteractionHint.shutdown();
      PartyInventoryMenu.shutdown();
      parties.clear();
      partyByPlayer.clear();
      pendingInvites.clear();
      dungeonParties.clear();
      onlineMembers.forEach(PartySidebar::clearPlayer);
      PartySidebar.shutdown();
   }

   public static Map<UUID, Party> getParties() {
      return Collections.unmodifiableMap(parties);
   }

   public static Party getParty(UUID playerId) {
      UUID partyId = partyByPlayer.get(playerId);
      return partyId == null ? null : parties.get(partyId);
   }

   public static boolean isInParty(UUID playerId) {
      return getParty(playerId) != null;
   }

   public static List<Player> getInvitablePlayers(Player owner) {
      Party ownerParty = getParty(owner.getUniqueId());
      if (ownerParty != null && ownerParty.isFull()) {
         return List.of();
      } else {
         List<Player> targets = new ArrayList<>();

         for (Player candidate : Bukkit.getOnlinePlayers()) {
            if (!candidate.equals(owner)
               && !candidate.hasMetadata("NPC")
               && candidate.hasPermission("elitemobs.party")
               && !isInParty(candidate.getUniqueId())
               && !hasActivePendingInvite(candidate.getUniqueId())) {
               targets.add(candidate);
            }
         }

         targets.sort(Comparator.comparing(Player::getName, String.CASE_INSENSITIVE_ORDER));
         return List.copyOf(targets);
      }
   }

   public static List<UUID> getDungeonEntryMemberIds(Player initiator) {
      UUID initiatorId = initiator.getUniqueId();
      Party party = getParty(initiatorId);
      if (PartyConfig.isEnabled() && party != null) {
         List<UUID> memberIds = new ArrayList<>(party.getMembersInDisplayOrder());
         memberIds.remove(initiatorId);
         memberIds.add(0, initiatorId);
         return List.copyOf(memberIds);
      } else {
         return List.of(initiatorId);
      }
   }

   public static boolean isDungeonEntryRosterCurrent(Player initiator, Collection<UUID> memberIds) {
      Set<UUID> snapshot = new HashSet<>(memberIds);
      if (snapshot.size() != memberIds.size() || !snapshot.contains(initiator.getUniqueId())) {
         return false;
      } else if (!PartyConfig.isEnabled()) {
         return snapshot.equals(Set.of(initiator.getUniqueId()));
      } else {
         Party party = getParty(initiator.getUniqueId());
         return party == null
            ? snapshot.equals(Set.of(initiator.getUniqueId()))
            : party.getMembers().equals(snapshot) && snapshot.stream().allMatch(memberId -> getParty(memberId) == party);
      }
   }

   public static void create(Player creator) {
      create(creator, true);
   }

   public static PartyOperationResult create(Player creator, boolean notify) {
      PartyOperationResult validation = validateActor(creator);
      if (!validation.isSuccess()) {
         return finish(creator, validation, notify, "");
      } else {
         invalidatePendingInvite(creator.getUniqueId());
         if (isInParty(creator.getUniqueId())) {
            return finish(creator, PartyOperationResult.ALREADY_IN_PARTY, notify, "");
         } else {
            Party party = new Party(creator.getUniqueId());
            parties.put(party.getId(), party);
            partyByPlayer.put(creator.getUniqueId(), party.getId());
            if (notify) {
               send(creator, PartyConfig.getPartyCreatedMessage());
            }

            PartySidebar.refresh(creator);
            return PartyOperationResult.SUCCESS;
         }
      }
   }

   public static void invite(Player inviter, String targetName) {
      invite(inviter, targetName, true);
   }

   public static PartyOperationResult invite(Player inviter, String targetName, boolean notify) {
      PartyOperationResult validation = validateActor(inviter);
      if (!validation.isSuccess()) {
         return finish(inviter, validation, notify, "");
      } else {
         Objects.requireNonNull(targetName, "targetName");
         Player target = Bukkit.getPlayerExact(targetName);
         if (target != null && target.isOnline()) {
            if (target.getUniqueId().equals(inviter.getUniqueId())) {
               return finish(inviter, PartyOperationResult.SELF_INVITE, notify, targetName);
            } else if (isInParty(target.getUniqueId())) {
               return finish(inviter, PartyOperationResult.TARGET_ALREADY_IN_PARTY, notify, targetName);
            } else if (!target.hasPermission("elitemobs.party")) {
               return finish(inviter, PartyOperationResult.TARGET_NO_PERMISSION, notify, target.getName());
            } else if (hasActivePendingInvite(target.getUniqueId())) {
               return finish(inviter, PartyOperationResult.INVITE_ALREADY_PENDING, notify, target.getName());
            } else {
               Party party = getParty(inviter.getUniqueId());
               if (party == null) {
                  PartyOperationResult created = create(inviter, notify);
                  if (!created.isSuccess()) {
                     return created;
                  }

                  party = getParty(inviter.getUniqueId());
               }

               if (party.isFull()) {
                  return finish(inviter, PartyOperationResult.PARTY_FULL, notify, targetName);
               } else {
                  pendingInvites.put(
                     target.getUniqueId(),
                     new PartyManager.PendingInvite(
                        party.getId(), inviter.getUniqueId(), System.nanoTime() + PartyConfig.getInviteTimeoutSeconds() * 1000000000L
                     )
                  );
                  if (notify) {
                     send(inviter, PartyConfig.getInviteSentMessage().replace("$player", target.getName()));
                     target.spigot()
                        .sendMessage(
                           new BaseComponent[]{
                              SpigotMessage.simpleMessage(format(PartyConfig.getInviteReceivedMessage().replace("$player", inviter.getName()))),
                              SpigotMessage.commandHoverMessage(
                                 format(PartyConfig.getInviteAcceptButton()), format(PartyConfig.getInviteAcceptHover()), "/em party accept"
                              )
                           }
                        );
                     if (PartyInventoryMenu.usesInventoryFallback(target)) {
                        PartyInventoryMenu.openInvitePrompt(target, inviter);
                     }
                  }

                  return PartyOperationResult.SUCCESS;
               }
            }
         } else {
            return finish(inviter, PartyOperationResult.PLAYER_UNAVAILABLE, notify, targetName);
         }
      }
   }

   static void ignoreInvite(Player player) {
      invalidatePendingInvite(player.getUniqueId());
   }

   static boolean openPendingInviteInventory(Player player) {
      PartyManager.PendingInvite invite = pendingInvites.get(player.getUniqueId());
      if (!isPendingInviteValid(player.getUniqueId(), invite)) {
         invalidatePendingInvite(player.getUniqueId());
         return false;
      } else {
         Player inviter = Bukkit.getPlayer(invite.inviterId());
         PartyInventoryMenu.openInvitePrompt(player, inviter);
         return true;
      }
   }

   static boolean hasActivePendingInvite(UUID playerId) {
      PartyManager.PendingInvite invite = pendingInvites.get(playerId);
      if (isPendingInviteValid(playerId, invite)) {
         return true;
      } else {
         if (invite != null) {
            invalidatePendingInvite(playerId);
         }

         return false;
      }
   }

   public static void accept(Player player) {
      accept(player, true);
   }

   public static PartyOperationResult accept(Player player, boolean notify) {
      PartyOperationResult validation = validateActor(player);
      if (!validation.isSuccess()) {
         return finish(player, validation, notify, "");
      } else if (isInParty(player.getUniqueId())) {
         invalidatePendingInvite(player.getUniqueId());
         return finish(player, PartyOperationResult.ALREADY_IN_PARTY, notify, "");
      } else {
         PartyManager.PendingInvite invite = invalidatePendingInvite(player.getUniqueId());
         if (invite == null) {
            return finish(player, PartyOperationResult.NO_PENDING_INVITE, notify, "");
         } else if (invite.expiresAtNanos() <= System.nanoTime()) {
            return finish(player, PartyOperationResult.INVITE_EXPIRED, notify, "");
         } else {
            Party party = parties.get(invite.partyId());
            Player inviter = Bukkit.getPlayer(invite.inviterId());
            Party inviterParty = getParty(invite.inviterId());
            if (party == null || inviter == null || !inviter.isOnline() || inviterParty == null || !inviterParty.getId().equals(party.getId())) {
               return finish(player, PartyOperationResult.INVITE_EXPIRED, notify, "");
            } else if (!party.addMember(player.getUniqueId())) {
               if (party.isFull()) {
                  invalidatePendingInvites(inviteEntry -> inviteEntry.partyId().equals(party.getId()));
               }

               return finish(player, PartyOperationResult.PARTY_FULL, notify, "");
            } else {
               if (party.isFull()) {
                  invalidatePendingInvites(inviteEntry -> inviteEntry.partyId().equals(party.getId()));
               }

               PartyDungeonReadyCheckManager.cancelForRosterChange(party);
               partyByPlayer.put(player.getUniqueId(), party.getId());
               if (notify) {
                  send(player, PartyConfig.getJoinedPartyMessage());
                  broadcast(party, PartyConfig.getMemberJoinedMessage().replace("$player", player.getName()), player.getUniqueId());
               }

               refresh(party);
               return PartyOperationResult.SUCCESS;
            }
         }
      }
   }

   public static void leave(Player player) {
      leave(player, true);
   }

   public static PartyOperationResult leave(Player player, boolean notify) {
      requirePrimaryThread();
      Objects.requireNonNull(player, "player");
      boolean wasInParty = isInParty(player.getUniqueId());
      leave(player.getUniqueId(), notify, notify);
      return wasInParty ? PartyOperationResult.SUCCESS : PartyOperationResult.NOT_IN_PARTY;
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onDungeonJoin(PlayerJoinDungeonEvent event) {
      if (PartyConfig.isEnabled()) {
         DungeonInstance dungeon = event.getDungeonInstance();
         List<Player> roster = dungeon.getParticipants()
            .stream()
            .filter(memberx -> !dungeon.isSpectator(memberx) && memberx.isOnline())
            .sorted(Comparator.comparing(Player::getName, String.CASE_INSENSITIVE_ORDER))
            .toList();
         if (roster.size() >= 2) {
            Set<UUID> rosterIds = new HashSet<>();
            roster.forEach(memberx -> rosterIds.add(memberx.getUniqueId()));
            Party party = parties.get(dungeonParties.get(dungeon.getRuntimeId()));
            if (party != null && !rosterIds.containsAll(party.getMembers())) {
               party = null;
            }

            if (party == null) {
               party = getParty(roster.get(0).getUniqueId());
               if (party == null || !rosterIds.containsAll(party.getMembers())) {
                  Player leader = roster.get(0);
                  leave(leader.getUniqueId(), false);
                  party = new Party(leader.getUniqueId());
                  parties.put(party.getId(), party);
                  partyByPlayer.put(leader.getUniqueId(), party.getId());
               }

               dungeonParties.put(dungeon.getRuntimeId(), party.getId());
            }

            for (Player member : roster) {
               if (getParty(member.getUniqueId()) != party) {
                  leave(member.getUniqueId(), false);
                  party.addAdmittedDungeonMember(member.getUniqueId());
                  partyByPlayer.put(member.getUniqueId(), party.getId());
                  send(member, PartyConfig.getJoinedPartyMessage());
               }
            }

            refresh(party);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onDungeonRemoved(InstancedDungeonRemoveEvent event) {
      dungeonParties.remove(event.getDungeonInstance().getRuntimeId());
   }

   public static List<Player> getNearbyMembers(Player partyMember, Location location) {
      Party party = getParty(partyMember.getUniqueId());
      return party == null ? List.of() : getNearbyMembers(party, location);
   }

   public static List<Player> getNearbyMembers(Party party, Location location) {
      if (PartyConfig.isEnabled() && party != null && location != null && location.getWorld() != null) {
         double maxDistanceSquared = PartyConfig.getSharedProgressRange() * PartyConfig.getSharedProgressRange();
         List<Player> players = new ArrayList<>();

         for (UUID memberId : party.getMembers()) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null
               && member.isOnline()
               && member.isValid()
               && member.getWorld().equals(location.getWorld())
               && !(member.getLocation().distanceSquared(location) > maxDistanceSquared)
               && PlayerData.isInMemory(memberId)) {
               MatchInstance matchInstance = PlayerData.getMatchInstance(member);
               if (matchInstance == null
                  || matchInstance.getState() == MatchInstance.InstancedRegionState.WAITING
                  || matchInstance.getState() == MatchInstance.InstancedRegionState.STARTING
                  || matchInstance.getStartingParticipantIds().contains(memberId)) {
                  players.add(member);
               }
            }
         }

         return players;
      } else {
         return List.of();
      }
   }

   public static List<Player> expandSharedCreditParticipants(Collection<Player> contributors, EliteEntity eliteEntity) {
      return expandSharedCreditParticipants(contributors, eliteEntity == null ? null : eliteEntity.getLocation());
   }

   public static List<Player> expandSharedCreditParticipants(Collection<Player> contributors, Location creditLocation) {
      LinkedHashMap<UUID, Player> eligible = new LinkedHashMap<>();
      Map<Party, List<Player>> nearbyByParty = new IdentityHashMap<>();

      for (Player contributor : contributors) {
         if (contributor != null && !contributor.hasMetadata("NPC")) {
            eligible.put(contributor.getUniqueId(), contributor);
            Party party = getParty(contributor.getUniqueId());
            if (party != null) {
               for (Player partyMember : nearbyByParty.computeIfAbsent(party, current -> getNearbyMembers(current, creditLocation))) {
                  eligible.put(partyMember.getUniqueId(), partyMember);
               }
            }
         }
      }

      return List.copyOf(eligible.values());
   }

   public static boolean shouldUsePartyLoot(Player player, EliteEntity eliteEntity) {
      if (PartyConfig.isEnabled() && player != null && eliteEntity != null) {
         if (eliteEntity instanceof InstancedBossEntity instance && instance.getDungeonInstance() == null) {
            return false;
         } else {
            Party party = getParty(player.getUniqueId());
            return party != null && getNearbyMembers(party, eliteEntity.getLocation()).size() > 1;
         }
      } else {
         return false;
      }
   }

   private static void leave(UUID playerId, boolean notifyPlayer) {
      leave(playerId, notifyPlayer, true);
   }

   private static void leave(UUID playerId, boolean notifyPlayer, boolean notifyParty) {
      invalidatePendingInvite(playerId);
      invalidatePendingInvites(invite -> invite.inviterId().equals(playerId));
      Party party = getParty(playerId);
      if (party == null) {
         if (notifyPlayer) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
               send(player, PartyConfig.getNotInPartyMessage());
            }
         }
      } else {
         UUID oldLeader = party.getLeader();
         String playerName = playerName(playerId);
         PartyDungeonReadyCheckManager.cancelForRosterChange(party);
         SharedLootTable.onPartyMemberLeave(party.getId(), playerId);
         party.removeMember(playerId);
         partyByPlayer.remove(playerId);
         Player leavingPlayer = Bukkit.getPlayer(playerId);
         if (leavingPlayer != null) {
            if (notifyPlayer) {
               send(leavingPlayer, PartyConfig.getLeftPartyMessage());
            }

            PartySidebar.clearPlayer(leavingPlayer);
         }

         if (party.isEmpty()) {
            parties.remove(party.getId());
            invalidatePendingInvites(invite -> invite.partyId().equals(party.getId()));
         } else {
            if (notifyParty) {
               broadcast(party, PartyConfig.getMemberLeftMessage().replace("$player", playerName), null);
               if (oldLeader.equals(playerId) && party.getLeader() != null) {
                  broadcast(party, PartyConfig.getLeaderChangedMessage().replace("$player", playerName(party.getLeader())), null);
               }
            }

            refresh(party);
         }
      }
   }

   private static void requirePrimaryThread() {
      if (!Bukkit.isPrimaryThread()) {
         throw new IllegalStateException("Party operations must run on the server thread");
      }
   }

   private static PartyOperationResult validateActor(Player player) {
      requirePrimaryThread();
      Objects.requireNonNull(player, "player");
      if (!PartyConfig.isEnabled()) {
         return PartyOperationResult.DISABLED;
      } else if (!player.isOnline()) {
         return PartyOperationResult.PLAYER_UNAVAILABLE;
      } else {
         return !player.hasPermission("elitemobs.party") ? PartyOperationResult.NO_PERMISSION : PartyOperationResult.SUCCESS;
      }
   }

   private static PartyOperationResult finish(Player player, PartyOperationResult result, boolean notify, String target) {
      if (notify) {
         String message = switch (result) {
            case DISABLED -> PartyConfig.getDisabledMessage();
            case NO_PERMISSION -> PartyConfig.getInventoryNoPermissionMessage();
            case PLAYER_UNAVAILABLE -> PartyConfig.getPlayerUnavailableMessage();
            case ALREADY_IN_PARTY -> PartyConfig.getAlreadyInPartyMessage();
            case SELF_INVITE -> PartyConfig.getSelfInviteMessage();
            case TARGET_ALREADY_IN_PARTY -> PartyConfig.getPlayerAlreadyInPartyMessage();
            case TARGET_NO_PERMISSION -> PartyConfig.getPlayerCannotUsePartiesMessage();
            case INVITE_ALREADY_PENDING -> PartyConfig.getInviteAlreadyPendingMessage();
            case PARTY_FULL -> PartyConfig.getPartyFullMessage();
            case NO_PENDING_INVITE -> PartyConfig.getNoPendingInviteMessage();
            case INVITE_EXPIRED -> PartyConfig.getInviteExpiredMessage();
            case NOT_IN_PARTY -> PartyConfig.getNotInPartyMessage();
            case SUCCESS -> throw new IllegalArgumentException("Success feedback belongs to its operation");
         };
         send(player, (message == null ? "" : message).replace("$player", target));
      }

      return result;
   }

   private static void refresh(Party party) {
      for (UUID memberId : party.getMembers()) {
         Player member = Bukkit.getPlayer(memberId);
         if (member != null) {
            PartySidebar.refresh(member);
         }
      }
   }

   private static void broadcast(Party party, String message, UUID excludedPlayer) {
      for (UUID memberId : party.getMembers()) {
         if (!memberId.equals(excludedPlayer)) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null) {
               send(member, message);
            }
         }
      }
   }

   private static void send(Player player, String message) {
      Logger.sendSimpleMessage(player, format(message));
   }

   public static void sendConfiguredMessage(Player player, String message) {
      if (player != null) {
         send(player, message);
      }
   }

   static String format(String message) {
      return (message == null ? "" : message).replace("$prefix", PartyConfig.getPrefix());
   }

   private static String playerName(UUID playerId) {
      String name = Bukkit.getOfflinePlayer(playerId).getName();
      return name == null ? PartyConfig.getUnknownPlayerName() : name;
   }

   static void cleanupExpiredInvites() {
      long now = System.nanoTime();
      invalidatePendingInvites(invite -> invite.expiresAtNanos() <= now);
   }

   private static boolean isPendingInviteValid(UUID invitedPlayerId, PartyManager.PendingInvite invite) {
      if (invite != null && invite.expiresAtNanos() > System.nanoTime() && !isInParty(invitedPlayerId)) {
         Party invitedParty = parties.get(invite.partyId());
         if (invitedParty != null && !invitedParty.isFull()) {
            Player inviter = Bukkit.getPlayer(invite.inviterId());
            Party inviterParty = getParty(invite.inviterId());
            return inviter != null && inviter.isOnline() && inviterParty != null && inviterParty.getId().equals(invitedParty.getId());
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private static PartyManager.PendingInvite invalidatePendingInvite(UUID invitedPlayerId) {
      PartyManager.PendingInvite removed = pendingInvites.remove(invitedPlayerId);
      PartyInventoryMenu.closeInvitePrompt(invitedPlayerId);
      return removed;
   }

   private static void invalidatePendingInvites(Predicate<PartyManager.PendingInvite> predicate) {
      List<UUID> invitedPlayerIds = pendingInvites.entrySet().stream().filter(entry -> predicate.test(entry.getValue())).map(Entry::getKey).toList();
      invitedPlayerIds.forEach(PartyManager::invalidatePendingInvite);
   }

   @EventHandler
   public void onPlayerQuit(PlayerQuitEvent event) {
      leave(event.getPlayer().getUniqueId(), false);
   }

   private record PendingInvite(UUID partyId, UUID inviterId, long expiresAtNanos) {
   }
}
