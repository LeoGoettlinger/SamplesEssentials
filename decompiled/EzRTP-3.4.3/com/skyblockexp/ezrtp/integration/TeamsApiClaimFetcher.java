package com.skyblockexp.ezrtp.integration;

import com.skyblockexp.ezrtp.gui.ClaimSnapshot;
import com.skyblockexp.ezrtp.util.MessageUtil;
import com.skyblockexp.teamsapi.api.TeamsAPI;
import com.skyblockexp.teamsapi.api.TeamsClaimService;
import com.skyblockexp.teamsapi.api.TeamsService;
import com.skyblockexp.teamsapi.model.Team;
import com.skyblockexp.teamsapi.model.TeamClaim;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.entity.Player;

public final class TeamsApiClaimFetcher {
   private TeamsApiClaimFetcher() {
   }

   public static Optional<TeamsApiClaimFetcher.FetchResult> fetch(Player player) {
      if (TeamsAPI.isAvailable() && TeamsAPI.isClaimAvailable()) {
         TeamsService teamsService = TeamsAPI.getService();
         TeamsClaimService claimService = TeamsAPI.getClaimService();
         if (teamsService != null && claimService != null) {
            Team team = (Team)teamsService.getPlayerTeam(player.getUniqueId()).orElse(null);
            if (team == null) {
               MessageUtil.send(player, "<red>You are not in a faction/team.</red>");
               return Optional.empty();
            } else {
               Collection<TeamClaim> rawClaims = claimService.getTeamClaims(team.getId());
               if (rawClaims.isEmpty()) {
                  MessageUtil.send(player, "<red>Your faction/team has no claims.</red>");
                  return Optional.empty();
               } else {
                  List<ClaimSnapshot> snapshots = new ArrayList<>(rawClaims.size());

                  for (TeamClaim claim : rawClaims) {
                     UUID ownerUuid = ClaimOwnerResolver.resolvePreferredOwnerUuid(teamsService, claim).orElse(null);
                     snapshots.add(new ClaimSnapshot(claim.getWorldName(), claim.getChunkX(), claim.getChunkZ(), ownerUuid));
                  }

                  snapshots.sort(Comparator.comparing(ClaimSnapshot::worldName).thenComparingInt(ClaimSnapshot::chunkX).thenComparingInt(ClaimSnapshot::chunkZ));
                  return Optional.of(new TeamsApiClaimFetcher.FetchResult(snapshots, team.getDisplayName(), team.getId()));
               }
            }
         } else {
            MessageUtil.send(player, "<red>TeamsAPI services are not available right now.</red>");
            return Optional.empty();
         }
      } else {
         MessageUtil.send(player, "<red>TeamsAPI is not available on this server.</red>");
         return Optional.empty();
      }
   }

   public record FetchResult(List<ClaimSnapshot> claims, String teamDisplayName, UUID teamId) {
   }
}
