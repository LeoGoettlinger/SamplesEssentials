package com.earth2me.essentials.commands;

import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import java.util.Collections;
import java.util.List;
import net.ess3.api.TranslatableException;
import org.bukkit.Server;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class Commandtphere extends EssentialsCommand {
   public Commandtphere() {
      super("tphere");
   }

   @Override
   public void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
      User player = this.getPlayer(server, user, args, 0);
      if (!player.isTeleportEnabled()) {
         throw new TranslatableException("teleportDisabled", player.getDisplayName());
      } else if (user.getWorld() != player.getWorld()
         && this.ess.getSettings().isWorldTeleportPermissions()
         && !user.isAuthorized("essentials.worlds." + user.getWorld().getName())) {
         throw new TranslatableException("noPerm", "essentials.worlds." + user.getWorld().getName());
      } else {
         user.getAsyncTeleport()
            .teleportPlayer(
               player, user.getBase(), new Trade(this.getName(), this.ess), TeleportCause.COMMAND, this.getNewExceptionFuture(user.getSource(), commandLabel)
            );
         throw new NoChargeException();
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final User user, final String commandLabel, final String[] args) {
      return args.length == 1 ? this.getPlayers(user) : Collections.emptyList();
   }
}
