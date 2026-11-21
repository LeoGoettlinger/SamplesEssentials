package com.earth2me.essentials.signs;

import com.earth2me.essentials.ChargeException;
import com.earth2me.essentials.RandomTeleport;
import com.earth2me.essentials.User;
import java.util.concurrent.CompletableFuture;
import net.ess3.api.IEssentials;
import net.ess3.api.MaxMoneyException;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class SignRandomTeleport extends EssentialsSign {
   public SignRandomTeleport() {
      super("RandomTeleport");
   }

   @Override
   protected boolean onSignInteract(EssentialsSign.ISign sign, User player, String username, IEssentials ess) throws SignException, ChargeException, MaxMoneyException {
      String name = sign.getLine(1);
      RandomTeleport randomTeleport = ess.getRandomTeleport();
      randomTeleport.getRandomLocation(name).thenAccept(location -> {
         CompletableFuture<Boolean> future = new CompletableFuture<>();
         future.thenAccept(success -> {
            if (success) {
               player.sendTl("tprSuccess");
            }
         });
         player.getAsyncTeleport().now(location, false, TeleportCause.COMMAND, future);
      });
      return true;
   }
}
