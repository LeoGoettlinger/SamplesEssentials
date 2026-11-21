package com.earth2me.essentials.commands;

import com.earth2me.essentials.User;
import java.util.Collections;
import java.util.List;
import net.ess3.api.TranslatableException;
import org.bukkit.Server;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class Commandtpohere extends EssentialsCommand {
   public Commandtpohere() {
      super("tpohere");
   }

   @Override
   public void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         User player = this.getPlayer(server, user, args, 0);
         if (user.getWorld() != player.getWorld()
            && this.ess.getSettings().isWorldTeleportPermissions()
            && !user.isAuthorized("essentials.worlds." + user.getWorld().getName())) {
            throw new TranslatableException("noPerm", "essentials.worlds." + user.getWorld().getName());
         } else {
            player.getAsyncTeleport().now(user.getBase(), false, TeleportCause.COMMAND, this.getNewExceptionFuture(user.getSource(), commandLabel));
         }
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final User user, final String commandLabel, final String[] args) {
      return args.length == 1 ? this.getPlayers(user) : Collections.emptyList();
   }
}
