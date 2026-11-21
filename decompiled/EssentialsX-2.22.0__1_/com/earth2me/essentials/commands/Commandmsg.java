package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.Console;
import com.earth2me.essentials.User;
import com.earth2me.essentials.messaging.IMessageRecipient;
import com.earth2me.essentials.utils.DateUtil;
import com.earth2me.essentials.utils.FormatUtil;
import java.util.Collections;
import java.util.List;
import net.ess3.api.TranslatableException;
import org.bukkit.Server;

public class Commandmsg extends EssentialsLoopCommand {
   public Commandmsg() {
      super("msg");
   }

   @Override
   public void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
      if (args.length < 2) {
         throw new NotEnoughArgumentsException();
      } else {
         String message = getFinalArg(args, 1);
         boolean canWildcard = sender.isAuthorized("essentials.msg.multiple");
         if (sender.isPlayer()) {
            User user = this.ess.getUser(sender.getPlayer());
            if (user.isMuted()) {
               String dateDiff = user.getMuteTimeout() > 0L ? DateUtil.formatDateDiff(user.getMuteTimeout()) : null;
               if (dateDiff == null) {
                  throw new TranslatableException(user.hasMuteReason() ? "voiceSilencedReason" : "voiceSilenced", user.getMuteReason());
               }

               throw new TranslatableException(user.hasMuteReason() ? "voiceSilencedReasonTime" : "voiceSilencedTime", dateDiff, user.getMuteReason());
            }

            message = FormatUtil.formatMessage(user, "essentials.msg", message);
         } else {
            message = FormatUtil.replaceFormat(message);
         }

         if (!args[0].equalsIgnoreCase("Console") && !args[0].equalsIgnoreCase(Console.displayName())) {
            this.loopOnlinePlayers(server, sender, false, canWildcard, args[0], new String[]{message});
         } else {
            IMessageRecipient messageSender = (IMessageRecipient)(sender.isPlayer() ? this.ess.getUser(sender.getPlayer()) : Console.getInstance());
            messageSender.sendMessage(Console.getInstance(), message);
         }
      }
   }

   @Override
   protected void updatePlayer(final Server server, final CommandSource sender, final User messageReceiver, final String[] args) {
      IMessageRecipient messageSender = (IMessageRecipient)(sender.isPlayer() ? this.ess.getUser(sender.getPlayer()) : Console.getInstance());
      messageSender.sendMessage(messageReceiver, args[0]);
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String[] args) {
      return args.length == 1 ? this.getPlayers(sender) : Collections.emptyList();
   }
}
