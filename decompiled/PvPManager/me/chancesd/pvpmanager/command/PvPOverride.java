package me.chancesd.pvpmanager.command;

import java.util.List;
import me.chancesd.pvpmanager.manager.PlayerManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.sdutils.command.BaseCommand;
import me.chancesd.pvpmanager.sdutils.command.CommandArgument;
import me.chancesd.pvpmanager.setting.Lang;
import me.chancesd.pvpmanager.setting.Permissions;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

public class PvPOverride extends BaseCommand {
   private final PlayerManager ph;

   public PvPOverride(PluginCommand pluginCommand, PlayerManager ph) {
      super(pluginCommand);
      this.ph = ph;
      this.description("Override all PvP protections").usage("/pvpoverride").permission(Permissions.COMMAND_OVERRIDE.getPermission()).playerOnly();
   }

   @Override
   public void execute(CommandSender sender, String label, List<CommandArgument> args) {
      Player player = (Player)sender;
      CombatPlayer pvPlayer = this.ph.get(player);
      boolean override = pvPlayer.toggleOverride();
      pvPlayer.message(Lang.PREFIX.msg() + ChatColor.DARK_GREEN + " PvP Override set to: " + ChatColor.GOLD + override);
   }
}
