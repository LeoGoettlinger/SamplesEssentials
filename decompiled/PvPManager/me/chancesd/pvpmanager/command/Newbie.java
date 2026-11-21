package me.chancesd.pvpmanager.command;

import java.util.List;
import me.chancesd.pvpmanager.manager.PlayerManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.sdutils.command.ArgumentType;
import me.chancesd.pvpmanager.sdutils.command.BaseCommand;
import me.chancesd.pvpmanager.sdutils.command.CommandArgument;
import me.chancesd.pvpmanager.sdutils.utils.ChatUtils;
import me.chancesd.pvpmanager.sdutils.utils.TimeUtil;
import me.chancesd.pvpmanager.setting.Conf;
import me.chancesd.pvpmanager.setting.Lang;
import me.chancesd.pvpmanager.setting.Permissions;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

public class Newbie extends BaseCommand {
   private static final String ARG_PLAYER = "player";

   public Newbie(PluginCommand pluginCommand, PlayerManager ph) {
      super(pluginCommand);
      this.description("Manage newbie protection").usage("/newbie <subcommand>");
      if (Conf.NEWBIE_ALLOW_DISABLE.asBool()) {
         this.subCommand("disable", new Newbie.DisableNewbieCommand(ph));
      }

      this.subCommand("checktime", new Newbie.CheckTimeCommand(ph));
      this.subCommand("add", new Newbie.AddNewbieCommand(ph));
      this.subCommand("remove", new Newbie.RemoveNewbieCommand(ph));
   }

   @Override
   public void execute(CommandSender sender, String label, List<CommandArgument> args) {
      this.showHelpMenu(sender);
   }

   private static class AddNewbieCommand extends BaseCommand {
      private final PlayerManager ph;

      public AddNewbieCommand(PlayerManager ph) {
         this.ph = ph;
         this.permission(new String[]{Permissions.COMMAND_NEWBIE.getPermission()})
            .argument("player", ArgumentType.PLAYER)
            .required()
            .endArgument()
            .usage("/newbie add <player>")
            .description("Add newbie protection to a player");
      }

      @Override
      public void execute(CommandSender sender, String label, List<CommandArgument> args) {
         Player targetPlayer = this.getArgument(args, "player").getAsPlayer();
         CombatPlayer target = this.ph.get(targetPlayer);
         if (target.isNewbie()) {
            sender.sendMessage(ChatUtils.colorize(Lang.PREFIX + " &#FFFF55" + target.getName() + " &#FFAAAAalready has newbie protection"));
         } else {
            target.setNewbie(true);
            sender.sendMessage(ChatUtils.colorize(Lang.PREFIX + " Added newbie protection to &#FFFF55" + target.getName()));
         }
      }
   }

   private static class CheckTimeCommand extends BaseCommand {
      private final PlayerManager ph;

      public CheckTimeCommand(PlayerManager ph) {
         this.ph = ph;
         this.argument("player", ArgumentType.PLAYER).endArgument().usage("/newbie checktime [player]").description("Check newbie protection time");
      }

      @Override
      public void execute(CommandSender sender, String label, List<CommandArgument> args) {
         if (args.isEmpty()) {
            if (!(sender instanceof Player player)) {
               ChatUtils.send(sender, "&#FF5555Console must specify a player: &#FFFFFF/newbie checktime <player>");
            } else {
               CombatPlayer combatPlayer = this.ph.get(player);
               if (!combatPlayer.isNewbie()) {
                  combatPlayer.message(Lang.ERROR_NOT_NEWBIE, new Object[0]);
               } else {
                  combatPlayer.message(Lang.NEWBIE_TIME_CHECK.msgTime(combatPlayer.getNewbieTimeLeft()));
               }
            }
         } else if (!sender.hasPermission(Permissions.COMMAND_NEWBIE.getPermission())) {
            ChatUtils.send(sender, "&#FF5555You don't have permission to check other players' newbie time.");
         } else {
            Player targetPlayer = this.getArgument(args, "player").getAsPlayer();
            CombatPlayer target = this.ph.get(targetPlayer);
            long timeLeft = target.getNewbieTimeLeft();
            sender.sendMessage(Lang.NEWBIE_TIME_CHECK_OTHER.msg(target.getName(), TimeUtil.getDiffDuration(Lang.NEWBIE_TIME_CHECK_OTHER, timeLeft)));
         }
      }
   }

   private static class DisableNewbieCommand extends BaseCommand {
      private final PlayerManager ph;

      public DisableNewbieCommand(PlayerManager ph) {
         this.ph = ph;
         this.playerOnly().description("Disable your newbie protection");
      }

      @Override
      public void execute(CommandSender sender, String label, List<CommandArgument> args) {
         Player player = (Player)sender;
         CombatPlayer combatPlayer = this.ph.get(player);
         if (!combatPlayer.isNewbie()) {
            combatPlayer.message(Lang.ERROR_NOT_NEWBIE, new Object[0]);
         } else {
            combatPlayer.setNewbie(false);
            combatPlayer.message(Lang.NEWBIE_PROTECTION_REMOVED, new Object[0]);
         }
      }
   }

   private static class RemoveNewbieCommand extends BaseCommand {
      private final PlayerManager ph;

      public RemoveNewbieCommand(PlayerManager ph) {
         this.ph = ph;
         this.permission(new String[]{Permissions.COMMAND_NEWBIE.getPermission()})
            .argument("player", ArgumentType.PLAYER)
            .required()
            .endArgument()
            .usage("/newbie remove <player>")
            .description("Remove newbie protection from a player");
      }

      @Override
      public void execute(CommandSender sender, String label, List<CommandArgument> args) {
         Player targetPlayer = this.getArgument(args, "player").getAsPlayer();
         CombatPlayer target = this.ph.get(targetPlayer);
         if (!target.isNewbie()) {
            sender.sendMessage(ChatUtils.colorize(Lang.PREFIX + " &#FFFF55" + target.getName() + " &#FFAAAAdoesn't have newbie protection"));
         } else {
            target.setNewbie(false);
            sender.sendMessage(ChatUtils.colorize(Lang.PREFIX + " Removed newbie protection from &#FFFF55" + target.getName()));
         }
      }
   }
}
