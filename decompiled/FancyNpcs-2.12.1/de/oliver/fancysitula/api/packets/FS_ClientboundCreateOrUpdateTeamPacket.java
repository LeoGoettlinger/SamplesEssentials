package de.oliver.fancysitula.api.packets;

import de.oliver.fancysitula.api.teams.FS_CollisionRule;
import de.oliver.fancysitula.api.teams.FS_NameTagVisibility;
import java.util.List;
import net.kyori.adventure.text.Component;

public abstract class FS_ClientboundCreateOrUpdateTeamPacket extends FS_ClientboundPacket {
   protected FS_ClientboundCreateOrUpdateTeamPacket.Method method;
   protected String teamName;
   protected FS_ClientboundCreateOrUpdateTeamPacket.CreateTeam createTeam;
   protected FS_ClientboundCreateOrUpdateTeamPacket.RemoveTeam removeTeam;
   protected FS_ClientboundCreateOrUpdateTeamPacket.UpdateTeam updateTeam;
   protected FS_ClientboundCreateOrUpdateTeamPacket.AddEntity addEntity;
   protected FS_ClientboundCreateOrUpdateTeamPacket.RemoveEntity removeEntity;

   public FS_ClientboundCreateOrUpdateTeamPacket(String teamName, FS_ClientboundCreateOrUpdateTeamPacket.CreateTeam createTeam) {
      this.method = FS_ClientboundCreateOrUpdateTeamPacket.Method.CREATE_TEAM;
      this.teamName = teamName;
      this.createTeam = createTeam;
   }

   public FS_ClientboundCreateOrUpdateTeamPacket(String teamName, FS_ClientboundCreateOrUpdateTeamPacket.RemoveTeam removeTeam) {
      this.method = FS_ClientboundCreateOrUpdateTeamPacket.Method.REMOVE_TEAM;
      this.teamName = teamName;
      this.removeTeam = removeTeam;
   }

   public FS_ClientboundCreateOrUpdateTeamPacket(String teamName, FS_ClientboundCreateOrUpdateTeamPacket.UpdateTeam updateTeam) {
      this.method = FS_ClientboundCreateOrUpdateTeamPacket.Method.UPDATE_TEAM;
      this.teamName = teamName;
      this.updateTeam = updateTeam;
   }

   public FS_ClientboundCreateOrUpdateTeamPacket(String teamName, FS_ClientboundCreateOrUpdateTeamPacket.AddEntity addEntity) {
      this.method = FS_ClientboundCreateOrUpdateTeamPacket.Method.ADD_ENTITY;
      this.teamName = teamName;
      this.addEntity = addEntity;
   }

   public FS_ClientboundCreateOrUpdateTeamPacket(String teamName, FS_ClientboundCreateOrUpdateTeamPacket.RemoveEntity removeEntity) {
      this.method = FS_ClientboundCreateOrUpdateTeamPacket.Method.REMOVE_ENTITY;
      this.teamName = teamName;
      this.removeEntity = removeEntity;
   }

   public FS_ClientboundCreateOrUpdateTeamPacket.Method getMethod() {
      return this.method;
   }

   public void setMethod(FS_ClientboundCreateOrUpdateTeamPacket.Method method) {
      this.method = method;
   }

   public String getTeamName() {
      return this.teamName;
   }

   public void setTeamName(String teamName) {
      this.teamName = teamName;
   }

   public FS_ClientboundCreateOrUpdateTeamPacket.CreateTeam getCreateTeam() {
      return this.createTeam;
   }

   public void setCreateTeam(FS_ClientboundCreateOrUpdateTeamPacket.CreateTeam createTeam) {
      this.createTeam = createTeam;
   }

   public FS_ClientboundCreateOrUpdateTeamPacket.UpdateTeam getUpdateTeam() {
      return this.updateTeam;
   }

   public void setUpdateTeam(FS_ClientboundCreateOrUpdateTeamPacket.UpdateTeam updateTeam) {
      this.updateTeam = updateTeam;
   }

   public FS_ClientboundCreateOrUpdateTeamPacket.AddEntity getAddEntity() {
      return this.addEntity;
   }

   public void setAddEntity(FS_ClientboundCreateOrUpdateTeamPacket.AddEntity addEntity) {
      this.addEntity = addEntity;
   }

   public FS_ClientboundCreateOrUpdateTeamPacket.RemoveEntity getRemoveEntity() {
      return this.removeEntity;
   }

   public void setRemoveEntity(FS_ClientboundCreateOrUpdateTeamPacket.RemoveEntity removeEntity) {
      this.removeEntity = removeEntity;
   }

   public static class AddEntity {
      private List<String> entities;

      public AddEntity(List<String> entities) {
         this.entities = entities;
      }

      public List<String> getEntities() {
         return this.entities;
      }

      public void setEntities(List<String> entities) {
         this.entities = entities;
      }
   }

   public static class CreateTeam {
      private Component displayName;
      private boolean allowFriendlyFire;
      private boolean canSeeFriendlyInvisibles;
      private FS_NameTagVisibility nameTagVisibility;
      private FS_CollisionRule collisionRule;
      private FS_Color color;
      private Component prefix;
      private Component suffix;
      private List<String> entities;

      public CreateTeam(
         Component displayName,
         boolean allowFriendlyFire,
         boolean canSeeFriendlyInvisibles,
         FS_NameTagVisibility nameTagVisibility,
         FS_CollisionRule collisionRule,
         FS_Color color,
         Component prefix,
         Component suffix,
         List<String> entities
      ) {
         this.displayName = displayName;
         this.allowFriendlyFire = allowFriendlyFire;
         this.canSeeFriendlyInvisibles = canSeeFriendlyInvisibles;
         this.nameTagVisibility = nameTagVisibility;
         this.collisionRule = collisionRule;
         this.color = color;
         this.prefix = prefix;
         this.suffix = suffix;
         this.entities = entities;
      }

      public Component getDisplayName() {
         return this.displayName;
      }

      public void setDisplayName(Component displayName) {
         this.displayName = displayName;
      }

      public boolean isAllowFriendlyFire() {
         return this.allowFriendlyFire;
      }

      public void setAllowFriendlyFire(boolean allowFriendlyFire) {
         this.allowFriendlyFire = allowFriendlyFire;
      }

      public boolean isCanSeeFriendlyInvisibles() {
         return this.canSeeFriendlyInvisibles;
      }

      public void setCanSeeFriendlyInvisibles(boolean canSeeFriendlyInvisibles) {
         this.canSeeFriendlyInvisibles = canSeeFriendlyInvisibles;
      }

      public FS_NameTagVisibility getNameTagVisibility() {
         return this.nameTagVisibility;
      }

      public void setNameTagVisibility(FS_NameTagVisibility nameTagVisibility) {
         this.nameTagVisibility = nameTagVisibility;
      }

      public FS_CollisionRule getCollisionRule() {
         return this.collisionRule;
      }

      public void setCollisionRule(FS_CollisionRule collisionRule) {
         this.collisionRule = collisionRule;
      }

      public FS_Color getColor() {
         return this.color;
      }

      public void setColor(FS_Color color) {
         this.color = color;
      }

      public Component getPrefix() {
         return this.prefix;
      }

      public void setPrefix(Component prefix) {
         this.prefix = prefix;
      }

      public Component getSuffix() {
         return this.suffix;
      }

      public void setSuffix(Component suffix) {
         this.suffix = suffix;
      }

      public List<String> getEntities() {
         return this.entities;
      }

      public void setEntities(List<String> entities) {
         this.entities = entities;
      }
   }

   public static enum Method {
      CREATE_TEAM,
      REMOVE_TEAM,
      UPDATE_TEAM,
      ADD_ENTITY,
      REMOVE_ENTITY;
   }

   public static class RemoveEntity {
      private List<String> entities;

      public RemoveEntity(List<String> entities) {
         this.entities = entities;
      }

      public List<String> getEntities() {
         return this.entities;
      }

      public void setEntities(List<String> entities) {
         this.entities = entities;
      }
   }

   public static class RemoveTeam {
   }

   public static class UpdateTeam {
      private Component displayName;
      private boolean allowFriendlyFire;
      private boolean canSeeFriendlyInvisibles;
      private FS_NameTagVisibility nameTagVisibility;
      private FS_CollisionRule collisionRule;
      private FS_Color color;
      private Component prefix;
      private Component suffix;

      public UpdateTeam(
         Component displayName,
         boolean allowFriendlyFire,
         boolean canSeeFriendlyInvisibles,
         FS_NameTagVisibility nameTagVisibility,
         FS_CollisionRule collisionRule,
         FS_Color color,
         Component prefix,
         Component suffix
      ) {
         this.displayName = displayName;
         this.allowFriendlyFire = allowFriendlyFire;
         this.canSeeFriendlyInvisibles = canSeeFriendlyInvisibles;
         this.nameTagVisibility = nameTagVisibility;
         this.collisionRule = collisionRule;
         this.color = color;
         this.prefix = prefix;
         this.suffix = suffix;
      }

      public Component getDisplayName() {
         return this.displayName;
      }

      public void setDisplayName(Component displayName) {
         this.displayName = displayName;
      }

      public boolean isAllowFriendlyFire() {
         return this.allowFriendlyFire;
      }

      public void setAllowFriendlyFire(boolean allowFriendlyFire) {
         this.allowFriendlyFire = allowFriendlyFire;
      }

      public boolean isCanSeeFriendlyInvisibles() {
         return this.canSeeFriendlyInvisibles;
      }

      public void setCanSeeFriendlyInvisibles(boolean canSeeFriendlyInvisibles) {
         this.canSeeFriendlyInvisibles = canSeeFriendlyInvisibles;
      }

      public FS_NameTagVisibility getNameTagVisibility() {
         return this.nameTagVisibility;
      }

      public void setNameTagVisibility(FS_NameTagVisibility nameTagVisibility) {
         this.nameTagVisibility = nameTagVisibility;
      }

      public FS_CollisionRule getCollisionRule() {
         return this.collisionRule;
      }

      public void setCollisionRule(FS_CollisionRule collisionRule) {
         this.collisionRule = collisionRule;
      }

      public FS_Color getColor() {
         return this.color;
      }

      public void setColor(FS_Color color) {
         this.color = color;
      }

      public Component getPrefix() {
         return this.prefix;
      }

      public void setPrefix(Component prefix) {
         this.prefix = prefix;
      }

      public Component getSuffix() {
         return this.suffix;
      }

      public void setSuffix(Component suffix) {
         this.suffix = suffix;
      }
   }
}
