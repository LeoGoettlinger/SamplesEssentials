package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.RandomTeleport;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import net.ess3.api.TranslatableException;
import net.ess3.api.events.UserRandomTeleportEvent;
import org.bukkit.Server;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class Commandtpr extends EssentialsCommand {
   public Commandtpr() {
      super("tpr");
   }

   @Override
   protected void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
      Trade charge = new Trade(this.getName(), this.ess);
      charge.isAffordableFor(user);
      RandomTeleport randomTeleport = this.ess.getRandomTeleport();
      String randomLocationName;
      User target;
      if (args.length == 0) {
         randomLocationName = randomTeleport.getDefaultLocation().replace("{world}", user.getLocation().getWorld().getName());
         target = user;
      } else {
         randomLocationName = args[0];
         if (!randomTeleport.hasLocation(randomLocationName)) {
            throw new TranslatableException("tprNotExist");
         }

         if (randomTeleport.isPerLocationPermission() && !user.isAuthorized("essentials.tpr.location." + randomLocationName)) {
            throw new TranslatableException("tprNoPermission");
         }

         if (args.length > 1 && user.isAuthorized("essentials.tpr.others")) {
            target = this.getPlayer(server, user, args, 1);
         } else {
            target = user;
         }
      }

      UserRandomTeleportEvent event = new UserRandomTeleportEvent(
         target,
         randomLocationName,
         randomTeleport.getCenter(randomLocationName),
         randomTeleport.getMinRange(randomLocationName),
         randomTeleport.getMaxRange(randomLocationName)
      );
      server.getPluginManager().callEvent(event);
      if (!event.isCancelled()) {
         target.sendTl("tprSuccess");
         if (target != user) {
            user.sendTl("tprOtherUser", target.getDisplayName());
         }

         (event.isModified()
               ? randomTeleport.getRandomLocation(event.getCenter(), event.getMinRange(), event.getMaxRange())
               : randomTeleport.getRandomLocation(randomLocationName))
            .thenAccept(location -> {
               CompletableFuture<Boolean> future = this.getNewExceptionFuture(user.getSource(), commandLabel);
               future.thenAccept(success -> {
                  if (success) {
                     target.sendTl("tprSuccessDone");
                  }
               });
               target.getAsyncTeleport().teleport(location, charge, TeleportCause.COMMAND, future);
            });
         throw new NoChargeException();
      }
   }

   @Override
   protected void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
      if (args.length < 2) {
         throw new NotEnoughArgumentsException();
      } else {
         RandomTeleport randomTeleport = this.ess.getRandomTeleport();
         User userToTeleport = this.getPlayer(server, sender, args, 1);
         String potentialLocation = args[0];
         if (!randomTeleport.hasLocation(potentialLocation)) {
            throw new TranslatableException("tprNotExist");
         } else {
            UserRandomTeleportEvent event = new UserRandomTeleportEvent(
               userToTeleport,
               potentialLocation,
               randomTeleport.getCenter(potentialLocation),
               randomTeleport.getMinRange(potentialLocation),
               randomTeleport.getMaxRange(potentialLocation)
            );
            server.getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
               userToTeleport.sendTl("tprSuccess");
               sender.sendTl("tprOtherUser", userToTeleport.getDisplayName());
               (event.isModified()
                     ? randomTeleport.getRandomLocation(event.getCenter(), event.getMinRange(), event.getMaxRange())
                     : randomTeleport.getRandomLocation(potentialLocation))
                  .thenAccept(location -> {
                     CompletableFuture<Boolean> future = this.getNewExceptionFuture(sender, commandLabel);
                     future.thenAccept(success -> {
                        if (success) {
                           userToTeleport.sendTl("tprSuccessDone");
                        }
                     });
                     userToTeleport.getAsyncTeleport().now(location, false, TeleportCause.COMMAND, future);
                  });
            }
         }
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String[] args) {
      RandomTeleport randomTeleport = this.ess.getRandomTeleport();
      if (args.length == 1) {
         return randomTeleport.isPerLocationPermission()
            ? randomTeleport.listLocations().stream().filter(name -> sender.isAuthorized("essentials.tpr.location." + name)).collect(Collectors.toList())
            : randomTeleport.listLocations();
      } else {
         return args.length == 2 && sender.isAuthorized("essentials.tpr.others") ? this.getPlayers(sender) : Collections.emptyList();
      }
   }
}
