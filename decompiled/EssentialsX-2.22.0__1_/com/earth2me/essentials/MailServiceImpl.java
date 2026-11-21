package com.earth2me.essentials;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import net.essentialsx.api.v2.events.UserMailEvent;
import net.essentialsx.api.v2.services.mail.MailMessage;
import net.essentialsx.api.v2.services.mail.MailSender;
import net.essentialsx.api.v2.services.mail.MailService;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;

public class MailServiceImpl implements MailService {
   private final transient ThreadLocal<SimpleDateFormat> df = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy/MM/dd HH:mm"));

   public MailServiceImpl(IEssentials ess) {
      ess.getServer().getServicesManager().register(MailService.class, this, ess, ServicePriority.Normal);
   }

   @Override
   public void sendMail(net.ess3.api.IUser recipient, MailSender sender, String message) {
      this.sendMail(recipient, sender, message, 0L);
   }

   @Override
   public void sendMail(net.ess3.api.IUser recipient, MailSender sender, String message, long expireAt) {
      this.sendMail(recipient, new MailMessage(false, false, sender.getName(), sender.getUUID(), System.currentTimeMillis(), expireAt, message));
   }

   @Override
   public void sendLegacyMail(net.ess3.api.IUser recipient, String message) {
      this.sendMail(recipient, new MailMessage(false, true, null, null, 0L, 0L, message));
   }

   private void sendMail(net.ess3.api.IUser recipient, MailMessage message) {
      UserMailEvent event = new UserMailEvent(recipient, message);
      Bukkit.getPluginManager().callEvent(event);
      if (!event.isCancelled()) {
         ArrayList<MailMessage> messages = recipient.getMailMessages();
         messages.add(0, message);
         recipient.setMailList(messages);
      }
   }

   @Override
   public String getMailLine(MailMessage mail) {
      String message = mail.getMessage();
      if (mail.isLegacy()) {
         return I18n.tlLiteral("mailMessage", message);
      } else {
         String expire = mail.getTimeExpire() != 0L ? "Timed" : "";
         return I18n.tlLiteral(
            (mail.isRead() ? "mailFormatNewRead" : "mailFormatNew") + expire,
            this.df.get().format(new Date(mail.getTimeSent())),
            mail.getSenderUsername(),
            message
         );
      }
   }

   @Override
   public String getMailTlKey(MailMessage message) {
      if (message.isLegacy()) {
         return "mailMessage";
      } else {
         String expire = message.getTimeExpire() != 0L ? "Timed" : "";
         return (message.isRead() ? "mailFormatNewRead" : "mailFormatNew") + expire;
      }
   }

   @Override
   public Object[] getMailTlArgs(MailMessage message) {
      return message.isLegacy()
         ? new Object[]{message.getMessage()}
         : new Object[]{this.df.get().format(new Date(message.getTimeSent())), message.getSenderUsername(), message.getMessage()};
   }
}
