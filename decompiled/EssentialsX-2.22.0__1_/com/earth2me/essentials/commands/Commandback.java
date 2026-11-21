package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import java.util.Collections;
import java.util.List;
import net.ess3.api.TranslatableException;
import org.bukkit.Server;

public class Commandback extends EssentialsCommand {
   public Commandback() {
      super("back");
   }

   @Override
   protected void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
      CommandSource sender = user.getSource();
      if (args.length > 0 && user.isAuthorized("essentials.back.others")) {
         this.parseOthers(server, sender, args, commandLabel);
      } else {
         this.teleportBack(sender, user, commandLabel);
      }
   }

   @Override
   protected void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
      if (args.length == 0) {
         throw new NotEnoughArgumentsException();
      } else {
         this.parseOthers(server, sender, args, commandLabel);
      }
   }

   private void parseOthers(final Server server, final CommandSource sender, final String[] args, final String commandLabel) throws Exception {
      User player = this.getPlayer(server, args, 0, true, false);
      sender.sendTl("backOther", player.getName());
      this.teleportBack(sender, player, commandLabel);
   }

   private void teleportBack(final CommandSource sender, final User user, final String commandLabel) throws Exception {
      if (user.getLastLocation() == null) {
         throw new TranslatableException("noLocationFound");
      } else {
         String lastWorldName = user.getLastLocation().getWorld().getName();
         User requester = null;
         if (sender.isPlayer()) {
            requester = this.ess.getUser(sender.getPlayer());
            if (user.getWorld() != user.getLastLocation().getWorld()
               && this.ess.getSettings().isWorldTeleportPermissions()
               && !user.isAuthorized("essentials.worlds." + lastWorldName)) {
               throw new TranslatableException("noPerm", "essentials.worlds." + lastWorldName);
            }

            if (!requester.isAuthorized("essentials.back.into." + lastWorldName)) {
               throw new TranslatableException("noPerm", "essentials.back.into." + lastWorldName);
            }
         }

         if (requester == null) {
            user.getAsyncTeleport().back(null, null, this.getNewExceptionFuture(sender, commandLabel));
         } else if (!requester.equals(user)) {
            Trade charge = new Trade(this.getName(), this.ess);
            charge.isAffordableFor(requester);
            user.getAsyncTeleport().back(requester, charge, this.getNewExceptionFuture(sender, commandLabel));
         } else {
            Trade charge = new Trade(this.getName(), this.ess);
            charge.isAffordableFor(user);
            user.getAsyncTeleport().back(charge, this.getNewExceptionFuture(sender, commandLabel));
         }

         throw new NoChargeException();
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final User user, final String commandLabel, final String[] args) {
      return user.isAuthorized("essentials.back.others") && args.length == 1 ? this.getPlayers(user) : Collections.emptyList();
   }
}
