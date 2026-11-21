package de.mistrx.buildpaste.listeners;

import de.mistrx.buildpaste.util.Functions;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;

public class ServerStartEvent implements Listener {
   @EventHandler
   public static void ServerStartEvent(ServerLoadEvent event) {
      Functions.setItemEqualsBlockHashmap();
   }
}
