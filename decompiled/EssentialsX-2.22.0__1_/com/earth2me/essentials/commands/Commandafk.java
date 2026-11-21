package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.DateUtil;
import java.util.Collections;
import java.util.List;
import net.ess3.api.TranslatableException;
import net.ess3.api.events.AfkStatusChangeEvent;
import org.bukkit.Server;

public class Commandafk extends EssentialsCommand {
   public Commandafk() {
      super("afk");
   }

   @Override
   public void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
      if (args.length > 0 && user.isAuthorized("essentials.afk.others")) {
         User afkUser = user;

         String message;
         try {
            afkUser = this.getPlayer(server, user, args, 0);
            message = args.length > 1 ? getFinalArg(args, 1) : null;
         } catch (PlayerNotFoundException var8) {
            message = getFinalArg(args, 0);
         }

         this.toggleAfk(user, afkUser, message);
      } else {
         String message = args.length > 0 ? getFinalArg(args, 0) : null;
         this.toggleAfk(user, user, message);
      }
   }

   @Override
   public void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
      if (args.length > 0) {
         User afkUser = this.getPlayer(server, args, 0, true, false);
         String message = args.length > 1 ? getFinalArg(args, 1) : null;
         this.toggleAfk(null, afkUser, message);
      } else {
         throw new NotEnoughArgumentsException();
      }
   }

   private void toggleAfk(final User sender, final User user, final String message) throws Exception {
      if (message != null && sender != null) {
         if (sender.isMuted()) {
            String dateDiff = sender.getMuteTimeout() > 0L ? DateUtil.formatDateDiff(sender.getMuteTimeout()) : null;
            if (dateDiff == null) {
               if (sender.hasMuteReason()) {
                  throw new TranslatableException("voiceSilencedReason", sender.getMuteReason());
               }

               throw new TranslatableException("voiceSilenced");
            }

            if (sender.hasMuteReason()) {
               throw new TranslatableException("voiceSilencedReasonTime", dateDiff, sender.getMuteReason());
            }

            throw new TranslatableException("voiceSilencedTime", dateDiff);
         }

         if (!sender.isAuthorized("essentials.afk.message")) {
            throw new TranslatableException("noPermToAFKMessage");
         }
      }

      user.setDisplayNick();
      boolean currentStatus = user.isAfk();
      boolean afterStatus = user.toggleAfk(AfkStatusChangeEvent.Cause.COMMAND);
      if (currentStatus != afterStatus) {
         String tlKey = "";
         String selfTlKey = "";
         if (!afterStatus) {
            if (!user.isHidden()) {
               tlKey = "userIsNotAway";
               selfTlKey = "userIsNotAwaySelf";
            }

            user.updateActivity(false, AfkStatusChangeEvent.Cause.COMMAND);
         } else {
            if (!user.isHidden()) {
               if (message != null) {
                  tlKey = "userIsAwayWithMessage";
                  selfTlKey = "userIsAwaySelfWithMessage";
               } else {
                  tlKey = "userIsAway";
                  selfTlKey = "userIsAwaySelf";
               }
            }

            user.setAfkMessage(message);
         }

         if (!tlKey.isEmpty() && this.ess.getSettings().broadcastAfkMessage()) {
            this.ess.broadcastTl(user, u -> u == user, tlKey, new Object[]{user.getDisplayName(), message});
         }

         if (!selfTlKey.isEmpty()) {
            user.sendTl(selfTlKey, user.getDisplayName(), message);
         }

         user.setDisplayNick();
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String[] args) {
      return args.length == 1 && sender.isAuthorized("essentials.afk.others") ? this.getPlayers(sender) : Collections.emptyList();
   }
}
