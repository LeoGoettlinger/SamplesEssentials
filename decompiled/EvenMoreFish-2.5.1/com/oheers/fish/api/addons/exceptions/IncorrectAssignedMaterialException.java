package com.oheers.fish.api.addons.exceptions;

public class IncorrectAssignedMaterialException extends Exception {
   private final String id;
   private final String configLocation;

   public IncorrectAssignedMaterialException(String configLocation, String id) {
      super(String.format("%s has an incorrect assigned material: %s", configLocation, id));
      this.id = id;
      this.configLocation = configLocation;
   }

   public String getId() {
      return this.id;
   }

   public String getConfigLocation() {
      return this.configLocation;
   }
}
