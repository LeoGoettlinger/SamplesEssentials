package org.enginehub.piston.exception;

import com.google.common.collect.ImmutableList;
import com.sk89q.worldedit.util.formatting.text.TextComponent;

public class NoSuchCommandException extends CommandException {
   private final String requestedCommand;

   public NoSuchCommandException(String requestedCommand) {
      super(TextComponent.of("No such command: " + requestedCommand), ImmutableList.of());
      this.requestedCommand = requestedCommand;
   }

   public String getRequestedCommand() {
      return this.requestedCommand;
   }
}
