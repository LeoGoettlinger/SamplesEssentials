package de.theredend2000.advancedhunt.lib.xseries.profiles.builder;

import de.theredend2000.advancedhunt.lib.xseries.profiles.exceptions.ProfileChangeException;

public final class ProfileFallback<T> {
   private final ProfileInstruction<T> instruction;
   private T object;
   private final ProfileChangeException error;

   public ProfileFallback(ProfileInstruction<T> instruction, T object, ProfileChangeException error) {
      this.instruction = instruction;
      this.object = object;
      this.error = error;
   }

   public T getObject() {
      return this.object;
   }

   public ProfileInstruction<T> getInstruction() {
      return this.instruction;
   }

   public void setObject(T object) {
      this.object = object;
   }

   public ProfileChangeException getError() {
      return this.error;
   }
}
