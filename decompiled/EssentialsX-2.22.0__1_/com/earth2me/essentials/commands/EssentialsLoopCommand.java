package com.earth2me.essentials.commands;

import com.earth2me.essentials.ChargeException;
import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.FormatUtil;
import com.earth2me.essentials.utils.StringUtil;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import net.ess3.api.MaxMoneyException;
import net.ess3.api.TranslatableException;
import org.bukkit.Server;
import org.bukkit.entity.Player;

public abstract class EssentialsLoopCommand extends EssentialsCommand {
   public EssentialsLoopCommand(final String command) {
      super(command);
   }

   protected void loopOfflinePlayers(
      final Server server,
      final CommandSource sender,
      final boolean multipleStringMatches,
      final boolean matchWildcards,
      final String searchTerm,
      final String[] commandArgs
   ) throws TranslatableException, NotEnoughArgumentsException {
      this.loopOfflinePlayersConsumer(
         server, sender, multipleStringMatches, matchWildcards, searchTerm, user -> this.updatePlayer(server, sender, user, commandArgs)
      );
   }

   protected void loopOfflinePlayersConsumer(
      final Server server,
      final CommandSource sender,
      final boolean multipleStringMatches,
      final boolean matchWildcards,
      final String searchTerm,
      final EssentialsLoopCommand.UserConsumer userConsumer
   ) throws TranslatableException, NotEnoughArgumentsException {
      if (searchTerm.isEmpty()) {
         throw new PlayerNotFoundException();
      } else if (!sender.isPlayer() || !searchTerm.equals("@s") && !searchTerm.equals("@p")) {
         UUID uuid = StringUtil.toUUID(searchTerm);
         if (uuid != null) {
            User matchedUser = this.ess.getUser(uuid);
            if (matchedUser == null) {
               throw new PlayerNotFoundException();
            }

            userConsumer.accept(matchedUser);
         } else if (matchWildcards && searchTerm.contentEquals("**")) {
            for (UUID u : this.ess.getUsers().getAllUserUUIDs()) {
               User user = this.ess.getUsers().loadUncachedUser(u);
               if (user != null) {
                  userConsumer.accept(user);
               }
            }
         } else if (matchWildcards && searchTerm.contentEquals("*")) {
            boolean skipHidden = sender.isPlayer() && !this.ess.getUser(sender.getPlayer()).canInteractVanished();

            for (User onlineUser : this.ess.getOnlineUsers()) {
               if (!skipHidden || !onlineUser.isHidden(sender.getPlayer()) || !onlineUser.isHiddenFrom(sender.getPlayer())) {
                  userConsumer.accept(onlineUser);
               }
            }
         } else if (multipleStringMatches) {
            if (searchTerm.trim().length() < 3) {
               throw new PlayerNotFoundException();
            }

            List<Player> matchedPlayers = server.matchPlayer(searchTerm);
            if (matchedPlayers.isEmpty()) {
               User matchedUser = this.getPlayer(server, searchTerm, true, true);
               userConsumer.accept(matchedUser);
            }

            for (Player matchPlayer : matchedPlayers) {
               User matchedUser = this.ess.getUser(matchPlayer);
               userConsumer.accept(matchedUser);
            }
         } else {
            User user = this.getPlayer(server, searchTerm, true, true);
            userConsumer.accept(user);
         }
      } else {
         userConsumer.accept((User)sender.getUser());
      }
   }

   protected void loopOnlinePlayers(
      final Server server,
      final CommandSource sender,
      final boolean multipleStringMatches,
      final boolean matchWildcards,
      final String searchTerm,
      final String[] commandArgs
   ) throws TranslatableException, NotEnoughArgumentsException {
      this.loopOnlinePlayersConsumer(
         server, sender, multipleStringMatches, matchWildcards, searchTerm, user -> this.updatePlayer(server, sender, user, commandArgs)
      );
   }

   protected void loopOnlinePlayersConsumer(
      final Server server,
      final CommandSource sender,
      final boolean multipleStringMatches,
      final boolean matchWildcards,
      final String searchTerm,
      final EssentialsLoopCommand.UserConsumer userConsumer
   ) throws NotEnoughArgumentsException, TranslatableException {
      if (searchTerm.isEmpty()) {
         throw new PlayerNotFoundException();
      } else if (!sender.isPlayer() || !searchTerm.equals("@s") && !searchTerm.equals("@p")) {
         boolean skipHidden = sender.isPlayer() && !this.ess.getUser(sender.getPlayer()).canInteractVanished();
         if (matchWildcards && (searchTerm.contentEquals("**") || searchTerm.contentEquals("*"))) {
            for (User onlineUser : this.ess.getOnlineUsers()) {
               if (!skipHidden || !onlineUser.isHidden(sender.getPlayer()) || !onlineUser.isHiddenFrom(sender.getPlayer())) {
                  userConsumer.accept(onlineUser);
               }
            }
         } else if (multipleStringMatches) {
            if (searchTerm.trim().length() < 2) {
               throw new PlayerNotFoundException();
            }

            boolean foundUser = false;
            List<Player> matchedPlayers = server.matchPlayer(searchTerm);
            if (matchedPlayers.isEmpty()) {
               String matchText = searchTerm.toLowerCase(Locale.ENGLISH);

               for (User player : this.ess.getOnlineUsers()) {
                  if (!skipHidden || !player.isHidden(sender.getPlayer()) || !player.isHiddenFrom(sender.getPlayer())) {
                     String displayName = FormatUtil.stripFormat(player.getDisplayName()).toLowerCase(Locale.ENGLISH);
                     if (displayName.contains(matchText)) {
                        foundUser = true;
                        userConsumer.accept(player);
                     }
                  }
               }
            } else {
               for (Player matchPlayer : matchedPlayers) {
                  User playerx = this.ess.getUser(matchPlayer);
                  if (!skipHidden || !playerx.isHidden(sender.getPlayer()) || !playerx.isHiddenFrom(sender.getPlayer())) {
                     foundUser = true;
                     userConsumer.accept(playerx);
                  }
               }
            }

            if (!foundUser) {
               throw new PlayerNotFoundException();
            }
         } else {
            User playerx = this.getPlayer(server, sender, searchTerm);
            userConsumer.accept(playerx);
         }
      } else {
         userConsumer.accept((User)sender.getUser());
      }
   }

   protected abstract void updatePlayer(Server server, CommandSource sender, User user, String[] args) throws NotEnoughArgumentsException, PlayerExemptException, ChargeException, MaxMoneyException;

   @Override
   protected List<String> getPlayers(final CommandSource interactor) {
      List<String> players = super.getPlayers(interactor);
      players.add("**");
      players.add("*");
      return players;
   }

   @Override
   protected List<String> getPlayers(final User interactor) {
      List<String> players = super.getPlayers(interactor);
      players.add("**");
      players.add("*");
      return players;
   }

   public interface UserConsumer {
      void accept(User user) throws NotEnoughArgumentsException, TranslatableException;
   }
}
