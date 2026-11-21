package org.lushplugins.lushrewards.command.subcommand;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lushplugins.lushrewards.LushRewards;
import org.lushplugins.lushrewards.libraries.lushlib.command.SubCommand;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.ChatColorHandler;
import org.lushplugins.lushrewards.libraries.lushlib.module.Module;
import org.lushplugins.lushrewards.module.RewardModule;
import org.lushplugins.lushrewards.module.dailyrewards.DailyRewardsModule;
import org.lushplugins.lushrewards.utils.PlayerUtils;

public class EditUserSubCommand extends SubCommand {
   public EditUserSubCommand() {
      super("edit-user");
      this.addRequiredPermission("lushrewards.edituser");
      this.addRequiredArgs(0, () -> {
         List<String> modules = new ArrayList<>(LushRewards.getInstance().getEnabledRewardModules().stream().map(Module::getId).toList());
         modules.add("*");
         return modules;
      });
      this.addSubCommand(new EditUserSubCommand.ResetSubCommand());
      this.addSubCommand(new EditUserSubCommand.ResetDaysSubCommand());
      this.addSubCommand(new EditUserSubCommand.SetDaysSubCommand());
      this.addSubCommand(new EditUserSubCommand.ResetStreakSubCommand());
      this.addSubCommand(new EditUserSubCommand.SetStreakSubCommand());
   }

   @Override
   public boolean execute(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args, @NotNull String[] fullArgs) {
      ChatColorHandler.sendMessage(
         sender, LushRewards.getInstance().getConfigManager().getMessage("incorrect-usage").replace("%command-usage%", "/rewards edit-user <module-id>")
      );
      return true;
   }

   private static boolean setDay(CommandSender sender, String nameOrUuid, List<RewardModule> modules, int dayNum) {
      UUID uuid;
      try {
         uuid = PlayerUtils.getUniqueId(nameOrUuid);
      } catch (IllegalArgumentException var8) {
         ChatColorHandler.sendMessage(sender, LushRewards.getInstance().getConfigManager().getMessage("unknown-player").replace("%player%", nameOrUuid));
         return false;
      }

      for (Module module : modules) {
         if (module instanceof DailyRewardsModule dailyRewardsModule) {
            dailyRewardsModule.getOrLoadUserData(uuid, false).thenAccept(userData -> {
               if (userData != null) {
                  userData.setDayNum(dayNum);
                  userData.setLastCollectedDate(LocalDate.of(1971, 10, 1));
                  dailyRewardsModule.saveUserData(userData);
               }
            });
         }
      }

      return true;
   }

   private static boolean setStreak(CommandSender sender, String nameOrUuid, List<RewardModule> modules, int streak) {
      UUID uuid;
      try {
         uuid = PlayerUtils.getUniqueId(nameOrUuid);
      } catch (IllegalArgumentException var8) {
         ChatColorHandler.sendMessage(sender, LushRewards.getInstance().getConfigManager().getMessage("unknown-player").replace("%player%", nameOrUuid));
         return false;
      }

      for (Module module : modules) {
         if (module instanceof DailyRewardsModule dailyRewardsModule) {
            dailyRewardsModule.getOrLoadUserData(uuid, false).thenAccept(userData -> {
               userData.setStreak(streak);
               dailyRewardsModule.saveUserData(userData);
            });
         }
      }

      return true;
   }

   private static boolean removeCollectedDays(CommandSender sender, String nameOrUuid, List<RewardModule> modules) {
      UUID uuid;
      try {
         uuid = PlayerUtils.getUniqueId(nameOrUuid);
      } catch (IllegalArgumentException var7) {
         ChatColorHandler.sendMessage(sender, LushRewards.getInstance().getConfigManager().getMessage("unknown-player").replace("%player%", nameOrUuid));
         return false;
      }

      for (Module module : modules) {
         if (module instanceof DailyRewardsModule dailyRewardsModule) {
            dailyRewardsModule.getOrLoadUserData(uuid, false).thenAccept(userData -> {
               userData.clearCollectedDays();
               userData.setLastCollectedDate(null);
               dailyRewardsModule.saveUserData(userData);
            });
         }
      }

      return true;
   }

   private static List<RewardModule> getModules(String moduleNames) {
      List<RewardModule> modules = new ArrayList<>();
      if (moduleNames.equals("*")) {
         return LushRewards.getInstance().getEnabledRewardModules();
      } else {
         for (String name : moduleNames.split(",")) {
            LushRewards.getInstance().getModule(name).ifPresent(module -> {
               if (module instanceof RewardModule rewardModule) {
                  modules.add(rewardModule);
               }
            });
         }

         return modules;
      }
   }

   private List<String> getRemainingModules(String arg) {
      List<String> possibleModules = new ArrayList<>(LushRewards.getInstance().getEnabledRewardModules().stream().map(Module::getId).toList());

      for (String moduleName : arg.split(",")) {
         possibleModules.remove(moduleName);
      }

      return possibleModules;
   }

   private static class ResetDaysSubCommand extends SubCommand {
      public ResetDaysSubCommand() {
         super("reset-days");
         this.addRequiredPermission("lushrewards.edituser.resetdays");
      }

      @Override
      public boolean execute(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args, @NotNull String[] fullArgs) {
         switch (args.length) {
            case 0:
               ChatColorHandler.sendMessage(
                  sender,
                  LushRewards.getInstance()
                     .getConfigManager()
                     .getMessage("incorrect-usage")
                     .replace("%command-usage%", "/rewards edit-user <module-id> reset-days <player>")
               );
               break;
            case 1:
               ChatColorHandler.sendMessage(
                  sender,
                  LushRewards.getInstance()
                     .getConfigManager()
                     .getMessage("confirm-command")
                     .replace("%command", String.format("/rewards edit-user <module-id> reset-days %s confirm", args[0]))
               );
               break;
            case 2:
               if (!args[1].equalsIgnoreCase("confirm")) {
                  ChatColorHandler.sendMessage(
                     sender,
                     LushRewards.getInstance()
                        .getConfigManager()
                        .getMessage("incorrect-usage")
                        .replace("%command-usage%", "/rewards edit-user <module-id> reset-days <player> confirm")
                  );
                  return true;
               }

               List<RewardModule> modules = EditUserSubCommand.getModules(fullArgs[1]);
               if (!EditUserSubCommand.setDay(sender, args[0], modules, 1)) {
                  return true;
               }

               ChatColorHandler.sendMessage(
                  sender,
                  LushRewards.getInstance()
                     .getConfigManager()
                     .getMessage("set-days-confirm")
                     .replace("%target%", args[0])
                     .replace("%day%", "1")
                     .replace("%module%", String.join(", ", modules.stream().map(Module::getId).toList()))
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
   }

   private static class ResetStreakSubCommand extends SubCommand {
      public ResetStreakSubCommand() {
         super("reset-streak");
         this.addRequiredPermission("lushrewards.edituser.resetstreak");
      }

      @Override
      public boolean execute(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args, @NotNull String[] fullArgs) {
         switch (args.length) {
            case 0:
               ChatColorHandler.sendMessage(
                  sender,
                  LushRewards.getInstance()
                     .getConfigManager()
                     .getMessage("incorrect-usage")
                     .replace("%command-usage%", "/rewards edit-user <module-id> reset-streak <player>")
               );
               break;
            case 1:
               ChatColorHandler.sendMessage(
                  sender,
                  LushRewards.getInstance()
                     .getConfigManager()
                     .getMessage("confirm-command")
                     .replace("%command%", String.format("/rewards edit-user <module-id> reset-streak %s confirm", args[0]))
               );
               break;
            case 2:
               if (!args[1].equalsIgnoreCase("confirm")) {
                  ChatColorHandler.sendMessage(
                     sender,
                     LushRewards.getInstance()
                        .getConfigManager()
                        .getMessage("incorrect-usage")
                        .replace("%command-usage%", "/rewards edit-user <module-id> reset-streak <player> confirm")
                  );
                  return true;
               }

               List<RewardModule> modules = EditUserSubCommand.getModules(fullArgs[1]);
               if (!EditUserSubCommand.setStreak(sender, args[0], modules, 1)) {
                  return true;
               }

               ChatColorHandler.sendMessage(
                  sender,
                  LushRewards.getInstance()
                     .getConfigManager()
                     .getMessage("set-streak-confirm")
                     .replace("%target%", args[0])
                     .replace("%streak%", "1")
                     .replace("%module%", String.join(", ", modules.stream().map(Module::getId).toList()))
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
   }

   private static class ResetSubCommand extends SubCommand {
      public ResetSubCommand() {
         super("reset");
         this.addRequiredPermission("lushrewards.edituser.reset");
      }

      @Override
      public boolean execute(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args, @NotNull String[] fullArgs) {
         switch (args.length) {
            case 0:
               ChatColorHandler.sendMessage(
                  sender,
                  LushRewards.getInstance()
                     .getConfigManager()
                     .getMessage("incorrect-usage")
                     .replace("%command-usage%", "/rewards edit-user <module-id> reset <player>")
               );
               break;
            case 1:
               ChatColorHandler.sendMessage(sender, LushRewards.getInstance().getConfigManager().getMessage("reset").replace("%target%", args[0]));
               break;
            case 2:
               if (!args[1].equalsIgnoreCase("confirm")) {
                  ChatColorHandler.sendMessage(
                     sender,
                     LushRewards.getInstance()
                        .getConfigManager()
                        .getMessage("incorrect-usage")
                        .replace("%command-usage%", "/rewards edit-user <module-id> reset <player> confirm")
                  );
                  return true;
               }

               List<RewardModule> modules = EditUserSubCommand.getModules(fullArgs[1]);
               if (!EditUserSubCommand.setDay(sender, args[0], modules, 1)
                  || !EditUserSubCommand.setStreak(sender, args[0], modules, 1)
                  || !EditUserSubCommand.removeCollectedDays(sender, args[0], modules)) {
                  return true;
               }

               ChatColorHandler.sendMessage(
                  sender,
                  LushRewards.getInstance()
                     .getConfigManager()
                     .getMessage("set-days-confirm")
                     .replace("%target%", args[0])
                     .replace("%day%", "1")
                     .replace("%module%", String.join(", ", modules.stream().map(Module::getId).toList()))
               );
         }

         return true;
      }

      @Override
      public List<String> tabComplete(
         @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args, @NotNull String[] fullArgs
      ) {
         return args.length == 1 ? Bukkit.getOnlinePlayers().stream().<String>map(Player::getName).toList() : null;
      }
   }

   private static class SetDaysSubCommand extends SubCommand {
      public SetDaysSubCommand() {
         super("set-days");
         this.addRequiredPermission("lushrewards.edituser.setdays");
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
                     .replace("%command-usage%", "/rewards edit-user <module-id> set-days <player> <day-num>")
               );
               break;
            case 2:
               ChatColorHandler.sendMessage(
                  sender,
                  LushRewards.getInstance()
                     .getConfigManager()
                     .getMessage("confirm-command")
                     .replace("%command%", String.format("/rewards edit-user <module-id> set-days %s %s confirm", args[0], args[1]))
               );
               break;
            case 3:
               if (!args[2].equalsIgnoreCase("confirm")) {
                  ChatColorHandler.sendMessage(
                     sender,
                     LushRewards.getInstance()
                        .getConfigManager()
                        .getMessage("incorrect-usage")
                        .replace("%command-usage%", "/rewards edit-user <module-id> set-days <player> <day-num> confirm")
                  );
                  return true;
               }

               int dayNum;
               try {
                  dayNum = Integer.parseInt(args[1]);
               } catch (NumberFormatException var8) {
                  ChatColorHandler.sendMessage(
                     sender,
                     LushRewards.getInstance()
                        .getConfigManager()
                        .getMessage("incorrect-usage")
                        .replace("%command-usage%", "/rewards edit-user <module-id> set-days <player> <day-num> confirm")
                  );
                  return true;
               }

               List<RewardModule> modules = EditUserSubCommand.getModules(fullArgs[1]);
               if (!EditUserSubCommand.setDay(sender, args[0], modules, dayNum)) {
                  return true;
               }

               ChatColorHandler.sendMessage(
                  sender,
                  LushRewards.getInstance()
                     .getConfigManager()
                     .getMessage("set-days-confirm")
                     .replace("%target%", args[0])
                     .replace("%day%", String.valueOf(dayNum))
                     .replace("%module%", String.join(", ", modules.stream().map(Module::getId).toList()))
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
   }

   private static class SetStreakSubCommand extends SubCommand {
      public SetStreakSubCommand() {
         super("set-streak");
         this.addRequiredPermission("lushrewards.edituser.setstreak");
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
                     .replace("%command-usage%", "/rewards edit-user <module-id> set-streak <player> <streak>")
               );
               break;
            case 2:
               ChatColorHandler.sendMessage(
                  sender,
                  LushRewards.getInstance()
                     .getConfigManager()
                     .getMessage("confirm-command")
                     .replace("%command%", String.format("/rewards edit-user <module-id> set-streak %s %s confirm", args[0], args[1]))
               );
               break;
            case 3:
               if (!args[2].equalsIgnoreCase("confirm")) {
                  ChatColorHandler.sendMessage(
                     sender,
                     LushRewards.getInstance()
                        .getConfigManager()
                        .getMessage("incorrect-usage")
                        .replace("%command-usage%", "/rewards edit-user <module-id> set-streak <player> <streak> confirm")
                  );
                  return true;
               }

               int streak;
               try {
                  streak = Integer.parseInt(args[1]);
               } catch (NumberFormatException var8) {
                  ChatColorHandler.sendMessage(
                     sender,
                     LushRewards.getInstance()
                        .getConfigManager()
                        .getMessage("incorrect-usage")
                        .replace("%command-usage%", "/rewards edit-user <module-id> set-streak <player> <streak> confirm")
                  );
                  return true;
               }

               List<RewardModule> modules = EditUserSubCommand.getModules(fullArgs[1]);
               if (!EditUserSubCommand.setStreak(sender, args[0], modules, streak)) {
                  return true;
               }

               ChatColorHandler.sendMessage(
                  sender,
                  LushRewards.getInstance()
                     .getConfigManager()
                     .getMessage("set-streak-confirm")
                     .replace("%target%", args[0])
                     .replace("%streak%", String.valueOf(streak))
                     .replace("%module%", String.join(", ", modules.stream().map(Module::getId).toList()))
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
   }
}
