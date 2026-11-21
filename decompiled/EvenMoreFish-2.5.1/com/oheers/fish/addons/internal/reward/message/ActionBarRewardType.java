package com.oheers.fish.addons.internal.reward.message;

import org.evenmorefish.fish.libs.daisylib.messages.message.MessageType;
import org.jspecify.annotations.NonNull;

public class ActionBarRewardType extends MessageRewardBase {
   @NonNull
   @Override
   public MessageType getMessageType() {
      return MessageType.ACTION_BAR;
   }

   @NonNull
   @Override
   public String getIdentifier() {
      return "ACTIONBAR";
   }

   @NonNull
   @Override
   public String getAuthor() {
      return "FireML";
   }
}
