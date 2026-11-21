package net.milkbowl.vault2.helper.subject;

import java.util.UUID;

public class Subject {
   private final String identifier;
   private final String displayIdentifier;
   private final SubjectType type;

   private Subject(final String identifier, final String displayIdentifier, final SubjectType type) {
      this.identifier = identifier;
      this.displayIdentifier = displayIdentifier;
      this.type = type;
   }

   public String identifier() {
      return this.identifier;
   }

   public UUID asUUID() {
      return UUID.fromString(this.identifier);
   }

   public String displayIdentifier() {
      return this.displayIdentifier;
   }

   public SubjectType type() {
      return this.type;
   }

   public static Subject player(final UUID uuid, final String username) {
      return new Subject(uuid.toString(), username, SubjectType.PLAYER);
   }

   public static Subject group(final String groupName) {
      return new Subject(groupName, groupName, SubjectType.GROUP);
   }
}
