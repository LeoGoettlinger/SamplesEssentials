package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.User;
import java.util.Collections;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Server;

public class Commandgetpos extends EssentialsCommand {
   public Commandgetpos() {
      super("getpos");
   }

   @Override
   public void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
      if (args.length > 0 && user.isAuthorized("essentials.getpos.others")) {
         User otherUser = this.getPlayer(server, user, args, 0);
         this.outputPosition(user.getSource(), otherUser.getLocation(), user.getLocation());
      } else {
         this.outputPosition(user.getSource(), user.getLocation(), null);
      }
   }

   @Override
   protected void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         User user = this.getPlayer(server, args, 0, true, false);
         this.outputPosition(sender, user.getLocation(), null);
      }
   }

   private void outputPosition(final CommandSource sender, final Location coords, final Location distance) {
      sender.sendTl("currentWorld", coords.getWorld().getName());
      sender.sendTl("posX", coords.getBlockX());
      sender.sendTl("posY", coords.getBlockY());
      sender.sendTl("posZ", coords.getBlockZ());
      sender.sendTl("posYaw", (coords.getYaw() + 360.0F) % 360.0F);
      sender.sendTl("posPitch", coords.getPitch());
      if (distance != null && coords.getWorld().equals(distance.getWorld())) {
         sender.sendTl("distance", coords.distance(distance));
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String[] args) {
      return args.length == 1 && sender.isAuthorized("essentials.getpos.others") ? this.getPlayers(sender) : Collections.emptyList();
   }
}
