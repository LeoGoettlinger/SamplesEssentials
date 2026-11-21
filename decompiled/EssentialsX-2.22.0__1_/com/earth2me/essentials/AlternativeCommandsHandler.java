package com.earth2me.essentials;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.stream.Collectors;
import net.ess3.provider.KnownCommandsProvider;
import org.bukkit.command.Command;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.plugin.Plugin;

public class AlternativeCommandsHandler {
   private final transient Map<String, List<WeakReference<Command>>> altCommands = new HashMap<>();
   private final transient Map<String, String> disabledList = new HashMap<>();
   private final transient IEssentials ess;

   public AlternativeCommandsHandler(final IEssentials ess) {
      this.ess = ess;

      for (Plugin plugin : ess.getServer().getPluginManager().getPlugins()) {
         if (plugin.isEnabled()) {
            this.addPlugin(plugin);
         }
      }
   }

   public final void addPlugin(final Plugin plugin) {
      if (!plugin.getDescription().getMain().contains("com.earth2me.essentials") && !plugin.getDescription().getMain().contains("net.essentialsx")) {
         for (Entry<String, Command> entry : this.getPluginCommands(plugin)) {
            String[] commandSplit = entry.getKey().split(":", 2);
            String commandName = commandSplit.length > 1 ? commandSplit[1] : entry.getKey();
            Command command = entry.getValue();
            List<WeakReference<Command>> pluginCommands = this.altCommands.computeIfAbsent(commandName.toLowerCase(Locale.ENGLISH), k -> new ArrayList<>());
            boolean found = false;
            Iterator<WeakReference<Command>> pluginCmdIterator = pluginCommands.iterator();

            while (pluginCmdIterator.hasNext()) {
               Command cmd = pluginCmdIterator.next().get();
               if (cmd == null) {
                  if (this.ess.getSettings().isDebug()) {
                     this.ess.getLogger().log(Level.INFO, "Essentials: Alternative command for " + commandName + " removed due to garbage collection");
                  }

                  pluginCmdIterator.remove();
               } else if (((PluginIdentifiableCommand)cmd).getPlugin().equals(plugin)) {
                  found = true;
                  break;
               }
            }

            if (!found) {
               pluginCommands.add(new WeakReference<>(command));
            }
         }
      }
   }

   private List<Entry<String, Command>> getPluginCommands(Plugin plugin) {
      Map<String, Command> commands = new HashMap<>();

      for (Entry<String, Command> entry : this.ess.provider(KnownCommandsProvider.class).getKnownCommands().entrySet()) {
         if (entry.getValue() instanceof PluginIdentifiableCommand && ((PluginIdentifiableCommand)entry.getValue()).getPlugin().equals(plugin)) {
            commands.put(entry.getKey(), entry.getValue());
         }
      }

      return commands.entrySet().stream().sorted((o1, o2) -> {
         if (o1.getKey().contains(":") && !o2.getKey().contains(":")) {
            return 1;
         } else {
            return !o1.getKey().contains(":") && o2.getKey().contains(":") ? -1 : 0;
         }
      }).collect(Collectors.toList());
   }

   public void removePlugin(final Plugin plugin) {
      Iterator<Entry<String, List<WeakReference<Command>>>> iterator = this.altCommands.entrySet().iterator();

      while (iterator.hasNext()) {
         Entry<String, List<WeakReference<Command>>> entry = iterator.next();
         Iterator<WeakReference<Command>> commands = entry.getValue().iterator();

         while (commands.hasNext()) {
            Command pc = commands.next().get();
            if (!(pc instanceof PluginIdentifiableCommand) || ((PluginIdentifiableCommand)pc).getPlugin().equals(plugin)) {
               commands.remove();
            }
         }

         if (entry.getValue().isEmpty()) {
            iterator.remove();
         }
      }
   }

   public Command getAlternative(final String label) {
      List<WeakReference<Command>> commands = this.altCommands.get(label);
      if (commands != null && !commands.isEmpty()) {
         if (commands.size() == 1) {
            return commands.get(0).get();
         } else {
            Iterator<WeakReference<Command>> iterator = commands.iterator();

            while (iterator.hasNext()) {
               Command cmd = iterator.next().get();
               if (cmd == null) {
                  iterator.remove();
               } else if (cmd.getName().equalsIgnoreCase(label)) {
                  return cmd;
               }
            }

            return commands.get(0).get();
         }
      } else {
         return null;
      }
   }

   public void executed(final String label, final Command pc) {
      if (pc instanceof PluginIdentifiableCommand) {
         String altString = ((PluginIdentifiableCommand)pc).getPlugin().getName() + ":" + pc.getName();
         if (this.ess.getSettings().isDebug()) {
            this.ess.getLogger().log(Level.INFO, "Essentials: Alternative command " + label + " found, using " + altString);
         }

         this.disabledList.put(label, altString);
      }
   }

   public Map<String, String> disabledCommands() {
      return this.disabledList;
   }
}
