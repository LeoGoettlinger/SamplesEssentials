package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.IUser;
import com.earth2me.essentials.User;
import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.bukkit.GameMode;
import org.bukkit.Server;

public class Commandgamemode extends EssentialsLoopCommand {
   private final List<String> STANDARD_OPTIONS = ImmutableList.of("creative", "survival", "adventure", "spectator", "toggle");

   public Commandgamemode() {
      super("gamemode");
   }

   @Override
   protected void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
      if (args.length == 0) {
         throw new NotEnoughArgumentsException();
      } else {
         if (args.length == 1) {
            this.loopOnlinePlayersConsumer(server, sender, false, true, args[0], user -> this.setUserGamemode(sender, this.matchGameMode(commandLabel), user));
         } else if (args.length == 2) {
            this.loopOnlinePlayersConsumer(server, sender, false, true, args[1], user -> this.setUserGamemode(sender, this.matchGameMode(args[0]), user));
         }
      }
   }

   @Override
   protected void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
      GameMode gameMode;
      if (args.length == 0) {
         gameMode = this.matchGameMode(commandLabel);
      } else {
         if (args.length > 1 && args[1].trim().length() > 2 && user.isAuthorized("essentials.gamemode.others")) {
            this.loopOnlinePlayersConsumer(
               server,
               user.getSource(),
               false,
               true,
               args[1],
               player -> this.setUserGamemode(user.getSource(), this.matchGameMode(args[0].toLowerCase(Locale.ENGLISH)), player)
            );
            return;
         }

         try {
            gameMode = this.matchGameMode(args[0].toLowerCase(Locale.ENGLISH));
         } catch (NotEnoughArgumentsException var7) {
            if (user.isAuthorized("essentials.gamemode.others")) {
               this.loopOnlinePlayersConsumer(
                  server, user.getSource(), false, true, args[0], player -> this.setUserGamemode(user.getSource(), this.matchGameMode(commandLabel), player)
               );
               return;
            }

            throw new NotEnoughArgumentsException();
         }
      }

      if (gameMode == null) {
         gameMode = user.getBase().getGameMode() == GameMode.SURVIVAL
            ? GameMode.CREATIVE
            : (user.getBase().getGameMode() == GameMode.CREATIVE ? GameMode.ADVENTURE : GameMode.SURVIVAL);
      }

      if (this.isProhibitedChange(user, gameMode)) {
         user.sendTl("cantGamemode", user.playerTl(gameMode.toString().toLowerCase(Locale.ENGLISH)));
      } else {
         user.getBase().setGameMode(gameMode);
         user.sendTl("gameMode", user.playerTl(user.getBase().getGameMode().toString().toLowerCase(Locale.ENGLISH)), user.getDisplayName());
      }
   }

   private void setUserGamemode(final CommandSource sender, final GameMode gameMode, final User user) throws NotEnoughArgumentsException {
      if (gameMode == null) {
         throw new NotEnoughArgumentsException(sender.tl("gameModeInvalid"));
      } else if (sender.isPlayer() && this.isProhibitedChange(sender.getUser(), gameMode)) {
         sender.sendTl("cantGamemode", gameMode.name());
      } else {
         user.getBase().setGameMode(gameMode);
         sender.sendTl("gameMode", sender.tl(gameMode.toString().toLowerCase(Locale.ENGLISH)), user.getDisplayName());
      }
   }

   private boolean isProhibitedChange(final IUser user, final GameMode to) {
      return user != null && !user.isAuthorized("essentials.gamemode.all") && !user.isAuthorized("essentials.gamemode." + to.name().toLowerCase());
   }

   private GameMode matchGameMode(String modeString) throws NotEnoughArgumentsException {
      GameMode mode = null;
      modeString = modeString.toLowerCase();
      if (modeString.equalsIgnoreCase("gmc")
         || modeString.equalsIgnoreCase("egmc")
         || modeString.contains("creat")
         || modeString.equalsIgnoreCase("1")
         || modeString.equalsIgnoreCase("c")) {
         mode = GameMode.CREATIVE;
      } else if (modeString.equalsIgnoreCase("gms")
         || modeString.equalsIgnoreCase("egms")
         || modeString.contains("survi")
         || modeString.equalsIgnoreCase("0")
         || modeString.equalsIgnoreCase("s")) {
         mode = GameMode.SURVIVAL;
      } else if (modeString.equalsIgnoreCase("gma")
         || modeString.equalsIgnoreCase("egma")
         || modeString.contains("advent")
         || modeString.equalsIgnoreCase("2")
         || modeString.equalsIgnoreCase("a")) {
         mode = GameMode.ADVENTURE;
      } else if (!modeString.equalsIgnoreCase("gmsp")
         && !modeString.equalsIgnoreCase("egmsp")
         && !modeString.contains("spec")
         && !modeString.equalsIgnoreCase("3")
         && !modeString.equalsIgnoreCase("sp")) {
         if (!modeString.equalsIgnoreCase("gmt")
            && !modeString.equalsIgnoreCase("egmt")
            && !modeString.contains("toggle")
            && !modeString.contains("cycle")
            && !modeString.equalsIgnoreCase("t")) {
            throw new NotEnoughArgumentsException();
         }
      } else {
         mode = GameMode.SPECTATOR;
      }

      return mode;
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String[] args) {
      if (args.length == 1) {
         try {
            this.matchGameMode(commandLabel);
            return this.getPlayers(sender);
         } catch (NotEnoughArgumentsException var6) {
            return this.STANDARD_OPTIONS;
         }
      } else {
         return args.length == 2 ? this.getPlayers(sender) : Collections.emptyList();
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final User user, final String commandLabel, final String[] args) {
      boolean isDirectGamemodeCommand;
      try {
         this.matchGameMode(commandLabel);
         isDirectGamemodeCommand = true;
      } catch (NotEnoughArgumentsException var7) {
         isDirectGamemodeCommand = false;
      }

      if (args.length == 1) {
         return user.isAuthorized("essentials.gamemode.others") && isDirectGamemodeCommand ? this.getPlayers(user) : this.STANDARD_OPTIONS;
      } else {
         return args.length == 2 && user.isAuthorized("essentials.gamemode.others") && !isDirectGamemodeCommand
            ? this.getPlayers(user)
            : Collections.emptyList();
      }
   }

   @Override
   protected void updatePlayer(final Server server, final CommandSource sender, final User user, final String[] args) {
   }
}
