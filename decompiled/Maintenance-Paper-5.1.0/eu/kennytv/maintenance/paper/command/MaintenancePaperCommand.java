package eu.kennytv.maintenance.paper.command;

import eu.kennytv.maintenance.core.Settings;
import eu.kennytv.maintenance.core.command.MaintenanceCommand;
import eu.kennytv.maintenance.paper.MaintenancePaperPlugin;
import eu.kennytv.maintenance.paper.util.PaperSenderInfo;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public final class MaintenancePaperCommand extends MaintenanceCommand implements CommandExecutor, TabCompleter {
   public MaintenancePaperCommand(MaintenancePaperPlugin plugin, Settings settings) {
      super(plugin, settings);
      this.registerCommands();
   }

   public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
      this.execute(new PaperSenderInfo(sender), args);
      return true;
   }

   public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
      return this.getSuggestions(new PaperSenderInfo(sender), args);
   }
}
