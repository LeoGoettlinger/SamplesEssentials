package eu.kennytv.maintenance.core.command.subcommand;

import eu.kennytv.maintenance.core.MaintenancePlugin;
import eu.kennytv.maintenance.core.command.CommandInfo;
import eu.kennytv.maintenance.core.util.SenderInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class ToggleCommand extends CommandInfo {
   public ToggleCommand(MaintenancePlugin plugin) {
      super(plugin, "toggle");
   }

   @Override
   public void execute(SenderInfo sender, String[] args) {
      if (args.length != 0 && args.length <= 2) {
         boolean maintenance = args[0].equalsIgnoreCase("on");
         String mode;
         if (maintenance && args.length == 2) {
            mode = args[1].toLowerCase(Locale.ROOT);
         } else if (!maintenance && args.length == 1) {
            mode = null;
         } else {
            if (!maintenance) {
               sender.send(this.getHelpMessage());
               return;
            }

            mode = null;
         }

         if (maintenance != this.plugin.isMaintenance()) {
            this.plugin.setMaintenance(maintenance, mode);
         } else if (mode != null) {
            this.plugin.getSettings().setActiveMode(mode);
         } else {
            sender.send(this.getMessage(maintenance ? "alreadyEnabled" : "alreadyDisabled", new String[0]));
         }
      } else {
         sender.send(this.getHelpMessage());
      }
   }

   @Override
   public List<String> getTabCompletion(SenderInfo sender, String[] args) {
      if (args.length == 2 && args[0].equalsIgnoreCase("on")) {
         List<String> modes = new ArrayList<>(this.getSettings().getPingMessages().getKeys());
         modes.remove("default");
         return modes;
      } else {
         return Collections.emptyList();
      }
   }
}
