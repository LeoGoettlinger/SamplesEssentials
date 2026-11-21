package com.viaversion.viaversion.commands.defaultsubs;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.command.ViaCommandSender;
import com.viaversion.viaversion.api.command.ViaSubCommand;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

public class PPSSubCmd implements ViaSubCommand {
   @Override
   public String name() {
      return "pps";
   }

   @Override
   public String description() {
      return "Shows the packets per second of online players.";
   }

   @Override
   public String usage() {
      return "pps";
   }

   @Override
   public boolean execute(ViaCommandSender sender, String[] args) {
      Map<ProtocolVersion, Set<String>> playerVersions = new TreeMap<>(ProtocolVersion::compareTo);
      int totalPackets = 0;
      int clients = 0;
      long max = 0L;

      for (UserConnection p : Via.getManager().getConnectionManager().getConnections()) {
         ProtocolVersion playerVersion = p.getProtocolInfo().protocolVersion();
         if (!playerVersions.containsKey(playerVersion)) {
            playerVersions.put(playerVersion, new HashSet<>());
         }

         if (p.getPacketTracker().getPacketsPerSecond() > -1) {
            playerVersions.get(playerVersion).add(p.getProtocolInfo().getUsername() + " (" + p.getPacketTracker().getPacketsPerSecond() + " PPS)");
            totalPackets += p.getPacketTracker().getPacketsPerSecond();
            if (p.getPacketTracker().getPacketsPerSecond() > max) {
               max = p.getPacketTracker().getPacketsPerSecond();
            }

            clients++;
         }
      }

      this.sendMessage(sender, "&4Live Packets Per Second", new Object[0]);
      if (clients > 1) {
         this.sendMessage(sender, "&cAverage: &f" + totalPackets / clients, new Object[0]);
         this.sendMessage(sender, "&cHighest: &f" + max, new Object[0]);
      }

      if (clients == 0) {
         this.sendMessage(sender, "&cNo clients to display.", new Object[0]);
      }

      for (Entry<ProtocolVersion, Set<String>> entry : playerVersions.entrySet()) {
         this.sendMessage(sender, "&8[&6%s&8]: &b%s", new Object[]{entry.getKey().getName(), entry.getValue()});
      }

      playerVersions.clear();
      return true;
   }
}
