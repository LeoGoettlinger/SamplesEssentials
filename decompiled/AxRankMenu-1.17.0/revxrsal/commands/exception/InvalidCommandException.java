package revxrsal.commands.exception;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.core.CommandPath;

@ThrowableFromCommand
public class InvalidCommandException extends RuntimeException {
   @NotNull
   private final CommandPath path;
   @NotNull
   private final String input;

   @NotNull
   public CommandPath getPath() {
      return this.path;
   }

   @NotNull
   public String getInput() {
      return this.input;
   }

   public InvalidCommandException(@NotNull CommandPath path, @NotNull String input) {
      if (path == null) {
         throw new NullPointerException("path is marked non-null but is null");
      } else if (input == null) {
         throw new NullPointerException("input is marked non-null but is null");
      } else {
         this.path = path;
         this.input = input;
      }
   }
}
