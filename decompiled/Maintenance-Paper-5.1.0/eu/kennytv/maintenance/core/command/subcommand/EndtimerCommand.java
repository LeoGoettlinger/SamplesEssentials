package eu.kennytv.maintenance.core.command.subcommand;

import eu.kennytv.maintenance.core.MaintenancePlugin;
import eu.kennytv.maintenance.core.command.CommandInfo;
import eu.kennytv.maintenance.core.util.DiscordWebhook;
import eu.kennytv.maintenance.core.util.SenderInfo;
import java.time.Duration;
import net.kyori.adventure.text.Component;

public final class EndtimerCommand extends CommandInfo {
   public EndtimerCommand(MaintenancePlugin plugin) {
      super(plugin, "timer");
   }

   @Override
   public void execute(SenderInfo sender, String[] args) {
      if (!this.checkArgs(sender, args, 2)) {
         Duration duration = this.plugin.getCommandManager().parseDurationAndCheckTask(sender, args[1]);
         if (duration == null) {
            sender.send(this.getHelpMessage());
         } else if (!this.plugin.isMaintenance()) {
            sender.send(this.getMessage("alreadyDisabled", new String[0]));
         } else {
            this.plugin.startMaintenanceRunnable(duration, false);
            Component message = this.getMessage("endtimerStarted", new String[]{"%TIME%", this.plugin.getRunnable().getTime()});
            sender.send(message);
            this.plugin
               .sendWebhookMessage(
                  "webhookEndtimerStarted",
                  DiscordWebhook.EventType.ENDTIMER_STARTED,
                  "%TIME%",
                  this.plugin.getRunnable().getTime(),
                  "%TIMESTAMP%",
                  this.plugin.getTargetTimestamp(duration)
               );
         }
      }
   }
}
