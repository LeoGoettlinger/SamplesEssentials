package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.Console;
import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import com.earth2me.essentials.messaging.IMessageRecipient;
import com.earth2me.essentials.utils.FormatUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import net.ess3.api.IUser;
import net.essentialsx.api.v2.events.HelpopMessageSendEvent;
import org.bukkit.Server;

public class Commandhelpop extends EssentialsCommand {
   public Commandhelpop() {
      super("helpop");
   }

   @Override
   public void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
      user.setDisplayNick();
      this.sendMessage(user, args);
   }

   @Override
   public void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
      this.sendMessage(Console.getInstance(), args);
   }

   private void sendMessage(final IMessageRecipient from, final String[] args) throws Exception {
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         String message = FormatUtil.stripFormat(getFinalArg(args, 0));
         this.ess.getLogger().log(Level.INFO, this.ess.getAdventureFacet().miniToLegacy(I18n.tlLiteral("helpOp", from.getDisplayName(), message)));
         List<IUser> recipients = new ArrayList<>();

         for (IUser user : this.ess.getOnlineUsers()) {
            if (user.isAuthorized("essentials.helpop.receive")) {
               recipients.add(user);
            }
         }

         HelpopMessageSendEvent sendEvent = new HelpopMessageSendEvent(from, recipients, message);
         this.ess.getServer().getPluginManager().callEvent(sendEvent);
         if (from instanceof IUser) {
            IUser sender = (IUser)from;
            if (!recipients.contains(sender)) {
               from.sendTl("helpOp", from.getDisplayName(), message);
            }
         }

         for (IUser recipient : sendEvent.getRecipients()) {
            recipient.sendTl("helpOp", new Object[]{from.getDisplayName(), message});
         }
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String[] args) {
      return Collections.emptyList();
   }
}
