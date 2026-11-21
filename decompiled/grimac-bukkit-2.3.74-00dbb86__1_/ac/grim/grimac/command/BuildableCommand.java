package ac.grim.grimac.command;

import ac.grim.grimac.platform.api.manager.cloud.CloudPlatformCommandArguments;
import ac.grim.grimac.platform.api.sender.Sender;
import ac.grim.grimac.shaded.incendo.cloud.CommandManager;

public interface BuildableCommand {
   void register(CommandManager<Sender> var1, CloudPlatformCommandArguments var2);
}
