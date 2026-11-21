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

public class ListSubCmd implements ViaSubCommand {
   @Override
   public String name() {
      return "list";
   }

   @Override
   public String description() {
      return "Shows lists of the versions from logged in players.";
   }

   @Override
   public String usage() {
      return "list";
   }

   @Override
   public boolean execute(ViaCommandSender sender, String[] args) {
      Map<ProtocolVersion, Set<String>> playerVersions = new TreeMap<>(ProtocolVersion::compareTo);

      for (UserConnection p : Via.getManager().getConnectionManager().getConnections()) {
         ProtocolVersion version = p.getProtocolInfo().protocolVersion();
         playerVersions.computeIfAbsent(version, s -> new HashSet<>()).add(p.getProtocolInfo().getUsername());
      }

      if (playerVersions.isEmpty()) {
         this.sendMessage(sender, "&cNo players found!", new Object[0]);
         return true;
      } else {
         for (Entry<ProtocolVersion, Set<String>> entry : playerVersions.entrySet()) {
            this.sendMessage(sender, "&8[&6%s&8] (&7%d&8): &b%s", new Object[]{entry.getKey().getName(), entry.getValue().size(), entry.getValue()});
         }

         playerVersions.clear();
         return true;
      }
   }
}
