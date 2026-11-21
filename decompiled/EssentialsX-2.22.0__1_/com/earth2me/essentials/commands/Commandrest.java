package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.IUser;
import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.VersionUtil;
import java.util.Collections;
import java.util.List;
import org.bukkit.Server;
import org.bukkit.Statistic;

public class Commandrest extends EssentialsLoopCommand {
   public Commandrest() {
      super("rest");
   }

   @Override
   public void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
      if (VersionUtil.PRE_FLATTENING) {
         sender.sendTl("unsupportedFeature");
      } else if (args.length == 0 && !sender.isPlayer()) {
         throw new NotEnoughArgumentsException();
      } else if (args.length > 0 && sender.isAuthorized("essentials.rest.others")) {
         this.loopOnlinePlayers(server, sender, false, true, args[0], null);
      } else {
         this.restPlayer(sender.getUser());
      }
   }

   @Override
   protected void updatePlayer(final Server server, final CommandSource sender, final User player, final String[] args) throws PlayerExemptException {
      this.restPlayer(player);
      sender.sendTl("restOther", player.getDisplayName());
   }

   private void restPlayer(final IUser user) {
      user.getBase().setStatistic(Statistic.TIME_SINCE_REST, 0);
      user.sendTl("rest");
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String[] args) {
      return args.length == 1 && sender.isAuthorized("essentials.rest.others") ? this.getPlayers(sender) : Collections.emptyList();
   }
}
