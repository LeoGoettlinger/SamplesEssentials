package com.oheers.fish.addons.internal.reward.message;

import org.evenmorefish.fish.libs.daisylib.messages.message.MessageType;
import org.jspecify.annotations.NonNull;

public class TitleRewardType extends MessageRewardBase {
   @NonNull
   @Override
   public MessageType getMessageType() {
      return MessageType.TITLE;
   }

   @NonNull
   @Override
   public String getIdentifier() {
      return "TITLE";
   }

   @NonNull
   @Override
   public String getAuthor() {
      return "FireML";
   }
}
