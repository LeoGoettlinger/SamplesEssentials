package net.ess3.provider;

import java.util.Map;
import org.bukkit.command.Command;

public interface KnownCommandsProvider extends Provider {
   Map<String, Command> getKnownCommands();
}
