package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.User;
import com.earth2me.essentials.adventure.AdventureUtil;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;

public class Commandnear extends EssentialsCommand {
   public Commandnear() {
      super("near");
   }

   @Override
   protected void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
      long maxRadius = this.ess.getSettings().getNearRadius();
      if (maxRadius == 0L) {
         maxRadius = 200L;
      }

      long radius = maxRadius;
      User otherUser = null;
      if (args.length > 0) {
         try {
            radius = Long.parseLong(args[0]);
         } catch (NumberFormatException var14) {
            try {
               otherUser = this.getPlayer(server, user, args, 0);
            } catch (Exception var13) {
            }
         }

         if (args.length > 1 && otherUser != null) {
            try {
               radius = Long.parseLong(args[1]);
            } catch (NumberFormatException var12) {
            }
         }
      }

      radius = Math.abs(radius);
      if (radius > maxRadius && !user.isAuthorized("essentials.near.maxexempt")) {
         user.sendTl("radiusTooBig", maxRadius);
         radius = maxRadius;
      }

      if (otherUser == null || !user.isAuthorized("essentials.near.others")) {
         otherUser = user;
      }

      user.sendTl("nearbyPlayers", AdventureUtil.parsed(this.getLocal(user.getSource(), otherUser, radius)));
   }

   @Override
   protected void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
      if (args.length == 0) {
         throw new NotEnoughArgumentsException();
      } else {
         User otherUser = this.getPlayer(server, args, 0, true, false);
         long radius = 200L;
         if (args.length > 1) {
            try {
               radius = Long.parseLong(args[1]);
            } catch (NumberFormatException var9) {
            }
         }

         sender.sendTl("nearbyPlayers", AdventureUtil.parsed(this.getLocal(sender, otherUser, radius)));
      }
   }

   private String getLocal(final CommandSource source, final User user, final long radius) {
      Location loc = user.getLocation();
      World world = loc.getWorld();
      StringBuilder output = new StringBuilder();
      long radiusSquared = radius * radius;
      boolean showHidden = user.canInteractVanished();
      Queue<User> nearbyPlayers = new PriorityQueue<>((o1, o2) -> (int)(o1.getLocation().distanceSquared(loc) - o2.getLocation().distanceSquared(loc)));

      for (User player : this.ess.getOnlineUsers()) {
         if (!player.equals(user)
            && !player.isAuthorized("essentials.near.exclude")
            && (!player.isHidden(user.getBase()) || showHidden || !player.isHiddenFrom(user.getBase()))) {
            Location playerLoc = player.getLocation();
            if (playerLoc.getWorld() == world) {
               long delta = (long)playerLoc.distanceSquared(loc);
               if (delta < radiusSquared) {
                  nearbyPlayers.offer(player);
               }
            }
         }
      }

      while (!nearbyPlayers.isEmpty()) {
         if (output.length() > 0) {
            output.append(", ");
         }

         User nearbyPlayer = nearbyPlayers.poll();
         if (nearbyPlayer != null) {
            output.append(user.playerTl("nearbyPlayersList", nearbyPlayer.getDisplayName(), (long)nearbyPlayer.getLocation().distance(loc)));
         }
      }

      return output.length() > 1 ? output.toString() : source.tl("none");
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final User user, final String commandLabel, final String[] args) {
      if (user.isAuthorized("essentials.near.others")) {
         if (args.length == 1) {
            return this.getPlayers(user);
         } else {
            return (List<String>)(args.length == 2
               ? Lists.newArrayList(new String[]{Integer.toString(this.ess.getSettings().getNearRadius())})
               : Collections.emptyList());
         }
      } else {
         return (List<String>)(args.length == 1
            ? Lists.newArrayList(new String[]{Integer.toString(this.ess.getSettings().getNearRadius())})
            : Collections.emptyList());
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String[] args) {
      if (args.length == 1) {
         return this.getPlayers(sender);
      } else {
         return (List<String>)(args.length == 2
            ? Lists.newArrayList(new String[]{Integer.toString(this.ess.getSettings().getNearRadius())})
            : Collections.emptyList());
      }
   }
}
