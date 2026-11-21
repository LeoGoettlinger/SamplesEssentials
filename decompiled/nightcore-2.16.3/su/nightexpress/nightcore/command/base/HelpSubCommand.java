package su.nightexpress.nightcore.command.base;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.NightCorePlugin;
import su.nightexpress.nightcore.command.CommandResult;
import su.nightexpress.nightcore.command.api.NightCommand;
import su.nightexpress.nightcore.command.impl.AbstractCommand;
import su.nightexpress.nightcore.core.CoreLang;

@Deprecated
public class HelpSubCommand extends AbstractCommand<NightCorePlugin> {
   public HelpSubCommand(@NonNull NightCorePlugin plugin) {
      super(plugin, new String[]{"help"});
      this.setDescription(CoreLang.COMMAND_HELP_DESC);
   }

   @Override
   protected void onExecute(@NonNull CommandSender sender, @NonNull CommandResult result) {
      NightCommand parent = this.getParent();
      if (parent != null) {
         if (!parent.hasPermission(sender)) {
            this.errorPermission(sender);
         } else {
            CoreLang.COMMAND_HELP_LIST.getMessage().replace("%name%", this.plugin.getNameLocalized()).replace("%entry%", list -> {
               Set<NightCommand> commands = new HashSet<>(parent.getChildrens());
               commands.stream().sorted(Comparator.comparing(command -> command.getAliases()[0])).forEach(children -> {
                  if (children.hasPermission(sender)) {
                     list.add(children.replacePlaceholders().apply(CoreLang.COMMAND_HELP_ENTRY.getString()));
                  }
               });
            }).send(sender);
         }
      }
   }
}
