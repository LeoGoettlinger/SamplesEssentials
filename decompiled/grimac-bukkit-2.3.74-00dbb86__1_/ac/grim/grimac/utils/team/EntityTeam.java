package ac.grim.grimac.utils.team;

import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.UserProfile;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import lombok.Generated;

public final class EntityTeam {
   public final String name;
   public final Set<String> entries = new HashSet<>();
   private final GrimPlayer player;
   private WrapperPlayServerTeams.CollisionRule collisionRule;

   public EntityTeam(GrimPlayer player, String name) {
      this.player = player;
      this.name = name;
   }

   public void update(WrapperPlayServerTeams teams) {
      teams.getTeamInfo().ifPresent(info -> this.collisionRule = info.getCollisionRule());
      TeamHandler teamHandler = this.player.checkManager.getPacketCheck(TeamHandler.class);
      WrapperPlayServerTeams.TeamMode mode = teams.getTeamMode();
      if (mode != WrapperPlayServerTeams.TeamMode.ADD_ENTITIES && mode != WrapperPlayServerTeams.TeamMode.CREATE) {
         if (mode == WrapperPlayServerTeams.TeamMode.REMOVE_ENTITIES) {
            label64:
            for (String teamPlayer : teams.getPlayers()) {
               if (teamPlayer.equals(this.player.user.getName())) {
                  teamHandler.setPlayerTeam(null);
               } else {
                  for (UserProfile profile : this.player.compensatedEntities.profiles.values()) {
                     if (profile.getName() != null && profile.getName().equals(teamPlayer)) {
                        String uuid = profile.getUUID().toString();
                        this.entries.remove(uuid);
                        teamHandler.removeEntityFromTeam(uuid);
                        continue label64;
                     }
                  }

                  teamHandler.removeEntityFromTeam(teamPlayer);
                  this.entries.remove(teamPlayer);
               }
            }
         } else if (mode == WrapperPlayServerTeams.TeamMode.REMOVE) {
            EntityTeam playersTeam = teamHandler.getPlayerTeam();
            if (playersTeam != null && playersTeam.name.equals(this.name)) {
               teamHandler.setPlayerTeam(null);
            }

            for (String entry : this.entries) {
               teamHandler.removeEntityFromTeam(entry);
            }

            this.entries.clear();
         }
      } else {
         label81:
         for (String teamPlayerx : teams.getPlayers()) {
            if (teamPlayerx.equals(this.player.user.getName())) {
               teamHandler.setPlayerTeam(this);
            } else {
               for (UserProfile profilex : this.player.compensatedEntities.profiles.values()) {
                  if (profilex.getName() != null && profilex.getName().equals(teamPlayerx)) {
                     teamHandler.addEntityToTeam(profilex.getUUID().toString(), this);
                     continue label81;
                  }
               }

               teamHandler.addEntityToTeam(teamPlayerx, this);
            }
         }
      }
   }

   @Override
   public boolean equals(Object o) {
      return this == o || o instanceof EntityTeam t && Objects.equals(this.name, t.name);
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.name);
   }

   @Generated
   public WrapperPlayServerTeams.CollisionRule getCollisionRule() {
      return this.collisionRule;
   }
}
