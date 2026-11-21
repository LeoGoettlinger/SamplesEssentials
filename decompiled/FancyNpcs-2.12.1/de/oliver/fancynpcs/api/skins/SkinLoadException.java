package de.oliver.fancynpcs.api.skins;

import org.jetbrains.annotations.NotNull;

public final class SkinLoadException extends RuntimeException {
   @NotNull
   private final SkinLoadException.Reason reason;

   public SkinLoadException(@NotNull SkinLoadException.Reason reason, @NotNull String message) {
      super(message);
      this.reason = reason;
   }

   @NotNull
   public SkinLoadException.Reason getReason() {
      return this.reason;
   }

   public static enum Reason {
      INVALID_URL,
      INVALID_FILE,
      INVALID_PLACEHOLDER,
      INVALID_USERNAME;
   }
}
