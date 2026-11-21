package org.enginehub.piston.exception;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import org.enginehub.piston.Command;

public class ConditionFailedException extends CommandException {
   public ConditionFailedException(ImmutableList<Command> commands) {
      super(TextComponent.of("Condition failed: " + ((Command)Iterables.getLast(commands)).getCondition()), commands);
   }

   public Command.Condition getCondition() {
      return ((Command)Iterables.getLast(this.commands)).getCondition();
   }
}
