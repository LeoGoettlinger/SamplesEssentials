package eu.kennytv.maintenance.core.command.subcommand;

import eu.kennytv.maintenance.core.MaintenancePlugin;
import eu.kennytv.maintenance.core.Settings;
import eu.kennytv.maintenance.core.command.CommandInfo;
import eu.kennytv.maintenance.core.config.ConfigSection;
import eu.kennytv.maintenance.core.util.SenderInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class RemoveMotdCommand extends CommandInfo {
   public RemoveMotdCommand(MaintenancePlugin plugin) {
      super(plugin, "setmotd");
   }

   @Override
   public void execute(SenderInfo sender, String[] args) {
      int indexOffset = 1;
      boolean timerPingMessages = false;
      if (args.length > indexOffset && args[indexOffset].equalsIgnoreCase("timer")) {
         if (!this.getSettings().hasTimerSpecificPingMessages()) {
            sender.send(this.getMessage("timerMotdDisabled", new String[0]));
            return;
         }

         indexOffset++;
         timerPingMessages = true;
      }

      if (args.length <= indexOffset) {
         sender.send(this.getHelpMessage());
      } else {
         String mode = "default";
         if (!this.plugin.isNumeric(args[indexOffset])) {
            mode = args[indexOffset].toLowerCase(Locale.ROOT);
            indexOffset++;
         }

         if (args.length == indexOffset + 1 && this.plugin.isNumeric(args[indexOffset])) {
            Settings settings = this.getSettings();
            ConfigSection section = settings.getConfig().getSection("ping-message");
            ConfigSection messageSection = section.getSection(timerPingMessages ? "timer-messages" : "messages");
            List<String> pingMessages = this.getModeMessages(messageSection, mode);
            if (pingMessages.isEmpty()) {
               sender.send(this.getMessage("motdListEmpty", new String[0]));
            } else if (mode.equals("default") && pingMessages.size() < 2) {
               sender.send(this.getMessage("removeMotdError", new String[0]));
            } else {
               int index = Integer.parseInt(args[indexOffset]);
               if (index != 0 && index <= pingMessages.size()) {
                  pingMessages.remove(index - 1);
                  if (pingMessages.isEmpty()) {
                     messageSection.remove(mode);
                  } else {
                     messageSection.set(mode, pingMessages);
                  }

                  settings.saveConfig();
                  settings.reloadConfigs();
                  sender.send(this.getMessage("removedMotd", new String[]{"%INDEX%", Integer.toString(index)}));
               } else {
                  sender.send(
                     this.getMessage(
                        "setMotdIndexError",
                        new String[]{"%MOTDS%", Integer.toString(pingMessages.size()), "%NEWAMOUNT%", Integer.toString(pingMessages.size())}
                     )
                  );
               }
            }
         } else {
            sender.send(this.getHelpMessage());
         }
      }
   }

   @Override
   public List<String> getTabCompletion(SenderInfo sender, String[] args) {
      int indexOffset;
      boolean timer;
      if (args.length > 1 && args[1].equalsIgnoreCase("timer")) {
         timer = true;
         indexOffset = 2;
      } else {
         timer = false;
         indexOffset = 1;
      }

      if (args.length == indexOffset + 1 && !this.plugin.isNumeric(args[indexOffset])) {
         return this.getModeCompletions(timer);
      } else if (args.length == indexOffset + 1 || args.length == indexOffset + 2 && !this.plugin.isNumeric(args[indexOffset])) {
         String mode = this.plugin.isNumeric(args[indexOffset]) ? "default" : args[indexOffset].toLowerCase(Locale.ROOT);
         int size = this.getModeMessages(this.getSettings().getConfig().getSection("ping-message").getSection(timer ? "timer-messages" : "messages"), mode)
            .size();
         List<String> list = new ArrayList<>(size);

         for (int i = 1; i <= size; i++) {
            list.add(Integer.toString(i));
         }

         return list;
      } else {
         return Collections.emptyList();
      }
   }

   private List<String> getModeCompletions(boolean timer) {
      List<String> modes = new ArrayList<>(
         this.getSettings().getConfig().getSection("ping-message").getSection(timer ? "timer-messages" : "messages").getKeys()
      );
      modes.remove("default");
      return modes;
   }

   private List<String> getModeMessages(ConfigSection section, String mode) {
      Object value = section.getObject(mode);
      if (value == null) {
         return List.of();
      } else {
         return value instanceof String s ? new ArrayList<>(Collections.singletonList(s)) : new ArrayList<>(section.getStringList(mode));
      }
   }
}
