package eu.kennytv.maintenance.core.command.subcommand;

import eu.kennytv.maintenance.core.MaintenancePlugin;
import eu.kennytv.maintenance.core.command.CommandInfo;
import eu.kennytv.maintenance.core.util.SenderInfo;

public final class DebugCommand extends CommandInfo {
   public DebugCommand(MaintenancePlugin plugin) {
      super(plugin, "debug", false);
   }

   @Override
   public void execute(SenderInfo sender, String[] args) {
      if (!this.checkArgs(sender, args, 1)) {
         this.plugin.setDebug(!this.plugin.isDebug());
         sender.sendPrefixedRich("<gray>Debug mode is now " + (this.plugin.isDebug() ? "<green>enabled" : "<red>disabled"));
      }
   }
}
