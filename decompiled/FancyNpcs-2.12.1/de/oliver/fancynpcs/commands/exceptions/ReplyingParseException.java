package de.oliver.fancynpcs.commands.exceptions;

import org.jetbrains.annotations.NotNull;

public final class ReplyingParseException extends RuntimeException {
   @NotNull
   private final Runnable runnable;

   private ReplyingParseException(@NotNull final Runnable runnable) {
      this.runnable = runnable;
   }

   public static ReplyingParseException replying(final Runnable runnable) {
      return new ReplyingParseException(runnable);
   }

   @NotNull
   public Runnable runnable() {
      return this.runnable;
   }
}
