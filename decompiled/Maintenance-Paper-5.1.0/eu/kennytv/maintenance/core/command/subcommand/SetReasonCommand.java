package eu.kennytv.maintenance.core.command.subcommand;

import eu.kennytv.maintenance.core.MaintenancePlugin;
import eu.kennytv.maintenance.core.command.CommandInfo;
import eu.kennytv.maintenance.core.util.SenderInfo;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class SetReasonCommand extends CommandInfo {
   public SetReasonCommand(MaintenancePlugin plugin) {
      super(plugin, "setreason");
   }

   @Override
   public void execute(SenderInfo sender, String[] args) {
      if (args.length < 2) {
         sender.send(this.getHelpMessage());
      } else {
         String value = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
         String reason;
         if (!value.equalsIgnoreCase("none") && !value.equalsIgnoreCase("clear") && !value.equals("-")) {
            reason = value;
         } else {
            reason = null;
         }

         this.getSettings().setActiveReason(reason);
         sender.send(this.getMessage("setReason", new String[]{"%REASON%", reason == null ? "-" : reason}));
      }
   }

   @Override
   public List<String> getTabCompletion(SenderInfo sender, String[] args) {
      return args.length != 2 ? Collections.emptyList() : Collections.singletonList("none");
   }
}
