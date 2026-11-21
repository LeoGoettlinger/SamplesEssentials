package com.sk89q.worldedit.bukkit;

import com.sk89q.worldedit.LocalSession;
import java.nio.charset.StandardCharsets;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class CUIChannelListener implements PluginMessageListener {
   private final WorldEditPlugin plugin;

   public CUIChannelListener(WorldEditPlugin plugin) {
      this.plugin = plugin;
   }

   public void onPluginMessageReceived(String channel, Player player, byte[] message) {
      LocalSession session = this.plugin.getSession(player);
      String text = new String(message, StandardCharsets.UTF_8);
      BukkitPlayer actor = this.plugin.wrapPlayer(player);
      session.handleCUIInitializationMessage(text, actor);
   }
}
