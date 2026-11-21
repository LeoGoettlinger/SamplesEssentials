package com.earth2me.essentials.messaging;

import net.essentialsx.api.v2.services.mail.MailSender;
import org.bukkit.entity.Player;

public interface IMessageRecipient extends MailSender {
   void sendMessage(String message);

   void sendTl(String tlKey, Object... args);

   String tlSender(String tlKey, Object... args);

   IMessageRecipient.MessageResponse sendMessage(IMessageRecipient recipient, String message);

   IMessageRecipient.MessageResponse onReceiveMessage(IMessageRecipient sender, String message);

   @Override
   String getName();

   String getDisplayName();

   boolean isReachable();

   IMessageRecipient getReplyRecipient();

   void setReplyRecipient(IMessageRecipient recipient);

   boolean isHiddenFrom(Player player);

   public static enum MessageResponse {
      SUCCESS,
      SUCCESS_BUT_AFK,
      MESSAGES_IGNORED,
      SENDER_IGNORED,
      UNREACHABLE,
      EVENT_CANCELLED;

      public boolean isSuccess() {
         return this == SUCCESS || this == SUCCESS_BUT_AFK;
      }
   }
}
