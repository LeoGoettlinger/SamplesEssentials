package de.theredend2000.advancedhunt.lib.cloud.exception;

import de.theredend2000.advancedhunt.lib.cloud.component.CommandComponent;
import java.util.List;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = API.Status.STABLE
)
public final class NoSuchCommandException extends CommandParseException {
   private final String suppliedCommand;

   @API(
      status = API.Status.INTERNAL,
      consumers = {"de.theredend2000.advancedhunt.lib.cloud.*"}
   )
   public NoSuchCommandException(@NonNull final Object commandSender, @NonNull final List<CommandComponent<?>> currentChain, @NonNull final String command) {
      super(commandSender, currentChain);
      this.suppliedCommand = command;
   }

   @Override
   public String getMessage() {
      StringBuilder builder = new StringBuilder();

      for (CommandComponent<?> commandComponent : this.currentChain()) {
         if (commandComponent != null) {
            builder.append(" ").append(commandComponent.name());
         }
      }

      return String.format("Unrecognized command input '%s' following chain%s", this.suppliedCommand, builder.toString());
   }

   @NonNull
   public String suppliedCommand() {
      return this.suppliedCommand;
   }

   @Override
   public synchronized Throwable fillInStackTrace() {
      return this;
   }

   @Override
   public synchronized Throwable initCause(final Throwable cause) {
      return this;
   }
}
