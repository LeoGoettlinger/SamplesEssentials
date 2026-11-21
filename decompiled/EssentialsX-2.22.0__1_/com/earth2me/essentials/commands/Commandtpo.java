package com.earth2me.essentials.commands;

import com.earth2me.essentials.User;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.ess3.api.TranslatableException;
import org.bukkit.Server;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class Commandtpo extends EssentialsCommand {
   public Commandtpo() {
      super("tpo");
   }

   @Override
   public void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
      switch (args.length) {
         case 0:
            throw new NotEnoughArgumentsException();
         case 1:
            User player = this.getPlayer(server, user, args, 0);
            if (user.getWorld() != player.getWorld()
               && this.ess.getSettings().isWorldTeleportPermissions()
               && !user.isAuthorized("essentials.worlds." + player.getWorld().getName())) {
               throw new TranslatableException("noPerm", "essentials.worlds." + player.getWorld().getName());
            }

            user.getAsyncTeleport().now(player.getBase(), false, TeleportCause.COMMAND, this.getNewExceptionFuture(user.getSource(), commandLabel));
            break;
         default:
            if (!user.isAuthorized("essentials.tp.others")) {
               throw new TranslatableException("noPerm", "essentials.tp.others");
            }

            User target = this.getPlayer(server, user, args, 0);
            User toPlayer = this.getPlayer(server, user, args, 1);
            if (target.getWorld() != toPlayer.getWorld()
               && this.ess.getSettings().isWorldTeleportPermissions()
               && !user.isAuthorized("essentials.worlds." + toPlayer.getWorld().getName())) {
               throw new TranslatableException("noPerm", "essentials.worlds." + toPlayer.getWorld().getName());
            }

            CompletableFuture<Boolean> future = this.getNewExceptionFuture(user.getSource(), commandLabel);
            target.getAsyncTeleport().now(toPlayer.getBase(), false, TeleportCause.COMMAND, future);
            future.thenAccept(success -> {
               if (success) {
                  target.sendTl("teleportAtoB", user.getDisplayName(), toPlayer.getDisplayName());
               }
            });
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final User user, final String commandLabel, final String[] args) {
      return args.length != 1 && (args.length != 2 || !user.isAuthorized("essentials.tp.others")) ? Collections.emptyList() : this.getPlayers(user);
   }
}
