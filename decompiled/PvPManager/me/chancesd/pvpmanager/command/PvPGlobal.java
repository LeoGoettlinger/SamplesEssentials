package me.chancesd.pvpmanager.command;

import java.util.List;
import me.chancesd.pvpmanager.manager.PlayerManager;
import me.chancesd.pvpmanager.sdutils.command.ArgumentType;
import me.chancesd.pvpmanager.sdutils.command.BaseCommand;
import me.chancesd.pvpmanager.sdutils.command.CommandArgument;
import me.chancesd.pvpmanager.setting.Lang;
import me.chancesd.pvpmanager.setting.Permissions;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;

public class PvPGlobal extends BaseCommand {
   private final PlayerManager playerManager;

   public PvPGlobal(PluginCommand pluginCommand, PlayerManager playerManager) {
      super(pluginCommand);
      this.playerManager = playerManager;
      this.description("Toggle PvP for the whole server")
         .usage("/pvpglobal <on|off>")
         .permission(Permissions.COMMAND_PVPGLOBAL.getPermission())
         .argument("status", ArgumentType.STRING)
         .required()
         .tabComplete("ON", "OFF")
         .endArgument();
   }

   @Override
   public void execute(CommandSender sender, String label, List<CommandArgument> args) {
      String statusArg = this.getArgument(args, "status").getValue().toLowerCase();
      boolean status = statusArg.equals("on");
      this.playerManager.setGlobalStatus(status);
      sender.sendMessage(Lang.PREFIX.msg() + ChatColor.DARK_GREEN + " Server PvP was set to " + status);
   }
}
