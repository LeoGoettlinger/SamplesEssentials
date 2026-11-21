package com.earth2me.essentials.api;

import com.earth2me.essentials.Trade;
import net.ess3.api.IUser;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

/** @deprecated */
public interface ITeleport {
   @Deprecated
   void now(Location loc, boolean cooldown, TeleportCause cause) throws Exception;

   @Deprecated
   void now(Player entity, boolean cooldown, TeleportCause cause) throws Exception;

   @Deprecated
   void teleport(Location loc, Trade chargeFor) throws Exception;

   @Deprecated
   void teleport(Location loc, Trade chargeFor, TeleportCause cause) throws Exception;

   @Deprecated
   void teleport(Player entity, Trade chargeFor, TeleportCause cause) throws Exception;

   @Deprecated
   void teleportPlayer(IUser otherUser, Location loc, Trade chargeFor, TeleportCause cause) throws Exception;

   @Deprecated
   void teleportPlayer(IUser otherUser, Player entity, Trade chargeFor, TeleportCause cause) throws Exception;

   @Deprecated
   void respawn(final Trade chargeFor, TeleportCause cause) throws Exception;

   @Deprecated
   void warp(IUser otherUser, String warp, Trade chargeFor, TeleportCause cause) throws Exception;

   @Deprecated
   void back(Trade chargeFor) throws Exception;

   @Deprecated
   void back(IUser teleporter, Trade chargeFor) throws Exception;

   @Deprecated
   void back() throws Exception;
}
