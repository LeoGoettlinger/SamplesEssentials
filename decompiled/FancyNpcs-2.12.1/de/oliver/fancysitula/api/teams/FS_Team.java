package de.oliver.fancysitula.api.teams;

import de.oliver.fancysitula.api.packets.FS_Color;
import java.util.List;
import net.kyori.adventure.text.Component;

public class FS_Team {
   private String teamName;
   private Component displayName;
   private boolean allowFriendlyFire;
   private boolean canSeeFriendlyInvisibles;
   private FS_NameTagVisibility nameTagVisibility;
   private FS_CollisionRule collisionRule;
   private FS_Color color;
   private Component prefix;
   private Component suffix;
   private List<String> entities;

   public FS_Team(
      String teamName,
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
      this.teamName = teamName;
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

   public String getTeamName() {
      return this.teamName;
   }

   public void setTeamName(String teamName) {
      this.teamName = teamName;
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
