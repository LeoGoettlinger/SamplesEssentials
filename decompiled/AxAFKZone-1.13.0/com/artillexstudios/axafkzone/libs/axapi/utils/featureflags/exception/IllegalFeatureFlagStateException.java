package com.artillexstudios.axafkzone.libs.axapi.utils.featureflags.exception;

public class IllegalFeatureFlagStateException extends IllegalStateException {
   public IllegalFeatureFlagStateException(String featureFlag) {
      super(featureFlag);
   }
}
