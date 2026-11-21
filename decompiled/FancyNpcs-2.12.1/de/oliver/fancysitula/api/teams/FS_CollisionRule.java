package de.oliver.fancysitula.api.teams;

public enum FS_CollisionRule {
   ALWAYS("always"),
   NEVER("never"),
   PUSH_OTHER_TEAMS("pushOtherTeams"),
   PUSH_OWN_TEAM("pushOwnTeam");

   private final String name;

   private FS_CollisionRule(String name) {
      this.name = name;
   }

   public String getName() {
      return this.name;
   }
}
