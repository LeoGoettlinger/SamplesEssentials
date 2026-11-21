package xyz.lychee.lagfixer.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.lychee.lagfixer.managers.CommandManager;
import xyz.lychee.lagfixer.managers.SupportManager;
import xyz.lychee.lagfixer.objects.ISupportNms;
import xyz.lychee.lagfixer.utils.MessageUtils;

public class PingCommand extends CommandManager.Subcommand {
   public PingCommand(CommandManager commandManager) {
      super(commandManager, "ping", "calculate average players ping");
   }

   @Override
   public void load() {
   }

   @Override
   public void unload() {
   }

   @Override
   public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
      ISupportNms nms = SupportManager.getInstance().getNms();
      if (args.length > 0) {
         Player player = Bukkit.getPlayer(args[0]);
         return player == null
            ? MessageUtils.sendMessage(true, sender, "&7Player not found on the server")
            : MessageUtils.sendMessage(true, sender, "&7" + player.getDisplayName() + "'s ping is &e" + nms.getPlayerPing(player) + "&7ms");
      } else {
         double averagePing = Bukkit.getOnlinePlayers().stream().mapToInt(nms::getPlayerPing).average().orElse(-1.0);
         return MessageUtils.sendMessage(true, sender, "&7Average players ping: &e" + averagePing);
      }
   }
}
