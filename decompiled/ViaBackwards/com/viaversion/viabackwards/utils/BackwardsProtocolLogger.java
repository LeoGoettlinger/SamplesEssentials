package com.viaversion.viabackwards.utils;

import com.viaversion.viabackwards.ViaBackwards;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.util.ProtocolLogger;

public final class BackwardsProtocolLogger extends ProtocolLogger {
   public BackwardsProtocolLogger(Class<? extends Protocol> protocol) {
      super(ViaBackwards.getPlatform().getLogger(), protocol);
   }
}
