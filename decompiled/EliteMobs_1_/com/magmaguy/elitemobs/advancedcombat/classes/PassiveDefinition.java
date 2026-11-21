package com.magmaguy.elitemobs.advancedcombat.classes;

import java.util.Objects;
import java.util.regex.Pattern;

public record PassiveDefinition(String id, String description) {
   private static final Pattern ID_PATTERN = Pattern.compile("[a-z][a-z0-9_.-]*");

   public PassiveDefinition(String id, String description) {
      Objects.requireNonNull(id, "id");
      Objects.requireNonNull(description, "description");
      if (!ID_PATTERN.matcher(id).matches()) {
         throw new IllegalArgumentException("Passive id must be a stable lowercase identifier: " + id);
      } else if (description.isBlank()) {
         throw new IllegalArgumentException("description must not be blank");
      } else if (!description.equals(description.strip())) {
         throw new IllegalArgumentException("description must not have surrounding whitespace");
      } else {
         this.id = id;
         this.description = description;
      }
   }
}
