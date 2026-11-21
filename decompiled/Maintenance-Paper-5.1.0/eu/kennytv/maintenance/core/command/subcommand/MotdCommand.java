package eu.kennytv.maintenance.core.command.subcommand;

import eu.kennytv.maintenance.core.MaintenancePlugin;
import eu.kennytv.maintenance.core.command.CommandInfo;
import eu.kennytv.maintenance.core.config.ConfigSection;
import eu.kennytv.maintenance.core.util.SenderInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.jetbrains.annotations.Nullable;

public final class MotdCommand extends CommandInfo {
   public MotdCommand(MaintenancePlugin plugin) {
      super(plugin, "motd");
   }

   @Override
   public void execute(SenderInfo sender, String[] args) {
      int indexOffset = 1;
      boolean timer = false;
      if (args.length > indexOffset && args[indexOffset].equalsIgnoreCase("timer")) {
         if (!this.getSettings().hasTimerSpecificPingMessages()) {
            sender.send(this.getMessage("timerMotdDisabled", new String[0]));
            return;
         }

         timer = true;
         indexOffset++;
      }

      if (args.length > indexOffset + 1) {
         sender.send(this.getHelpMessage());
      } else {
         String mode = args.length == indexOffset + 1 ? args[indexOffset].toLowerCase(Locale.ROOT) : "default";
         ConfigSection section = this.getSettings().getConfig().getSection("ping-message").getSection(timer ? "timer-messages" : "messages");
         this.sendList(sender, this.getModeMessages(section, mode));
      }
   }

   private void sendList(SenderInfo sender, @Nullable List<String> list) {
      if (list != null && !list.isEmpty()) {
         sender.send(this.getMessage("motdList", new String[0]));

         for (int i = 0; i < list.size(); i++) {
            sender.sendRich("<aqua>" + (i + 1) + "<dark_gray><st>---------");
            sender.sendRich(list.get(i).replace("<br>", "\n"));
         }

         sender.sendRich("<dark_gray><st>----------");
      } else {
         sender.send(this.getMessage("motdListEmpty", new String[0]));
      }
   }

   @Override
   public List<String> getTabCompletion(SenderInfo sender, String[] args) {
      if (args.length < 2 || args.length > 3) {
         return Collections.emptyList();
      } else if (args.length == 2) {
         List<String> list = new ArrayList<>();
         if (this.getSettings().hasTimerSpecificPingMessages()) {
            list.add("timer");
         }

         List<String> modes = new ArrayList<>(this.getSettings().getConfig().getSection("ping-message").getSection("messages").getKeys());
         modes.remove("default");
         list.addAll(modes);
         return list;
      } else if (args[1].equalsIgnoreCase("timer") && this.getSettings().hasTimerSpecificPingMessages()) {
         List<String> modes = new ArrayList<>(this.getSettings().getConfig().getSection("ping-message").getSection("timer-messages").getKeys());
         modes.remove("default");
         return modes;
      } else {
         return Collections.emptyList();
      }
   }

   @Nullable
   private List<String> getModeMessages(ConfigSection section, String mode) {
      return section.getObject(mode) instanceof String s ? Collections.singletonList(s) : section.getStringList(mode);
   }
}
