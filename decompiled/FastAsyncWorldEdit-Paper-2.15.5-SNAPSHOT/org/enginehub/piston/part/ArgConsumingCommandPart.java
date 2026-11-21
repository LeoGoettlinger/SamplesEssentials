package org.enginehub.piston.part;

import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;

public interface ArgConsumingCommandPart extends CommandPart {
   TranslatableComponent getArgumentName();
}
