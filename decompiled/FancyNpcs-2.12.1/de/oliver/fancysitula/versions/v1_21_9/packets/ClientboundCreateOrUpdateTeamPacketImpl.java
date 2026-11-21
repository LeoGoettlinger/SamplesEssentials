package de.oliver.fancysitula.versions.v1_21_9.packets;

import de.oliver.fancysitula.api.entities.FS_RealPlayer;
import de.oliver.fancysitula.api.packets.FS_ClientboundCreateOrUpdateTeamPacket;
import de.oliver.fancysitula.api.teams.FS_CollisionRule;
import de.oliver.fancysitula.api.teams.FS_NameTagVisibility;
import de.oliver.fancysitula.versions.v1_21_9.utils.VanillaPlayerAdapter;
import io.papermc.paper.adventure.PaperAdventure;
import net.minecraft.ChatFormatting;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket.Action;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team.CollisionRule;
import net.minecraft.world.scores.Team.Visibility;

public class ClientboundCreateOrUpdateTeamPacketImpl extends FS_ClientboundCreateOrUpdateTeamPacket {
   private static final Scoreboard SCOREBOARD = new Scoreboard();

   public ClientboundCreateOrUpdateTeamPacketImpl(String teamName, FS_ClientboundCreateOrUpdateTeamPacket.CreateTeam createTeam) {
      super(teamName, createTeam);
   }

   public ClientboundCreateOrUpdateTeamPacketImpl(String teamName, FS_ClientboundCreateOrUpdateTeamPacket.RemoveTeam removeTeam) {
      super(teamName, removeTeam);
   }

   public ClientboundCreateOrUpdateTeamPacketImpl(String teamName, FS_ClientboundCreateOrUpdateTeamPacket.UpdateTeam updateTeam) {
      super(teamName, updateTeam);
   }

   public ClientboundCreateOrUpdateTeamPacketImpl(String teamName, FS_ClientboundCreateOrUpdateTeamPacket.AddEntity addEntity) {
      super(teamName, addEntity);
   }

   public ClientboundCreateOrUpdateTeamPacketImpl(String teamName, FS_ClientboundCreateOrUpdateTeamPacket.RemoveEntity removeEntity) {
      super(teamName, removeEntity);
   }

   @Override
   public Object createPacket() {
      return switch (this.method) {
         case CREATE_TEAM -> this.createCreateTeamPacket();
         case REMOVE_TEAM -> this.createRemoveTeamPacket();
         case UPDATE_TEAM -> this.createUpdateTeamPacket();
         case ADD_ENTITY -> this.createAddEntityPacket();
         case REMOVE_ENTITY -> this.createRemoveEntityPacket();
      };
   }

   private Object createCreateTeamPacket() {
      if (this.createTeam == null) {
         return null;
      } else {
         PlayerTeam playerTeam = new PlayerTeam(SCOREBOARD, this.teamName);
         playerTeam.setDisplayName(PaperAdventure.asVanilla(this.createTeam.getDisplayName()));
         playerTeam.setAllowFriendlyFire(this.createTeam.isAllowFriendlyFire());
         playerTeam.setSeeFriendlyInvisibles(this.createTeam.isCanSeeFriendlyInvisibles());
         playerTeam.setNameTagVisibility(this.convertVisibility(this.createTeam.getNameTagVisibility()));
         playerTeam.setCollisionRule(this.convertCollisionRule(this.createTeam.getCollisionRule()));
         playerTeam.setColor(ChatFormatting.getById(this.createTeam.getColor().getId()));
         playerTeam.setPlayerPrefix(PaperAdventure.asVanilla(this.createTeam.getPrefix()));
         playerTeam.setPlayerSuffix(PaperAdventure.asVanilla(this.createTeam.getSuffix()));

         for (String entity : this.createTeam.getEntities()) {
            playerTeam.getPlayers().add(entity);
         }

         return ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(playerTeam, true);
      }
   }

   private Object createRemoveTeamPacket() {
      if (this.removeTeam == null) {
         return null;
      } else {
         PlayerTeam playerTeam = new PlayerTeam(SCOREBOARD, this.teamName);
         return ClientboundSetPlayerTeamPacket.createRemovePacket(playerTeam);
      }
   }

   private Object createUpdateTeamPacket() {
      if (this.updateTeam == null) {
         return null;
      } else {
         PlayerTeam playerTeam = new PlayerTeam(SCOREBOARD, this.teamName);
         playerTeam.setDisplayName(PaperAdventure.asVanilla(this.updateTeam.getDisplayName()));
         playerTeam.setAllowFriendlyFire(this.updateTeam.isAllowFriendlyFire());
         playerTeam.setSeeFriendlyInvisibles(this.updateTeam.isCanSeeFriendlyInvisibles());
         playerTeam.setNameTagVisibility(this.convertVisibility(this.updateTeam.getNameTagVisibility()));
         playerTeam.setCollisionRule(this.convertCollisionRule(this.updateTeam.getCollisionRule()));
         playerTeam.setColor(ChatFormatting.getById(this.updateTeam.getColor().getId()));
         playerTeam.setPlayerPrefix(PaperAdventure.asVanilla(this.updateTeam.getPrefix()));
         playerTeam.setPlayerSuffix(PaperAdventure.asVanilla(this.updateTeam.getSuffix()));
         return ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(playerTeam, false);
      }
   }

   private Object createAddEntityPacket() {
      if (this.addEntity == null) {
         return null;
      } else {
         PlayerTeam playerTeam = new PlayerTeam(SCOREBOARD, this.teamName);

         for (String entity : this.addEntity.getEntities()) {
            playerTeam.getPlayers().add(entity);
         }

         return ClientboundSetPlayerTeamPacket.createMultiplePlayerPacket(playerTeam, this.addEntity.getEntities(), Action.ADD);
      }
   }

   private Object createRemoveEntityPacket() {
      if (this.removeEntity == null) {
         return null;
      } else {
         PlayerTeam playerTeam = new PlayerTeam(SCOREBOARD, this.teamName);

         for (String entity : this.removeEntity.getEntities()) {
            playerTeam.getPlayers().add(entity);
         }

         return ClientboundSetPlayerTeamPacket.createMultiplePlayerPacket(playerTeam, this.removeEntity.getEntities(), Action.REMOVE);
      }
   }

   @Override
   protected void sendPacketTo(FS_RealPlayer player) {
      ClientboundSetPlayerTeamPacket packet = (ClientboundSetPlayerTeamPacket)this.createPacket();
      ServerPlayer vanillaPlayer = VanillaPlayerAdapter.asVanilla(player.getBukkitPlayer());
      vanillaPlayer.connection.send(packet);
   }

   private Visibility convertVisibility(FS_NameTagVisibility visibility) {
      return switch (visibility) {
         case ALWAYS -> Visibility.ALWAYS;
         case NEVER -> Visibility.NEVER;
         case HIDE_FOR_OTHER_TEAMS -> Visibility.HIDE_FOR_OTHER_TEAMS;
         case HIDE_FOR_OWN_TEAM -> Visibility.HIDE_FOR_OWN_TEAM;
      };
   }

   private CollisionRule convertCollisionRule(FS_CollisionRule rule) {
      return switch (rule) {
         case ALWAYS -> CollisionRule.ALWAYS;
         case NEVER -> CollisionRule.NEVER;
         case PUSH_OTHER_TEAMS -> CollisionRule.PUSH_OTHER_TEAMS;
         case PUSH_OWN_TEAM -> CollisionRule.PUSH_OWN_TEAM;
      };
   }
}
