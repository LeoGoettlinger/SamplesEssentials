package com.skyblockexp.ezrtp.integration;

import com.skyblockexp.ezrtp.config.EzCountdownIntegrationSettings;
import com.skyblockexp.ezrtp.config.RandomTeleportSettings;
import com.skyblockexp.ezrtp.message.MessageProvider;
import com.skyblockexp.ezrtp.pvptag.PvpTagService;
import java.util.UUID;
import java.util.function.Consumer;
import org.bukkit.entity.Player;

public interface EzCountdownDisplayBridge {
   boolean startCountdown(
      Player var1,
      int var2,
      EzCountdownIntegrationSettings var3,
      RandomTeleportSettings var4,
      PvpTagService var5,
      MessageProvider var6,
      Consumer<Boolean> var7,
      Runnable var8
   );

   void cancelCountdown(UUID var1, Player var2);

   void shutdown();
}
