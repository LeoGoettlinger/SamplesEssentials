package org.enginehub.piston.impl;

import com.google.auto.service.AutoService;
import org.enginehub.piston.Command;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.CommandManagerService;

@AutoService({CommandManagerService.class})
public class CommandManagerServiceImpl implements CommandManagerService {
   @Override
   public String id() {
      return "default-impl";
   }

   @Override
   public CommandManager newCommandManager() {
      return new CommandManagerImpl();
   }

   @Override
   public Command.Builder newCommand(String name) {
      return CommandImpl.builder(name);
   }
}
