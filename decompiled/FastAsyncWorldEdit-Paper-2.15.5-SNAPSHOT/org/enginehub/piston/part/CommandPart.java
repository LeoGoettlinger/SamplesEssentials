package org.enginehub.piston.part;

import com.sk89q.worldedit.util.formatting.text.Component;
import org.enginehub.piston.CommandParameters;

public interface CommandPart {
   default boolean in(CommandParameters parameters) {
      return parameters.has(this);
   }

   Component getTextRepresentation();

   Component getDescription();

   boolean isRequired();
}
