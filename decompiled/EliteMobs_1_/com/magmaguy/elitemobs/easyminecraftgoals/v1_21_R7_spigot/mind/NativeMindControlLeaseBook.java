package com.magmaguy.elitemobs.easyminecraftgoals.v1_21_R7_spigot.mind;

import com.magmaguy.elitemobs.magmacore.ai.MindControl;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

final class NativeMindControlLeaseBook {
   private static final Comparator<NativeMindBehaviorControl> ORDER = Comparator.comparingInt(NativeMindBehaviorControl::priority)
      .thenComparing(NativeMindBehaviorControl::identifier);
   private final EnumMap<MindControl, NativeMindBehaviorControl> owners = new EnumMap<>(MindControl.class);

   boolean acquire(NativeMindBehaviorControl requester, Set<MindControl> requested) {
      if (requested.isEmpty()) {
         return true;
      } else {
         Set<NativeMindBehaviorControl> displaced = new LinkedHashSet<>();

         for (MindControl control : requested) {
            NativeMindBehaviorControl current = this.owners.get(control);
            if (current != null && current != requester) {
               if (ORDER.compare(current, requester) <= 0) {
                  return false;
               }

               displaced.add(current);
            }
         }

         List<NativeMindBehaviorControl> ordered = new ArrayList<>(displaced);
         ordered.sort(ORDER);

         for (NativeMindBehaviorControl current : ordered) {
            current.preempt();
         }

         for (MindControl controlx : requested) {
            this.owners.put(controlx, requester);
         }

         return true;
      }
   }

   void release(NativeMindBehaviorControl owner) {
      this.owners.entrySet().removeIf(entry -> entry.getValue() == owner);
   }

   boolean holds(NativeMindBehaviorControl owner, MindControl control) {
      return this.owners.get(control) == owner;
   }

   void clear() {
      this.owners.clear();
   }
}
