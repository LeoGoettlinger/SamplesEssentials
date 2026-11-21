package com.magmaguy.elitemobs.npcs.chatter;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

class NPCProximityState {
   private final Set<NPCProximityKey> nearbyPairs = new HashSet<>();

   NPCProximityState.ProximityChanges update(Set<NPCProximityKey> visiblePairs) {
      Set<NPCProximityKey> entered = new HashSet<>();

      for (NPCProximityKey visiblePair : visiblePairs) {
         if (!this.nearbyPairs.contains(visiblePair)) {
            entered.add(visiblePair);
         }
      }

      Set<NPCProximityKey> left = new HashSet<>();
      Iterator<NPCProximityKey> iterator = this.nearbyPairs.iterator();

      while (iterator.hasNext()) {
         NPCProximityKey trackedPair = iterator.next();
         if (!visiblePairs.contains(trackedPair)) {
            left.add(trackedPair);
            iterator.remove();
         }
      }

      this.nearbyPairs.addAll(visiblePairs);
      return new NPCProximityState.ProximityChanges(entered, left);
   }

   void clear() {
      this.nearbyPairs.clear();
   }

   record ProximityChanges(Set<NPCProximityKey> entered, Set<NPCProximityKey> left) {
   }
}
