package com.skyblockexp.ezrtp.api;

import com.skyblockexp.ezrtp.teleport.TeleportReason;
import java.util.function.Consumer;
import org.bukkit.entity.Player;

public interface TeleportService {
   void teleportPlayer(Player var1, TeleportReason var2);

   void teleportPlayer(Player var1, Object var2, TeleportReason var3);

   void teleportPlayer(Player var1, TeleportReason var2, Consumer<Boolean> var3);

   void teleportPlayer(Player var1, Object var2, TeleportReason var3, Consumer<Boolean> var4);
}
