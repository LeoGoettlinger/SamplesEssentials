package org.enginehub.piston;

public interface CommandManagerService {
   String id();

   CommandManager newCommandManager();

   Command.Builder newCommand(String var1);
}
