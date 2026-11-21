package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.DateUtil;
import com.earth2me.essentials.utils.FormatUtil;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.ess3.api.TranslatableException;
import net.essentialsx.api.v2.events.UserActionEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class Commandme extends EssentialsCommand {
   public Commandme() {
      super("me");
   }

   @Override
   public void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
      if (user.isMuted()) {
         String dateDiff = user.getMuteTimeout() > 0L ? DateUtil.formatDateDiff(user.getMuteTimeout()) : null;
         if (dateDiff == null) {
            throw new TranslatableException(user.hasMuteReason() ? "voiceSilencedReason" : "voiceSilenced", user.getMuteReason());
         } else {
            throw new TranslatableException(user.hasMuteReason() ? "voiceSilencedReasonTime" : "voiceSilencedTime", dateDiff, user.getMuteReason());
         }
      } else if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         String message = getFinalArg(args, 0);
         message = FormatUtil.formatMessage(user, "essentials.chat", message);
         user.setDisplayNick();
         long radius = this.ess.getSettings().getChatRadius();
         if (radius < 1L) {
            this.ess.broadcastTl("action", new Object[]{user.getDisplayName(), message});
            this.ess
               .getServer()
               .getPluginManager()
               .callEvent(new UserActionEvent(user, message, Collections.unmodifiableCollection(this.ess.getServer().getOnlinePlayers())));
         } else {
            radius *= radius;
            World world = user.getWorld();
            Location loc = user.getLocation();
            Set<User> outList = new HashSet<>();

            for (Player player : Bukkit.getOnlinePlayers()) {
               User onlineUser = this.ess.getUser(player);
               if (!onlineUser.equals(user)) {
                  boolean abort = false;
                  Location playerLoc = onlineUser.getLocation();
                  if (playerLoc.getWorld() != world) {
                     abort = true;
                  } else if (onlineUser.isIgnoredPlayer(user)) {
                     abort = true;
                  } else {
                     double delta = playerLoc.distanceSquared(loc);
                     if (delta > radius) {
                        abort = true;
                     }
                  }

                  if (abort) {
                     if (onlineUser.isAuthorized("essentials.chat.spy")) {
                        outList.add(onlineUser);
                     }
                  } else {
                     outList.add(onlineUser);
                  }
               } else {
                  outList.add(onlineUser);
               }
            }

            if (outList.size() < 2) {
               user.sendTl("localNoOne");
            }

            for (User onlineUser : outList) {
               onlineUser.sendTl("action", user.getDisplayName(), message);
            }

            if (UserActionEvent.getHandlerList().getRegisteredListeners().length > 0) {
               Set<Player> outListPlayers = new HashSet<>();

               for (User onlineUser : outList) {
                  outListPlayers.add(onlineUser.getBase());
               }

               this.ess.getServer().getPluginManager().callEvent(new UserActionEvent(user, message, Collections.unmodifiableCollection(outListPlayers)));
            }
         }
      }
   }

   @Override
   public void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         String message = getFinalArg(args, 0);
         message = FormatUtil.replaceFormat(message);
         this.ess.broadcastTl("action", new Object[]{"@", message});
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String[] args) {
      return Collections.emptyList();
   }
}
