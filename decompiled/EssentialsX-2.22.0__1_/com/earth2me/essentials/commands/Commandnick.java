package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.FormatUtil;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import net.ess3.api.TranslatableException;
import net.ess3.api.events.NickChangeEvent;
import org.bukkit.Server;

public class Commandnick extends EssentialsLoopCommand {
   public Commandnick() {
      super("nick");
   }

   @Override
   public void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         if (args.length > 1 && user.isAuthorized("essentials.nick.others")) {
            this.loopOfflinePlayers(server, user.getSource(), false, true, args[0], this.formatNickname(user, args[1]).split(" "));
            user.sendTl("nickChanged");
         } else {
            this.updatePlayer(server, user.getSource(), user, this.formatNickname(user, args[0]).split(" "));
         }
      }
   }

   @Override
   public void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
      if (args.length < 2) {
         throw new NotEnoughArgumentsException();
      } else {
         this.loopOfflinePlayers(server, sender, false, true, args[0], this.formatNickname(null, args[1]).split(" "));
         sender.sendTl("nickChanged");
      }
   }

   @Override
   protected void updatePlayer(final Server server, final CommandSource sender, final User target, final String[] args) throws NotEnoughArgumentsException {
      String nick = args[0];
      if ("off".equalsIgnoreCase(nick)) {
         this.setNickname(server, sender, target, null);
         target.sendTl("nickNoMore");
      } else if (target.getName().equalsIgnoreCase(nick)) {
         this.setNickname(server, sender, target, nick);
         String strippedDisplay = FormatUtil.stripFormat(target.getDisplayName());
         if (strippedDisplay != null && !strippedDisplay.equalsIgnoreCase(target.getName())) {
            target.sendTl("nickNoMore");
         }

         target.sendTl("nickSet", this.ess.getSettings().changeDisplayName() ? target.getDisplayName() : nick);
      } else {
         if (this.nickInUse(target, nick)) {
            throw new NotEnoughArgumentsException(sender.tl("nickInUse"));
         }

         this.setNickname(server, sender, target, nick);
         target.sendTl("nickSet", this.ess.getSettings().changeDisplayName() ? target.getDisplayName() : nick);
      }
   }

   private String formatNickname(final User user, final String nick) throws Exception {
      String newNick = user == null ? FormatUtil.replaceFormat(nick) : FormatUtil.formatString(user, "essentials.nick", nick);
      if (!newNick.matches(this.ess.getSettings().getNickRegex()) && user != null && !user.isAuthorized("essentials.nick.allowunsafe")) {
         throw new TranslatableException("nickNamesAlpha");
      } else if (this.getNickLength(newNick) > this.ess.getSettings().getMaxNickLength()) {
         throw new TranslatableException("nickTooLong");
      } else if (FormatUtil.stripFormat(newNick).length() < 1) {
         throw new TranslatableException("nickNamesAlpha");
      } else if (user != null
         && user.isAuthorized("essentials.nick.changecolors")
         && !user.isAuthorized("essentials.nick.changecolors.bypass")
         && !FormatUtil.stripFormat(newNick).equals(user.getName())
         && !nick.equalsIgnoreCase("off")) {
         throw new TranslatableException("nickNamesOnlyColorChanges");
      } else if (user != null && !user.isAuthorized("essentials.nick.blacklist.bypass") && this.isNickBanned(newNick)) {
         throw new TranslatableException("nickNameBlacklist", nick);
      } else {
         return newNick;
      }
   }

   private boolean isNickBanned(final String newNick) {
      for (Predicate<String> predicate : this.ess.getSettings().getNickBlacklist()) {
         if (predicate.test(newNick)) {
            return true;
         }
      }

      return false;
   }

   private int getNickLength(final String nick) {
      return this.ess.getSettings().ignoreColorsInMaxLength() ? FormatUtil.stripFormat(nick).length() : FormatUtil.unformatString(nick).length();
   }

   private boolean nickInUse(final User target, final String nick) {
      String lowerNick = FormatUtil.stripFormat(nick.toLowerCase(Locale.ENGLISH));

      for (User onlinePlayer : this.ess.getOnlineUsers()) {
         if (!target.getBase().getName().equals(onlinePlayer.getName())) {
            String matchNick = FormatUtil.stripFormat(onlinePlayer.getNickname());
            if (matchNick != null && !matchNick.isEmpty() && lowerNick.equals(matchNick.toLowerCase(Locale.ENGLISH))
               || lowerNick.equals(onlinePlayer.getName().toLowerCase(Locale.ENGLISH))) {
               return true;
            }
         }
      }

      User fetchedUser = this.ess.getUser(lowerNick);
      return fetchedUser != null && fetchedUser != target;
   }

   private void setNickname(final Server server, final CommandSource sender, final User target, final String nickname) {
      NickChangeEvent nickEvent = new NickChangeEvent(sender.getUser(), target, nickname);
      server.getPluginManager().callEvent(nickEvent);
      if (!nickEvent.isCancelled()) {
         target.setNickname(nickname);
         target.setDisplayNick();
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String[] args) {
      return args.length == 1 && sender.isAuthorized("essentials.nick.others") ? this.getPlayers(sender) : Collections.emptyList();
   }
}
