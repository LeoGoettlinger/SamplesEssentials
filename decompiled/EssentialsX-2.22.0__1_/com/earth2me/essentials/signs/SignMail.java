package com.earth2me.essentials.signs;

import com.earth2me.essentials.User;
import java.util.ArrayList;
import java.util.ListIterator;
import net.ess3.api.IEssentials;
import net.essentialsx.api.v2.services.mail.MailMessage;

public class SignMail extends EssentialsSign {
   public SignMail() {
      super("Mail");
   }

   @Override
   protected boolean onSignInteract(final EssentialsSign.ISign sign, final User player, final String username, final IEssentials ess) throws SignException {
      ArrayList<MailMessage> mail = player.getMailMessages();
      ListIterator<MailMessage> iterator = mail.listIterator();
      boolean hadMail = false;

      while (iterator.hasNext()) {
         MailMessage mailObj = iterator.next();
         if (mailObj.isExpired()) {
            iterator.remove();
         } else {
            hadMail = true;
            player.sendMessage(ess.getMail().getMailLine(mailObj));
            iterator.set(
               new MailMessage(
                  true,
                  mailObj.isLegacy(),
                  mailObj.getSenderUsername(),
                  mailObj.getSenderUUID(),
                  mailObj.getTimeSent(),
                  mailObj.getTimeExpire(),
                  mailObj.getMessage()
               )
            );
         }
      }

      if (!hadMail) {
         player.sendTl("noNewMail");
         return false;
      } else {
         player.setMailList(mail);
         player.sendTl("markMailAsRead");
         return true;
      }
   }
}
