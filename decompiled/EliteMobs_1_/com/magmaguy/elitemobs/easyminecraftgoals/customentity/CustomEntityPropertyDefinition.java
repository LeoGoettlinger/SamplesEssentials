package com.magmaguy.elitemobs.easyminecraftgoals.customentity;

import java.util.Objects;

public record CustomEntityPropertyDefinition(String identifier, CustomEntityPropertyType type) {
   public CustomEntityPropertyDefinition(String identifier, CustomEntityPropertyType type) {
      if (identifier != null && !identifier.isBlank()) {
         Objects.requireNonNull(type, "type");
         this.identifier = identifier;
         this.type = type;
      } else {
         throw new IllegalArgumentException("Property identifier cannot be blank");
      }
   }
}
