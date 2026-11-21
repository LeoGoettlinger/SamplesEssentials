package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.User;
import com.earth2me.essentials.adventure.AdventureUtil;
import com.earth2me.essentials.utils.NumberUtil;
import java.util.Collections;
import java.util.List;
import org.bukkit.Server;

public class Commandbalance extends EssentialsCommand {
   public Commandbalance() {
      super("balance");
   }

   @Override
   protected void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
      if (args.length == 0) {
         throw new NotEnoughArgumentsException();
      } else {
         User target = this.getPlayer(server, args, 0, true, true);
         sender.sendTl(
            "balanceOther",
            target.isHidden() ? target.getName() : target.getDisplayName(),
            AdventureUtil.parsed(NumberUtil.displayCurrency(target.getMoney(), this.ess))
         );
      }
   }

   @Override
   public void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
      if (args.length == 1 && user.isAuthorized("essentials.balance.others")) {
         User target = this.getPlayer(server, args, 0, false, true);
         user.sendTl(
            "balanceOther",
            target.isHidden() ? target.getName() : target.getDisplayName(),
            AdventureUtil.parsed(NumberUtil.displayCurrency(target.getMoney(), this.ess))
         );
      } else {
         if (args.length >= 2) {
            throw new NotEnoughArgumentsException();
         }

         user.sendTl("balance", AdventureUtil.parsed(NumberUtil.displayCurrency(user.getMoney(), this.ess)));
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String[] args) {
      return args.length == 1 && sender.isAuthorized("essentials.balance.others") ? this.getPlayers(sender) : Collections.emptyList();
   }
}
