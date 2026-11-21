package org.enginehub.piston.exception;

import com.google.common.collect.ImmutableList;
import com.sk89q.worldedit.util.formatting.text.Component;
import org.enginehub.piston.Command;

public class StopExecutionException extends CommandException {
   public StopExecutionException(Component message) {
      this(message, ImmutableList.of());
   }

   public StopExecutionException(Component message, ImmutableList<Command> commands) {
      super(message, commands);
   }
}
