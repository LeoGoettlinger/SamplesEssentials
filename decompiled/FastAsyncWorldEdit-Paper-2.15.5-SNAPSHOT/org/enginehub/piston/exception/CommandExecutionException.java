package org.enginehub.piston.exception;

import com.google.common.collect.ImmutableList;
import org.enginehub.piston.Command;

public class CommandExecutionException extends CommandException {
   public CommandExecutionException(Throwable cause, ImmutableList<Command> commands) {
      super(cause, commands);
   }
}
