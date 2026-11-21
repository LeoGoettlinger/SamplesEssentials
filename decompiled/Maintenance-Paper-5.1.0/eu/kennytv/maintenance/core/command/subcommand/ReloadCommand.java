package eu.kennytv.maintenance.core.command.subcommand;

import eu.kennytv.maintenance.core.MaintenancePlugin;
import eu.kennytv.maintenance.core.command.CommandInfo;
import eu.kennytv.maintenance.core.util.SenderInfo;

public final class ReloadCommand extends CommandInfo {
   public ReloadCommand(MaintenancePlugin plugin) {
      super(plugin, "reload");
   }

   @Override
   public void execute(SenderInfo sender, String[] args) {
      if (!this.checkArgs(sender, args, 1)) {
         this.getSettings().reloadConfigs();
         sender.send(this.getMessage("reload", new String[0]));
      }
   }
}
