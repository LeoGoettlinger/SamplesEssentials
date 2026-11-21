package me.chancesd.pvpmanager.command;

import java.util.List;
import me.chancesd.pvpmanager.manager.PlayerManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.player.UntagReason;
import me.chancesd.pvpmanager.sdutils.command.ArgumentType;
import me.chancesd.pvpmanager.sdutils.command.BaseCommand;
import me.chancesd.pvpmanager.sdutils.command.CommandArgument;
import me.chancesd.pvpmanager.setting.Lang;
import me.chancesd.pvpmanager.setting.Permissions;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

public class Untag extends BaseCommand {
   private final PlayerManager playerManager;

   public Untag(PluginCommand pluginCommand, PlayerManager playerManager) {
      super(pluginCommand);
      this.playerManager = playerManager;
      this.permission(new String[]{Permissions.COMMAND_UNTAG.getPermission()})
         .description("Untags a player")
         .usage("/untag <player|*>")
         .argument("target", ArgumentType.PLAYER_OR_ALL)
         .required()
         .endArgument();
   }

   @Override
   public void execute(CommandSender sender, String label, List<CommandArgument> args) {
      CommandArgument targetArg = this.getArgument(args, "target");
      if (targetArg.isAllPlayers()) {
         this.untagAll(sender);
      } else {
         Player targetPlayer = targetArg.getAsPlayerOrWildcard();
         CombatPlayer combatPlayer = this.playerManager.get(targetPlayer);
         if (!combatPlayer.isInCombat()) {
            sender.sendMessage(Lang.PREFIX + " §cThat player is not in combat");
         } else {
            combatPlayer.untag(UntagReason.COMMAND);
            sender.sendMessage(Lang.PREFIX + " " + combatPlayer.getName() + " §2has been untagged");
         }
      }
   }

   private void untagAll(CommandSender sender) {
      int untaggedCount = 0;

      for (CombatPlayer player : this.playerManager.getPlayers().values()) {
         if (player.isInCombat()) {
            player.untag(UntagReason.COMMAND);
            untaggedCount++;
         }
      }

      if (untaggedCount == 0) {
         sender.sendMessage(Lang.PREFIX + " §eNo players were in combat");
      } else {
         sender.sendMessage(Lang.PREFIX + " §aAll players have been untagged");
      }
   }
}
