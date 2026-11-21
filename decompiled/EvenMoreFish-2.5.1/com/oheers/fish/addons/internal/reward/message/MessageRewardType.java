package com.oheers.fish.addons.internal.reward.message;

import org.evenmorefish.fish.libs.daisylib.messages.message.MessageType;
import org.jspecify.annotations.NonNull;

public class MessageRewardType extends MessageRewardBase {
   @NonNull
   @Override
   public String getIdentifier() {
      return "MESSAGE";
   }

   @NonNull
   @Override
   public String getAuthor() {
      return "Oheers";
   }

   @NonNull
   @Override
   public MessageType getMessageType() {
      return MessageType.CHAT;
   }
}
