package net.coreprotect.listener.channel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.coreprotect.CoreProtect;
import net.coreprotect.config.Config;
import net.coreprotect.language.Phrase;
import net.coreprotect.utility.Chat;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class PluginChannelHandshakeListener implements PluginMessageListener, Listener {
   public static final String pluginChannel = "coreprotect:handshake";
   private final int networkingProtocolVersion = 1;
   private final Set<UUID> pluginChannelPlayers;
   private static PluginChannelHandshakeListener instance;

   public PluginChannelHandshakeListener() {
      instance = this;
      this.pluginChannelPlayers = new HashSet<>();
   }

   public static PluginChannelHandshakeListener getInstance() {
      return instance;
   }

   public Set<UUID> getPluginChannelPlayers() {
      return this.pluginChannelPlayers;
   }

   public boolean isPluginChannelPlayer(CommandSender commandSender) {
      return !(commandSender instanceof Player) ? false : this.getPluginChannelPlayers().contains(((Player)commandSender).getUniqueId());
   }

   @EventHandler
   public void onPlayerQuit(PlayerQuitEvent event) {
      this.getPluginChannelPlayers().remove(event.getPlayer().getUniqueId());
   }

   public void onPluginMessageReceived(String s, Player player, byte[] bytes) {
      this.handleHandshake(s, player, bytes);
   }

   private void handleHandshake(String channel, Player player, byte[] bytes) {
      if (player.hasPermission("coreprotect.networking")) {
         if (channel.equals("coreprotect:handshake")) {
            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            DataInputStream dis = new DataInputStream(in);

            try {
               String modVersion = dis.readUTF();
               String modId = dis.readUTF();
               int protocolVersion = dis.readInt();
               if (Config.getGlobal().NETWORK_DEBUG) {
                  Chat.console(new String(bytes));
                  Chat.console(modVersion);
                  Chat.console(modId);
                  Chat.console(String.valueOf(protocolVersion));
               }

               if (protocolVersion != 1) {
                  Chat.console(Phrase.build(Phrase.NETWORK_CONNECTION, player.getName(), modId, modVersion, "{2}"));
                  return;
               }

               this.getPluginChannelPlayers().add(player.getUniqueId());
               Chat.console(Phrase.build(Phrase.NETWORK_CONNECTION, player.getName(), modId, modVersion, "{1}"));
               player.sendPluginMessage(CoreProtect.getInstance(), "coreprotect:handshake", this.sendRegistered());
            } catch (Exception var9) {
               Chat.console(var9.toString());
               var9.printStackTrace();
            }
         }
      }
   }

   private byte[] sendRegistered() throws IOException {
      ByteArrayOutputStream msgBytes = new ByteArrayOutputStream();
      DataOutputStream msgOut = new DataOutputStream(msgBytes);
      msgOut.writeBoolean(true);
      return msgBytes.toByteArray();
   }
}
