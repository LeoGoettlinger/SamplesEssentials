package org.enginehub.piston.gen;

import java.util.Collection;
import org.enginehub.piston.CommandManager;

public interface CommandRegistration<CI> {
   CommandRegistration<CI> commandManager(CommandManager var1);

   CommandRegistration<CI> containerInstance(CI var1);

   CommandRegistration<CI> listeners(Collection<CommandCallListener> var1);

   void build();
}
