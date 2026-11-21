package net.essentialsx.api.v2.services.mail;

import net.ess3.api.IUser;

public interface MailService {
   void sendMail(IUser recipient, MailSender sender, String message);

   void sendMail(IUser recipient, MailSender sender, String message, long expireAt);

   @Deprecated
   void sendLegacyMail(IUser recipient, String message);

   String getMailLine(MailMessage message);

   String getMailTlKey(MailMessage message);

   Object[] getMailTlArgs(MailMessage message);
}
