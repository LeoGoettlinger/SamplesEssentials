package me.byteful.plugin.leveltools.api;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class RegistrationResult {
   private final boolean success;
   private final String profileId;
   private final String profileType;
   private final String errorMessage;

   private RegistrationResult(boolean success, @NotNull String profileId, @NotNull String profileType, @Nullable String errorMessage) {
      this.success = success;
      this.profileId = profileId;
      this.profileType = profileType;
      this.errorMessage = errorMessage;
   }

   @NotNull
   public static RegistrationResult success(@NotNull String profileId, @NotNull String profileType) {
      return new RegistrationResult(true, profileId, profileType, null);
   }

   @NotNull
   public static RegistrationResult alreadyExists(@NotNull String profileId, @NotNull String profileType) {
      return new RegistrationResult(false, profileId, profileType, profileType + " profile '" + profileId + "' already exists");
   }

   @NotNull
   public static RegistrationResult missingReference(@NotNull String profileId, @NotNull String refType, @NotNull String refId) {
      return new RegistrationResult(false, profileId, "item", "Item profile '" + profileId + "' references unknown " + refType + " profile: '" + refId + "'");
   }

   @NotNull
   public static RegistrationResult materialConflict(@NotNull String profileId, @NotNull Material material, @NotNull String existingId) {
      return new RegistrationResult(false, profileId, "item", "Material " + material.name() + " already registered to profile '" + existingId + "'");
   }

   @NotNull
   public static RegistrationResult error(@NotNull String profileId, @NotNull String profileType, @NotNull String errorMessage) {
      return new RegistrationResult(false, profileId, profileType, errorMessage);
   }

   public boolean isSuccess() {
      return this.success;
   }

   @NotNull
   public String getProfileId() {
      return this.profileId;
   }

   @NotNull
   public String getProfileType() {
      return this.profileType;
   }

   @Nullable
   public String getErrorMessage() {
      return this.errorMessage;
   }

   @Override
   public String toString() {
      return this.success
         ? "RegistrationResult{success=true, profileId='" + this.profileId + "', profileType='" + this.profileType + "'}"
         : "RegistrationResult{success=false, profileId='" + this.profileId + "', profileType='" + this.profileType + "', error='" + this.errorMessage + "'}";
   }
}
