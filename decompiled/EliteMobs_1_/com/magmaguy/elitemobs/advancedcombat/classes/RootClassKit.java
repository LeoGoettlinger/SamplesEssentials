package com.magmaguy.elitemobs.advancedcombat.classes;

import java.util.Objects;

public record RootClassKit(ClassResourceType resourceType, AbilityDefinition mobility) {
   public RootClassKit(ClassResourceType resourceType, AbilityDefinition mobility) {
      resourceType = Objects.requireNonNull(resourceType, "resourceType");
      mobility = Objects.requireNonNull(mobility, "mobility");
      if (mobility.slot() != AbilitySlot.MOBILITY) {
         throw new IllegalArgumentException("A root class kit must contain a Mobility ability");
      } else {
         this.resourceType = resourceType;
         this.mobility = mobility;
      }
   }
}
