package org.lushplugins.lushrewards.hook;

import java.util.UUID;
import org.geysermc.floodgate.api.FloodgateApi;
import org.lushplugins.lushrewards.libraries.lushlib.hook.Hook;

public class FloodgateHook extends Hook {
   public FloodgateHook() {
      super(HookId.FLOODGATE.toString());
   }

   public boolean isFloodgatePlayer(UUID uuid) {
      return FloodgateApi.getInstance().isFloodgatePlayer(uuid);
   }
}
