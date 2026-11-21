package me.chancesd.pvpmanager.command;

import java.util.List;
import me.chancesd.pvpmanager.manager.PlayerManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.sdutils.command.ArgumentType;
import me.chancesd.pvpmanager.sdutils.command.BaseCommand;
import me.chancesd.pvpmanager.sdutils.command.CommandArgument;
import me.chancesd.pvpmanager.setting.Lang;
import me.chancesd.pvpmanager.setting.Permissions;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

public class PvPStatus extends BaseCommand {
   private final PlayerManager playerManager;

   public PvPStatus(PluginCommand pluginCommand, PlayerManager playerManager) {
      super(pluginCommand);
      this.playerManager = playerManager;
      this.description("Check PvP combat status")
         .usage("/pvpstatus [player]")
         .permission(Permissions.COMMAND_PVPSTATUS.getPermission())
         .argument("target", ArgumentType.PLAYER)
         .requirePermission(Permissions.COMMAND_PVPSTATUS_OTHERS.getPermission())
         .endArgument();
   }

   @Override
   public void execute(CommandSender sender, String label, List<CommandArgument> args) {
      if (args.isEmpty()) {
         if (!(sender instanceof Player player)) {
            sender.sendMessage(Lang.ERROR_NOT_PLAYER.msg());
         } else {
            CombatPlayer pvpPlayer = this.playerManager.get(player);
            if (!pvpPlayer.hasPvPEnabled()) {
               pvpPlayer.message(Lang.SELF_STATUS_DISABLED, new Object[0]);
            } else {
               pvpPlayer.message(Lang.SELF_STATUS_ENABLED, new Object[0]);
            }
         }
      } else {
         CommandArgument targetArg = this.getArgument(args, "target");
         Player targetPlayer = targetArg.getAsPlayer();
         CombatPlayer target = this.playerManager.get(targetPlayer);
         if (!target.hasPvPEnabled()) {
            sender.sendMessage(Lang.OTHERS_STATUS_DISABLED.msg(targetPlayer.getName()));
         } else {
            sender.sendMessage(Lang.OTHER_STATUS_ENABLED.msg(targetPlayer.getName()));
         }
      }
   }
}
