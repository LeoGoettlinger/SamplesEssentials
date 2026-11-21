package com.magmaguy.elitemobs.advancedcombat.classes;

import java.util.Objects;
import java.util.regex.Pattern;

public record AbilityDefinition(String id, String displayName, AbilitySlot slot, String description) {
   private static final Pattern ID_PATTERN = Pattern.compile("[a-z][a-z0-9_.-]*");

   public AbilityDefinition(String id, String displayName, AbilitySlot slot, String description) {
      id = requireId(id);
      displayName = requireText(displayName, "displayName");
      slot = Objects.requireNonNull(slot, "slot");
      description = requireText(description, "description");
      this.id = id;
      this.displayName = displayName;
      this.slot = slot;
      this.description = description;
   }

   private static String requireId(String id) {
      String value = requireText(id, "id");
      if (!ID_PATTERN.matcher(value).matches()) {
         throw new IllegalArgumentException("Ability id must be a stable lowercase identifier: " + value);
      } else {
         return value;
      }
   }

   private static String requireText(String value, String field) {
      Objects.requireNonNull(value, field);
      if (value.isBlank()) {
         throw new IllegalArgumentException(field + " must not be blank");
      } else if (!value.equals(value.strip())) {
         throw new IllegalArgumentException(field + " must not have surrounding whitespace");
      } else {
         return value;
      }
   }
}
