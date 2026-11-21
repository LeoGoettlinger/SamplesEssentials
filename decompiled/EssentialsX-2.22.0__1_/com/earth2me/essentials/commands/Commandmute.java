package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.I18n;
import com.earth2me.essentials.OfflinePlayerStub;
import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.DateUtil;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import net.ess3.api.TranslatableException;
import net.ess3.api.events.MuteStatusChangeEvent;
import org.bukkit.Server;

public class Commandmute extends EssentialsCommand {
   public Commandmute() {
      super("mute");
   }

   @Override
   public void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
      boolean isUnmute = commandLabel.toLowerCase(Locale.ENGLISH).contains("unmute");
      boolean nomatch = false;
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         User user;
         try {
            user = this.getPlayer(server, args, 0, true, true);
         } catch (PlayerNotFoundException var20) {
            nomatch = true;
            user = this.ess.getUser(new OfflinePlayerStub(args[0], this.ess.getServer()));
         }

         if (isUnmute && !user.getMuted()) {
            sender.sendTl("playerNotMuted", user.getDisplayName());
         } else {
            if (!user.getBase().isOnline() && sender.isPlayer()) {
               if (!sender.isAuthorized("essentials.mute.offline")) {
                  throw new TranslatableException("muteExemptOffline");
               }
            } else if (!isUnmute && user.isAuthorized("essentials.mute.exempt")) {
               throw new TranslatableException("muteExempt");
            }

            long muteTimestamp = 0L;
            String muteReason = null;
            if (!isUnmute && args.length > 1) {
               String time = args[1];

               try {
                  muteTimestamp = DateUtil.parseDateDiff(time, true);
                  muteReason = getFinalArg(args, 2);
               } catch (Exception var19) {
                  muteReason = getFinalArg(args, 1);
               }

               long maxMuteLength = this.ess.getSettings().getMaxMute() * 1000L;
               if (maxMuteLength > 0L
                  && muteTimestamp - GregorianCalendar.getInstance().getTimeInMillis() > maxMuteLength
                  && sender.isPlayer()
                  && !this.ess.getUser(sender.getPlayer()).isAuthorized("essentials.mute.unlimited")) {
                  sender.sendTl("oversizedMute");
                  throw new NoChargeException();
               }
            }

            boolean willMute = !isUnmute && (args.length > 1 || !user.getMuted());
            User controller = sender.isPlayer() ? this.ess.getUser(sender.getPlayer()) : null;
            MuteStatusChangeEvent event = new MuteStatusChangeEvent(user, controller, willMute, muteTimestamp, muteReason);
            this.ess.getServer().getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
               if (muteReason != null) {
                  user.setMuteReason(muteReason.isEmpty() ? null : muteReason);
                  user.setMuted(true);
               } else {
                  user.setMuted(!user.getMuted());
                  if (!user.getMuted()) {
                     user.setMuteReason(null);
                  }
               }

               user.setMuteTimeout(muteTimestamp);
               boolean muted = user.getMuted();
               String muteTime = DateUtil.formatDateDiff(muteTimestamp);
               if (nomatch) {
                  sender.sendTl("userUnknown", user.getName());
               }

               if (muted) {
                  if (muteTimestamp > 0L) {
                     if (!user.hasMuteReason()) {
                        sender.sendTl("mutedPlayerFor", user.getDisplayName(), muteTime);
                        user.sendTl("playerMutedFor", muteTime);
                     } else {
                        sender.sendTl("mutedPlayerForReason", user.getDisplayName(), muteTime, user.getMuteReason());
                        user.sendTl("playerMutedForReason", muteTime, user.getMuteReason());
                     }
                  } else if (!user.hasMuteReason()) {
                     sender.sendTl("mutedPlayer", user.getDisplayName());
                     user.sendTl("playerMuted");
                  } else {
                     sender.sendTl("mutedPlayerReason", user.getDisplayName(), user.getMuteReason());
                     user.sendTl("playerMutedReason", user.getMuteReason());
                  }

                  String tlKey;
                  Object[] objects;
                  if (user.hasMuteReason()) {
                     if (muteTimestamp > 0L) {
                        tlKey = "muteNotifyForReason";
                        objects = new Object[]{sender.getSender().getName(), user.getName(), muteTime, user.getMuteReason()};
                     } else {
                        tlKey = "muteNotify";
                        objects = new Object[]{sender.getSender().getName(), user.getName(), user.getMuteReason()};
                     }
                  } else {
                     tlKey = muteTimestamp > 0L ? "muteNotifyFor" : "muteNotify";
                     objects = new Object[]{sender.getSender().getName(), user.getName(), muteTime};
                  }

                  this.ess.getLogger().log(Level.INFO, this.ess.getAdventureFacet().miniToLegacy(I18n.tlLiteral(tlKey, objects)));
                  this.ess.broadcastTl(null, "essentials.mute.notify", tlKey, objects);
               } else {
                  sender.sendTl("unmutedPlayer", user.getDisplayName());
                  user.sendTl("playerUnmuted");
                  this.ess
                     .getLogger()
                     .log(Level.INFO, this.ess.getAdventureFacet().miniToLegacy(I18n.tlLiteral("unmuteNotify", sender.getSender().getName(), user.getName())));
                  this.ess.broadcastTl(null, "essentials.mute.notify", "unmuteNotify", new Object[]{sender.getSender().getName(), user.getName()});
               }
            }
         }
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String[] args) {
      return args.length == 1 ? this.getPlayers(sender) : COMMON_DATE_DIFFS;
   }
}
