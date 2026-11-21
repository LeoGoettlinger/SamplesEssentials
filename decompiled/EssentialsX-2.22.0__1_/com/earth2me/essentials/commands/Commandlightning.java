package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.User;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import org.bukkit.Server;
import org.bukkit.entity.LightningStrike;

public class Commandlightning extends EssentialsLoopCommand {
   public Commandlightning() {
      super("lightning");
   }

   @Override
   public void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
      if (args.length != 0 && sender.isAuthorized("essentials.lightning.others")) {
         int power = 5;
         if (args.length > 1) {
            try {
               power = Integer.parseInt(args[1]);
            } catch (NumberFormatException var7) {
            }
         }

         int finalPower = power;
         this.loopOnlinePlayersConsumer(server, sender, false, true, args[0], player -> {
            sender.sendTl("lightningUse", player.getDisplayName());
            LightningStrike strike = player.getBase().getWorld().strikeLightningEffect(player.getBase().getLocation());
            if (!player.isGodModeEnabled()) {
               player.getBase().damage(finalPower, strike);
            }

            if (this.ess.getSettings().warnOnSmite()) {
               player.sendTl("lightningSmited");
            }
         });
         this.loopOnlinePlayers(server, sender, true, true, args[0], null);
      } else if (sender.isPlayer()) {
         sender.getPlayer().getWorld().strikeLightning(sender.getUser().getTargetBlock(600).getLocation());
      } else {
         throw new NotEnoughArgumentsException();
      }
   }

   @Override
   protected void updatePlayer(final Server server, final CommandSource sender, final User matchUser, final String[] args) {
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final User user, final String commandLabel, final String[] args) {
      return !user.isAuthorized("essentials.lightning.others") ? Collections.emptyList() : super.getTabCompleteOptions(server, user, commandLabel, args);
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String[] args) {
      if (args.length == 1) {
         return this.getPlayers(sender);
      } else {
         return (List<String>)(args.length == 2 ? Lists.newArrayList(new String[]{"5"}) : Collections.emptyList());
      }
   }
}
