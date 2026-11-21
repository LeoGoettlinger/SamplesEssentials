package eu.kennytv.maintenance.core.command.subcommand;

import eu.kennytv.maintenance.core.MaintenancePlugin;
import eu.kennytv.maintenance.core.Settings;
import eu.kennytv.maintenance.core.command.CommandInfo;
import eu.kennytv.maintenance.core.config.ConfigSection;
import eu.kennytv.maintenance.core.util.SenderInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class SetMotdCommand extends CommandInfo {
   public SetMotdCommand(MaintenancePlugin plugin) {
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

      if (args.length <= indexOffset + 2) {
         sender.send(this.getHelpMessage());
      } else {
         String mode = "default";
         if (!this.plugin.isNumeric(args[indexOffset])) {
            mode = args[indexOffset].toLowerCase(Locale.ROOT);
            indexOffset++;
         }

         if (args.length > indexOffset + 2 && this.plugin.isNumeric(args[indexOffset])) {
            Settings settings = this.getSettings();
            ConfigSection section = settings.getConfig().getSection("ping-message");
            ConfigSection messageSection = section.getSection(timerPingMessages ? "timer-messages" : "messages");
            List<String> pingMessages = this.getOrCreateModeMessages(messageSection, mode);
            int index = Integer.parseInt(args[indexOffset]);
            if (index == 0 || index > pingMessages.size() + 1) {
               sender.send(
                  this.getMessage(
                     "setMotdIndexError",
                     new String[]{"%MOTDS%", Integer.toString(pingMessages.size()), "%NEWAMOUNT%", Integer.toString(pingMessages.size() + 1)}
                  )
               );
            } else if (!this.plugin.isNumeric(args[indexOffset + 1])) {
               sender.send(this.getMessage("setMotdLineError", new String[0]));
            } else {
               int line = Integer.parseInt(args[indexOffset + 1]);
               if (line != 1 && line != 2) {
                  sender.send(this.getMessage("setMotdLineError", new String[0]));
               } else {
                  String message = String.join(" ", Arrays.copyOfRange(args, indexOffset + 2, args.length));
                  String oldMessage = index > pingMessages.size() ? "" : pingMessages.get(index - 1);
                  String newMessage;
                  if (line == 1) {
                     newMessage = oldMessage.contains("<br>") ? message + "<br>" + oldMessage.split("<br>", 2)[1] : message;
                  } else {
                     newMessage = oldMessage.contains("<br>") ? oldMessage.split("<br>", 2)[0] + "<br>" + message : oldMessage + "<br>" + message;
                  }

                  if (index > pingMessages.size()) {
                     pingMessages.add(newMessage);
                  } else {
                     pingMessages.set(index - 1, newMessage);
                  }

                  messageSection.set(mode, pingMessages);
                  settings.saveConfig();
                  settings.reloadConfigs();
                  sender.send(
                     settings.getMessage(
                        "setMotd", "%LINE%", Integer.toString(line), "%INDEX%", Integer.toString(index), "%MOTD%", newMessage.replace("<br>", "\n")
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

      if (args.length == indexOffset + 2) {
         return Arrays.asList("1", "2");
      } else if (args.length == indexOffset + 1 && !this.plugin.isNumeric(args[indexOffset])) {
         return this.getModeCompletions(timer);
      } else if (args.length != indexOffset + 1) {
         return Collections.emptyList();
      } else {
         String mode = this.plugin.isNumeric(args[indexOffset]) ? "default" : args[indexOffset].toLowerCase(Locale.ROOT);
         ConfigSection section = this.getSettings().getConfig().getSection("ping-message").getSection(timer ? "timer-messages" : "messages");
         int size = this.getOrCreateModeMessages(section, mode).size();
         List<String> list = new ArrayList<>();

         for (int i = 1; i <= size + 1; i++) {
            list.add(String.valueOf(i));
         }

         return list;
      }
   }

   private List<String> getModeCompletions(boolean timer) {
      List<String> modes = new ArrayList<>(
         this.getSettings().getConfig().getSection("ping-message").getSection(timer ? "timer-messages" : "messages").getKeys()
      );
      modes.remove("default");
      return modes;
   }

   private List<String> getOrCreateModeMessages(ConfigSection section, String mode) {
      Object value = section.getObject(mode);
      if (value == null) {
         return new ArrayList<>();
      } else {
         return value instanceof String s ? new ArrayList<>(Collections.singletonList(s)) : new ArrayList<>(section.getStringList(mode));
      }
   }
}
