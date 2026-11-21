package me.chancesd.pvpmanager.command;

import java.util.List;
import me.chancesd.pvpmanager.manager.PlayerManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.sdutils.command.ArgumentType;
import me.chancesd.pvpmanager.sdutils.command.BaseCommand;
import me.chancesd.pvpmanager.sdutils.command.CommandArgument;
import me.chancesd.pvpmanager.sdutils.utils.ChatUtils;
import me.chancesd.pvpmanager.setting.Conf;
import me.chancesd.pvpmanager.setting.Lang;
import me.chancesd.pvpmanager.setting.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

public class Tag extends BaseCommand {
   private static final String ARG_TARGET = "target";
   private static final String ARG_TIME = "time";
   private final PlayerManager ph;

   public Tag(PluginCommand pluginCommand, PlayerManager ph) {
      super(pluginCommand);
      this.ph = ph;
      String defaultCombatTime = String.valueOf(Conf.TIME_IN_COMBAT.asInt());
      this.description("Tag a player in combat")
         .usage("/tag [player] [time]")
         .permission(Permissions.COMMAND_TAG.getPermission())
         .argument("target", ArgumentType.PLAYER_OR_ALL)
         .requirePermission(Permissions.COMMAND_TAG_OTHERS.getPermission())
         .endArgument()
         .argument("time", ArgumentType.INTEGER)
         .defaultValue(defaultCombatTime)
         .dependsOn("target")
         .tabComplete(defaultCombatTime, "30", "60", "120")
         .endArgument();
   }

   @Override
   public void execute(CommandSender sender, String label, List<CommandArgument> args) {
      if (args.isEmpty()) {
         if (sender instanceof Player player) {
            this.showPlayerTagStatus(this.ph.get(player));
         } else {
            ChatUtils.send(sender, "&#FF5555This command can only be used by players when checking your own tag status.");
         }
      } else {
         CommandArgument targetArg = this.getArgument(args, "target");
         CommandArgument timeArg = this.getArgument(args, "time");
         long timeInMs = timeArg.getAsInt() * 1000L;
         if (timeInMs <= 0L) {
            ChatUtils.send(sender, Lang.PREFIX + " &#FF5555Combat time must be positive (greater than 0)");
         } else {
            if (targetArg.isWildcard()) {
               this.tagAll(sender, timeInMs);
            } else {
               Player targetPlayer = targetArg.getAsPlayerOrWildcard();
               this.tagPlayer(sender, targetPlayer.getName(), timeInMs);
            }
         }
      }
   }

   private void showPlayerTagStatus(CombatPlayer pvPlayer) {
      if (!pvPlayer.isInCombat()) {
         pvPlayer.message(Lang.TAG_NOT_IN_COMBAT, new Object[0]);
      } else {
         pvPlayer.message(Lang.TAG_TIME_LEFT.msgTime(pvPlayer.getTagTimeLeft()));
      }
   }

   private void tagAll(CommandSender sender, long time) {
      for (CombatPlayer player : this.ph.getPlayers().values()) {
         player.tag(true, player, time);
      }

      ChatUtils.send(sender, Lang.PREFIX + " &aAll players have been tagged");
   }

   private void tagPlayer(CommandSender sender, String name, long time) {
      CombatPlayer target = this.ph.get(Bukkit.getPlayer(name));
      if (target.isInCombat()) {
         ChatUtils.send(sender, Lang.PREFIX + " &#FF5555That player is already in combat");
      } else {
         target.tag(true, target, time);
         ChatUtils.send(sender, Lang.PREFIX + " &f" + target.getName() + " &ahas been tagged for &f" + time / 1000L + " &aseconds");
      }
   }
}
