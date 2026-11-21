package com.artillexstudios.axafkzone.libs.lamp.exception;

import com.artillexstudios.axafkzone.libs.lamp.command.CommandCategory;
import org.jetbrains.annotations.NotNull;

@ThrowableFromCommand
public class NoSubcommandSpecifiedException extends RuntimeException {
   @NotNull
   private final CommandCategory category;

   @NotNull
   public CommandCategory getCategory() {
      return this.category;
   }

   public NoSubcommandSpecifiedException(@NotNull CommandCategory category) {
      if (category == null) {
         throw new NullPointerException("category is marked non-null but is null");
      } else {
         this.category = category;
      }
   }
}
