package com.earth2me.essentials.api;

import com.earth2me.essentials.Trade;
import java.util.concurrent.CompletableFuture;
import net.ess3.api.IUser;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public interface IAsyncTeleport {
   void now(Location loc, boolean cooldown, TeleportCause cause, CompletableFuture<Boolean> future);

   void now(Player entity, boolean cooldown, TeleportCause cause, CompletableFuture<Boolean> future);

   void nowUnsafe(Location loc, TeleportCause cause, CompletableFuture<Boolean> future);

   void teleport(Location loc, Trade chargeFor, TeleportCause cause, CompletableFuture<Boolean> future);

   void teleport(Player entity, Trade chargeFor, TeleportCause cause, CompletableFuture<Boolean> future);

   void teleportPlayer(IUser otherUser, Location loc, Trade chargeFor, TeleportCause cause, CompletableFuture<Boolean> future);

   void teleportPlayer(IUser otherUser, Player entity, Trade chargeFor, TeleportCause cause, CompletableFuture<Boolean> future);

   void respawn(final Trade chargeFor, TeleportCause cause, CompletableFuture<Boolean> future);

   void warp(IUser otherUser, String warp, Trade chargeFor, TeleportCause cause, CompletableFuture<Boolean> future);

   void back(Trade chargeFor, CompletableFuture<Boolean> future);

   void back(IUser teleporter, Trade chargeFor, CompletableFuture<Boolean> future);

   void back(CompletableFuture<Boolean> future);
}
