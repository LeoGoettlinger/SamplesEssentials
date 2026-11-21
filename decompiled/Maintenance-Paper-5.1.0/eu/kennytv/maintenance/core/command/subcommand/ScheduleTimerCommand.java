package eu.kennytv.maintenance.core.command.subcommand;

import eu.kennytv.maintenance.core.MaintenancePlugin;
import eu.kennytv.maintenance.core.command.CommandInfo;
import eu.kennytv.maintenance.core.util.SenderInfo;
import java.time.Duration;

public final class ScheduleTimerCommand extends CommandInfo {
   public ScheduleTimerCommand(MaintenancePlugin plugin) {
      super(plugin, "timer");
   }

   @Override
   public void execute(SenderInfo sender, String[] args) {
      if (!this.checkArgs(sender, args, 3)) {
         Duration enableIn = this.plugin.getCommandManager().parseDurationAndCheckTask(sender, args[1]);
         Duration maintenanceDuration = this.plugin.getCommandManager().parseDurationAndCheckTask(sender, args[2], false);
         if (enableIn == null || maintenanceDuration == null) {
            sender.send(this.getHelpMessage());
         } else if (this.plugin.isMaintenance()) {
            sender.send(this.getMessage("alreadyEnabled", new String[0]));
         } else {
            this.plugin.scheduleMaintenanceRunnable(enableIn, maintenanceDuration);
            sender.send(
               this.getMessage(
                  "scheduletimerStarted",
                  new String[]{
                     "%TIME%", this.plugin.getRunnable().getTime(), "%DURATION%", this.plugin.getFormattedTime((int)maintenanceDuration.getSeconds())
                  }
               )
            );
         }
      }
   }
}
