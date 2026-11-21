package com.magmaguy.elitemobs.advancedcombat.minions;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class AbyssalGateOwnershipLedger {
   private final Map<UUID, Set<GateBlockPosition>> blocksByGate = new LinkedHashMap<>();
   private final Map<GateBlockPosition, Set<UUID>> gatesByBlock = new HashMap<>();

   public void reserve(UUID gateId, Collection<GateBlockPosition> blocks) {
      Objects.requireNonNull(gateId, "gateId");
      Objects.requireNonNull(blocks, "blocks");
      if (this.blocksByGate.containsKey(gateId)) {
         throw new IllegalStateException("Gate is already reserved: " + gateId);
      } else {
         LinkedHashSet<GateBlockPosition> unique = new LinkedHashSet<>(blocks);
         if (unique.isEmpty()) {
            throw new IllegalArgumentException("A gate needs at least one block");
         } else {
            this.blocksByGate.put(gateId, Set.copyOf(unique));

            for (GateBlockPosition block : unique) {
               this.gatesByBlock.computeIfAbsent(block, ignored -> new LinkedHashSet<>()).add(gateId);
            }
         }
      }
   }

   public Set<GateBlockPosition> release(UUID gateId) {
      Objects.requireNonNull(gateId, "gateId");
      Set<GateBlockPosition> blocks = this.blocksByGate.remove(gateId);
      if (blocks == null) {
         return Set.of();
      } else {
         LinkedHashSet<GateBlockPosition> unowned = new LinkedHashSet<>();

         for (GateBlockPosition block : blocks) {
            Set<UUID> owners = this.gatesByBlock.get(block);
            if (owners != null && owners.remove(gateId) && owners.isEmpty()) {
               this.gatesByBlock.remove(block);
               unowned.add(block);
            }
         }

         return Set.copyOf(unowned);
      }
   }

   public boolean isOwned(GateBlockPosition position) {
      Set<UUID> owners = this.gatesByBlock.get(Objects.requireNonNull(position, "position"));
      return owners != null && !owners.isEmpty();
   }

   public boolean isEmpty() {
      return this.blocksByGate.isEmpty() && this.gatesByBlock.isEmpty();
   }
}
