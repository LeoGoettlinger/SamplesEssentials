package org.enginehub.piston;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.List;

public interface CommandParseResult {
   ImmutableList<Command> getExecutionPath();

   default Command getPrimaryCommand() {
      return (Command)Iterables.getLast(this.getExecutionPath());
   }

   ImmutableList<ArgBinding> getBoundArguments();

   default List<String> getOriginalArguments() {
      return Lists.transform(this.getBoundArguments(), ArgBinding::getInput);
   }

   CommandParameters getParameters();
}
