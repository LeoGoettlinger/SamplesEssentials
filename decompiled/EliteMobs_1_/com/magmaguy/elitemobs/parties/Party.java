package com.magmaguy.elitemobs.parties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class Party {
   public static final int MAX_MEMBERS = 5;
   private final UUID id = UUID.randomUUID();
   private final LinkedHashSet<UUID> members = new LinkedHashSet<>();
   private UUID leader;

   Party(UUID creator) {
      this.leader = creator;
      this.members.add(creator);
   }

   public UUID getId() {
      return this.id;
   }

   public UUID getLeader() {
      return this.leader;
   }

   public Set<UUID> getMembers() {
      return Collections.unmodifiableSet(this.members);
   }

   public List<UUID> getMembersInDisplayOrder() {
      List<UUID> ordered = new ArrayList<>();
      ordered.add(this.leader);

      for (UUID member : this.members) {
         if (!member.equals(this.leader)) {
            ordered.add(member);
         }
      }

      return ordered;
   }

   public boolean isFull() {
      return this.members.size() >= 5;
   }

   boolean addAdmittedDungeonMember(UUID playerId) {
      return this.members.add(playerId);
   }

   boolean addMember(UUID playerId) {
      return !this.isFull() && this.members.add(playerId);
   }

   boolean removeMember(UUID playerId) {
      boolean removed = this.members.remove(playerId);
      if (!removed) {
         return false;
      } else {
         if (playerId.equals(this.leader)) {
            this.leader = this.members.stream().findFirst().orElse(null);
         }

         return true;
      }
   }

   boolean isEmpty() {
      return this.members.isEmpty();
   }
}
