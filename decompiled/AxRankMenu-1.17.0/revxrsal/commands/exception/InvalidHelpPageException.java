package revxrsal.commands.exception;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.help.CommandHelp;

@ThrowableFromCommand
public class InvalidHelpPageException extends RuntimeException {
   @NotNull
   private final CommandHelp<?> commandHelp;
   private final int page;
   private final int elementsPerPage;

   public int getPageCount() {
      return this.commandHelp.getPageSize(this.elementsPerPage);
   }

   @NotNull
   public CommandHelp<?> getCommandHelp() {
      return this.commandHelp;
   }

   public int getPage() {
      return this.page;
   }

   public int getElementsPerPage() {
      return this.elementsPerPage;
   }

   public InvalidHelpPageException(@NotNull CommandHelp<?> commandHelp, int page, int elementsPerPage) {
      if (commandHelp == null) {
         throw new NullPointerException("commandHelp is marked non-null but is null");
      } else {
         this.commandHelp = commandHelp;
         this.page = page;
         this.elementsPerPage = elementsPerPage;
      }
   }
}
