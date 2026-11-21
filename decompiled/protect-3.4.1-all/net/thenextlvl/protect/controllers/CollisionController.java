package net.thenextlvl.protect.controllers;

import java.util.Optional;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class CollisionController {
   private static final String TEAM_NAME = "collision_protection";
   @Nullable
   private Team collisionTeam;

   private Team getOrCreateCollisionTeam(Player player) {
      return this.getCollisionTeam(player).orElseGet(() -> {
         Team team = player.getScoreboard().registerNewTeam("collision_protection");
         team.setOption(Option.COLLISION_RULE, OptionStatus.NEVER);
         return team;
      });
   }

   private Optional<Team> getCollisionTeam(Player player) {
      return Optional.ofNullable(player.getScoreboard().getTeam("collision_protection"));
   }

   public void setCollidable(Player player, boolean collides) {
      Team team = this.getOrCreateCollisionTeam(player);
      if (collides) {
         team.removePlayer(player);
      } else {
         team.addPlayer(player);
      }
   }

   public void remove(Player player) {
      this.getCollisionTeam(player).ifPresent(Team::unregister);
   }
}
