package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.IUser;
import com.earth2me.essentials.User;
import com.earth2me.essentials.craftbukkit.SetExpFix;
import com.earth2me.essentials.utils.NumberUtil;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.bukkit.Server;

public class Commandexp extends EssentialsLoopCommand {
   public Commandexp() {
      super("exp");
   }

   @Override
   public void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
      IUser user = sender.getUser();
      if (args.length != 0 && (args.length >= 2 || user != null)) {
         Commandexp.ExpCommands cmd;
         try {
            cmd = Commandexp.ExpCommands.valueOf(args[0].toUpperCase(Locale.ENGLISH));
         } catch (Exception var8) {
            throw new NotEnoughArgumentsException(var8);
         }

         if (!cmd.hasPermission(user)) {
            user.sendTl("noAccessSubCommand", "/" + commandLabel + " " + cmd.name().toLowerCase(Locale.ENGLISH));
         } else {
            switch (cmd) {
               case SET:
                  if (args.length == 3 && cmd.hasOtherPermission(user)) {
                     this.loopOnlinePlayersConsumer(server, sender, false, true, args[1], player -> this.setExp(sender, player, args[2], false));
                  } else {
                     if (args.length != 2 || user == null) {
                        throw new NotEnoughArgumentsException();
                     }

                     this.setExp(sender, user, args[1], false);
                  }

                  return;
               case GIVE:
                  if (args.length == 3 && cmd.hasOtherPermission(user)) {
                     this.loopOnlinePlayersConsumer(server, sender, false, true, args[1], player -> this.setExp(sender, player, args[2], true));
                  } else {
                     if (args.length != 2 || user == null) {
                        throw new NotEnoughArgumentsException();
                     }

                     this.setExp(sender, user, args[1], true);
                  }

                  return;
               case TAKE:
                  if (args.length == 3 && cmd.hasOtherPermission(user)) {
                     this.loopOnlinePlayersConsumer(server, sender, false, true, args[1], player -> this.setExp(sender, player, "-" + args[2], true));
                  } else {
                     if (args.length != 2) {
                        throw new NotEnoughArgumentsException();
                     }

                     this.setExp(sender, user, "-" + args[1], true);
                  }

                  return;
               case RESET:
                  if (args.length == 2 && cmd.hasOtherPermission(user)) {
                     this.loopOnlinePlayersConsumer(server, sender, false, true, args[1], player -> this.setExp(sender, player, "0", false));
                  } else {
                     if (user == null) {
                        throw new NotEnoughArgumentsException();
                     }

                     this.setExp(sender, user, "0", false);
                  }

                  return;
               case SHOW:
                  if (args.length != 2 || user != null && !user.isAuthorized("essentials.exp.others")) {
                     if (user == null) {
                        throw new NotEnoughArgumentsException();
                     }

                     this.showExp(sender, user);
                  } else {
                     this.showExp(sender, this.getPlayer(server, sender, args[1]));
                  }

                  return;
               default:
                  throw new NotEnoughArgumentsException();
            }
         }
      } else if (user == null) {
         throw new NotEnoughArgumentsException();
      } else {
         this.showExp(sender, user);
      }
   }

   private void showExp(final CommandSource sender, final IUser target) {
      sender.sendTl(
         "exp",
         target.getDisplayName(),
         SetExpFix.getTotalExperience(target.getBase()),
         target.getBase().getLevel(),
         SetExpFix.getExpUntilNextLevel(target.getBase())
      );
   }

   private void setExp(final CommandSource sender, final IUser target, String strAmount, final boolean give) throws NotEnoughArgumentsException {
      strAmount = strAmount.toLowerCase(Locale.ENGLISH);
      long amount;
      if (strAmount.contains("l")) {
         strAmount = strAmount.replaceAll("l", "");
         int neededLevel = Integer.parseInt(strAmount);
         if (give) {
            neededLevel += target.getBase().getLevel();
         }

         amount = SetExpFix.getExpToLevel(neededLevel);
         SetExpFix.setTotalExperience(target.getBase(), 0);
      } else {
         amount = Long.parseLong(strAmount);
         if (amount > 2147483647L || amount < -2147483648L) {
            throw new NotEnoughArgumentsException();
         }
      }

      if (give) {
         amount += SetExpFix.getTotalExperience(target.getBase());
      }

      if (amount > 2147483647L) {
         amount = 2147483647L;
      }

      if (amount < 0L) {
         amount = 0L;
      }

      SetExpFix.setTotalExperience(target.getBase(), (int)amount);
      sender.sendTl("expSet", target.getDisplayName(), amount);
   }

   @Override
   protected void updatePlayer(final Server server, final CommandSource sender, final User user, final String[] args) {
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final User user, final String commandLabel, final String[] args) {
      if (args.length == 1) {
         List<String> options = Lists.newArrayList(new String[]{"show"});

         for (Commandexp.ExpCommands cmd : Commandexp.ExpCommands.values()) {
            if (cmd.hasPermission(user)) {
               options.add(cmd.name().toLowerCase(Locale.ENGLISH));
            }
         }

         return options;
      } else {
         if (args.length == 2) {
            if (args[0].equalsIgnoreCase("set") && user.isAuthorized("essentials.exp.set")
               || args[0].equalsIgnoreCase("give") && user.isAuthorized("essentials.exp.give")
               || args[0].equalsIgnoreCase("take") && user.isAuthorized("essentials.exp.take")) {
               String levellessArg = args[1].toLowerCase(Locale.ENGLISH).replaceAll("l", "");
               if (NumberUtil.isInt(levellessArg)) {
                  return Lists.newArrayList(new String[]{levellessArg + "l"});
               }
            }

            if (user.isAuthorized("essentials.exp.others")) {
               return this.getPlayers(user);
            }
         } else if (args.length == 3 && !args[0].equalsIgnoreCase("show") && !args[0].equalsIgnoreCase("reset")) {
            String levellessArg = args[2].toLowerCase(Locale.ENGLISH).replaceAll("l", "");
            if (NumberUtil.isInt(levellessArg)) {
               return Lists.newArrayList(new String[]{levellessArg + "l"});
            }
         }

         return Collections.emptyList();
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String[] args) {
      if (args.length == 1) {
         List<String> list = new ArrayList<>();

         for (Commandexp.ExpCommands cmd : Commandexp.ExpCommands.values()) {
            list.add(cmd.name().toLowerCase(Locale.ENGLISH));
         }

         return list;
      } else if (args.length == 2) {
         if (!args[0].equalsIgnoreCase("set") && !args[0].equalsIgnoreCase("give")) {
            return this.getPlayers(sender);
         } else {
            String levellessArg = args[1].toLowerCase(Locale.ENGLISH).replace("l", "");
            return (List<String>)(NumberUtil.isInt(levellessArg) ? Lists.newArrayList(new String[]{levellessArg, args[1] + "l"}) : Collections.emptyList());
         }
      } else {
         return args.length != 3 || !args[0].equalsIgnoreCase("set") && !args[0].equalsIgnoreCase("give") ? Collections.emptyList() : this.getPlayers(sender);
      }
   }

   private static enum ExpCommands {
      SET,
      GIVE,
      TAKE,
      RESET,
      SHOW(false);

      private final boolean permCheck;

      private ExpCommands() {
         this.permCheck = true;
      }

      private ExpCommands(final boolean perm) {
         this.permCheck = perm;
      }

      boolean hasPermission(final IUser user) {
         return user == null || !this.permCheck || user.isAuthorized("essentials.exp." + this.name().toLowerCase(Locale.ENGLISH));
      }

      boolean hasOtherPermission(final IUser user) {
         return user == null || user.isAuthorized("essentials.exp." + this.name().toLowerCase(Locale.ENGLISH) + ".others");
      }
   }
}
