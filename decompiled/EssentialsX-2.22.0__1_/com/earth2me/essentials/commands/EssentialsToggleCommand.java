package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.User;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import org.bukkit.Server;
import org.bukkit.entity.Player;

public abstract class EssentialsToggleCommand extends EssentialsCommand {
   final String othersPermission;

   public EssentialsToggleCommand(final String command, final String othersPermission) {
      super(command);
      this.othersPermission = othersPermission;
   }

   protected void handleToggleWithArgs(final Server server, final User user, final String[] args) throws Exception {
      if (args.length == 1) {
         Boolean toggle = this.matchToggleArgument(args[0]);
         if (toggle == null && user.isAuthorized(this.othersPermission)) {
            this.toggleOtherPlayers(server, user.getSource(), args);
         } else {
            this.togglePlayer(user.getSource(), user, toggle);
         }
      } else if (args.length == 2 && user.isAuthorized(this.othersPermission)) {
         this.toggleOtherPlayers(server, user.getSource(), args);
      } else {
         this.togglePlayer(user.getSource(), user, null);
      }
   }

   protected Boolean matchToggleArgument(final String arg) {
      if (arg.equalsIgnoreCase("on") || arg.startsWith("ena") || arg.equalsIgnoreCase("1")) {
         return true;
      } else {
         return !arg.equalsIgnoreCase("off") && !arg.startsWith("dis") && !arg.equalsIgnoreCase("0") ? null : false;
      }
   }

   protected void toggleOtherPlayers(final Server server, final CommandSource sender, final String[] args) throws PlayerNotFoundException, NotEnoughArgumentsException {
      if (args.length >= 1 && args[0].trim().length() >= 2) {
         boolean skipHidden = sender.isPlayer() && !this.ess.getUser(sender.getPlayer()).canInteractVanished();
         boolean foundUser = false;

         for (Player matchPlayer : server.matchPlayer(args[0])) {
            User player = this.ess.getUser(matchPlayer);
            if (!skipHidden || !player.isHidden(sender.getPlayer()) || !player.isHiddenFrom(sender.getPlayer())) {
               foundUser = true;
               if (args.length > 1) {
                  Boolean toggle = this.matchToggleArgument(args[1]);
                  this.togglePlayer(sender, player, toggle);
               } else {
                  this.togglePlayer(sender, player, null);
               }
            }
         }

         if (!foundUser) {
            throw new PlayerNotFoundException();
         }
      } else {
         throw new PlayerNotFoundException();
      }
   }

   protected abstract void togglePlayer(CommandSource sender, User user, Boolean enabled) throws NotEnoughArgumentsException;

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final User user, final String commandLabel, final String[] args) {
      if (args.length == 1) {
         return (List<String>)(user.isAuthorized(this.othersPermission) ? this.getPlayers(user) : Lists.newArrayList(new String[]{"enable", "disable"}));
      } else {
         return (List<String>)(args.length == 2 && user.isAuthorized(this.othersPermission)
            ? Lists.newArrayList(new String[]{"enable", "disable"})
            : Collections.emptyList());
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String[] args) {
      if (args.length == 1) {
         return this.getPlayers(sender);
      } else {
         return (List<String>)(args.length == 2 ? Lists.newArrayList(new String[]{"enable", "disable"}) : Collections.emptyList());
      }
   }
}
