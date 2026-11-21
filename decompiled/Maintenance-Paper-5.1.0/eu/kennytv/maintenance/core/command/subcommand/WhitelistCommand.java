package eu.kennytv.maintenance.core.command.subcommand;

import eu.kennytv.maintenance.core.MaintenancePlugin;
import eu.kennytv.maintenance.core.command.CommandInfo;
import eu.kennytv.maintenance.core.util.SenderInfo;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import net.kyori.adventure.text.Component;

public final class WhitelistCommand extends CommandInfo {
   public WhitelistCommand(MaintenancePlugin plugin) {
      super(plugin, "whitelist.list");
   }

   @Override
   public void execute(SenderInfo sender, String[] args) {
      if (!this.checkArgs(sender, args, 1)) {
         Map<UUID, String> players = this.getSettings().getWhitelistedPlayers();
         if (players.isEmpty()) {
            sender.send(this.getMessage("whitelistEmpty", new String[0]));
         } else {
            sender.send(this.getMessage("whitelistedPlayers", new String[0]));
            Component format = this.getMessage("whitelistedPlayersFormat", new String[0]);

            for (Entry<UUID, String> entry : players.entrySet()) {
               sender.send(
                  format.replaceText(builder -> builder.matchLiteral("%NAME%").replacement(entry.getValue()))
                     .replaceText(builder -> builder.matchLiteral("%UUID%").replacement(entry.getKey().toString()))
               );
            }

            sender.send(Component.empty());
         }
      }
   }
}
