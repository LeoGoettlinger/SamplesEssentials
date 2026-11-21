package org.lushplugins.lushrewards.command.subcommand;

import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lushplugins.lushrewards.LushRewards;
import org.lushplugins.lushrewards.data.RewardUser;
import org.lushplugins.lushrewards.libraries.lushlib.command.SubCommand;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.ChatColorHandler;
import org.lushplugins.lushrewards.module.playtimerewards.PlaytimeRewardsModule;
import org.lushplugins.lushrewards.module.playtimetracker.PlaytimeTrackerModule;

public class SetPlaytimeSubCommand extends SubCommand {
   public SetPlaytimeSubCommand() {
      super("set-playtime");
      this.addRequiredPermission("lushrewards.edituser.setplaytime");
   }

   @Override
   public boolean execute(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args, @NotNull String[] fullArgs) {
      switch (args.length) {
         case 0:
         case 1:
            ChatColorHandler.sendMessage(
               sender,
               LushRewards.getInstance()
                  .getConfigManager()
                  .getMessage("incorrect-usage")
                  .replace("%command-usage%", "/rewards edit-user <module-id> set-playtime <player> <playtime>")
            );
            break;
         case 2:
            ChatColorHandler.sendMessage(
               sender,
               LushRewards.getInstance()
                  .getConfigManager()
                  .getMessage("confirm-command")
                  .replace("%command%", String.format("/rewards set-playtime %s %s confirm", args[0], args[1]))
            );
            break;
         case 3:
            if (!args[2].equalsIgnoreCase("confirm")) {
               ChatColorHandler.sendMessage(
                  sender,
                  LushRewards.getInstance()
                     .getConfigManager()
                     .getMessage("incorrect-usage")
                     .replace("%command-usage%", "/rewards edit-user <module-id> set-playtime <player> <playtime> confirm")
               );
               return true;
            }

            int playtime;
            try {
               playtime = Integer.parseInt(args[1]);
            } catch (NumberFormatException var8) {
               ChatColorHandler.sendMessage(
                  sender,
                  LushRewards.getInstance()
                     .getConfigManager()
                     .getMessage("incorrect-usage")
                     .replace("%command-usage%", "/rewards edit-user <module-id> set-playtime <player> <playtime> confirm")
               );
               return true;
            }

            if (!this.setPlaytime(sender, args[0], playtime)) {
               return true;
            }

            ChatColorHandler.sendMessage(
               sender,
               LushRewards.getInstance()
                  .getConfigManager()
                  .getMessage("set-playtime-confirm")
                  .replace("%target%", args[0])
                  .replace("%playtime%", String.valueOf(playtime))
            );
            return true;
      }

      return true;
   }

   @Override
   public List<String> tabComplete(
      @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args, @NotNull String[] fullArgs
   ) {
      return args.length == 1 ? Bukkit.getOnlinePlayers().stream().<String>map(Player::getName).toList() : null;
   }

   private boolean setPlaytime(CommandSender sender, String nameOrUuid, int playtime) {
      Player player = Bukkit.getPlayer(nameOrUuid);
      UUID uuid;
      if (player != null) {
         uuid = player.getUniqueId();
      } else {
         try {
            uuid = UUID.fromString(nameOrUuid);
         } catch (IllegalArgumentException var7) {
            ChatColorHandler.sendMessage(sender, LushRewards.getInstance().getConfigManager().getMessage("unknown-player").replace("%player%", nameOrUuid));
            return false;
         }
      }

      RewardUser rewardUser = LushRewards.getInstance().getDataManager().getRewardUser(uuid);
      if (rewardUser != null) {
         LushRewards.getInstance()
            .getModule("playtime-tracker")
            .ifPresent(module -> ((PlaytimeTrackerModule)module).getPlaytimeTracker(uuid).setGlobalPlaytime(playtime));
         rewardUser.setMinutesPlayed(playtime);
         LushRewards.getInstance().getRewardModules().forEach(module -> {
            if (module instanceof PlaytimeRewardsModule playtimeRewardsModule) {
               PlaytimeRewardsModule.UserData userData = playtimeRewardsModule.getUserData(uuid);
               if (userData.getLastCollectedPlaytime() > playtime) {
                  userData.setLastCollectedPlaytime(playtime);
               }
            }
         });
         return true;
      } else {
         return false;
      }
   }
}
