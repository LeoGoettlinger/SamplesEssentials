package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.IUser;
import com.earth2me.essentials.User;
import com.earth2me.essentials.adventure.AdventureUtil;
import com.earth2me.essentials.utils.DescParseTickFormat;
import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;
import org.bukkit.Server;
import org.bukkit.World;

public class Commandptime extends EssentialsLoopCommand {
   private static final List<String> getAliases = Arrays.asList("get", "list", "show", "display");

   public Commandptime() {
      super("ptime");
   }

   @Override
   public void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
      if (args.length == 0 || getAliases.contains(args[0].toLowerCase())) {
         if (args.length > 1) {
            if (args[1].equals("*") || args[1].equals("**")) {
               sender.sendTl("pTimePlayers");
            }

            this.loopOnlinePlayersConsumer(server, sender, false, true, args[1], player -> this.getUserTime(sender, player));
            return;
         }

         if (args.length == 1 || sender.isPlayer()) {
            if (sender.isPlayer()) {
               this.getUserTime(sender, sender.getUser());
               return;
            }

            throw new NotEnoughArgumentsException();
         }

         if (this.ess.getOnlinePlayers().size() > 1) {
            sender.sendTl("pTimePlayers");
         }

         for (User player : this.ess.getOnlineUsers()) {
            this.getUserTime(sender, player);
         }
      }

      if (args.length > 1 && !sender.isAuthorized("essentials.ptime.others") && !args[1].equalsIgnoreCase(sender.getSelfSelector())) {
         sender.sendTl("pTimeOthersPermission");
      } else {
         String time = args[0];
         boolean fixed = time.startsWith("@");
         if (fixed) {
            time = time.substring(1);
         }

         Long ticks;
         if (DescParseTickFormat.meansReset(time)) {
            ticks = null;
         } else {
            try {
               ticks = DescParseTickFormat.parse(time);
            } catch (NumberFormatException var10) {
               throw new NotEnoughArgumentsException(var10);
            }
         }

         StringJoiner joiner = new StringJoiner(", ");
         this.loopOnlinePlayersConsumer(server, sender, false, true, args.length > 1 ? args[1] : sender.getSelfSelector(), player -> {
            this.setUserTime(player, ticks, !fixed);
            joiner.add(player.getName());
         });
         if (ticks == null) {
            sender.sendTl("pTimeReset", joiner.toString());
         } else {
            String formattedTime = DescParseTickFormat.format(ticks);
            sender.sendTl(fixed ? "pTimeSetFixed" : "pTimeSet", AdventureUtil.parsed(formattedTime), joiner.toString());
         }
      }
   }

   public void getUserTime(final CommandSource sender, final IUser user) {
      if (user != null) {
         if (user.getBase().getPlayerTimeOffset() == 0L) {
            sender.sendTl("pTimeNormal", user.getName());
         } else {
            String time = DescParseTickFormat.format(user.getBase().getPlayerTime());
            sender.sendTl(user.getBase().isPlayerTimeRelative() ? "pTimeCurrent" : "pTimeCurrentFixed", user.getName(), AdventureUtil.parsed(time));
         }
      }
   }

   private void setUserTime(final User user, final Long ticks, final Boolean relative) {
      if (ticks == null) {
         user.getBase().resetPlayerTime();
      } else {
         World world = user.getWorld();
         long time = user.getBase().getPlayerTime();
         time -= time % 24000L;
         time += 24000L + ticks;
         if (relative) {
            time -= world.getTime();
         }

         user.getBase().setPlayerTime(time, relative);
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String[] args) {
      User user = this.ess.getUser(sender.getPlayer());
      if (args.length == 1) {
         return Lists.newArrayList(new String[]{"get", "reset", "sunrise", "day", "morning", "noon", "afternoon", "sunset", "night", "midnight"});
      } else {
         return args.length != 2 || !getAliases.contains(args[0]) && user != null && !user.isAuthorized("essentials.ptime.others")
            ? Collections.emptyList()
            : this.getPlayers(sender);
      }
   }

   @Override
   protected void updatePlayer(final Server server, final CommandSource sender, final User user, final String[] args) {
   }
}
