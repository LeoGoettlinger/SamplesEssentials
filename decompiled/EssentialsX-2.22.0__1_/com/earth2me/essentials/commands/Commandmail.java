package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.Console;
import com.earth2me.essentials.User;
import com.earth2me.essentials.messaging.IMessageRecipient;
import com.earth2me.essentials.textreader.SimpleTextPager;
import com.earth2me.essentials.textreader.SimpleTranslatableText;
import com.earth2me.essentials.utils.DateUtil;
import com.earth2me.essentials.utils.FormatUtil;
import com.earth2me.essentials.utils.NumberUtil;
import com.earth2me.essentials.utils.StringUtil;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;
import net.ess3.api.TranslatableException;
import net.essentialsx.api.v2.services.mail.MailMessage;
import org.bukkit.Server;

public class Commandmail extends EssentialsCommand {
   private static int mailsPerMinute = 0;
   private static long timestamp = 0L;

   public Commandmail() {
      super("mail");
   }

   @Override
   public void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
      if (args.length >= 1 && "read".equalsIgnoreCase(args[0])) {
         ArrayList<MailMessage> mail = user.getMailMessages();
         if (mail != null && mail.size() != 0) {
            SimpleTranslatableText input = new SimpleTranslatableText();
            ListIterator<MailMessage> iterator = mail.listIterator();

            while (iterator.hasNext()) {
               MailMessage mailObj = iterator.next();
               if (mailObj.isExpired()) {
                  iterator.remove();
               } else {
                  input.addLine(this.ess.getMail().getMailTlKey(mailObj), this.ess.getMail().getMailTlArgs(mailObj));
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

            if (input.getLines().isEmpty()) {
               user.sendTl("noMail");
               throw new NoChargeException();
            } else {
               SimpleTextPager pager = new SimpleTextPager(input);
               pager.showPage(user.getSource(), args.length > 1 ? args[1] : null, commandLabel + " " + args[0]);
               user.sendTl("mailClear");
               user.setMailList(mail);
            }
         } else {
            user.sendTl("noMail");
            throw new NoChargeException();
         }
      } else if (args.length < 3 || !"send".equalsIgnoreCase(args[0])) {
         if (args.length < 4 || !"sendtemp".equalsIgnoreCase(args[0])) {
            if (args.length <= 1 || !"sendall".equalsIgnoreCase(args[0])) {
               if (args.length < 3 || !"sendtempall".equalsIgnoreCase(args[0])) {
                  if (args.length >= 1 && "clear".equalsIgnoreCase(args[0])) {
                     User mailUser = user;
                     int toRemove = -1;
                     if (args.length > 1) {
                        if (NumberUtil.isPositiveInt(args[1])) {
                           toRemove = Integer.parseInt(args[1]);
                        } else {
                           if (!user.isAuthorized("essentials.mail.clear.others")) {
                              throw new TranslatableException("noPerm", "essentials.mail.clear.others");
                           }

                           mailUser = this.getPlayer(this.ess.getServer(), user, args, 1, true);
                           if (args.length > 2 && NumberUtil.isPositiveInt(args[2])) {
                              toRemove = Integer.parseInt(args[2]);
                           }
                        }
                     }

                     ArrayList<MailMessage> mails = mailUser.getMailMessages();
                     if (mails != null && !mails.isEmpty()) {
                        if (toRemove > 0) {
                           if (toRemove > mails.size()) {
                              user.sendTl("mailClearIndex", mails.size());
                              throw new NoChargeException();
                           }

                           mails.remove(toRemove - 1);
                           mailUser.setMailList(mails);
                        } else {
                           mailUser.setMailList(null);
                        }

                        user.sendTl("mailCleared");
                     } else {
                        user.sendTl(mailUser == user ? "noMail" : "noMailOther", mailUser.getDisplayName());
                        throw new NoChargeException();
                     }
                  } else if (args.length < 1 || !"clearall".equalsIgnoreCase(args[0])) {
                     throw new NotEnoughArgumentsException();
                  } else if (!user.isAuthorized("essentials.mail.clearall")) {
                     throw new TranslatableException("noPerm", "essentials.mail.clearall");
                  } else {
                     this.ess.runTaskAsynchronously(new Commandmail.ClearAll());
                     user.sendTl("mailClearedAll");
                  }
               } else if (!user.isAuthorized("essentials.mail.sendtempall")) {
                  throw new TranslatableException("noPerm", "essentials.mail.sendtempall");
               } else {
                  this.ess
                     .runTaskAsynchronously(
                        new Commandmail.SendAll(
                           user,
                           FormatUtil.formatMessage(user, "essentials.mail", StringUtil.sanitizeString(FormatUtil.stripFormat(getFinalArg(args, 2)))),
                           DateUtil.parseDateDiff(args[1], true)
                        )
                     );
                  user.sendTl("mailSent");
               }
            } else if (!user.isAuthorized("essentials.mail.sendall")) {
               throw new TranslatableException("noPerm", "essentials.mail.sendall");
            } else {
               this.ess
                  .runTaskAsynchronously(
                     new Commandmail.SendAll(
                        user, FormatUtil.formatMessage(user, "essentials.mail", StringUtil.sanitizeString(FormatUtil.stripFormat(getFinalArg(args, 1)))), 0L
                     )
                  );
               user.sendTl("mailSent");
            }
         } else if (!user.isAuthorized("essentials.mail.sendtemp")) {
            throw new TranslatableException("noPerm", "essentials.mail.sendtemp");
         } else if (user.isMuted()) {
            String dateDiff = user.getMuteTimeout() > 0L ? DateUtil.formatDateDiff(user.getMuteTimeout()) : null;
            if (dateDiff == null) {
               throw new TranslatableException(user.hasMuteReason() ? "voiceSilencedReason" : "voiceSilenced", user.getMuteReason());
            } else {
               throw new TranslatableException(user.hasMuteReason() ? "voiceSilencedReasonTime" : "voiceSilencedTime", dateDiff, user.getMuteReason());
            }
         } else {
            User u;
            try {
               u = this.getPlayer(server, args[1], true, true);
            } catch (PlayerNotFoundException var10) {
               throw new TranslatableException("playerNeverOnServer", args[1]);
            }

            long dateDiff = DateUtil.parseDateDiff(args[2], true);
            String msg = FormatUtil.formatMessage(user, "essentials.mail", StringUtil.sanitizeString(FormatUtil.stripFormat(getFinalArg(args, 3))));
            if (msg.length() > 1000) {
               throw new TranslatableException("mailTooLong");
            } else {
               if (!u.isIgnoredPlayer(user)) {
                  if (Math.abs(System.currentTimeMillis() - timestamp) > 60000L) {
                     timestamp = System.currentTimeMillis();
                     mailsPerMinute = 0;
                  }

                  mailsPerMinute++;
                  if (mailsPerMinute > this.ess.getSettings().getMailsPerMinute()) {
                     throw new TranslatableException("mailDelay", this.ess.getSettings().getMailsPerMinute());
                  }

                  u.sendMail(user, msg, dateDiff);
               }

               user.sendTl("mailSentToExpire", u.getDisplayName(), DateUtil.formatDateDiff(dateDiff), u.getName());
               user.sendMessage(msg);
            }
         }
      } else if (!user.isAuthorized("essentials.mail.send")) {
         throw new TranslatableException("noPerm", "essentials.mail.send");
      } else if (user.isMuted()) {
         String dateDiff = user.getMuteTimeout() > 0L ? DateUtil.formatDateDiff(user.getMuteTimeout()) : null;
         if (dateDiff == null) {
            throw new TranslatableException(user.hasMuteReason() ? "voiceSilencedReason" : "voiceSilenced", user.getMuteReason());
         } else {
            throw new TranslatableException(user.hasMuteReason() ? "voiceSilencedReasonTime" : "voiceSilencedTime", dateDiff, user.getMuteReason());
         }
      } else {
         User ux;
         try {
            ux = this.getPlayer(server, args[1], true, true);
         } catch (PlayerNotFoundException var9) {
            throw new TranslatableException("playerNeverOnServer", args[1]);
         }

         String msg = FormatUtil.formatMessage(user, "essentials.mail", StringUtil.sanitizeString(FormatUtil.stripFormat(getFinalArg(args, 2))));
         if (msg.length() > 1000) {
            throw new TranslatableException("mailTooLong");
         } else {
            if (!ux.isIgnoredPlayer(user)) {
               if (Math.abs(System.currentTimeMillis() - timestamp) > 60000L) {
                  timestamp = System.currentTimeMillis();
                  mailsPerMinute = 0;
               }

               mailsPerMinute++;
               if (mailsPerMinute > this.ess.getSettings().getMailsPerMinute()) {
                  throw new TranslatableException("mailDelay", this.ess.getSettings().getMailsPerMinute());
               }

               ux.sendMail(user, msg);
            }

            user.sendTl("mailSentTo", ux.getDisplayName(), ux.getName());
            user.sendMessage(msg);
         }
      }
   }

   @Override
   protected void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
      if (args.length >= 1 && "read".equalsIgnoreCase(args[0])) {
         throw new TranslatableException("onlyPlayers", commandLabel + " read");
      } else if (args.length > 1 && "clear".equalsIgnoreCase(args[0])) {
         User mailUser = this.getPlayer(server, args[1], true, true);
         int toRemove = args.length > 2 ? (NumberUtil.isPositiveInt(args[2]) ? Integer.parseInt(args[2]) : -1) : -1;
         ArrayList<MailMessage> mails = mailUser.getMailMessages();
         if (mails != null && !mails.isEmpty()) {
            if (toRemove > 0) {
               if (toRemove > mails.size()) {
                  sender.sendTl("mailClearIndex", mails.size());
                  throw new NoChargeException();
               }

               mails.remove(toRemove - 1);
               mailUser.setMailList(mails);
            } else {
               mailUser.setMailList(null);
            }

            sender.sendTl("mailCleared");
         } else {
            sender.sendTl("noMailOther", mailUser.getDisplayName());
            throw new NoChargeException();
         }
      } else if (args.length >= 1 && "clearall".equalsIgnoreCase(args[0])) {
         this.ess.runTaskAsynchronously(new Commandmail.ClearAll());
         sender.sendTl("mailClearedAll");
      } else if (args.length >= 3 && "send".equalsIgnoreCase(args[0])) {
         User u;
         try {
            u = this.getPlayer(server, args[1], true, true);
         } catch (PlayerNotFoundException var8) {
            throw new TranslatableException("playerNeverOnServer", args[1]);
         }

         u.sendMail(Console.getInstance(), FormatUtil.replaceFormat(getFinalArg(args, 2)));
         sender.sendTl("mailSent");
      } else if (args.length >= 4 && "sendtemp".equalsIgnoreCase(args[0])) {
         User u;
         try {
            u = this.getPlayer(server, args[1], true, true);
         } catch (PlayerNotFoundException var9) {
            throw new TranslatableException("playerNeverOnServer", args[1]);
         }

         long dateDiff = DateUtil.parseDateDiff(args[2], true);
         u.sendMail(Console.getInstance(), FormatUtil.replaceFormat(getFinalArg(args, 3)), dateDiff);
         sender.sendTl("mailSent");
      } else if (args.length >= 2 && "sendall".equalsIgnoreCase(args[0])) {
         this.ess.runTaskAsynchronously(new Commandmail.SendAll(Console.getInstance(), FormatUtil.replaceFormat(getFinalArg(args, 1)), 0L));
         sender.sendTl("mailSent");
      } else if (args.length >= 3 && "sendtempall".equalsIgnoreCase(args[0])) {
         long dateDiff = DateUtil.parseDateDiff(args[1], true);
         this.ess.runTaskAsynchronously(new Commandmail.SendAll(Console.getInstance(), FormatUtil.replaceFormat(getFinalArg(args, 2)), dateDiff));
         sender.sendTl("mailSent");
      } else if (args.length >= 2) {
         User u;
         try {
            u = this.getPlayer(server, args[0], true, true);
         } catch (PlayerNotFoundException var10) {
            throw new TranslatableException("playerNeverOnServer", args[0]);
         }

         u.sendMail(Console.getInstance(), FormatUtil.replaceFormat(getFinalArg(args, 1)));
         sender.sendTl("mailSent");
      } else {
         throw new NotEnoughArgumentsException();
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final User user, final String commandLabel, final String[] args) {
      if (args.length == 1) {
         List<String> options = Lists.newArrayList(new String[]{"read", "clear"});
         if (user.isAuthorized("essentials.mail.send")) {
            options.add("send");
         }

         if (user.isAuthorized("essentials.mail.sendtemp")) {
            options.add("sendtemp");
         }

         if (user.isAuthorized("essentials.mail.sendall")) {
            options.add("sendall");
         }

         if (user.isAuthorized("essentials.mail.sendtempall")) {
            options.add("sendtempall");
         }

         if (user.isAuthorized("essentials.mail.clearall")) {
            options.add("clearall");
         }

         return options;
      } else {
         if (args.length == 2) {
            if (args[0].equalsIgnoreCase("send") && user.isAuthorized("essentials.mail.send")
               || args[0].equalsIgnoreCase("sendtemp") && user.isAuthorized("essentials.mail.sendtemp")
               || args[0].equalsIgnoreCase("clear") && user.isAuthorized("essentials.mail.clear.others")) {
               return this.getPlayers(user);
            }

            if (args[0].equalsIgnoreCase("sendtempall") && user.isAuthorized("essentials.mail.sendtempall")) {
               return COMMON_DATE_DIFFS;
            }

            if (args[0].equalsIgnoreCase("read")) {
               ArrayList<MailMessage> mail = user.getMailMessages();
               int pages = mail != null ? mail.size() / 9 + (mail.size() % 9 > 0 ? 1 : 0) : 0;
               if (pages == 0) {
                  return Lists.newArrayList(new String[]{"0"});
               }

               List<String> optionsx = new ArrayList<>();

               for (int i = 0; i < pages; i++) {
                  optionsx.add(String.valueOf(i + 1));
               }

               return optionsx;
            }

            if (args[0].equalsIgnoreCase("clear")) {
               ArrayList<MailMessage> mail = user.getMailMessages();
               if (mail.size() >= 9) {
                  return Lists.newArrayList(new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9"});
               }

               List<String> optionsx = new ArrayList<>();

               for (int i = 0; i < mail.size(); i++) {
                  optionsx.add(String.valueOf(i + 1));
               }

               return optionsx;
            }
         } else if (args.length == 3 && args[0].equalsIgnoreCase("sendtemp") && user.isAuthorized("essentials.mail.sendtemp")) {
            return COMMON_DATE_DIFFS;
         }

         return Collections.emptyList();
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String[] args) {
      if (args.length == 1) {
         return Lists.newArrayList(new String[]{"send", "sendall", "sendtemp", "sendtempall", "clearall", "clear"});
      } else {
         if (args.length == 2) {
            if (args[0].equalsIgnoreCase("send") || args[0].equalsIgnoreCase("sendtemp") || args[0].equalsIgnoreCase("clear")) {
               return this.getPlayers(sender);
            }

            if (args[0].equalsIgnoreCase("sendtempall")) {
               return COMMON_DATE_DIFFS;
            }
         } else if (args.length == 3 && args[0].equalsIgnoreCase("sendtemp")) {
            return COMMON_DATE_DIFFS;
         }

         return Collections.emptyList();
      }
   }

   private class ClearAll implements Runnable {
      private ClearAll() {
      }

      @Override
      public void run() {
         for (UUID u : Commandmail.this.ess.getUsers().getAllUserUUIDs()) {
            User user = Commandmail.this.ess.getUsers().loadUncachedUser(u);
            if (user != null) {
               user.setMailList(null);
            }
         }
      }
   }

   private class SendAll implements Runnable {
      private final IMessageRecipient messageRecipient;
      private final String message;
      private final long dateDiff;

      SendAll(IMessageRecipient messageRecipient, String message, long dateDiff) {
         this.messageRecipient = messageRecipient;
         this.message = message;
         this.dateDiff = dateDiff;
      }

      @Override
      public void run() {
         for (UUID u : Commandmail.this.ess.getUsers().getAllUserUUIDs()) {
            User user = Commandmail.this.ess.getUsers().loadUncachedUser(u);
            if (user != null) {
               user.sendMail(this.messageRecipient, this.message, this.dateDiff);
            }
         }
      }
   }
}
