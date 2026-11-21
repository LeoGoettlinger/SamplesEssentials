package eu.kennytv.maintenance.core.command.subcommand;

import eu.kennytv.maintenance.core.MaintenancePlugin;
import eu.kennytv.maintenance.core.command.CommandInfo;
import eu.kennytv.maintenance.core.runnable.MaintenanceRunnable;
import eu.kennytv.maintenance.core.util.SenderInfo;
import net.kyori.adventure.text.Component;

public final class StatusCommand extends CommandInfo {
   public StatusCommand(MaintenancePlugin plugin) {
      super(plugin, "status");
   }

   @Override
   public void execute(SenderInfo sender, String[] args) {
      MaintenanceRunnable task = this.plugin.getCurrentTask();
      if (task != null) {
         String key = task.shouldEnable() ? "maintenanceStatusOffWithStartTimer" : "maintenanceStatusOnWithEndTimer";
         sender.send(this.getMessage(key, new String[]{"%TIME%", task.getTime()}));
      } else {
         sender.send(this.getMessage(this.plugin.isMaintenance() ? "maintenanceStatusOn" : "maintenanceStatusOff", new String[0]));
      }
   }

   @Override
   public Component getHelpMessage() {
      return this.getMessage("helpGlobalStatus", new String[0]);
   }
}
