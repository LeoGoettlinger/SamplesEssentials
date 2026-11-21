package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.User;
import java.util.Collections;
import java.util.List;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class Commandfeed extends EssentialsLoopCommand {
   public Commandfeed() {
      super("feed");
   }

   @Override
   protected void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
      if (!user.isAuthorized("essentials.feed.cooldown.bypass")) {
         user.healCooldown();
      }

      if (args.length > 0 && user.isAuthorized("essentials.feed.others")) {
         this.loopOnlinePlayers(server, user.getSource(), true, true, args[0], null);
      } else {
         this.feedPlayer(user.getBase());
         user.sendTl("feed");
      }
   }

   @Override
   protected void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         this.loopOnlinePlayers(server, sender, true, true, args[0], null);
      }
   }

   @Override
   protected void updatePlayer(final Server server, final CommandSource sender, final User player, final String[] args) throws PlayerExemptException {
      try {
         this.feedPlayer(player.getBase());
         sender.sendTl("feedOther", player.getDisplayName());
      } catch (QuietAbortException var6) {
      }
   }

   private void feedPlayer(final Player player) throws QuietAbortException {
      int amount = 30;
      FoodLevelChangeEvent flce = new FoodLevelChangeEvent(player, 30);
      this.ess.getServer().getPluginManager().callEvent(flce);
      if (flce.isCancelled()) {
         throw new QuietAbortException();
      } else {
         player.setFoodLevel(Math.min(flce.getFoodLevel(), 20));
         player.setSaturation(10.0F);
         player.setExhaustion(0.0F);
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String[] args) {
      return args.length == 1 && sender.isAuthorized("essentials.feed.others") ? this.getPlayers(sender) : Collections.emptyList();
   }
}
