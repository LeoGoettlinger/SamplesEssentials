package org.enginehub.piston;

import com.google.common.collect.ImmutableSet;
import org.enginehub.piston.part.CommandPart;

public interface ArgBinding {
   String getInput();

   default boolean isExactMatch(CommandPart part) {
      return true;
   }

   ImmutableSet<CommandPart> getParts();
}
