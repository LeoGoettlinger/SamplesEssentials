package com.oheers.fish.messages;

import com.oheers.fish.config.MessageConfig;

public enum PrefixType {
   NONE(null, null),
   ADMIN("prefix-admin", "<red>[EvenMoreFish] "),
   DEFAULT("prefix-regular", "<green>[EvenMoreFish] "),
   ERROR("prefix-error", "<red>[EvenMoreFish] ");

   private final String id;
   private final String normal;

   private PrefixType(final String id, final String normal) {
      this.id = id;
      this.normal = normal;
   }

   public EMFSingleMessage getPrefix() {
      return this.id == null ? EMFSingleMessage.empty() : EMFSingleMessage.fromString(MessageConfig.getInstance().getConfig().getString(this.id, this.normal));
   }
}
