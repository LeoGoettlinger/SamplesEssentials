package com.earth2me.essentials.commands.essentials;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.User;
import com.earth2me.essentials.commands.EssentialsCommand;
import com.earth2me.essentials.commands.EssentialsTreeNode;
import com.earth2me.essentials.utils.DateUtil;
import com.earth2me.essentials.utils.FloatUtil;
import com.earth2me.essentials.utils.NumberUtil;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class CleanupCommand extends EssentialsTreeNode {
   public CleanupCommand() {
      super("cleanup");
   }

   @Override
   protected void run(final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
      if (args.length >= 1 && NumberUtil.isInt(args[0])) {
         sender.sendTl("cleaning");
         long daysArg = Long.parseLong(args[0]);
         double moneyArg = args.length >= 2 ? FloatUtil.parseDouble(args[1].replaceAll("[^0-9.]", "")) : 0.0;
         int homesArg = args.length >= 3 && NumberUtil.isInt(args[2]) ? Integer.parseInt(args[2]) : 0;
         this.ess
            .runTaskAsynchronously(
               () -> {
                  long currTime = System.currentTimeMillis();

                  for (UUID u : this.ess.getUsers().getAllUserUUIDs()) {
                     User user = this.ess.getUsers().loadUncachedUser(u);
                     if (user != null) {
                        long lastLog = user.getLastLogout();
                        if (lastLog == 0L) {
                           lastLog = user.getLastLogin();
                        }

                        if (lastLog == 0L) {
                           user.setLastLogin(currTime);
                        }

                        if (!user.isNPC()) {
                           long timeDiff = currTime - lastLog;
                           long milliDays = daysArg * 24L * 60L * 60L * 1000L;
                           int homeCount = user.getHomes().size();
                           double moneyCount = user.getMoney().doubleValue();
                           if (lastLog != 0L && timeDiff >= milliDays && homeCount <= homesArg && !(moneyCount > moneyArg)) {
                              if (this.ess.getSettings().isDebug()) {
                                 this.ess
                                    .getLogger()
                                    .info(
                                       "Deleting user: "
                                          + user.getName()
                                          + " Money: "
                                          + moneyCount
                                          + " Homes: "
                                          + homeCount
                                          + " Last seen: "
                                          + DateUtil.formatDateDiff(lastLog)
                                    );
                              }

                              user.reset();
                           }
                        }
                     }
                  }

                  sender.sendTl("cleaned");
               }
            );
      } else {
         sender.sendMessage("This sub-command will delete users who haven't logged in in the last <days> days.");
         sender.sendMessage("Optional parameters define the minimum amount required to prevent deletion.");
         sender.sendMessage("Unless you define larger default values, this command will ignore people who have more than 0 money/homes.");
         throw new Exception("/" + commandLabel + " cleanup <days> [money] [homes]");
      }
   }

   @Override
   protected List<String> tabComplete(CommandSource sender, String commandLabel, String[] args) {
      if (args.length == 1) {
         return EssentialsCommand.COMMON_DURATIONS;
      } else {
         return (List<String>)(args.length != 2 && args.length != 3 ? Collections.emptyList() : Lists.newArrayList(new String[]{"-1", "0"}));
      }
   }
}
