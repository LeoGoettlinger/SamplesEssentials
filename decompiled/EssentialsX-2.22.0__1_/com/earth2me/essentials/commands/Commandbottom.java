package com.earth2me.essentials.commands;

import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.LocationUtil;
import java.util.concurrent.CompletableFuture;
import net.ess3.provider.WorldInfoProvider;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class Commandbottom extends EssentialsCommand {
   public Commandbottom() {
      super("bottom");
   }

   @Override
   public void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
      int bottomX = user.getLocation().getBlockX();
      int bottomZ = user.getLocation().getBlockZ();
      float pitch = user.getLocation().getPitch();
      float yaw = user.getLocation().getYaw();
      Location unsafe = new Location(user.getWorld(), bottomX, this.ess.provider(WorldInfoProvider.class).getMinHeight(user.getWorld()), bottomZ, yaw, pitch);
      Location safe = LocationUtil.getSafeDestination(this.ess, unsafe);
      CompletableFuture<Boolean> future = this.getNewExceptionFuture(user.getSource(), commandLabel);
      future.thenAccept(success -> {
         if (success) {
            user.sendTl("teleportBottom", safe.getWorld().getName(), safe.getBlockX(), safe.getBlockY(), safe.getBlockZ());
         }
      });
      user.getAsyncTeleport().teleport(safe, new Trade(this.getName(), this.ess), TeleportCause.COMMAND, future);
   }
}
