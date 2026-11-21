package com.magmaguy.elitemobs.advancedcombat.progression;

import java.util.Arrays;
import java.util.Optional;

public enum InputProfile {
   JAVA_HOTBAR_LAYER("java_hotbar_layer");

   public static final InputProfile DEFAULT = JAVA_HOTBAR_LAYER;
   private final String storedId;

   private InputProfile(String storedId) {
      this.storedId = storedId;
   }

   public String storedId() {
      return this.storedId;
   }

   public static Optional<InputProfile> fromStoredId(String storedId) {
      return storedId == null ? Optional.empty() : Arrays.stream(values()).filter(profile -> profile.storedId.equals(storedId)).findFirst();
   }
}
