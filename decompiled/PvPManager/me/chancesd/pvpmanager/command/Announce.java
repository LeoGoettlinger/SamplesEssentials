package me.chancesd.pvpmanager.command;

import java.util.List;
import me.chancesd.pvpmanager.sdutils.command.ArgumentType;
import me.chancesd.pvpmanager.sdutils.command.BaseCommand;
import me.chancesd.pvpmanager.sdutils.command.CommandArgument;
import me.chancesd.pvpmanager.sdutils.utils.ChatUtils;
import me.chancesd.pvpmanager.setting.Lang;
import me.chancesd.pvpmanager.setting.Permissions;
import me.chancesd.pvpmanager.setting.lang.Replacement;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;

public class Announce extends BaseCommand {
   public Announce(PluginCommand pluginCommand) {
      super(pluginCommand);
      this.description("Broadcasts a message to all players")
         .permission(Permissions.COMMAND_ANNOUNCE.getPermission())
         .argument("message", ArgumentType.STRING_ARRAY)
         .required()
         .endArgument();
   }

   @Override
   public void execute(CommandSender sender, String label, List<CommandArgument> args) {
      String message = this.getArgument(args, "message").getValue();
      String colorizedMessage = ChatUtils.colorize(message).replace(Replacement.PREFIX.getPlaceholder(), Lang.PREFIX.msg());
      Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(colorizedMessage));
      Bukkit.getConsoleSender().sendMessage(colorizedMessage);
   }
}
