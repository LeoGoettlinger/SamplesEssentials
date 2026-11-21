package com.magmaguy.elitemobs.advancedcombat.abilities;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class AbilityRuntimeEvidenceLedger {
   private static final int MAX_PER_CASTER = 512;
   private final Map<UUID, Deque<AbilityRuntimeObservation>> observations = new HashMap<>();

   public synchronized void record(AbilityRuntimeObservation observation) {
      Deque<AbilityRuntimeObservation> caster = this.observations.computeIfAbsent(observation.casterId(), ignored -> new ArrayDeque<>());

      while (caster.size() >= 512) {
         caster.removeFirst();
      }

      caster.addLast(observation);
   }

   public synchronized List<AbilityRuntimeObservation> snapshot(UUID casterId) {
      Deque<AbilityRuntimeObservation> caster = this.observations.get(casterId);
      return caster == null ? List.of() : List.copyOf(caster);
   }

   public synchronized List<AbilityRuntimeObservation> drain(UUID casterId) {
      Deque<AbilityRuntimeObservation> caster = this.observations.remove(casterId);
      return caster == null ? List.of() : List.copyOf(new ArrayList<>(caster));
   }

   public synchronized void clear() {
      this.observations.clear();
   }

   public synchronized void discard(UUID casterId) {
      this.observations.remove(casterId);
   }
}
