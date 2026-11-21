package de.theredend2000.advancedhunt.lib.xseries.profiles.exceptions;

import org.jetbrains.annotations.NotNull;

public final class UnknownPlayerException extends InvalidProfileException {
   private final Object unknownObject;

   public UnknownPlayerException(Object unknownObject, String message) {
      super(unknownObject.toString(), message);
      this.unknownObject = unknownObject;
   }

   @NotNull
   public Object getUnknownObject() {
      return this.unknownObject;
   }
}
