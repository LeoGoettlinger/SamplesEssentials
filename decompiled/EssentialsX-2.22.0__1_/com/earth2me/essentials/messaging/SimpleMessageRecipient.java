package com.earth2me.essentials.messaging;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.IEssentials;
import com.earth2me.essentials.IUser;
import com.earth2me.essentials.User;
import com.earth2me.essentials.adventure.AdventureUtil;
import java.lang.ref.WeakReference;
import java.util.UUID;
import net.ess3.api.events.PrivateMessagePreSendEvent;
import net.ess3.api.events.PrivateMessageSentEvent;
import org.bukkit.entity.Player;

public class SimpleMessageRecipient implements IMessageRecipient {
   private final IEssentials ess;
   private final IMessageRecipient parent;
   private long lastMessageMs;
   private WeakReference<IMessageRecipient> replyRecipient;

   public SimpleMessageRecipient(final IEssentials ess, final IMessageRecipient parent) {
      this.ess = ess;
      this.parent = parent;
   }

   protected static User getUser(final IMessageRecipient recipient) {
      if (recipient instanceof SimpleMessageRecipient) {
         return ((SimpleMessageRecipient)recipient).parent instanceof User ? (User)((SimpleMessageRecipient)recipient).parent : null;
      } else {
         return recipient instanceof User ? (User)recipient : null;
      }
   }

   @Override
   public void sendMessage(final String message) {
      this.parent.sendMessage(message);
   }

   @Override
   public void sendTl(String tlKey, Object... args) {
      this.parent.sendTl(tlKey, args);
   }

   @Override
   public String tlSender(String tlKey, Object... args) {
      return this.parent.tlSender(tlKey, args);
   }

   @Override
   public String getName() {
      return this.parent.getName();
   }

   @Override
   public UUID getUUID() {
      return this.parent.getUUID();
   }

   @Override
   public String getDisplayName() {
      return this.parent.getDisplayName();
   }

   @Override
   public IMessageRecipient.MessageResponse sendMessage(final IMessageRecipient recipient, String message) {
      PrivateMessagePreSendEvent preSendEvent = new PrivateMessagePreSendEvent(this.parent, recipient, message);
      this.ess.getServer().getPluginManager().callEvent(preSendEvent);
      if (preSendEvent.isCancelled()) {
         return IMessageRecipient.MessageResponse.EVENT_CANCELLED;
      } else {
         message = preSendEvent.getMessage();
         IMessageRecipient.MessageResponse messageResponse = recipient.onReceiveMessage(this.parent, message);
         switch (messageResponse) {
            case UNREACHABLE:
               this.sendTl("recentlyForeverAlone", recipient.getDisplayName());
               break;
            case MESSAGES_IGNORED:
               this.sendTl("msgIgnore", recipient.getDisplayName());
            case SENDER_IGNORED:
               break;
            case SUCCESS_BUT_AFK:
               if (((IUser)recipient).getAfkMessage() != null) {
                  this.sendTl("userAFKWithMessage", recipient.getDisplayName(), ((IUser)recipient).getAfkMessage());
               } else {
                  this.sendTl("userAFK", recipient.getDisplayName());
               }
            default:
               this.sendTl("msgFormat", AdventureUtil.parsed(this.tlSender("meSender")), recipient.getDisplayName(), message);
               if (this.ess.getSettings().isSocialSpyMessages()) {
                  User senderUser = getUser(this);
                  User recipientUser = getUser(recipient);
                  if (senderUser != null
                     && !senderUser.isAuthorized("essentials.chat.spy.exempt")
                     && recipientUser != null
                     && !recipientUser.isAuthorized("essentials.chat.spy.exempt")) {
                     String senderName = this.ess.getSettings().isSocialSpyDisplayNames() ? this.getDisplayName() : this.getName();
                     String recipientName = this.ess.getSettings().isSocialSpyDisplayNames() ? recipient.getDisplayName() : recipient.getName();

                     for (User onlineUser : this.ess.getOnlineUsers()) {
                        if (onlineUser.isSocialSpyEnabled() && !onlineUser.equals(senderUser) && !onlineUser.equals(recipient)) {
                           if (senderUser.isMuted() && this.ess.getSettings().getSocialSpyListenMutedPlayers()) {
                              onlineUser.sendComponent(
                                 this.ess
                                    .getAdventureFacet()
                                    .deserializeMiniMessage(
                                       this.tlSender("socialSpyMutedPrefix") + I18n.tlLiteral("socialSpyMsgFormat", senderName, recipientName, message)
                                    )
                              );
                           } else {
                              onlineUser.sendComponent(
                                 this.ess
                                    .getAdventureFacet()
                                    .deserializeMiniMessage(
                                       I18n.tlLiteral("socialSpyPrefix") + I18n.tlLiteral("socialSpyMsgFormat", senderName, recipientName, message)
                                    )
                              );
                           }
                        }
                     }
                  }
               }
         }

         if (messageResponse.isSuccess()) {
            this.setReplyRecipient(recipient);
         }

         PrivateMessageSentEvent sentEvent = new PrivateMessageSentEvent(this.parent, recipient, message, messageResponse);
         this.ess.getServer().getPluginManager().callEvent(sentEvent);
         return messageResponse;
      }
   }

   @Override
   public IMessageRecipient.MessageResponse onReceiveMessage(final IMessageRecipient sender, final String message) {
      if (!this.isReachable()) {
         return IMessageRecipient.MessageResponse.UNREACHABLE;
      } else {
         User user = getUser(this);
         boolean afk = false;
         boolean isLastMessageReplyRecipient = this.ess.getSettings().isLastMessageReplyRecipient();
         if (user != null) {
            if (user.isIgnoreMsg() && sender instanceof IUser && !((IUser)sender).isAuthorized("essentials.msgtoggle.bypass")) {
               return IMessageRecipient.MessageResponse.MESSAGES_IGNORED;
            }

            afk = user.isAfk();
            isLastMessageReplyRecipient = user.isLastMessageReplyRecipient();
            if (sender instanceof IUser && user.isIgnoredPlayer((IUser)sender)) {
               return IMessageRecipient.MessageResponse.SENDER_IGNORED;
            }
         }

         this.sendTl("msgFormat", sender.getDisplayName(), AdventureUtil.parsed(this.tlSender("meRecipient")), message);
         if (isLastMessageReplyRecipient) {
            long timeout = this.ess.getSettings().getLastMessageReplyRecipientTimeout() * 1000L;
            if (this.getReplyRecipient() == null || !this.getReplyRecipient().isReachable() || System.currentTimeMillis() - this.lastMessageMs > timeout) {
               this.setReplyRecipient(sender);
            }
         } else {
            this.setReplyRecipient(sender);
         }

         this.lastMessageMs = System.currentTimeMillis();
         return afk ? IMessageRecipient.MessageResponse.SUCCESS_BUT_AFK : IMessageRecipient.MessageResponse.SUCCESS;
      }
   }

   @Override
   public boolean isReachable() {
      return this.parent.isReachable();
   }

   @Override
   public IMessageRecipient getReplyRecipient() {
      return this.replyRecipient == null ? null : this.replyRecipient.get();
   }

   @Override
   public void setReplyRecipient(final IMessageRecipient replyRecipient) {
      this.replyRecipient = new WeakReference<>(replyRecipient);
   }

   @Override
   public boolean isHiddenFrom(Player player) {
      return this.parent.isHiddenFrom(player);
   }
}
